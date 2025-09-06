package com.gridfs.datanode;

import com.gridfs.proto.*;
import io.grpc.stub.StreamObserver;

import java.io.OutputStream;
import java.nio.file.Path;

public class ReplicationServiceImpl extends ReplicationServiceGrpc.ReplicationServiceImplBase {

    private final StorageManager storage;

    public ReplicationServiceImpl(StorageManager storage) {
        this.storage = storage;
    }

    // Otro DN empuja un bloque hacia este DN
    @Override
    public StreamObserver<BlockChunk> pushBlock(StreamObserver<WriteAck> responseObserver) {
        return new StreamObserver<BlockChunk>() {
            private OutputStream out;
            private String blockId = null;
            private long bytes = 0;

            @Override
            public void onNext(BlockChunk chunk) {
                try {
                    if (blockId == null) {
                        blockId = chunk.getBlockId();
                        out = storage.openBlockWrite(blockId);
                    }
                    byte[] data = chunk.getData().toByteArray();
                    out.write(data);
                    bytes += data.length;
                    if (chunk.getEof()) out.flush();
                } catch (Exception e) {
                    onError(e);
                }
            }

            @Override
            public void onError(Throwable t) {
                try { if (out != null) out.close(); } catch (Exception ignore) {}
                responseObserver.onNext(WriteAck.newBuilder()
                        .setOk(false).setBlockId(blockId == null ? "" : blockId)
                        .setBytesReceived(bytes).setMessage("pushBlock error: " + t.getMessage()).build());
                responseObserver.onCompleted();
            }

            @Override
            public void onCompleted() {
                try {
                    if (out != null) out.close();
                    Path p = storage.pathForBlock(blockId);
                    String sum = storage.sha256(p);
                    responseObserver.onNext(WriteAck.newBuilder()
                            .setOk(true).setMessage("OK")
                            .setBlockId(blockId).setBytesReceived(bytes).setChecksum(sum).build());
                    responseObserver.onCompleted();
                } catch (Exception e) {
                    onError(e);
                }
            }
        };
    }
}
