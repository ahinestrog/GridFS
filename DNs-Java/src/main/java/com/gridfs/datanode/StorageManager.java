package com.gridfs.datanode;

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.HexFormat;

public class StorageManager {
    private final Path baseDir;
    private final int chunkSize;

    public StorageManager(Path baseDir, int chunkSize) throws IOException {
        this.baseDir = baseDir;
        this.chunkSize = chunkSize;
        Files.createDirectories(baseDir);
    }

    public Path pathForBlock(String blockId) {
        return baseDir.resolve(blockId);
    }

    public OutputStream openBlockWrite(String blockId) throws IOException {
        Path p = pathForBlock(blockId);
        Files.createDirectories(p.getParent() == null ? baseDir : p.getParent());
        return Files.newOutputStream(p, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    }

    public InputStream openBlockRead(String blockId) throws IOException {
        Path p = pathForBlock(blockId);
        return Files.newInputStream(p, StandardOpenOption.READ);
    }

    public boolean deleteBlock(String blockId) throws IOException {
        Path p = pathForBlock(blockId);
        return Files.deleteIfExists(p);
    }

    public String sha256(Path p) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        try (InputStream in = Files.newInputStream(p)) {
            byte[] buf = new byte[64 * 1024];
            int n;
            while ((n = in.read(buf)) > 0) md.update(buf, 0, n);
        }
        return HexFormat.of().formatHex(md.digest());
    }

    public int getChunkSize() {
        return chunkSize;
    }
}
