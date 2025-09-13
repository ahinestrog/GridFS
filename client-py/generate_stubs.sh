  #!/usr/bin/env bash
  set -euo pipefail

  ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../proto" && pwd)"
  OUT_DIR="$ROOT_DIR"

  if [ -z "${VIRTUAL_ENV:-}" ]; then
    echo "ERROR: activa el venv primero:"
    echo "  source .venv/bin/activate"
    exit 1
  fi

  python -m pip install --upgrade pip
  python -m pip install grpcio==1.66.0 grpcio-tools==1.66.0

  mkdir -p "$OUT_DIR"

  # Archivos .proto en la raíz del proyecto
  python -m grpc_tools.protoc \
    -I "$ROOT_DIR" \
    --python_out="$OUT_DIR" \
    --grpc_python_out="$OUT_DIR" \
    "$ROOT_DIR/common.proto" \
    "$ROOT_DIR/admin.proto" \
    "$ROOT_DIR/master.proto" \
    "$ROOT_DIR/datanode.proto" 

  echo "[OK] Stubs generados en $OUT_DIR:"
  ls -1 "$OUT_DIR" | grep -E '_pb2(_grpc)?.py'