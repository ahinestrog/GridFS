package com.gridfs.datanode;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;

public class HeartbeatTask implements Runnable {
  private final String masterAddr;
  private final StorageManager storage;
  private final String datanodeId;
  private final AtomicBoolean running = new AtomicBoolean(false);
  private Thread t;

  public HeartbeatTask(String masterAddr, StorageManager storage, String datanodeId) {
    this.masterAddr = masterAddr;
    this.storage = storage;
    this.datanodeId = datanodeId;
  }

  public void start() {
    if (running.compareAndSet(false, true)) {
      t = new Thread(this, "hb-thread");
      t.start();
    }
  }

  public void stop() {
    running.set(false);
    if (t != null) t.interrupt();
  }

  private long storageDirFreeBytes() {
    try {
      return new java.io.File(".").getFreeSpace();
    } catch (Exception e) {
      return -1;
    }
  }
}
