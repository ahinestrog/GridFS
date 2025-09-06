package com.gridfs.datanode;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;

// ===== Ajusta estos imports a TU java_package =====
/*
import gridfs.MasterHeartbeatGrpc;
import gridfs.Master; // Master.Heartbeat, Master.HeartbeatAck (según tu proto)
*/
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

  @Override
  public void run() {
    /*
    ManagedChannel ch = ManagedChannelBuilder.forTarget(masterAddr).usePlaintext().build();
    MasterHeartbeatGrpc.MasterHeartbeatStub stub = MasterHeartbeatGrpc.newStub(ch);

    StreamObserver<Master.HeartbeatAck> down = new StreamObserver<>() {
      @Override public void onNext(Master.HeartbeatAck value) {}
      @Override public void onError(Throwable t) { t.printStackTrace(); }
      @Override public void onCompleted() {}
    };

    StreamObserver<Master.Heartbeat> up = stub.stream(down); // <-- ajusta el nombre del RPC

    while (running.get()) {
      try {
        long free = storageDirFreeBytes(); // o Files.getFileStore(root).getUsableSpace()
        long used = -1; // si quieres, calcula exacto recorriendo el storage
        String host = InetAddress.getLocalHost().getHostName();

        Master.Heartbeat hb = Master.Heartbeat.newBuilder()
            .setDatanodeId(datanodeId != null ? datanodeId : host)
            .setFreeBytes(free)
            .setUsedBytes(used)
            .build();

        up.onNext(hb);
        Thread.sleep(2000);
      } catch (Exception e) {
        e.printStackTrace();
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
      }
    }
    up.onCompleted();
    ch.shutdownNow();
    */
  }

  private long storageDirFreeBytes() {
    // Implementa según tu StorageManager si quieres un valor real
    try {
      return new java.io.File(".").getFreeSpace();
    } catch (Exception e) {
      return -1;
    }
  }
}
