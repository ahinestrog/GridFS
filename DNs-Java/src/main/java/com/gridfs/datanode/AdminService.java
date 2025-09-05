package com.gridfs.datanode;
import com.gridfs.proto.DataNodeAdminGrpc; import com.gridfs.proto.Admin.AdminOrderRequest; import com.gridfs.proto.Admin.AdminOrderResponse; import com.gridfs.proto.Admin.ReplicateCmd; import com.gridfs.proto.Common.BlockChunk; import com.gridfs.proto.Common.WriteAck; import com.gridfs.proto.ReplicationServiceGrpc; import com.google.protobuf.ByteString; import io.grpc.ManagedChannelBuilder; import io.grpc.stub.StreamObserver; import java.io.RandomAccessFile; import java.nio.file.*;


public class AdminService extends DataNodeAdminGrpc.DataNodeAdminImplBase {
private final Path base; public AdminService(String dir){ this.base=Path.of(dir);}
@Override public void adminOrder(AdminOrderRequest req, StreamObserver<AdminOrderResponse> out){ try{
if(req.hasReplicate()){ replicate(req.getReplicate()); }
else if(req.hasDeleteBlock()){ Files.deleteIfExists(base.resolve(req.getDeleteBlock().getBlockId())); }
out.onNext(AdminOrderResponse.newBuilder().setOk(true).setMessage("OK").build()); out.onCompleted();
} catch(Exception e){ out.onNext(AdminOrderResponse.newBuilder().setOk(false).setMessage(e.getMessage()).build()); out.onCompleted(); } }
private void replicate(ReplicateCmd cmd) throws Exception { var p=base.resolve(cmd.getBlockId()); try(var raf=new RandomAccessFile(p.toFile(),"r")){
var ch=ManagedChannelBuilder.forTarget(cmd.getTargetDn()).usePlaintext().build(); var async=ReplicationServiceGrpc.newStub(ch); final WriteAck[] ack=new WriteAck[1]; StreamObserver<WriteAck> resp=new StreamObserver<>(){ public void onNext(WriteAck v){ ack[0]=v;} public void onError(Throwable t){ t.printStackTrace(); } public void onCompleted(){} };
StreamObserver<BlockChunk> req = async.pushBlock(resp); long seq=0; byte[] buf=new byte[1024*1024]; int n; while((n=raf.read(buf))>0){ req.onNext(BlockChunk.newBuilder().setBlockId(cmd.getBlockId()).setSeq(seq++).setData(ByteString.copyFrom(buf,0,n)).setEof(false).build()); } req.onNext(BlockChunk.newBuilder().setBlockId(cmd.getBlockId()).setSeq(seq).setEof(true).build()); req.onCompleted(); }
}
}
