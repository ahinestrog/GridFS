package com.gridfs.datanode;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * DataNodeServer
 * - Levanta dos servidores gRPC:
 *   * IO server (cliente<->DN y DN<->DN): DataNodeIOService + ReplicationServiceImpl
 *   * Admin server (master->DN): AdminService
 * - Envía heartbeats al Master por un stream bidi (HeartbeatClient)
 *
 * Variables de entorno (con defaults):
 *   DN_ID           (default: dn-1)
 *   MASTER_ADDR     (default: localhost:50051)
 *   DN_IO_PORT      (default: 50052)
 *   DN_ADMIN_PORT   (default: 50053)
 *   DN_CHUNK_SIZE   (default: 65536)
 *   DN_DATA_DIR     (default: ./data)
 */
public class DataNodeServer {

    private final Server ioServer;
    private final Server adminServer;
    private final Server dualServer;   // wrapper para manejar ambos a la vez
    private final HeartbeatClient hb;

    public DataNodeServer(String nodeId,
                          String masterAddr,
                          int ioPort,
                          int adminPort,
                          int chunkSize,
                          String dataDir) throws Exception {

        StorageManager storage = new StorageManager(Path.of(dataDir), chunkSize);

        // >>> FIX: normaliza MASTER_ADDR para evitar resolver 'unix://'
        String normalizedMaster = normalizeTarget(masterAddr);

        // Heartbeats al Master (usará el target normalizado)
        this.hb = new HeartbeatClient(nodeId, normalizedMaster);

        // Servidor de I/O (WriteBlock, ReadBlock, FsOp) + Replicación DN<->DN
        this.ioServer = ServerBuilder.forPort(ioPort)
                .addService(new DataNodeIOService(storage))
                .addService(new ReplicationServiceImpl(storage))
                .build();

        // Servidor Admin (órdenes del Master)
        this.adminServer = ServerBuilder.forPort(adminPort)
                .addService(new AdminService(storage))
                .build();

        // Contenedor que arranca y espera por ambos
        this.dualServer = new DualServer(ioServer, adminServer);
    }

    public void start() throws Exception {
        dualServer.start();
    }

    public void blockUntilShutdown() throws Exception {
        dualServer.awaitTermination();
    }

    public void shutdown() {
        dualServer.shutdown();
    }

    public static void main(String[] args) throws Exception {
        String nodeId     = getenv("DN_ID", "dn-1");
        String masterAddr = getenv("MASTER_ADDR", "localhost:50051");
        int ioPort        = Integer.parseInt(getenv("DN_IO_PORT", "50052"));
        int adminPort     = Integer.parseInt(getenv("DN_ADMIN_PORT", "50053"));
        int chunkSize     = Integer.parseInt(getenv("DN_CHUNK_SIZE", "65536"));
        String dataDir    = getenv("DN_DATA_DIR", "./data");

        DataNodeServer app = new DataNodeServer(nodeId, masterAddr, ioPort, adminPort, chunkSize, dataDir);

        // Iniciar heartbeats después de construir el cliente
        app.hb.start(Map.of(
                "node_id", nodeId,
                "io_port", String.valueOf(ioPort),
                "admin_port", String.valueOf(adminPort),
                "data_dir", dataDir
        ), 2000);

        // Shutdown ordenado
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try { app.hb.close(); } catch (Exception ignore) {}
            app.shutdown();
        }));

        app.start();
        System.out.printf("DataNode started: IO=%d, ADMIN=%d, nodeId=%s, dataDir=%s%n",
                ioPort, adminPort, nodeId, dataDir);
        app.blockUntilShutdown();
    }

    private static String getenv(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? def : v;
    }

    // ---- FIX: normaliza targets para gRPC forTarget(...) ----
    // Si no hay esquema, antepone "dns:///" (evita caer en 'unix://')
    private static String normalizeTarget(String addr) {
        if (addr == null || addr.isBlank()) return "dns:///localhost:50051";
        String a = addr.trim();
        // soporta IPv6 con corchetes [::1]:50051
        if (a.contains("://")) return a; // ya trae esquema
        return "dns:///" + a;
    }

    /**
     * DualServer: wrapper de dos io.grpc.Server
     * - Arranca ambos
     * - Espera por ambos
     * - Implementa correctamente awaitTermination(long, TimeUnit) devolviendo boolean
     */
    private static class DualServer extends Server {
        private final Server a, b;

        DualServer(Server a, Server b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public Server start() throws java.io.IOException {
            a.start();
            b.start();
            return this;
        }

        @Override
        public Server shutdown() {
            a.shutdown();
            b.shutdown();
            return this;
        }

        @Override
        public Server shutdownNow() {
            a.shutdownNow();
            b.shutdownNow();
            return this;
        }

        @Override
        public boolean isShutdown() {
            return a.isShutdown() && b.isShutdown();
        }

        @Override
        public boolean isTerminated() {
            return a.isTerminated() && b.isTerminated();
        }

        @Override
        public void awaitTermination() throws InterruptedException {
            a.awaitTermination();
            b.awaitTermination();
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
            long start = System.nanoTime();
            boolean aDone = a.awaitTermination(timeout, unit);
            long elapsedNs = System.nanoTime() - start;

            long totalNs = unit.toNanos(timeout);
            long remainingNs = totalNs - elapsedNs;
            if (remainingNs < 0) remainingNs = 0;

            boolean bDone = b.awaitTermination(remainingNs, TimeUnit.NANOSECONDS);
            return aDone && bDone;
        }

        @Override
        public int getPort() {
            // No hay puerto único; devolvemos -1
            return -1;
        }

        @Override
        public String toString() {
            return "DualServer(IO=" + a.getPort() + ", ADMIN=" + b.getPort() + ")";
        }
    }
}
