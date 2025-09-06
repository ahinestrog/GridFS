package com.gridfs.proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * Cliente &lt;-&gt; DataNode
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.64.0)",
    comments = "Source: datanode.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class DataNodeIOGrpc {

  private DataNodeIOGrpc() {}

  public static final java.lang.String SERVICE_NAME = "gridfs.DataNodeIO";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.gridfs.proto.BlockChunk,
      com.gridfs.proto.WriteAck> getWriteBlockMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "WriteBlock",
      requestType = com.gridfs.proto.BlockChunk.class,
      responseType = com.gridfs.proto.WriteAck.class,
      methodType = io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
  public static io.grpc.MethodDescriptor<com.gridfs.proto.BlockChunk,
      com.gridfs.proto.WriteAck> getWriteBlockMethod() {
    io.grpc.MethodDescriptor<com.gridfs.proto.BlockChunk, com.gridfs.proto.WriteAck> getWriteBlockMethod;
    if ((getWriteBlockMethod = DataNodeIOGrpc.getWriteBlockMethod) == null) {
      synchronized (DataNodeIOGrpc.class) {
        if ((getWriteBlockMethod = DataNodeIOGrpc.getWriteBlockMethod) == null) {
          DataNodeIOGrpc.getWriteBlockMethod = getWriteBlockMethod =
              io.grpc.MethodDescriptor.<com.gridfs.proto.BlockChunk, com.gridfs.proto.WriteAck>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "WriteBlock"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.gridfs.proto.BlockChunk.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.gridfs.proto.WriteAck.getDefaultInstance()))
              .setSchemaDescriptor(new DataNodeIOMethodDescriptorSupplier("WriteBlock"))
              .build();
        }
      }
    }
    return getWriteBlockMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.gridfs.proto.BlockId,
      com.gridfs.proto.BlockChunk> getReadBlockMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ReadBlock",
      requestType = com.gridfs.proto.BlockId.class,
      responseType = com.gridfs.proto.BlockChunk.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<com.gridfs.proto.BlockId,
      com.gridfs.proto.BlockChunk> getReadBlockMethod() {
    io.grpc.MethodDescriptor<com.gridfs.proto.BlockId, com.gridfs.proto.BlockChunk> getReadBlockMethod;
    if ((getReadBlockMethod = DataNodeIOGrpc.getReadBlockMethod) == null) {
      synchronized (DataNodeIOGrpc.class) {
        if ((getReadBlockMethod = DataNodeIOGrpc.getReadBlockMethod) == null) {
          DataNodeIOGrpc.getReadBlockMethod = getReadBlockMethod =
              io.grpc.MethodDescriptor.<com.gridfs.proto.BlockId, com.gridfs.proto.BlockChunk>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ReadBlock"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.gridfs.proto.BlockId.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.gridfs.proto.BlockChunk.getDefaultInstance()))
              .setSchemaDescriptor(new DataNodeIOMethodDescriptorSupplier("ReadBlock"))
              .build();
        }
      }
    }
    return getReadBlockMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.gridfs.proto.FsOpRequest,
      com.gridfs.proto.FsOpResponse> getFsOpMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "FsOp",
      requestType = com.gridfs.proto.FsOpRequest.class,
      responseType = com.gridfs.proto.FsOpResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.gridfs.proto.FsOpRequest,
      com.gridfs.proto.FsOpResponse> getFsOpMethod() {
    io.grpc.MethodDescriptor<com.gridfs.proto.FsOpRequest, com.gridfs.proto.FsOpResponse> getFsOpMethod;
    if ((getFsOpMethod = DataNodeIOGrpc.getFsOpMethod) == null) {
      synchronized (DataNodeIOGrpc.class) {
        if ((getFsOpMethod = DataNodeIOGrpc.getFsOpMethod) == null) {
          DataNodeIOGrpc.getFsOpMethod = getFsOpMethod =
              io.grpc.MethodDescriptor.<com.gridfs.proto.FsOpRequest, com.gridfs.proto.FsOpResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "FsOp"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.gridfs.proto.FsOpRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.gridfs.proto.FsOpResponse.getDefaultInstance()))
              .setSchemaDescriptor(new DataNodeIOMethodDescriptorSupplier("FsOp"))
              .build();
        }
      }
    }
    return getFsOpMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static DataNodeIOStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<DataNodeIOStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<DataNodeIOStub>() {
        @java.lang.Override
        public DataNodeIOStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new DataNodeIOStub(channel, callOptions);
        }
      };
    return DataNodeIOStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static DataNodeIOBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<DataNodeIOBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<DataNodeIOBlockingStub>() {
        @java.lang.Override
        public DataNodeIOBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new DataNodeIOBlockingStub(channel, callOptions);
        }
      };
    return DataNodeIOBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static DataNodeIOFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<DataNodeIOFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<DataNodeIOFutureStub>() {
        @java.lang.Override
        public DataNodeIOFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new DataNodeIOFutureStub(channel, callOptions);
        }
      };
    return DataNodeIOFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * Cliente &lt;-&gt; DataNode
   * </pre>
   */
  public interface AsyncService {

    /**
     * <pre>
     * Cliente sube un bloque (stream) y recibe un ACK final "sincrónico"
     * </pre>
     */
    default io.grpc.stub.StreamObserver<com.gridfs.proto.BlockChunk> writeBlock(
        io.grpc.stub.StreamObserver<com.gridfs.proto.WriteAck> responseObserver) {
      return io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall(getWriteBlockMethod(), responseObserver);
    }

    /**
     * <pre>
     * Cliente descarga un bloque como stream
     * </pre>
     */
    default void readBlock(com.gridfs.proto.BlockId request,
        io.grpc.stub.StreamObserver<com.gridfs.proto.BlockChunk> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getReadBlockMethod(), responseObserver);
    }

    /**
     * <pre>
     * Comandos básicos de FS locales del DN
     * </pre>
     */
    default void fsOp(com.gridfs.proto.FsOpRequest request,
        io.grpc.stub.StreamObserver<com.gridfs.proto.FsOpResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getFsOpMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service DataNodeIO.
   * <pre>
   * Cliente &lt;-&gt; DataNode
   * </pre>
   */
  public static abstract class DataNodeIOImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return DataNodeIOGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service DataNodeIO.
   * <pre>
   * Cliente &lt;-&gt; DataNode
   * </pre>
   */
  public static final class DataNodeIOStub
      extends io.grpc.stub.AbstractAsyncStub<DataNodeIOStub> {
    private DataNodeIOStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected DataNodeIOStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new DataNodeIOStub(channel, callOptions);
    }

    /**
     * <pre>
     * Cliente sube un bloque (stream) y recibe un ACK final "sincrónico"
     * </pre>
     */
    public io.grpc.stub.StreamObserver<com.gridfs.proto.BlockChunk> writeBlock(
        io.grpc.stub.StreamObserver<com.gridfs.proto.WriteAck> responseObserver) {
      return io.grpc.stub.ClientCalls.asyncClientStreamingCall(
          getChannel().newCall(getWriteBlockMethod(), getCallOptions()), responseObserver);
    }

    /**
     * <pre>
     * Cliente descarga un bloque como stream
     * </pre>
     */
    public void readBlock(com.gridfs.proto.BlockId request,
        io.grpc.stub.StreamObserver<com.gridfs.proto.BlockChunk> responseObserver) {
      io.grpc.stub.ClientCalls.asyncServerStreamingCall(
          getChannel().newCall(getReadBlockMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Comandos básicos de FS locales del DN
     * </pre>
     */
    public void fsOp(com.gridfs.proto.FsOpRequest request,
        io.grpc.stub.StreamObserver<com.gridfs.proto.FsOpResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getFsOpMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service DataNodeIO.
   * <pre>
   * Cliente &lt;-&gt; DataNode
   * </pre>
   */
  public static final class DataNodeIOBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<DataNodeIOBlockingStub> {
    private DataNodeIOBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected DataNodeIOBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new DataNodeIOBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Cliente descarga un bloque como stream
     * </pre>
     */
    public java.util.Iterator<com.gridfs.proto.BlockChunk> readBlock(
        com.gridfs.proto.BlockId request) {
      return io.grpc.stub.ClientCalls.blockingServerStreamingCall(
          getChannel(), getReadBlockMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Comandos básicos de FS locales del DN
     * </pre>
     */
    public com.gridfs.proto.FsOpResponse fsOp(com.gridfs.proto.FsOpRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getFsOpMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service DataNodeIO.
   * <pre>
   * Cliente &lt;-&gt; DataNode
   * </pre>
   */
  public static final class DataNodeIOFutureStub
      extends io.grpc.stub.AbstractFutureStub<DataNodeIOFutureStub> {
    private DataNodeIOFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected DataNodeIOFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new DataNodeIOFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Comandos básicos de FS locales del DN
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.gridfs.proto.FsOpResponse> fsOp(
        com.gridfs.proto.FsOpRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getFsOpMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_READ_BLOCK = 0;
  private static final int METHODID_FS_OP = 1;
  private static final int METHODID_WRITE_BLOCK = 2;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_READ_BLOCK:
          serviceImpl.readBlock((com.gridfs.proto.BlockId) request,
              (io.grpc.stub.StreamObserver<com.gridfs.proto.BlockChunk>) responseObserver);
          break;
        case METHODID_FS_OP:
          serviceImpl.fsOp((com.gridfs.proto.FsOpRequest) request,
              (io.grpc.stub.StreamObserver<com.gridfs.proto.FsOpResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_WRITE_BLOCK:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.writeBlock(
              (io.grpc.stub.StreamObserver<com.gridfs.proto.WriteAck>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getWriteBlockMethod(),
          io.grpc.stub.ServerCalls.asyncClientStreamingCall(
            new MethodHandlers<
              com.gridfs.proto.BlockChunk,
              com.gridfs.proto.WriteAck>(
                service, METHODID_WRITE_BLOCK)))
        .addMethod(
          getReadBlockMethod(),
          io.grpc.stub.ServerCalls.asyncServerStreamingCall(
            new MethodHandlers<
              com.gridfs.proto.BlockId,
              com.gridfs.proto.BlockChunk>(
                service, METHODID_READ_BLOCK)))
        .addMethod(
          getFsOpMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.gridfs.proto.FsOpRequest,
              com.gridfs.proto.FsOpResponse>(
                service, METHODID_FS_OP)))
        .build();
  }

  private static abstract class DataNodeIOBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    DataNodeIOBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.gridfs.proto.Datanode.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("DataNodeIO");
    }
  }

  private static final class DataNodeIOFileDescriptorSupplier
      extends DataNodeIOBaseDescriptorSupplier {
    DataNodeIOFileDescriptorSupplier() {}
  }

  private static final class DataNodeIOMethodDescriptorSupplier
      extends DataNodeIOBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    DataNodeIOMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (DataNodeIOGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new DataNodeIOFileDescriptorSupplier())
              .addMethod(getWriteBlockMethod())
              .addMethod(getReadBlockMethod())
              .addMethod(getFsOpMethod())
              .build();
        }
      }
    }
    return result;
  }
}
