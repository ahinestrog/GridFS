#include <grpcpp/grpcpp.h>
#include <iostream>
#include <memory>
#include <string>

#include "master.grpc.pb.h"
#include "common.grpc.pb.h"
#include "heartbeat_store.h"
#include "metastore.h"

// Implementadas en tus .cpp correspondientes:
std::unique_ptr<gridfs::MasterService::Service>   MakeMasterService(MetaStore*);
std::unique_ptr<gridfs::MasterHeartbeat::Service> MakeHeartbeatService(HeartbeatStore*);

int main(int argc, char** argv) {
  const std::string addr = "0.0.0.0:50051";

  // Archivos por defecto si no se pasan por argv
  const char* hb_path = (argc > 1) ? argv[1] : "heartbeats.db";
  const char* ms_path = (argc > 2) ? argv[2] : "metastore.db";

  HeartbeatStore hb(hb_path);
  MetaStore      ms(ms_path);

  grpc::ServerBuilder builder;
  builder.AddListeningPort(addr, grpc::InsecureServerCredentials());
  builder.RegisterService(MakeMasterService(&ms).release());
  builder.RegisterService(MakeHeartbeatService(&hb).release());

  std::unique_ptr<grpc::Server> server = builder.BuildAndStart();
  std::cout << "Master en " << addr << "\n";
  server->Wait();
  return 0;
}
