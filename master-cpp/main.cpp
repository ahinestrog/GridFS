#include <grpcpp/grpcpp.h>
#include "master.grpc.pb.h"
#include "common.grpc.pb.h"
#include <iostream>
#include "heartbeat_store.h"
#include "metastore.h"


std::unique_ptr<gridfs::MasterService::Service> MakeMasterService(MetaStore*);
std::unique_ptr<gridfs::MasterHeartbeat::Service> MakeHeartbeatService(HeartbeatStore*);


int main(int argc,char** argv){
const std::string addr = "0.0.0.0:50051";
HeartbeatStore hb(argc>1?argv[1]:"heartbeats.db");
MetaStore ms(argc>2?argv[2]:"metastore.db");
grpc::ServerBuilder b; b.AddListeningPort(addr, grpc::InsecureServerCredentials());
b.RegisterService(MakeMasterService(&ms).release());
b.RegisterService(MakeHeartbeatService(&hb).release());
auto server=b.BuildAndStart(); std::cout<<"Master en "<<addr<<"
"; server->Wait(); return 0; }