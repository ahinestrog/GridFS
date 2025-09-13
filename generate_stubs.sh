#!/usr/bin/env bash
set -euo pipefail
# Genera stubs de Python en generated/py-stubs/
python -m grpc_tools.protoc -Iproto \
    --python_out=generated/py-stubs --grpc_python_out=generated/py-stubs \
    proto/common.proto proto/master.proto proto/datanode.proto proto/admin.proto
# Puedes agregar aquí comandos para C++ y Java si lo necesitas
