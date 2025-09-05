package com.gridfs.datanode;
import com.gridfs.proto.MasterHeartbeatGrpc; import com.gridfs.proto.Common.HeartbeatAck; import com.gridfs.proto.Common.HeartbeatKv;
import io.grpc.ManagedChannelBuilder; import io.grpc.stub.StreamObserver; import java.lang.management.ManagementFactory; import java.nio.file.FileStore; import java.nio.file.FileSystems; import java.time.Instant; import java.util.Timer; import java.util.TimerTask;


public class HeartbeatClient {
private final String nodeId; private final com.gridfs.proto.MasterHeartbeatGrpc.MasterHeartbeatStub stub; private final DataNodeIOService io;
public HeartbeatClient(String master, String nodeId, DataNodeIOService io){ this.nodeId=nodeId; var ch=ManagedChannelBuilder.forTarget(master).usePlaintext().build(); this.stub=MasterHeartbeatGrpc.newStub(ch); this.io=io; }
public void start(){
StreamObserver<HeartbeatKv> req = stub.streamStatus(new StreamObserver<HeartbeatAck>(){ public void onNext(HeartbeatAck v){} public void onError(Throwable t){ t.printStackTrace(); } public void onCompleted(){} });
Timer t = new Timer(true); t.scheduleAtFixedRate(new TimerTask(){ @Override public void run(){ send(req,"alive","1"); send(req,"pid", ManagementFactory.getRuntimeMXBean().getName()); send(req,"blocks_count", String.valueOf(io.getBlocksCount())); long free=0,total=0; try{ for(FileStore s: FileSystems.getDefault().getFileStores()){ free+=s.getUnallocatedSpace(); total+=s.getTotalSpace(); } }catch(Exception ignored){} send(req,"disk_free_bytes", String.valueOf(free)); send(req,"disk_total_bytes", String.valueOf(total)); }},0,5000);
}
private void send(StreamObserver<HeartbeatKv> req, String k, String v){ req.onNext(HeartbeatKv.newBuilder().setNodeId(nodeId).setKey(k).setValue(v).setTsUnixMs(Instant.now().toEpochMilli()).build()); }
}