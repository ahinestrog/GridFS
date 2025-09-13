#include <grpcpp/grpcpp.h>
#include "master.grpc.pb.h"
#include "common.pb.h"
#include "heartbeat_store.h"


using grpc::ServerContext;
using grpc::Status;
using grpc::ServerReaderWriter;
using proto::MasterHeartbeat;
using proto::HeartbeatKv;
using proto::HeartbeatAck;

class HB final : public MasterHeartbeat::Service {
public: 
    explicit HB(HeartbeatStore* st): st_(st) {}

    Status StreamStatus(ServerContext*, 
                        ServerReaderWriter<HeartbeatAck, HeartbeatKv>* stream) override {
        HeartbeatKv kv; 
        while(stream->Read(&kv)){
            try{
                st_->Upsert(kv.node_id(), kv.key(), kv.value(), kv.ts_unix_ms());
                HeartbeatAck a; a.set_ok(true); a.set_message("ok");
                
                stream->Write(a);
            } catch(const std::exception& e){
                HeartbeatAck a; a.set_ok(false); a.set_message(e.what());
                stream->Write(a);
            }
        }
        return Status::OK;
    }
private:
    HeartbeatStore* st_;
};

std::unique_ptr<proto::MasterHeartbeat::Service> MakeHeartbeatService(HeartbeatStore* s){
    return std::make_unique<HB>(s);
}