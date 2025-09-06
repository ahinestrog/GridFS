set -euo pipefail

# Verifica que estás en un venv (PEP 668-friendly)
if [ -z "${VIRTUAL_ENV:-}" ]; then
  echo "ERROR: activa el venv primero:"
  echo "  source .venv/bin/activate"
  exit 1
fi

python -m pip install --upgrade pip
python -m pip install grpcio==1.66.0 grpcio-tools==1.66.0


python -m grpc_tools.protoc \
  -I proto \
  --python_out=. \
  --grpc_python_out=. \
  proto/master.proto proto/datanode.proto

echo "[OK] Stubs generados: master_pb2(_grpc).py y datanode_pb2(_grpc).py"
