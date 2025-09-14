#include <grpcpp/grpcpp.h>
#include "master.grpc.pb.h"
#include "common.pb.h"
#include "metastore.h"
#include "heartbeat_store.h"
#include <mutex>
#include <random>
#include <unordered_set>
#include <algorithm>
#include <map>

// ...

using grpc::ServerContext;
using grpc::Status;
using grpc::StatusCode;
using proto::MasterService;
using proto::PutPlanRequest; using proto::PutPlanResponse; using proto::BlockAssignment;
using proto::GetPlanRequest; using proto::GetPlanResponse; using proto::BlockLocation;

// ---- SOLO IO (no admin). Sube aquí los DN IO que tengas (50052, 50054, ...).
static std::vector<std::string> kDataNodes = {
    "127.0.0.1:50052", "127.0.0.1:50054"
};

// Normaliza host:puerto → 127.0.0.1 y fuerza IO (si vino admin 50053/50055 ⇒ 50052/50054)
static std::string NormalizeIO(const std::string& ep) {
    auto s = ep;
    // host:port
    auto pos = s.rfind(':');
    if (pos == std::string::npos) return s;
    std::string host = s.substr(0, pos);
    std::string ps   = s.substr(pos + 1);
    int port = 0;
    try { port = std::stoi(ps); } catch (...) { return s; }
    if (host == "localhost") host = "127.0.0.1";
    // si es admin (impar), usa io = admin-1
    if (port % 2 == 1) port -= 1;
    return host + ":" + std::to_string(port);
}

class MasterSvcImpl final : public MasterService::Service {
    Status RegisterUser(ServerContext* ctx, const proto::RegisterUserRequest* req, proto::RegisterUserResponse* resp) override {
        const std::string& user = req->user();
        const std::string& pass = req->pass();
        if (user.empty() || pass.empty()) {
            resp->set_ok(false);
            resp->set_error("Usuario y contraseña requeridos");
            return Status(StatusCode::INVALID_ARGUMENT, "Usuario y contraseña requeridos");
        }
        bool ok = ms_->RegisterUser(user, pass);
        resp->set_ok(ok);
        resp->set_error(ok ? "" : "No se pudo registrar el usuario (¿ya existe?)");
        return Status::OK;
    }
public:
    explicit MasterSvcImpl(MetaStore* ms, HeartbeatStore* hb): ms_(ms), hb_(hb) {}

    Status PutPlan(ServerContext*, const PutPlanRequest* req, PutPlanResponse* resp) override {
            // Autenticación
            const auto& auth = req->has_auth() ? req->auth() : proto::Auth();
            if (!ms_->ValidateUser(auth.user(), auth.pass())) {
                return Status(StatusCode::PERMISSION_DENIED, "Usuario o contraseña incorrectos");
            }
            // ...existing code...
            const auto fname = req->filename();
            const int64_t blocks = (req->filesize() + req->block_size() - 1) / req->block_size();
            if (blocks == 0) {
                {
                    std::lock_guard<std::mutex> lk(mu_);
                    empty_files_.insert(fname);
                }
                std::vector<BlockAssignment> none;
                ms_->SavePutPlan(fname, none, auth.user());
                return Status::OK;
            }
            // Obtener nodos activos del HeartbeatStore y mapear a direcciones IP
            std::vector<std::string> active_node_ids = hb_->GetActiveNodes();
            std::vector<std::string> active_nodes;
            
            if (!active_node_ids.empty()) {
                // Mapear IDs de nodos a direcciones IP reales
                for (const auto& node_id : active_node_ids) {
                    std::string ip_address = MapNodeIdToIP(node_id);
                    if (!ip_address.empty()) {
                        active_nodes.push_back(ip_address);
                    }
                }
            }
            
            if (active_nodes.empty()) {
                // Fallback a la lista estática si no hay nodos activos
                std::vector<std::string> ios; ios.reserve(kDataNodes.size());
                for (auto& ep : kDataNodes) ios.push_back(NormalizeIO(ep));
                std::sort(ios.begin(), ios.end());
                ios.erase(std::unique(ios.begin(), ios.end()), ios.end());
                if (ios.empty()) return Status(StatusCode::FAILED_PRECONDITION, "Sin DataNodes registrados (IO)");
                active_nodes = ios;
            }
            std::lock_guard<std::mutex> lk(mu_);
            std::vector<BlockAssignment> asgs; asgs.reserve(blocks);
            int rep = std::max(1, req->replication());
            rep = std::min(rep, static_cast<int>(active_nodes.size()));
            
            for (int64_t i = 0; i < blocks; ++i) {
                BlockAssignment a;
                a.set_block_id(fname + "#blk" + std::to_string(i));
                
                // Seleccionar nodo primario basado en espacio disponible
                std::string primary = SelectBestNode(active_nodes);
                if (primary.empty()) {
                    return Status(StatusCode::FAILED_PRECONDITION, "No hay nodos disponibles");
                }
                a.set_primary_dn(primary);
                
                // Seleccionar réplicas (excluyendo el primario)
                std::vector<std::string> replica_candidates;
                for (const auto& node : active_nodes) {
                    if (node != primary) {
                        replica_candidates.push_back(node);
                    }
                }
                
                // Ordenar réplicas por espacio disponible (descendente)
                std::sort(replica_candidates.begin(), replica_candidates.end(), 
                    [this](const std::string& a, const std::string& b) {
                        std::string node_id_a = MapIPToNodeId(a);
                        std::string node_id_b = MapIPToNodeId(b);
                        return hb_->GetFreeSpace(node_id_a) > hb_->GetFreeSpace(node_id_b);
                    });
                
                // Agregar réplicas hasta alcanzar el factor de replicación
                for (size_t j = 0; j < replica_candidates.size() && j < static_cast<size_t>(rep - 1); ++j) {
                    *a.add_replica_dns() = replica_candidates[j];
                }
                
                asgs.push_back(a);
                *resp->add_assignments() = a;
            }
            ms_->SavePutPlan(fname, asgs, auth.user());
            return Status::OK;
    }

    Status GetPlan(ServerContext*, const GetPlanRequest* req, GetPlanResponse* resp) override {
            // Autenticación (opcional, si quieres proteger Get también)
            // ...existing code...
            const auto fname = req->filename();
                auto rows = ms_->GetFileLayout(fname, req->has_auth() ? req->auth().user() : "");
            if (rows.empty()) {
                std::lock_guard<std::mutex> lk(mu_);
                if (empty_files_.count(fname)) return Status::OK;
                return Status(StatusCode::NOT_FOUND, "No existe");
            }
            for (const auto& r : rows) {
                BlockLocation loc;
                loc.set_block_id(r.block_id);
                loc.set_primary_dn(NormalizeIO(r.primary_dn));
                size_t p = 0, q = 0;
                while ((q = r.replicas_csv.find(',', p)) != std::string::npos) {
                    auto s = r.replicas_csv.substr(p, q - p);
                    if (!s.empty()) *loc.add_replica_dns() = NormalizeIO(s);
                    p = q + 1;
                }
                auto s = r.replicas_csv.substr(p);
                if (!s.empty()) *loc.add_replica_dns() = NormalizeIO(s);
                *resp->add_locations() = loc;
            }
            return Status::OK;
    }

    // NUEVOS MÉTODOS
    Status Ls(ServerContext* ctx, const proto::LsRequest* req, proto::LsResponse* resp) override {
            // Autenticación
            const auto& auth = req->auth();
            if (!ms_->ValidateUser(auth.user(), auth.pass())) {
                return Status(StatusCode::PERMISSION_DENIED, "Usuario o contraseña incorrectos");
            }
            // Listar archivos y directorios reales
        auto files = ms_->ListFiles(auth.user());
        auto dirs = ms_->ListDirs(auth.user());
            for (const auto& d : dirs) resp->add_entries(d + "/");
            for (const auto& f : files) resp->add_entries(f);
            return Status::OK;
    }

    Status Rm(ServerContext* ctx, const proto::RmRequest* req, proto::RmResponse* resp) override {
            // Autenticación
            const auto& auth = req->auth();
            if (!ms_->ValidateUser(auth.user(), auth.pass())) {
                resp->set_ok(false);
                resp->set_error("Usuario o contraseña incorrectos");
                return Status(StatusCode::PERMISSION_DENIED, "Usuario o contraseña incorrectos");
            }
        bool ok = ms_->RemoveFile(req->path(), auth.user());
            resp->set_ok(ok);
            resp->set_error(ok ? "" : "No se pudo eliminar el archivo");
            return Status::OK;
    }

    Status Mkdir(ServerContext* ctx, const proto::MkdirRequest* req, proto::MkdirResponse* resp) override {
            // Autenticación
            const auto& auth = req->auth();
            if (!ms_->ValidateUser(auth.user(), auth.pass())) {
                resp->set_ok(false);
                resp->set_error("Usuario o contraseña incorrectos");
                return Status(StatusCode::PERMISSION_DENIED, "Usuario o contraseña incorrectos");
            }
        bool ok = ms_->CreateDir(req->path(), auth.user());
            resp->set_ok(ok);
            resp->set_error(ok ? "" : "No se pudo crear el directorio");
            return Status::OK;
    }

    Status Rmdir(ServerContext* ctx, const proto::RmdirRequest* req, proto::RmdirResponse* resp) override {
            // Autenticación
            const auto& auth = req->auth();
            if (!ms_->ValidateUser(auth.user(), auth.pass())) {
                resp->set_ok(false);
                resp->set_error("Usuario o contraseña incorrectos");
                return Status(StatusCode::PERMISSION_DENIED, "Usuario o contraseña incorrectos");
            }
        bool ok = ms_->RemoveDir(req->path(), auth.user());
            resp->set_ok(ok);
            resp->set_error(ok ? "" : "No se pudo eliminar el directorio");
            return Status::OK;
    }

    // Eliminado: método Auth no existe en el proto

private:
    std::mutex mu_;
    MetaStore* ms_;
    HeartbeatStore* hb_;
    std::unordered_set<std::string> empty_files_;
    
    // Método para seleccionar el mejor nodo basado en espacio disponible
    std::string SelectBestNode(const std::vector<std::string>& available_nodes);
    // Método para mapear ID de nodo a dirección IP
    std::string MapNodeIdToIP(const std::string& node_id);
    // Método para mapear dirección IP a ID de nodo
    std::string MapIPToNodeId(const std::string& node_ip);
};

// ...

std::string MasterSvcImpl::SelectBestNode(const std::vector<std::string>& available_nodes) {
    if (available_nodes.empty()) {
        return "";
    }
    
    std::string best_node = available_nodes[0];
    long long max_free_space = 0;
    
    for (const auto& node_ip : available_nodes) {
        // Obtener el ID del nodo desde la IP
        std::string node_id = MapIPToNodeId(node_ip);
        if (!node_id.empty()) {
            long long free_space = hb_->GetFreeSpace(node_id);
            if (free_space > max_free_space) {
                max_free_space = free_space;
                best_node = node_ip;
            }
        }
    }
    
    return best_node;
}

std::string MasterSvcImpl::MapNodeIdToIP(const std::string& node_id) {
    // Mapeo simple basado en el ID del nodo
    if (node_id == "dn-1") {
        return "127.0.0.1:50052";
    } else if (node_id == "dn-2") {
        return "127.0.0.1:50054";
    }
    // Agregar más mapeos según sea necesario
    return "";
}

std::string MasterSvcImpl::MapIPToNodeId(const std::string& node_ip) {
    // Mapeo inverso de IP a ID de nodo
    if (node_ip == "127.0.0.1:50052") {
        return "dn-1";
    } else if (node_ip == "127.0.0.1:50054") {
        return "dn-2";
    }
    return "";
}

std::unique_ptr<proto::MasterService::Service> MakeMasterService(MetaStore* ms, HeartbeatStore* hb){
    return std::make_unique<MasterSvcImpl>(ms, hb);
}
