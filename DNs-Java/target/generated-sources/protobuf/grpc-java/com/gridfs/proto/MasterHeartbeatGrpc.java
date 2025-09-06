package com.gridfs.proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * Master &lt;-&gt; DataNode (bidi) para heartbeats/estado
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.64.0)",
    comments = "Source: master.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class MasterHeartbeatGrpc {

  private MasterHeartbeatGrpc() {}

  public static final java.lang.String SERVICE_NAME = "gridfs.MasterHeartbeat";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.gridfs.proto.HeartbeatKv,
      com.gridfs.proto.HeartbeatAck> getStreamStatusMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "StreamStatus",
      requestType = com.gridfs.proto.HeartbeatKv.class,
      responseType = com.gridfs.proto.HeartbeatAck.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<com.gridfs.proto.HeartbeatKv,
      com.gridfs.proto.HeartbeatAck> getStreamStatusMethod() {
    io.grpc.MethodDescriptor<com.gridfs.proto.HeartbeatKv, com.gridfs.proto.HeartbeatAck> getStreamStatusMethod;
    if ((getStreamStatusMethod = MasterHeartbeatGrpc.getStreamStatusMethod) == null) {
      synchronized (MasterHeartbeatGrpc.class) {
        if ((getStreamStatusMethod = MasterHeartbeatGrpc.getStreamStatusMethod) == null) {
          MasterHeartbeatGrpc.getStreamStatusMethod = getStreamStatusMethod =
              io.grpc.MethodDescriptor.<com.gridfs.proto.HeartbeatKv, com.gridfs.proto.HeartbeatAck>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "StreamStatus"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.gridfs.proto.HeartbeatKv.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.gridfs.proto.HeartbeatAck.getDefaultInstance()))
              .setSchemaDescriptor(new MasterHeartbeatMethodDescriptorSupplier("StreamStatus"))
              .build();
        }
      }
    }
    return getStreamStatusMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static MasterHeartbeatStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<MasterHeartbeatStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<MasterHeartbeatStub>() {
        @java.lang.Override
        public MasterHeartbeatStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new MasterHeartbeatStub(channel, callOptions);
        }
      };
    return MasterHeartbeatStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static MasterHeartbeatBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<MasterHeartbeatBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<MasterHeartbeatBlockingStub>() {
        @java.lang.Override
        public MasterHeartbeatBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new MasterHeartbeatBlockingStub(channel, callOptions);
        }
      };
    return MasterHeartbeatBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static MasterHeartbeatFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<MasterHeartbeatFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<MasterHeartbeatFutureStub>() {
        @java.lang.Override
        public MasterHeartbeatFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new MasterHeartbeatFutureStub(channel, callOptions);
        }
      };
    return MasterHeartbeatFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * Master &lt;-&gt; DataNode (bidi) para heartbeats/estado
   * </pre>
   */
  public interface AsyncService {

    /**
     */
    default io.grpc.stub.StreamObserver<com.gridfs.proto.HeartbeatKv> streamStatus(
        io.grpc.stub.StreamObserver<com.gridfs.proto.HeartbeatAck> responseObserver) {
      return io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall(getStreamStatusMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service MasterHeartbeat.
   * <pre>
   * Master &lt;-&gt; DataNode (bidi) para heartbeats/estado
   * </pre>
   */
  public static abstract class MasterHeartbeatImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return MasterHeartbeatGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service MasterHeartbeat.
   * <pre>
   * Master &lt;-&gt; DataNode (bidi) para heartbeats/estado
   * </pre>
   */
  public static final class MasterHeartbeatStub
      extends io.grpc.stub.AbstractAsyncStub<MasterHeartbeatStub> {
    private MasterHeartbeatStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MasterHeartbeatStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new MasterHeartbeatStub(channel, callOptions);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<com.gridfs.proto.HeartbeatKv> streamStatus(
        io.grpc.stub.StreamObserver<com.gridfs.proto.HeartbeatAck> responseObserver) {
      return io.grpc.stub.ClientCalls.asyncBidiStreamingCall(
          getChannel().newCall(getStreamStatusMethod(), getCallOptions()), responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service MasterHeartbeat.
   * <pre>
   * Master &lt;-&gt; DataNode (bidi) para heartbeats/estado
   * </pre>
   */
  public static final class MasterHeartbeatBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<MasterHeartbeatBlockingStub> {
    private MasterHeartbeatBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MasterHeartbeatBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new MasterHeartbeatBlockingStub(channel, callOptions);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service MasterHeartbeat.
   * <pre>
   * Master &lt;-&gt; DataNode (bidi) para heartbeats/estado
   * </pre>
   */
  public static final class MasterHeartbeatFutureStub
      extends io.grpc.stub.AbstractFutureStub<MasterHeartbeatFutureStub> {
    private MasterHeartbeatFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MasterHeartbeatFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new MasterHeartbeatFutureStub(channel, callOptions);
    }
  }

  private static final int METHODID_STREAM_STATUS = 0;

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
        case METHODID_STREAM_STATUS:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.streamStatus(
              (io.grpc.stub.StreamObserver<com.gridfs.proto.HeartbeatAck>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getStreamStatusMethod(),
          io.grpc.stub.ServerCalls.asyncBidiStreamingCall(
            new MethodHandlers<
              com.gridfs.proto.HeartbeatKv,
              com.gridfs.proto.HeartbeatAck>(
                service, METHODID_STREAM_STATUS)))
        .build();
  }

  private static abstract class MasterHeartbeatBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    MasterHeartbeatBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.gridfs.proto.Master.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("MasterHeartbeat");
    }
  }

  private static final class MasterHeartbeatFileDescriptorSupplier
      extends MasterHeartbeatBaseDescriptorSupplier {
    MasterHeartbeatFileDescriptorSupplier() {}
  }

  private static final class MasterHeartbeatMethodDescriptorSupplier
      extends MasterHeartbeatBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    MasterHeartbeatMethodDescriptorSupplier(java.lang.String methodName) {
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
      synchronized (MasterHeartbeatGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new MasterHeartbeatFileDescriptorSupplier())
              .addMethod(getStreamStatusMethod())
              .build();
        }
      }
    }
    return result;
  }
}
