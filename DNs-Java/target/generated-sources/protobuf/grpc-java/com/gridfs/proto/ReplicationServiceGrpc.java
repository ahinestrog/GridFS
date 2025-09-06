package com.gridfs.proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * DataNode &lt;-&gt; DataNode (replicación en cadena)
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.64.0)",
    comments = "Source: datanode.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class ReplicationServiceGrpc {

  private ReplicationServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "gridfs.ReplicationService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.gridfs.proto.BlockChunk,
      com.gridfs.proto.WriteAck> getPushBlockMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PushBlock",
      requestType = com.gridfs.proto.BlockChunk.class,
      responseType = com.gridfs.proto.WriteAck.class,
      methodType = io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
  public static io.grpc.MethodDescriptor<com.gridfs.proto.BlockChunk,
      com.gridfs.proto.WriteAck> getPushBlockMethod() {
    io.grpc.MethodDescriptor<com.gridfs.proto.BlockChunk, com.gridfs.proto.WriteAck> getPushBlockMethod;
    if ((getPushBlockMethod = ReplicationServiceGrpc.getPushBlockMethod) == null) {
      synchronized (ReplicationServiceGrpc.class) {
        if ((getPushBlockMethod = ReplicationServiceGrpc.getPushBlockMethod) == null) {
          ReplicationServiceGrpc.getPushBlockMethod = getPushBlockMethod =
              io.grpc.MethodDescriptor.<com.gridfs.proto.BlockChunk, com.gridfs.proto.WriteAck>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PushBlock"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.gridfs.proto.BlockChunk.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.gridfs.proto.WriteAck.getDefaultInstance()))
              .setSchemaDescriptor(new ReplicationServiceMethodDescriptorSupplier("PushBlock"))
              .build();
        }
      }
    }
    return getPushBlockMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static ReplicationServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ReplicationServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ReplicationServiceStub>() {
        @java.lang.Override
        public ReplicationServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ReplicationServiceStub(channel, callOptions);
        }
      };
    return ReplicationServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static ReplicationServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ReplicationServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ReplicationServiceBlockingStub>() {
        @java.lang.Override
        public ReplicationServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ReplicationServiceBlockingStub(channel, callOptions);
        }
      };
    return ReplicationServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static ReplicationServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ReplicationServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ReplicationServiceFutureStub>() {
        @java.lang.Override
        public ReplicationServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ReplicationServiceFutureStub(channel, callOptions);
        }
      };
    return ReplicationServiceFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * DataNode &lt;-&gt; DataNode (replicación en cadena)
   * </pre>
   */
  public interface AsyncService {

    /**
     */
    default io.grpc.stub.StreamObserver<com.gridfs.proto.BlockChunk> pushBlock(
        io.grpc.stub.StreamObserver<com.gridfs.proto.WriteAck> responseObserver) {
      return io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall(getPushBlockMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service ReplicationService.
   * <pre>
   * DataNode &lt;-&gt; DataNode (replicación en cadena)
   * </pre>
   */
  public static abstract class ReplicationServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return ReplicationServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service ReplicationService.
   * <pre>
   * DataNode &lt;-&gt; DataNode (replicación en cadena)
   * </pre>
   */
  public static final class ReplicationServiceStub
      extends io.grpc.stub.AbstractAsyncStub<ReplicationServiceStub> {
    private ReplicationServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ReplicationServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ReplicationServiceStub(channel, callOptions);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<com.gridfs.proto.BlockChunk> pushBlock(
        io.grpc.stub.StreamObserver<com.gridfs.proto.WriteAck> responseObserver) {
      return io.grpc.stub.ClientCalls.asyncClientStreamingCall(
          getChannel().newCall(getPushBlockMethod(), getCallOptions()), responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service ReplicationService.
   * <pre>
   * DataNode &lt;-&gt; DataNode (replicación en cadena)
   * </pre>
   */
  public static final class ReplicationServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<ReplicationServiceBlockingStub> {
    private ReplicationServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ReplicationServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ReplicationServiceBlockingStub(channel, callOptions);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service ReplicationService.
   * <pre>
   * DataNode &lt;-&gt; DataNode (replicación en cadena)
   * </pre>
   */
  public static final class ReplicationServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<ReplicationServiceFutureStub> {
    private ReplicationServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ReplicationServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ReplicationServiceFutureStub(channel, callOptions);
    }
  }

  private static final int METHODID_PUSH_BLOCK = 0;

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
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_PUSH_BLOCK:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.pushBlock(
              (io.grpc.stub.StreamObserver<com.gridfs.proto.WriteAck>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getPushBlockMethod(),
          io.grpc.stub.ServerCalls.asyncClientStreamingCall(
            new MethodHandlers<
              com.gridfs.proto.BlockChunk,
              com.gridfs.proto.WriteAck>(
                service, METHODID_PUSH_BLOCK)))
        .build();
  }

  private static abstract class ReplicationServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    ReplicationServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.gridfs.proto.Datanode.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("ReplicationService");
    }
  }

  private static final class ReplicationServiceFileDescriptorSupplier
      extends ReplicationServiceBaseDescriptorSupplier {
    ReplicationServiceFileDescriptorSupplier() {}
  }

  private static final class ReplicationServiceMethodDescriptorSupplier
      extends ReplicationServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    ReplicationServiceMethodDescriptorSupplier(java.lang.String methodName) {
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
      synchronized (ReplicationServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new ReplicationServiceFileDescriptorSupplier())
              .addMethod(getPushBlockMethod())
              .build();
        }
      }
    }
    return result;
  }
}
