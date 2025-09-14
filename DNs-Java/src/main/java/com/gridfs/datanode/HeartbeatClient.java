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
 * - Acepta MASTER_ADDR en formas: "host:port", "dns:///host:port", "127.0.0.1:50051"
 * - Siempre construye el canal con forAddress(host, port) "
 */
public class HeartbeatClient implements AutoCloseable {
    private final String nodeId;
    private final ManagedChannel ch;
    private final MasterHeartbeatGrpc.MasterHeartbeatStub stub;
    private StreamObserver<HeartbeatKv> upstream;
    private final StorageManager storage;
    private final ScheduledExecutorService ses =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "hb-scheduler");
                t.setDaemon(true);
                return t;
            });

    public HeartbeatClient(String nodeId, String masterAddress, StorageManager storage) {
        this.nodeId = Objects.requireNonNull(nodeId, "nodeId");
        this.storage = Objects.requireNonNull(storage, "storage");

        // Parsear dirección del Master de forma segura
        AbstractMap.SimpleEntry<String,Integer> hp = parseHostPortSafe(masterAddress, 50051);
        String host = hp.getKey();
        int port = hp.getValue();

        System.out.printf("[HB] MASTER_ADDR resolved -> %s:%d%n", host, port);

        this.ch = ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .build();

    this.stub = MasterHeartbeatGrpc.newStub(ch);
    }

    /** Inicia el stream y programa beats periódicos. */
    public void start(Map<String,String> initialKv, long periodMs) {
        this.upstream = stub.streamStatus(new StreamObserver<>() {
            @Override public void onNext(HeartbeatAck value) {}
            @Override public void onError(Throwable t) {
                System.err.println("[HB] stream error: " + t.getMessage());
            }
            @Override public void onCompleted() {
                System.out.println("[HB] stream completed");
            }
        });

        if (initialKv != null) {
            initialKv.forEach(this::send);
        }

        // Beats periódicos
        ses.scheduleAtFixedRate(() -> {
            try { 
                send("heartbeat", "1");
                // Reportar espacio disponible de nodos periódicamente
                sendSpaceInfo();
            }
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

    /** Envía información de espacio disponible del storage. */
    private void sendSpaceInfo() {
        try {
            long freeSpace = storage.getFreeSpace();
            long totalSpace = storage.getTotalSpace();
            long usedSpace = totalSpace - freeSpace;
            
            send("free_space_bytes", String.valueOf(freeSpace));
            send("total_space_bytes", String.valueOf(totalSpace));
            send("used_space_bytes", String.valueOf(usedSpace));
        } catch (Exception e) {
            System.err.println("[HB] Error getting space info: " + e.getMessage());
        }
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

    private static AbstractMap.SimpleEntry<String,Integer> parseHostPortSafe(String endpoint, int defPort) {
        // Get default host from environment variable
        String defaultHost = System.getenv("GRIDFS_HOST");
        if (defaultHost == null || defaultHost.isBlank()) {
            defaultHost = "localhost";
        }
        String host = defaultHost;
        int port = defPort;

        if (endpoint == null || endpoint.isBlank()) {
            return new AbstractMap.SimpleEntry<>(host, port);
        }

        String addr = endpoint.trim();

        // Quitar esquema si viene (p.ej., "dns:///" o "ipv4:///")
        int schemeIdx = addr.indexOf("://");
        if (schemeIdx >= 0) addr = addr.substring(schemeIdx + 3);
        while (addr.startsWith("/")) addr = addr.substring(1);

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

        if (host.isEmpty()) host = defaultHost;
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
