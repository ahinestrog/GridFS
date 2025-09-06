cmake_minimum_required(VERSION 3.16)
project(master_cpp LANGUAGES CXX)

# ---- C++ standard & warnings -------------------------------------------------
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
add_compile_options(-Wall -Wextra -Wpedantic)

# ---- Hints Ubuntu/WSL (opcionales) -----------------------------------------
list(APPEND CMAKE_PREFIX_PATH "/usr/lib/x86_64-linux-gnu/cmake/protobuf")
list(APPEND CMAKE_PREFIX_PATH "/usr/lib/x86_64-linux-gnu/cmake/grpc")

# ---- Dependencias: Protobuf + gRPC ------------------------------------------
find_package(Protobuf REQUIRED)          # puede resolver en modo "módulo"
find_package(gRPC CONFIG REQUIRED)       # gRPC::grpc++

message(STATUS "Protobuf libs: ${Protobuf_LIBRARIES}")
message(STATUS "gRPC OK: gRPC::grpc++")

# Compat: target moderno vs variables del módulo
if (TARGET protobuf::libprotobuf)
  set(PROTOBUF_TARGET protobuf::libprotobuf)
elseif (TARGET Protobuf::libprotobuf)
  set(PROTOBUF_TARGET Protobuf::libprotobuf)
else()
  set(PROTOBUF_TARGET ${Protobuf_LIBRARIES})
endif()

# Herramientas necesarias
find_program(GRPC_CPP_PLUGIN_EXECUTABLE grpc_cpp_plugin REQUIRED)
if (NOT Protobuf_PROTOC_EXECUTABLE)
  find_program(Protobuf_PROTOC_EXECUTABLE protoc REQUIRED)
endif()

# ---- SQLite3 -----------------------------------------------------------------
find_package(SQLite3 REQUIRED)
message(STATUS "SQLite3 include dir: ${SQLite3_INCLUDE_DIRS}")
message(STATUS "SQLite3 libs: ${SQLite3_LIBRARIES}")

if (TARGET SQLite::SQLite3)
  set(SQLITE3_TARGET SQLite::SQLite3)
else()
  set(SQLITE3_TARGET ${SQLite3_LIBRARIES})
  include_directories(${SQLite3_INCLUDE_DIRS})
endif()

# ---- Proto files --------------------------------------------------------------
# Todos los .proto ubicados en ../proto (hermano de master-cpp)
set(PROTO_ROOT "${CMAKE_SOURCE_DIR}/../proto")
if (NOT EXISTS "${PROTO_ROOT}")
  message(FATAL_ERROR "No existe ${PROTO_ROOT}. Ajusta PROTO_ROOT en CMakeLists.txt.")
endif()

file(GLOB PROTO_FILES "${PROTO_ROOT}/*.proto")
if (PROTO_FILES STREQUAL "")
  message(FATAL_ERROR "No se encontraron .proto en ${PROTO_ROOT}")
endif()
message(STATUS "Proto files: ${PROTO_FILES}")

# Directorios de includes (headers generados quedan en el binario actual)
include_directories(${CMAKE_CURRENT_BINARY_DIR})
include_directories(${Protobuf_INCLUDE_DIRS})
include_directories(${PROTO_ROOT})  # por si hay imports entre .proto

# Mensajes protobuf (genera *.pb.cc/*.pb.h para TODOS los .proto)
include(${CMAKE_ROOT}/Modules/FindProtobuf.cmake)
protobuf_generate_cpp(PROTO_SRCS PROTO_HDRS ${PROTO_FILES})

# Servicios gRPC (genera *.grpc.pb.cc/*.grpc.pb.h para TODOS los .proto)
set(GRPC_SRCS)
set(GRPC_HDRS)
foreach(_pf ${PROTO_FILES})
  get_filename_component(_base "${_pf}" NAME_WE)
  set(_grpc_src "${CMAKE_CURRENT_BINARY_DIR}/${_base}.grpc.pb.cc")
  set(_grpc_hdr "${CMAKE_CURRENT_BINARY_DIR}/${_base}.grpc.pb.h")

  add_custom_command(
    OUTPUT ${_grpc_src} ${_grpc_hdr}
    COMMAND ${Protobuf_PROTOC_EXECUTABLE}
    ARGS --grpc_out ${CMAKE_CURRENT_BINARY_DIR}
         --plugin=protoc-gen-grpc=${GRPC_CPP_PLUGIN_EXECUTABLE}
         -I ${PROTO_ROOT} ${_pf}
    DEPENDS ${_pf}
    COMMENT "gRPC C++ desde ${_pf}"
    VERBATIM
  )

  list(APPEND GRPC_SRCS ${_grpc_src})
  list(APPEND GRPC_HDRS ${_grpc_hdr})
endforeach()

# Librería con todo lo generado
add_library(dfs_proto_lib
  ${PROTO_SRCS} ${PROTO_HDRS}
  ${GRPC_SRCS} ${GRPC_HDRS}
)
target_include_directories(dfs_proto_lib PUBLIC ${CMAKE_CURRENT_BINARY_DIR})
target_link_libraries(dfs_proto_lib PUBLIC gRPC::grpc++ ${PROTOBUF_TARGET})

# ---- Ejecutable master --------------------------------------------------------
# Compila TODOS los .cpp en master-cpp/ (no recursivo)
file(GLOB MASTER_SOURCES CONFIGURE_DEPENDS
  "${CMAKE_SOURCE_DIR}/*.cpp"
)

if (MASTER_SOURCES STREQUAL "")
  message(FATAL_ERROR "No hay .cpp en ${CMAKE_SOURCE_DIR}")
endif()

add_executable(master ${MASTER_SOURCES})
target_link_libraries(master
  PRIVATE
    dfs_proto_lib
    gRPC::grpc++
    ${PROTOBUF_TARGET}
    ${SQLITE3_TARGET}
)
