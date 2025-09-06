package com.gridfs.datanode;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * DataNodeServer (corregido)
 *
 * Características:
 * - Lee parámetros desde ENV y/o FLAGS (los FLAGS tienen prioridad).
 * - FLAGS soportados: --id, --master, --port, --admin, --chunk-size, --storage
 * - Normaliza MASTER_ADDR para gRPC (evita resolver 'unix://').
 * - Evita "Address already in use":
 *     * Si adminPort == ioPort => construye UN solo Server con TODOS los servicios.
 *     * Si son distintos => construye dos Servers (IO y ADMIN) en puertos diferentes.
 * - Heartbeats al Master via HeartbeatClient (cliente gRPC saliente; NO abre puertos).
 *
 * ENV disponibles (si no pasas flags):
 *   DN_ID           (default: dn-1)
 *   MASTER_ADDR     (default: 127.0.0.1:50051)   // preferimos 127.0.0.1 en WSL
 *   DN_IO_PORT      (default: 50052)
 *   DN_ADMIN_PORT   (default: 50053)
 *   DN_CHUNK_SIZE   (default: 65536)
 *   DN_DATA_DIR     (default: ./data)
 */
public final class DataNodeServer {

    private final Server serverUnified;  // cuando IO==ADMIN, un solo server
    private final Server ioServer;       // cuando IO!=ADMIN
    private final Server adminServer;    // cuando IO!=ADMIN
    /* package */ final HeartbeatClient hb;

    private DataNodeServer(
            String nodeId,
            String masterAddr,
            int ioPort,
            int adminPort,
            int chunkSize,
            String dataDir
    ) throws Exception {

        // Almacenamiento local
        StorageManager storage = new StorageManager(Path.of(dataDir), chunkSize);

        // Normaliza dirección del master (evita 'unix://', soporta sin esquema)
        String normalizedMaster = normalizeTarget(masterAddr);

        // Cliente de Heartbeats (no abre puertos, solo canal saliente)
        this.hb = new HeartbeatClient(nodeId, normalizedMaster);

        // Servicios gRPC
        DataNodeIOService ioSvc = new DataNodeIOService(storage);          // Cliente<->DN y DN<->DN (streaming)
        ReplicationServiceImpl replSvc = new ReplicationServiceImpl(storage);
        AdminService adminSvc = new AdminService(storage);                  // Órdenes del Master

        // Si los dos puertos son iguales, evitamos doble bind creando UN solo server
        if (ioPort == adminPort) {
            this.serverUnified = ServerBuilder.forPort(ioPort)
                    .addService(ioSvc)
                    .addService(replSvc)
                    .addService(adminSvc)
                    .build();
            this.ioServer = null;
            this.adminServer = null;
            logf("[DN] Config: UNIFIED server en %d (IO + ADMIN)", ioPort);
        } else {
            // Dos servidores separados en puertos distintos
            this.serverUnified = null;
            this.ioServer = ServerBuilder.forPort(ioPort)
                    .addService(ioSvc)
                    .addService(replSvc)
                    .build();
            this.adminServer = ServerBuilder.forPort(adminPort)
                    .addService(adminSvc)
                    .build();
            logf("[DN] Config: IO=%d  ADMIN=%d (dos servidores)", ioPort, adminPort);
        }
    }

    /* ===== Ciclo de vida ===== */

    public void start() throws Exception {
        if (serverUnified != null) {
            serverUnified.start();
        } else {
            ioServer.start();
            adminServer.start();
        }
    }

    public void blockUntilShutdown() throws Exception {
        if (serverUnified != null) {
            serverUnified.awaitTermination();
        } else {
            // Espera a que ambos terminen.
            new DualServer(ioServer, adminServer).awaitTermination();
        }
    }

    public void shutdown() {
        try {
            if (serverUnified != null) serverUnified.shutdown();
            if (ioServer != null) ioServer.shutdown();
            if (adminServer != null) adminServer.shutdown();
        } catch (Throwable ignore) { /* noop */ }
    }

    /* ===== MAIN con flags>env ===== */

    public static void main(String[] args) throws Exception {
        // ENV por defecto (preferimos 127.0.0.1 en WSL)
        String nodeIdEnv  = getenv("DN_ID", "dn-1");
        String masterEnv  = getenv("MASTER_ADDR", "127.0.0.1:50051");
        int ioPortEnv     = parseInt(getenv("DN_IO_PORT", "50052"), 50052);
        int adminPortEnv  = parseInt(getenv("DN_ADMIN_PORT", "50053"), 50053);
        int chunkSizeEnv  = parseInt(getenv("DN_CHUNK_SIZE", "65536"), 65536);
        String dataDirEnv = getenv("DN_DATA_DIR", "./data");

        // FLAGS con prioridad sobre ENV
        Map<String, String> cli = parseArgs(args);
        String nodeId     = cli.getOrDefault("id", nodeIdEnv);
        String masterAddr = cli.getOrDefault("master", masterEnv);
        int ioPort        = parseInt(cli.getOrDefault("port", String.valueOf(ioPortEnv)), ioPortEnv);
        int adminPort     = parseInt(cli.getOrDefault("admin", String.valueOf(adminPortEnv)), adminPortEnv);
        int chunkSize     = parseInt(cli.getOrDefault("chunk-size", String.valueOf(chunkSizeEnv)), chunkSizeEnv);
        String dataDir    = cli.getOrDefault("storage", dataDirEnv);

        // Logs de parámetros finales (para depurar fácil)
        logf("[DN] Params finales -> id=%s master=%s ioPort=%d adminPort=%d chunk=%d dir=%s",
                nodeId, masterAddr, ioPort, adminPort, chunkSize, dataDir);

        // Validaciones básicas
        if (ioPort == 50051) {
            logf("[WARN] ioPort=%d coincide con el puerto del Master. Cámbialo.", ioPort);
        }
        if (ioPort <= 0 || adminPort <= 0) {
            throw new IllegalArgumentException("Puertos inválidos (<=0)");
        }

        DataNodeServer app = new DataNodeServer(nodeId, masterAddr, ioPort, adminPort, chunkSize, dataDir);

        // Heartbeats (stream bidi). NO abre puertos; sólo canal cliente -> master.
        app.hb.start(Map.of(
                "node_id", nodeId,
                "io_port", String.valueOf(ioPort),
                "admin_port", String.valueOf(adminPort),
                "data_dir", dataDir
        ), 2000);

        // Shutdown ordenado y robusto
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try { app.hb.close(); } catch (Throwable ignore) {}
            try { app.shutdown(); } catch (Throwable ignore) {}
        }));

        app.start();
        logf("DataNode started: IO=%d, ADMIN=%d, nodeId=%s, dataDir=%s",
                ioPort, adminPort, nodeId, dataDir);
        app.blockUntilShutdown();
    }

    /* ===== Utilidades ===== */

    private static int parseInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }

    private static String getenv(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? def : v.trim();
    }

    /** Flags estilo "--k=v" o "--k v". Devuelve mapa {k->v} sin los "--". */
    private static Map<String, String> parseArgs(String[] args) {
        HashMap<String, String> m = new HashMap<>();
        if (args == null) return m;
        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            if (a == null || !a.startsWith("--")) continue;
            String kv = a.substring(2);
            int eq = kv.indexOf('=');
            if (eq > 0) {
                String k = kv.substring(0, eq).trim();
                String v = kv.substring(eq + 1).trim();
                if (!k.isEmpty()) m.put(k, v);
            } else {
                String k = kv.trim();
                String v = (i + 1 < args.length && (args[i + 1] != null) && !args[i + 1].startsWith("--"))
                        ? args[++i].trim()
                        : "true";
                if (!k.isEmpty()) m.put(k, v);
            }
        }
        return m;
    }

    // Normaliza targets para gRPC ManagedChannelBuilder.forTarget(...)
    // Si no hay esquema, antepone "dns:///" (evita resolver 'unix://').
    private static String normalizeTarget(String addr) {
        if (addr == null || addr.isBlank()) return "dns:///127.0.0.1:50051";
        String a = addr.trim();
        // Si ya trae esquema (dns://, ipv4://, etc.), respetar:
        if (a.contains("://")) return a;
        // Caso general: host:port o [ipv6]:port
        return "dns:///" + a;
    }

    private static void logf(String fmt, Object... args) {
        System.out.println(String.format(fmt, args));
    }

    /** Wrapper de dos Servers para esperar terminación de ambos. */
    private static final class DualServer extends Server {
        private final Server a, b;

        DualServer(Server a, Server b) {
            this.a = a;
            this.b = b;
        }

        @Override public Server start() throws java.io.IOException { a.start(); b.start(); return this; }
        @Override public Server shutdown() { a.shutdown(); b.shutdown(); return this; }
        @Override public Server shutdownNow() { a.shutdownNow(); b.shutdownNow(); return this; }
        @Override public boolean isShutdown() { return a.isShutdown() && b.isShutdown(); }
        @Override public boolean isTerminated() { return a.isTerminated() && b.isTerminated(); }

        @Override
        public void awaitTermination() throws InterruptedException {
            a.awaitTermination();
            b.awaitTermination();
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
            long totalNs = unit.toNanos(timeout);
            long start = System.nanoTime();
            boolean aDone = a.awaitTermination(timeout, unit);
            long used = System.nanoTime() - start;
            long remain = Math.max(0, totalNs - used);
            boolean bDone = b.awaitTermination(remain, TimeUnit.NANOSECONDS);
            return aDone && bDone;
        }

        @Override public int getPort() { return -1; }
        @Override public String toString() { return "DualServer(a=" + a.getPort() + ", b=" + b.getPort() + ")"; }
    }
}
