package com.gridfs.datanode;
import io.grpc.Server; import io.grpc.ServerBuilder;
public class DataNodeServer {
public static void main(String[] args) throws Exception {
int port = 50052; String masterAddr = (args.length>0? args[0] : "localhost:50051"); String nodeId = (args.length>1? args[1] : "dn-1"); String dataDir = (args.length>2? args[2] : "/tmp/gridfs-dn1");
var ioSvc = new DataNodeIOService(dataDir); var replSvc = new ReplicationService(dataDir); var fsSvc = new FsService(dataDir); var adminSvc = new AdminService(dataDir);
Server s = ServerBuilder.forPort(port).addService(ioSvc).addService(replSvc).addService(fsSvc).addService(adminSvc).build().start();
System.out.println("DataNode en puerto "+port+" dir="+dataDir);
new HeartbeatClient(masterAddr, nodeId, ioSvc).start();
s.awaitTermination();
}
}