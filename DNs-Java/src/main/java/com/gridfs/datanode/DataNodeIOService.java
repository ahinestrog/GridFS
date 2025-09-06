package com.gridfs.datanode;

import com.gridfs.proto.*;
import io.grpc.stub.StreamObserver;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DataNodeIOService extends DataNodeIOGrpc.DataNodeIOImplBase {

    private final StorageManager storage;

    public DataNodeIOService(StorageManager storage) {
        this.storage = storage;
    }

    // Cliente -> stream chunks -> ACK final
    @Override
    public StreamObserver<BlockChunk> writeBlock(StreamObserver<WriteAck> responseObserver) {
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
                    if (chunk.getEof()) {
                        out.flush();
                    }
                } catch (Exception e) {
                    onError(e);
                }
            }

            @Override
            public void onError(Throwable t) {
                try { if (out != null) out.close(); } catch (Exception ignore) {}
                WriteAck ack = WriteAck.newBuilder()
                        .setBlockId(blockId == null ? "" : blockId)
                        .setBytesReceived(bytes)
                        .setOk(false)
                        .setMessage("writeBlock error: " + t.getMessage())
                        .build();
                responseObserver.onNext(ack);
                responseObserver.onCompleted();
            }

            @Override
            public void onCompleted() {
                try {
                    if (out != null) out.close();
                    Path p = storage.pathForBlock(blockId);
                    String sum = storage.sha256(p);
                    WriteAck ack = WriteAck.newBuilder()
                            .setBlockId(blockId)
                            .setBytesReceived(bytes)
                            .setChecksum(sum)
                            .setOk(true)
                            .setMessage("OK")
                            .build();
                    responseObserver.onNext(ack);
                    responseObserver.onCompleted();
                } catch (Exception e) {
                    onError(e);
                }
            }
        };
    }

    // Cliente pide un bloque por ID -> stream de chunks
    @Override
    public void readBlock(BlockId request, StreamObserver<BlockChunk> responseObserver) {
        String blockId = request.getBlockId();
        try (InputStream in = storage.openBlockRead(blockId)) {
            byte[] buf = new byte[storage.getChunkSize()];
            long seq = 0;
            int n;
            while ((n = in.read(buf)) > 0) {
                BlockChunk.Builder b = BlockChunk.newBuilder()
                        .setBlockId(blockId)
                        .setSeq(seq++)
                        .setData(com.google.protobuf.ByteString.copyFrom(buf, 0, n))
                        .setEof(false);
                responseObserver.onNext(b.build());
            }
            responseObserver.onNext(BlockChunk.newBuilder().setBlockId(blockId).setSeq(seq).setEof(true).build());
            responseObserver.onCompleted();
        } catch (IOException e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void fsOp(FsOpRequest request, StreamObserver<FsOpResponse> responseObserver) {
        try {
            FsOpResponse.Builder rb = FsOpResponse.newBuilder().setOk(true).setMessage("OK");
            String path = request.getPath();
            switch (request.getOp()) {
                case LS -> {
                    Path p = Path.of(path);
                    List<FsEntry> entries = new ArrayList<>();
                    if (Files.isDirectory(p)) {
                        try (var s = Files.list(p)) {
                            s.forEach(pp -> {
                                try {
                                    entries.add(FsEntry.newBuilder()
                                            .setName(pp.getFileName().toString())
                                            .setIsDir(Files.isDirectory(pp))
                                            .setSize(Files.isDirectory(pp) ? 0 : Files.size(pp))
                                            .build());
                                } catch (IOException ignored) {}
                            });
                        }
                    }
                    rb.addAllEntries(entries);
                }
                case MKDIR -> Files.createDirectories(Path.of(path));
                case RM -> Files.deleteIfExists(Path.of(path));
                case RMDIR -> Files.delete(Path.of(path));
                default -> {}
            }
            responseObserver.onNext(rb.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(FsOpResponse.newBuilder().setOk(false).setMessage(e.getMessage()).build());
            responseObserver.onCompleted();
        }
    }
}
