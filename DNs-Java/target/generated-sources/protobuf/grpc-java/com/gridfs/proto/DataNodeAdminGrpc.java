package com.gridfs.proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * Servicio expuesto por el DataNode para recibir órdenes
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.64.0)",
    comments = "Source: admin.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class DataNodeAdminGrpc {

  private DataNodeAdminGrpc() {}

  public static final java.lang.String SERVICE_NAME = "gridfs.DataNodeAdmin";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.gridfs.proto.AdminOrderRequest,
      com.gridfs.proto.AdminOrderResponse> getAdminOrderMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "AdminOrder",
      requestType = com.gridfs.proto.AdminOrderRequest.class,
      responseType = com.gridfs.proto.AdminOrderResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.gridfs.proto.AdminOrderRequest,
      com.gridfs.proto.AdminOrderResponse> getAdminOrderMethod() {
    io.grpc.MethodDescriptor<com.gridfs.proto.AdminOrderRequest, com.gridfs.proto.AdminOrderResponse> getAdminOrderMethod;
    if ((getAdminOrderMethod = DataNodeAdminGrpc.getAdminOrderMethod) == null) {
      synchronized (DataNodeAdminGrpc.class) {
        if ((getAdminOrderMethod = DataNodeAdminGrpc.getAdminOrderMethod) == null) {
          DataNodeAdminGrpc.getAdminOrderMethod = getAdminOrderMethod =
              io.grpc.MethodDescriptor.<com.gridfs.proto.AdminOrderRequest, com.gridfs.proto.AdminOrderResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "AdminOrder"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.gridfs.proto.AdminOrderRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.gridfs.proto.AdminOrderResponse.getDefaultInstance()))
              .setSchemaDescriptor(new DataNodeAdminMethodDescriptorSupplier("AdminOrder"))
              .build();
        }
      }
    }
    return getAdminOrderMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static DataNodeAdminStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<DataNodeAdminStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<DataNodeAdminStub>() {
        @java.lang.Override
        public DataNodeAdminStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new DataNodeAdminStub(channel, callOptions);
        }
      };
    return DataNodeAdminStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static DataNodeAdminBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<DataNodeAdminBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<DataNodeAdminBlockingStub>() {
        @java.lang.Override
        public DataNodeAdminBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new DataNodeAdminBlockingStub(channel, callOptions);
        }
      };
    return DataNodeAdminBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static DataNodeAdminFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<DataNodeAdminFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<DataNodeAdminFutureStub>() {
        @java.lang.Override
        public DataNodeAdminFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new DataNodeAdminFutureStub(channel, callOptions);
        }
      };
    return DataNodeAdminFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * Servicio expuesto por el DataNode para recibir órdenes
   * </pre>
   */
  public interface AsyncService {

    /**
     */
    default void adminOrder(com.gridfs.proto.AdminOrderRequest request,
        io.grpc.stub.StreamObserver<com.gridfs.proto.AdminOrderResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getAdminOrderMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service DataNodeAdmin.
   * <pre>
   * Servicio expuesto por el DataNode para recibir órdenes
   * </pre>
   */
  public static abstract class DataNodeAdminImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return DataNodeAdminGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service DataNodeAdmin.
   * <pre>
   * Servicio expuesto por el DataNode para recibir órdenes
   * </pre>
   */
  public static final class DataNodeAdminStub
      extends io.grpc.stub.AbstractAsyncStub<DataNodeAdminStub> {
    private DataNodeAdminStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected DataNodeAdminStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new DataNodeAdminStub(channel, callOptions);
    }

    /**
     */
    public void adminOrder(com.gridfs.proto.AdminOrderRequest request,
        io.grpc.stub.StreamObserver<com.gridfs.proto.AdminOrderResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getAdminOrderMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service DataNodeAdmin.
   * <pre>
   * Servicio expuesto por el DataNode para recibir órdenes
   * </pre>
   */
  public static final class DataNodeAdminBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<DataNodeAdminBlockingStub> {
    private DataNodeAdminBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected DataNodeAdminBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new DataNodeAdminBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.gridfs.proto.AdminOrderResponse adminOrder(com.gridfs.proto.AdminOrderRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getAdminOrderMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service DataNodeAdmin.
   * <pre>
   * Servicio expuesto por el DataNode para recibir órdenes
   * </pre>
   */
  public static final class DataNodeAdminFutureStub
      extends io.grpc.stub.AbstractFutureStub<DataNodeAdminFutureStub> {
    private DataNodeAdminFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected DataNodeAdminFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new DataNodeAdminFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.gridfs.proto.AdminOrderResponse> adminOrder(
        com.gridfs.proto.AdminOrderRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getAdminOrderMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_ADMIN_ORDER = 0;

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
        case METHODID_ADMIN_ORDER:
          serviceImpl.adminOrder((com.gridfs.proto.AdminOrderRequest) request,
              (io.grpc.stub.StreamObserver<com.gridfs.proto.AdminOrderResponse>) responseObserver);
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
          getAdminOrderMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.gridfs.proto.AdminOrderRequest,
              com.gridfs.proto.AdminOrderResponse>(
                service, METHODID_ADMIN_ORDER)))
        .build();
  }

  private static abstract class DataNodeAdminBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    DataNodeAdminBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.gridfs.proto.Admin.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("DataNodeAdmin");
    }
  }

  private static final class DataNodeAdminFileDescriptorSupplier
      extends DataNodeAdminBaseDescriptorSupplier {
    DataNodeAdminFileDescriptorSupplier() {}
  }

  private static final class DataNodeAdminMethodDescriptorSupplier
      extends DataNodeAdminBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    DataNodeAdminMethodDescriptorSupplier(java.lang.String methodName) {
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
      synchronized (DataNodeAdminGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new DataNodeAdminFileDescriptorSupplier())
              .addMethod(getAdminOrderMethod())
              .build();
        }
      }
    }
    return result;
  }
}
