cmake_minimum_required(VERSION 3.16)
project(gridfs_master CXX)
set(CMAKE_CXX_STANDARD 17)
find_package(gRPC CONFIG REQUIRED)
find_package(Protobuf CONFIG REQUIRED)
find_package(SQLite3 REQUIRED)


set(PROTO_DIR ${CMAKE_SOURCE_DIR}/../proto)
set(GEN_DIR ${CMAKE_BINARY_DIR}/generated)
file(MAKE_DIRECTORY ${GEN_DIR})


# Genera stubs para TODOS los protos
set(ALL_PROTOS ${PROTO_DIR}/common.proto ${PROTO_DIR}/master.proto ${PROTO_DIR}/datanode.proto ${PROTO_DIR}/admin.proto)
protobuf_generate(TARGET gridfs_proto LANGUAGE cpp OUT_VAR PROTO_SRCS PROTO_HDRS PROTOS ${ALL_PROTOS} PROTOC_OUT_DIR ${GEN_DIR})
grpc_generate(TARGET gridfs_grpc LANGUAGE cpp PROTOS ${ALL_PROTOS} GENERATE_GRPC_FILES PROTOC_OUT_DIR ${GEN_DIR})


include_directories(${GEN_DIR})
add_library(proto_objs ${PROTO_SRCS} ${PROTO_HDRS})
add_library(grpc_objs ${gridfs_grpc_SRCS} ${gridfs_grpc_HDRS})


add_executable(master
main.cc heartbeat_store.cc metastore.cc master_service.cc heartbeat_service.cc)


target_link_libraries(master gRPC::grpc++ proto_objs grpc_objs SQLite::SQLite3)