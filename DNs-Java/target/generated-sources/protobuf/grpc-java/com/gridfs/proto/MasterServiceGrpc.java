package com.gridfs.proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.64.0)",
    comments = "Source: master.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class MasterServiceGrpc {

  private MasterServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "gridfs.MasterService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.gridfs.proto.PutPlanRequest,
      com.gridfs.proto.PutPlanResponse> getPutPlanMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PutPlan",
      requestType = com.gridfs.proto.PutPlanRequest.class,
      responseType = com.gridfs.proto.PutPlanResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.gridfs.proto.PutPlanRequest,
      com.gridfs.proto.PutPlanResponse> getPutPlanMethod() {
    io.grpc.MethodDescriptor<com.gridfs.proto.PutPlanRequest, com.gridfs.proto.PutPlanResponse> getPutPlanMethod;
    if ((getPutPlanMethod = MasterServiceGrpc.getPutPlanMethod) == null) {
      synchronized (MasterServiceGrpc.class) {
        if ((getPutPlanMethod = MasterServiceGrpc.getPutPlanMethod) == null) {
          MasterServiceGrpc.getPutPlanMethod = getPutPlanMethod =
              io.grpc.MethodDescriptor.<com.gridfs.proto.PutPlanRequest, com.gridfs.proto.PutPlanResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PutPlan"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.gridfs.proto.PutPlanRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.gridfs.proto.PutPlanResponse.getDefaultInstance()))
              .setSchemaDescriptor(new MasterServiceMethodDescriptorSupplier("PutPlan"))
              .build();
        }
      }
    }
    return getPutPlanMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.gridfs.proto.GetPlanRequest,
      com.gridfs.proto.GetPlanResponse> getGetPlanMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetPlan",
      requestType = com.gridfs.proto.GetPlanRequest.class,
      responseType = com.gridfs.proto.GetPlanResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.gridfs.proto.GetPlanRequest,
      com.gridfs.proto.GetPlanResponse> getGetPlanMethod() {
    io.grpc.MethodDescriptor<com.gridfs.proto.GetPlanRequest, com.gridfs.proto.GetPlanResponse> getGetPlanMethod;
    if ((getGetPlanMethod = MasterServiceGrpc.getGetPlanMethod) == null) {
      synchronized (MasterServiceGrpc.class) {
        if ((getGetPlanMethod = MasterServiceGrpc.getGetPlanMethod) == null) {
          MasterServiceGrpc.getGetPlanMethod = getGetPlanMethod =
              io.grpc.MethodDescriptor.<com.gridfs.proto.GetPlanRequest, com.gridfs.proto.GetPlanResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetPlan"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.gridfs.proto.GetPlanRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.gridfs.proto.GetPlanResponse.getDefaultInstance()))
              .setSchemaDescriptor(new MasterServiceMethodDescriptorSupplier("GetPlan"))
              .build();
        }
      }
    }
    return getGetPlanMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static MasterServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<MasterServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<MasterServiceStub>() {
        @java.lang.Override
        public MasterServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new MasterServiceStub(channel, callOptions);
        }
      };
    return MasterServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static MasterServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<MasterServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<MasterServiceBlockingStub>() {
        @java.lang.Override
        public MasterServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new MasterServiceBlockingStub(channel, callOptions);
        }
      };
    return MasterServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static MasterServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<MasterServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<MasterServiceFutureStub>() {
        @java.lang.Override
        public MasterServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new MasterServiceFutureStub(channel, callOptions);
        }
      };
    return MasterServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void putPlan(com.gridfs.proto.PutPlanRequest request,
        io.grpc.stub.StreamObserver<com.gridfs.proto.PutPlanResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getPutPlanMethod(), responseObserver);
    }

    /**
     */
    default void getPlan(com.gridfs.proto.GetPlanRequest request,
        io.grpc.stub.StreamObserver<com.gridfs.proto.GetPlanResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetPlanMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service MasterService.
   */
  public static abstract class MasterServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return MasterServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service MasterService.
   */
  public static final class MasterServiceStub
      extends io.grpc.stub.AbstractAsyncStub<MasterServiceStub> {
    private MasterServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MasterServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new MasterServiceStub(channel, callOptions);
    }

    /**
     */
    public void putPlan(com.gridfs.proto.PutPlanRequest request,
        io.grpc.stub.StreamObserver<com.gridfs.proto.PutPlanResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getPutPlanMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getPlan(com.gridfs.proto.GetPlanRequest request,
        io.grpc.stub.StreamObserver<com.gridfs.proto.GetPlanResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetPlanMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service MasterService.
   */
  public static final class MasterServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<MasterServiceBlockingStub> {
    private MasterServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MasterServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new MasterServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.gridfs.proto.PutPlanResponse putPlan(com.gridfs.proto.PutPlanRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getPutPlanMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.gridfs.proto.GetPlanResponse getPlan(com.gridfs.proto.GetPlanRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetPlanMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service MasterService.
   */
  public static final class MasterServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<MasterServiceFutureStub> {
    private MasterServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MasterServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new MasterServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.gridfs.proto.PutPlanResponse> putPlan(
        com.gridfs.proto.PutPlanRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getPutPlanMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.gridfs.proto.GetPlanResponse> getPlan(
        com.gridfs.proto.GetPlanRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetPlanMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_PUT_PLAN = 0;
  private static final int METHODID_GET_PLAN = 1;

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
        case METHODID_PUT_PLAN:
          serviceImpl.putPlan((com.gridfs.proto.PutPlanRequest) request,
              (io.grpc.stub.StreamObserver<com.gridfs.proto.PutPlanResponse>) responseObserver);
          break;
        case METHODID_GET_PLAN:
          serviceImpl.getPlan((com.gridfs.proto.GetPlanRequest) request,
              (io.grpc.stub.StreamObserver<com.gridfs.proto.GetPlanResponse>) responseObserver);
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
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getPutPlanMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.gridfs.proto.PutPlanRequest,
              com.gridfs.proto.PutPlanResponse>(
                service, METHODID_PUT_PLAN)))
        .addMethod(
          getGetPlanMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.gridfs.proto.GetPlanRequest,
              com.gridfs.proto.GetPlanResponse>(
                service, METHODID_GET_PLAN)))
        .build();
  }

  private static abstract class MasterServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    MasterServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.gridfs.proto.Master.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("MasterService");
    }
  }

  private static final class MasterServiceFileDescriptorSupplier
      extends MasterServiceBaseDescriptorSupplier {
    MasterServiceFileDescriptorSupplier() {}
  }

  private static final class MasterServiceMethodDescriptorSupplier
      extends MasterServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    MasterServiceMethodDescriptorSupplier(java.lang.String methodName) {
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
      synchronized (MasterServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new MasterServiceFileDescriptorSupplier())
              .addMethod(getPutPlanMethod())
              .addMethod(getGetPlanMethod())
              .build();
        }
      }
    }
    return result;
  }
}
