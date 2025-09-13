package com.gridfs.datanode;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.net.ServerSocket;
import java.io.IOException;

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
        // Leemos MASTER_ADDR y dejamos que el resto se autocalcule si no está en env
        String masterAddr = getenv("MASTER_ADDR", "localhost:50051");

        String envIoPort    = System.getenv("DN_IO_PORT");
        String envAdminPort = System.getenv("DN_ADMIN_PORT");
        String envNodeId    = System.getenv("DN_ID");
        String envDataDir   = System.getenv("DN_DATA_DIR");
        String envChunkSize = System.getenv("DN_CHUNK_SIZE");

        // 1) Seleccionar puertos: si no están definidos, buscar par libre empezando en 50052/50053
        int[] pair = selectIoAdminPorts(envIoPort, envAdminPort);
        int ioPort = pair[0];
        int adminPort = pair[1];

        // 2) Derivar índice y defaults coherentes: dn-{n} y /tmp/gridfs/dn{n}
        int index = Math.max(1, ((ioPort - 50052) / 2) + 1);
        String nodeId = envNodeId == null || envNodeId.isBlank() ? ("dn-" + index) : envNodeId;
        String dataDir = envDataDir == null || envDataDir.isBlank() ? ("/tmp/gridfs/dn" + index) : envDataDir;

        // 3) Chunk size default
        int chunkSize = parseIntOrDefault(envChunkSize, 65536);

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

    // ---- Utilidades de selección de puertos y parsing ----
    private static int[] selectIoAdminPorts(String envIo, String envAdmin) {
        // Si ambos definidos, usarlos
        if (isNonBlank(envIo) && isNonBlank(envAdmin)) {
            return new int[]{parseIntOrDefault(envIo, 50052), parseIntOrDefault(envAdmin, 50053)};
        }
        // Si uno definido, intentar respetarlo y buscar el otro cercano (par par/impar)
        if (isNonBlank(envIo)) {
            int io = parseIntOrDefault(envIo, 50052);
            int admin = (io % 2 == 0) ? io + 1 : io - 1; // parear con el vecino
            if (!isPortFree(admin)) {
                // buscar siguiente par libre a partir del menor par
                int[] p = findNextFreePair(Math.min(io - (io % 2), admin - (admin % 2)), 50052);
                return p;
            }
            if (!isPortFree(io)) {
                int[] p = findNextFreePair(alignEven(io), 50052);
                return p;
            }
            return new int[]{io, admin};
        }
        if (isNonBlank(envAdmin)) {
            int admin = parseIntOrDefault(envAdmin, 50053);
            int io = (admin % 2 == 0) ? admin - 1 : admin + 1;
            if (!isPortFree(io)) {
                int[] p = findNextFreePair(alignEven(io), 50052);
                return p;
            }
            if (!isPortFree(admin)) {
                int[] p = findNextFreePair(alignEven(io), 50052);
                return p;
            }
            return new int[]{io, admin};
        }
        // Ninguno definido: buscar par libre 50052/50053, 50054/50055, ...
        return findNextFreePair(50052, 50052);
    }

    private static int[] findNextFreePair(int startEven, int min) {
        int p = Math.max(alignEven(startEven), Math.max(alignEven(min), 50052));
        while (p < 65534) {
            int io = p;
            int admin = p + 1;
            if (isPortFree(io) && isPortFree(admin)) {
                return new int[]{io, admin};
            }
            p += 2;
        }
        // Fallback: usar puertos efímeros si no encontramos (muy improbable)
        int io = randomFreePort();
        int admin = randomFreePort();
        return new int[]{io, admin};
    }

    private static int alignEven(int x) { return (x % 2 == 0) ? x : x - 1; }

    private static boolean isNonBlank(String s) { return s != null && !s.isBlank(); }

    private static int parseIntOrDefault(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }

    private static boolean isPortFree(int port) {
        try (ServerSocket ss = new ServerSocket(port)) {
            ss.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static int randomFreePort() {
        try (ServerSocket ss = new ServerSocket(0)) {
            ss.setReuseAddress(true);
            return ss.getLocalPort();
        } catch (IOException e) {
            // último recurso
            return 0;
        }
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
