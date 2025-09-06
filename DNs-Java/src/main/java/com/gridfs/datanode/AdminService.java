package com.gridfs.datanode;

import com.google.protobuf.ByteString;
import com.gridfs.proto.AdminOrderRequest;
import com.gridfs.proto.AdminOrderResponse;
import com.gridfs.proto.BlockChunk;
import com.gridfs.proto.DataNodeAdminGrpc;
import com.gridfs.proto.ReplicationServiceGrpc;
import com.gridfs.proto.WriteAck;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.io.InputStream;
import java.util.AbstractMap;
import java.util.concurrent.TimeUnit;

/**
 * Servicio de órdenes del Master para un DataNode.
 * Implementa:
 *   - ReplicateCmd  -> empuja un bloque a otro DataNode (DN<->DN)
 *   - DeleteBlock   -> elimina el bloque local
 *
 * NOTA: Se evita ManagedChannelBuilder.forTarget(...) para no depender del
 * esquema por defecto (p.ej. unix://). En su lugar, parseamos host:puerto y
 * usamos forAddress(host, port).
 */
public class AdminService extends DataNodeAdminGrpc.DataNodeAdminImplBase {

    private final StorageManager storage;

    public AdminService(StorageManager storage) {
        this.storage = storage;
    }

    @Override
    public void adminOrder(AdminOrderRequest request, StreamObserver<AdminOrderResponse> responseObserver) {
        try {
            if (request.hasReplicate()) {
                var cmd = request.getReplicate(); // ReplicateCmd
                replicateBlockTo(cmd.getBlockId(), cmd.getTargetDn());
                responseObserver.onNext(AdminOrderResponse.newBuilder()
                        .setOk(true).setMessage("Replicated").build());
            } else if (request.hasDeleteBlock()) {
                boolean ok = storage.deleteBlock(request.getDeleteBlock().getBlockId());
                responseObserver.onNext(AdminOrderResponse.newBuilder()
                        .setOk(ok).setMessage(ok ? "Deleted" : "NotFound").build());
            } else {
                responseObserver.onNext(AdminOrderResponse.newBuilder()
                        .setOk(false).setMessage("No order").build());
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(AdminOrderResponse.newBuilder()
                    .setOk(false).setMessage(e.getMessage()).build());
            responseObserver.onCompleted();
        }
    }

    /**
     * Empuja un bloque a otro DataNode usando ReplicationService.PushBlock.
     * targetEndpoint puede venir como:
     *   - "host:50052"
     *   - "dns:///host:50052"
     *   - "[2001:db8::1]:50052"
     */
    private void replicateBlockTo(String blockId, String targetEndpoint) throws Exception {
        var hp = parseHostPort(targetEndpoint, 50052); // puerto por defecto para ReplicationService
        String host = hp.getKey();
        int port = hp.getValue();

        ManagedChannel ch = ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .build();

        var stub = ReplicationServiceGrpc.newStub(ch);

        StreamObserver<WriteAck> ackObs = new StreamObserver<>() {
            @Override public void onNext(WriteAck value) {
                System.out.println("Replica ACK ok=" + value.getOk() + " msg=" + value.getMessage());
            }
            @Override public void onError(Throwable t) {
                System.err.println("Replica error: " + t.getMessage());
            }
            @Override public void onCompleted() { }
        };

        StreamObserver<BlockChunk> up = null;
        try {
            up = stub.pushBlock(ackObs);

            try (InputStream in = storage.openBlockRead(blockId)) {
                byte[] buf = new byte[storage.getChunkSize()];
                long seq = 0; int n;
                while ((n = in.read(buf)) > 0) {
                    up.onNext(BlockChunk.newBuilder()
                            .setBlockId(blockId).setSeq(seq++)
                            .setData(ByteString.copyFrom(buf, 0, n))
                            .setEof(false).build());
                }
                // EOF
                up.onNext(BlockChunk.newBuilder()
                        .setBlockId(blockId).setSeq(seq).setEof(true).build());
                up.onCompleted();
            }
        } catch (Exception ex) {
            if (up != null) {
                try { up.onError(ex); } catch (Exception ignore) {}
            }
            throw ex;
        } finally {
            ch.shutdown();
            ch.awaitTermination(3, TimeUnit.SECONDS);
        }
    }

    // ---------- helpers ----------

    /**
     * Parsea host:puerto (con soporte para dns:/// y IPv6 entre corchetes).
     * Devuelve (host, port). Si no se puede parsear, usa localhost:defPort.
     */
    private static AbstractMap.SimpleEntry<String, Integer> parseHostPort(String endpoint, int defPort) {
        String host = "localhost";
        int port = defPort;

        if (endpoint == null || endpoint.isBlank()) {
            return new AbstractMap.SimpleEntry<>(host, port);
        }

        String addr = endpoint.trim();

        // Quitar esquema si trae (p.ej. dns:///)
        int schemeIdx = addr.indexOf("://");
        if (schemeIdx >= 0) {
            addr = addr.substring(schemeIdx + 3);
        }
        // Quitar slashes extra (dns:///host:port -> host:port)
        while (addr.startsWith("/")) addr = addr.substring(1);

        // IPv6: [2001:db8::1]:50052
        if (addr.startsWith("[")) {
            int r = addr.indexOf(']');
            if (r > 0) {
                host = addr.substring(1, r);
                int colon = addr.indexOf(':', r);
                if (colon > 0 && colon < addr.length() - 1) {
                    try { port = Integer.parseInt(addr.substring(colon + 1).trim()); } catch (Exception ignore) {}
                }
                return new AbstractMap.SimpleEntry<>(host, port);
            }
        }

        // host:port (último ':')
        int lastColon = addr.lastIndexOf(':');
        if (lastColon > 0 && lastColon < addr.length() - 1 && addr.indexOf(':') == lastColon) {
            host = addr.substring(0, lastColon).trim();
            try { port = Integer.parseInt(addr.substring(lastColon + 1).trim()); } catch (Exception ignore) { port = defPort; }
        } else {
            // solo host
            host = addr.isBlank() ? "localhost" : addr;
            port = defPort;
        }
        return new AbstractMap.SimpleEntry<>(host, port);
    }
}
