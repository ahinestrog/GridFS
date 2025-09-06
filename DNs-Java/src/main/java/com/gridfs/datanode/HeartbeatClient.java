package com.gridfs.datanode;

import com.gridfs.proto.HeartbeatAck;
import com.gridfs.proto.HeartbeatKv;
import com.gridfs.proto.MasterHeartbeatGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.time.Instant;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * Cliente de heartbeats hacia el Master.
 * - Acepta MASTER_ADDR en formas: "host:port", "dns:///host:port", "127.0.0.1:50051", "[::1]:50051"
 * - Siempre construye el canal con forAddress(host, port) para evitar resolver "unix://"
 */
public class HeartbeatClient implements AutoCloseable {
    private final String nodeId;
    private final ManagedChannel ch;
    private final MasterHeartbeatGrpc.MasterHeartbeatStub stub;
    private StreamObserver<HeartbeatKv> upstream;
    private final ScheduledExecutorService ses =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "hb-scheduler");
                t.setDaemon(true);
                return t;
            });

    public HeartbeatClient(String nodeId, String masterAddress) {
        this.nodeId = Objects.requireNonNull(nodeId, "nodeId");

        // Parsear dirección del Master de forma segura
        AbstractMap.SimpleEntry<String,Integer> hp = parseHostPortSafe(masterAddress, 50051);
        String host = hp.getKey();
        int port = hp.getValue();

        System.out.printf("[HB] MASTER_ADDR resolved -> %s:%d%n", host, port);

        this.ch = ManagedChannelBuilder
                .forAddress(host, port)   // <- clave: no usar forTarget
                .usePlaintext()
                .build();

        this.stub = MasterHeartbeatGrpc.newStub(ch);
    }

    /** Inicia el stream y programa beats periódicos. */
    public void start(Map<String,String> initialKv, long periodMs) {
        this.upstream = stub.streamStatus(new StreamObserver<>() {
            @Override public void onNext(HeartbeatAck value) { /* opcional: log acks */ }
            @Override public void onError(Throwable t) {
                System.err.println("[HB] stream error: " + t.getMessage());
            }
            @Override public void onCompleted() {
                System.out.println("[HB] stream completed");
            }
        });

        // Primer envío de KV iniciales (metadata del nodo)
        if (initialKv != null) {
            initialKv.forEach(this::send);
        }

        // Beats periódicos
        ses.scheduleAtFixedRate(() -> {
            try { send("heartbeat", "1"); }
            catch (Exception e) { e.printStackTrace(); }
        }, periodMs, periodMs, TimeUnit.MILLISECONDS);
    }

    /** Envía una pareja key=value como HeartbeatKv. */
    public void send(String key, String value) {
        if (upstream == null) return;
        HeartbeatKv hb = HeartbeatKv.newBuilder()
                .setNodeId(nodeId)
                .setKey(key)
                .setValue(value == null ? "" : value)
                .setTsUnixMs(Instant.now().toEpochMilli())
                .build();
        upstream.onNext(hb);
    }

    @Override
    public void close() {
        try {
            if (upstream != null) upstream.onCompleted();
        } catch (Exception ignore) {}
        ses.shutdownNow();
        ch.shutdown();
        try {
            ch.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    // ---------- helpers ----------

    /**
     * Parsea host:puerto con soporte para:
     *   - prefijo de esquema (dns:///)
     *   - múltiples '/' iniciales
     *   - IPv6 entre corchetes: [::1]:50051
     * Si no se puede parsear, devuelve localhost:defPort.
     */
    private static AbstractMap.SimpleEntry<String,Integer> parseHostPortSafe(String endpoint, int defPort) {
        String host = "localhost";
        int port = defPort;

        if (endpoint == null || endpoint.isBlank()) {
            return new AbstractMap.SimpleEntry<>(host, port);
        }

        String addr = endpoint.trim();

        // Quitar esquema si viene (p.ej., "dns:///" o "ipv4:///")
        int schemeIdx = addr.indexOf("://");
        if (schemeIdx >= 0) addr = addr.substring(schemeIdx + 3);
        // Quitar slashes extra (dns:///host:port -> host:port)
        while (addr.startsWith("/")) addr = addr.substring(1);

        // IPv6: [2001:db8::1]:50051
        if (addr.startsWith("[")) {
            int r = addr.indexOf(']');
            if (r > 0) {
                host = addr.substring(1, r);
                int colon = addr.indexOf(':', r);
                if (colon > 0 && colon < addr.length() - 1) {
                    port = parsePortOrDefault(addr.substring(colon + 1), defPort);
                }
                return new AbstractMap.SimpleEntry<>(host, port);
            }
        }

        // host:port (único ':')
        int lastColon = addr.lastIndexOf(':');
        if (lastColon > 0 && lastColon < addr.length() - 1 && addr.indexOf(':') == lastColon) {
            host = safeTrim(addr.substring(0, lastColon));
            port = parsePortOrDefault(addr.substring(lastColon + 1), defPort);
        } else {
            host = safeTrim(addr);
            port = defPort;
        }

        if (host.isEmpty()) host = "localhost";
        return new AbstractMap.SimpleEntry<>(host, port);
    }

    private static int parsePortOrDefault(String s, int def) {
        try { return Integer.parseInt(s.trim()); }
        catch (Exception ignore) { return def; }
    }

    private static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }
}
