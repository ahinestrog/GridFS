package com.gridfs.datanode;
import com.gridfs.proto.DataNodeIOGrpc; import com.gridfs.proto.Common.BlockChunk; import com.gridfs.proto.Common.BlockId; import com.gridfs.proto.Common.WriteAck; import com.google.protobuf.ByteString; import io.grpc.stub.StreamObserver; import java.io.FileOutputStream; import java.io.RandomAccessFile; import java.nio.file.*; import java.security.MessageDigest; import java.util.HexFormat;


public class DataNodeIOService extends DataNodeIOGrpc.DataNodeIOImplBase {
private final Path base; public DataNodeIOService(String dir){ this.base = Path.of(dir); this.base.toFile().mkdirs(); }
public int getBlocksCount(){ var list = this.base.toFile().listFiles(f->f.isFile()); return list==null?0:list.length; }
@Override public StreamObserver<BlockChunk> writeBlock(StreamObserver<WriteAck> out){
return new StreamObserver<>(){
Path tmp=null; String id=null; long bytes=0; MessageDigest md; { try{ md=MessageDigest.getInstance("SHA-256"); }catch(Exception e){ throw new RuntimeException(e);} }
public void onNext(BlockChunk c){ try{ if(id==null){ id=c.getBlockId(); tmp=base.resolve(id+".part"); Files.deleteIfExists(tmp); Files.createFile(tmp);} byte[] d=c.getData().toByteArray(); md.update(d); try(var fos=new FileOutputStream(tmp.toFile(),true)){ fos.write(d);} bytes+=d.length; if(c.getEof()) Files.move(tmp, base.resolve(id), StandardCopyOption.REPLACE_EXISTING);} catch(Exception e){ out.onNext(WriteAck.newBuilder().setOk(false).setBlockId(id==null?"":id).setBytesReceived(bytes).setMessage("Error: "+e.getMessage()).build()); out.onCompleted(); } }
public void onError(Throwable t){ t.printStackTrace(); }
public void onCompleted(){ var cs=HexFormat.of().formatHex(md.digest()); out.onNext(WriteAck.newBuilder().setOk(true).setBlockId(id==null?"":id).setBytesReceived(bytes).setChecksum(cs).setMessage("OK").build()); out.onCompleted(); }
}; }
@Override public void readBlock(BlockId req, StreamObserver<BlockChunk> out){ var p=base.resolve(req.getBlockId()); try(var raf=new RandomAccessFile(p.toFile(),"r")){ long seq=0; byte[] buf=new byte[1024*1024]; int n; while((n=raf.read(buf))>0){ out.onNext(BlockChunk.newBuilder().setBlockId(req.getBlockId()).setSeq(seq++).setData(ByteString.copyFrom(buf,0,n)).setEof(false).build()); } out.onNext(BlockChunk.newBuilder().setBlockId(req.getBlockId()).setSeq(seq).setEof(true).build()); out.onCompleted(); } catch(Exception e){ out.onError(e);} }
}