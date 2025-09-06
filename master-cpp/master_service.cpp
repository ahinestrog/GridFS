#include <grpcpp/grpcpp.h>
#include "master.grpc.pb.h"
#include "common.pb.h"
#include "metastore.h"
#include <mutex>
#include <random>
#include <unordered_set>
#include <algorithm>

// ...

using grpc::ServerContext;
using grpc::Status;
using grpc::StatusCode;
using gridfs::MasterService;
using gridfs::PutPlanRequest; using gridfs::PutPlanResponse; using gridfs::BlockAssignment;
using gridfs::GetPlanRequest; using gridfs::GetPlanResponse; using gridfs::BlockLocation;

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
public:
    explicit MasterSvcImpl(MetaStore* ms): ms_(ms) {}

    Status PutPlan(ServerContext*, const PutPlanRequest* req, PutPlanResponse* resp) override {
        const auto fname = req->filename();
        const int64_t blocks = (req->filesize() + req->block_size() - 1) / req->block_size();

        // --- archivo vacío: registrar y responder plan vacío
        if (blocks == 0) {
            {
                std::lock_guard<std::mutex> lk(mu_);
                empty_files_.insert(fname);
            }
            // registra en metastore aunque no haya bloques
            std::vector<BlockAssignment> none;
            ms_->SavePutPlan(fname, none);
            return Status::OK;
        }

        // candidatos IO normalizados y únicos
        std::vector<std::string> ios; ios.reserve(kDataNodes.size());
        for (auto& ep : kDataNodes) ios.push_back(NormalizeIO(ep));
        std::sort(ios.begin(), ios.end());
        ios.erase(std::unique(ios.begin(), ios.end()), ios.end());
        if (ios.empty()) return Status(StatusCode::FAILED_PRECONDITION, "Sin DataNodes registrados (IO)");

        std::lock_guard<std::mutex> lk(mu_);
        std::mt19937 rng{std::random_device{}()};
        std::vector<BlockAssignment> asgs; asgs.reserve(blocks);

        int rep = std::max(1, req->replication());
        rep = std::min(rep, static_cast<int>(ios.size())); // no más réplicas que DNs

        for (int64_t i = 0; i < blocks; ++i) {
            BlockAssignment a;
            a.set_block_id(fname + "#blk" + std::to_string(i));

            // elige primario al azar
            size_t idx = static_cast<size_t>(rng() % ios.size());
            std::string primary = ios[idx];
            a.set_primary_dn(primary);

            // réplicas: las siguientes en el vector
            for (size_t j = 0, added = 0; j < ios.size() && added < static_cast<size_t>(rep - 1); ++j) {
                if (j == idx) continue;
                *a.add_replica_dns() = ios[j];
                ++added;
            }

            asgs.push_back(a);
            *resp->add_assignments() = a;
        }

        ms_->SavePutPlan(fname, asgs);
        return Status::OK;
    }

    Status GetPlan(ServerContext*, const GetPlanRequest* req, GetPlanResponse* resp) override {
        const auto fname = req->filename();
        auto rows = ms_->GetFileLayout(fname);

        // si no hay filas, puede ser archivo vacío previamente registrado
        if (rows.empty()) {
            std::lock_guard<std::mutex> lk(mu_);
            if (empty_files_.count(fname)) return Status::OK; // plan vacío (0 locations)
            return Status(StatusCode::NOT_FOUND, "No existe");
        }

        for (const auto& r : rows) {
            BlockLocation loc;
            loc.set_block_id(r.block_id);
            // normaliza IO también al responder
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

private:
    std::mutex mu_;
    MetaStore* ms_;
    std::unordered_set<std::string> empty_files_; // registra archivos de tamaño 0
};

// ...

std::unique_ptr<gridfs::MasterService::Service> MakeMasterService(MetaStore* ms){
    return std::make_unique<MasterSvcImpl>(ms);
}
