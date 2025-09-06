package com.gridfs.datanode;

import com.gridfs.proto.DataNodeIOGrpc;
import com.gridfs.proto.FsOpRequest;
import com.gridfs.proto.FsOpResponse;
import com.gridfs.proto.FsEntry;

import io.grpc.stub.StreamObserver;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class FsService extends DataNodeIOGrpc.DataNodeIOImplBase {

    @Override
    public void fsOp(FsOpRequest request, StreamObserver<FsOpResponse> responseObserver) {
        try {
            FsOpResponse.Builder rb = FsOpResponse.newBuilder().setOk(true).setMessage("OK");
            String path = request.getPath();
            switch (request.getOp()) {
                case LS -> {
                    Path p = Path.of(path);
                    var entries = new ArrayList<FsEntry>();
                    if (Files.isDirectory(p)) {
                        try (var s = Files.list(p)) {
                            s.forEach(pp -> {
                                try {
                                    entries.add(FsEntry.newBuilder()
                                            .setName(pp.getFileName().toString())
                                            .setIsDir(Files.isDirectory(pp))
                                            .setSize(Files.isDirectory(pp) ? 0 : Files.size(pp))
                                            .build());
                                } catch (Exception ignore) {}
                            });
                        }
                    }
                    rb.addAllEntries(entries);
                }
                case MKDIR -> Files.createDirectories(Path.of(path));
                case RM    -> Files.deleteIfExists(Path.of(path));
                case RMDIR -> Files.delete(Path.of(path));
                default    -> {}
            }
            responseObserver.onNext(rb.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(FsOpResponse.newBuilder().setOk(false).setMessage(e.getMessage()).build());
            responseObserver.onCompleted();
        }
    }
}
