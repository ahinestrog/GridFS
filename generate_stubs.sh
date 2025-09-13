#!/usr/bin/env bash
set -euo pipefail

# Carpeta raíz de los .proto
PROTO_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/proto" && pwd)"
# Carpeta destino de los stubs
OUT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/generated/py-stubs" && pwd)"

if [ -z "${VIRTUAL_ENV:-}" ]; then
  echo "ERROR: activa el venv primero:"
  echo "  source .venv/bin/activate"
  exit 1
fi

python -m pip install --upgrade pip
python -m pip install grpcio==1.66.0 grpcio-tools==1.66.0

mkdir -p "$OUT_DIR"

# Generar los stubs en la carpeta destino
python -m grpc_tools.protoc \
  -I "$PROTO_DIR" \
  --python_out="$OUT_DIR" \
  --grpc_python_out="$OUT_DIR" \
  "$PROTO_DIR/common.proto" \
  "$PROTO_DIR/admin.proto" \
  "$PROTO_DIR/master.proto" \
  "$PROTO_DIR/datanode.proto"


echo "[OK] Stubs generados en $OUT_DIR:"
ls -1 "$OUT_DIR" | grep -E '_pb2(_grpc)?.py'
