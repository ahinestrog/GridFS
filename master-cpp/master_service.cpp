#include <grpcpp/grpcpp.h>
#include "master.grpc.pb.h"
#include "common.pb.h"
#include "metastore.h"
#include <mutex>
#include <random>


using grpc::ServerContext; using grpc::Status; using grpc::StatusCode;
using gridfs::MasterService; using gridfs::PutPlanRequest; using gridfs::PutPlanResponse; using gridfs::GetPlanRequest; using gridfs::GetPlanResponse; using gridfs::BlockAssignment; using gridfs::BlockLocation;


static std::vector<std::string> kDataNodes = { "localhost:50052", "localhost:50053", "localhost:50054" };


class MasterSvcImpl final : public MasterService::Service {
public: explicit MasterSvcImpl(MetaStore* ms): ms_(ms) {}
Status PutPlan(ServerContext*, const PutPlanRequest* req, PutPlanResponse* resp) override {
const int64_t blocks = (req->filesize() + req->block_size() - 1) / req->block_size();
if (kDataNodes.empty()) return Status(StatusCode::FAILED_PRECONDITION, "Sin DataNodes registrados");
std::lock_guard<std::mutex> lk(mu_); std::mt19937 rng{std::random_device{}()};
std::vector<BlockAssignment> asgs; asgs.reserve(blocks);
for (int64_t i=0;i<blocks;++i){ BlockAssignment a; a.set_block_id(req->filename()+"#blk"+std::to_string(i)); std::string primary=kDataNodes[rng()%kDataNodes.size()]; a.set_primary_dn(primary); int rep=std::max(1,req->replication()); for(const auto& dn:kDataNodes){ if(dn!=primary && a.replica_dns_size()<rep-1) *a.add_replica_dns()=dn; } asgs.push_back(a); *resp->add_assignments()=a; }
ms_->SavePutPlan(req->filename(), asgs); return Status::OK; }
Status GetPlan(ServerContext*, const GetPlanRequest* req, GetPlanResponse* resp) override {
auto rows = ms_->GetFileLayout(req->filename()); if(rows.empty()) return Status(StatusCode::NOT_FOUND, "No existe");
for(const auto& r: rows){ BlockLocation loc; loc.set_block_id(r.block_id); loc.set_primary_dn(r.primary_dn); size_t p=0,q=0; while((q=r.replicas_csv.find(',',p))!=std::string::npos){ auto s=r.replicas_csv.substr(p,q-p); if(!s.empty()) *loc.add_replica_dns()=s; p=q+1; } auto s=r.replicas_csv.substr(p); if(!s.empty()) *loc.add_replica_dns()=s; *resp->add_locations()=loc; } return Status::OK; }
private: std::mutex mu_; MetaStore* ms_; };


std::unique_ptr<gridfs::MasterService::Service> MakeMasterService(MetaStore* ms){ return std::make_unique<MasterSvcImpl>(ms); }