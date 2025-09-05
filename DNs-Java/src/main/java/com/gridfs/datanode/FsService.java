package com.gridfs.datanode;
import com.gridfs.proto.DataNodeIOGrpc; import com.gridfs.proto.Common.FsOpRequest; import com.gridfs.proto.Common.FsOpResponse; import com.gridfs.proto.Common.FsEntry; import io.grpc.stub.StreamObserver; import java.io.File; import java.nio.file.*;
public class FsService extends DataNodeIOGrpc.DataNodeIOImplBase { private final Path base; public FsService(String dir){ this.base=Path.of(dir); this.base.toFile().mkdirs(); }
@Override public void fsOp(FsOpRequest r, StreamObserver<FsOpResponse> out){ try{ switch(r.getOp()){
case LS -> { var b=FsOpResponse.newBuilder().setOk(true); File[] files=base.toFile().listFiles(); if(files!=null) for(File f: files){ b.addEntries(FsEntry.newBuilder().setName(f.getName()).setIsDir(f.isDirectory()).setSize(f.isFile()?f.length():0)); } out.onNext(b.build()); out.onCompleted(); }
case MKDIR -> { Files.createDirectories(base.resolve(r.getPath())); out.onNext(FsOpResponse.newBuilder().setOk(true).build()); out.onCompleted(); }
case RM -> { Files.deleteIfExists(base.resolve(r.getPath())); out.onNext(FsOpResponse.newBuilder().setOk(true).build()); out.onCompleted(); }
case RMDIR -> { Files.deleteIfExists(base.resolve(r.getPath())); out.onNext(FsOpResponse.newBuilder().setOk(true).build()); out.onCompleted(); }
default -> throw new IllegalArgumentException("Op no soportada"); } } catch(Exception e){ out.onNext(FsOpResponse.newBuilder().setOk(false).setMessage(e.getMessage()).build()); out.onCompleted(); } }
}
