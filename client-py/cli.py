import os, sys, argparse, grpc
# stubs (funciona si generaste en plano o en paquete com.gridfs.proto)
try:
    from master_pb2 import PutPlanRequest, GetPlanRequest
    from master_pb2_grpc import MasterServiceStub
    from common_pb2 import BlockChunk, BlockId
    from datanode_pb2_grpc import DataNodeIOStub
except ModuleNotFoundError:
    from com.gridfs.proto.master_pb2 import PutPlanRequest, GetPlanRequest
    from com.gridfs.proto.master_pb2_grpc import MasterServiceStub
    from com.gridfs.proto.common_pb2 import BlockChunk, BlockId
    from com.gridfs.proto.datanode_pb2_grpc import DataNodeIOStub

MASTER_ADDR = os.environ.get("MASTER_ADDR", "127.0.0.1:50051")
BLOCK_SIZE  = int(os.environ.get("CHUNK_SIZE", 1024*1024))  # tamaño por bloque en el DFS
STREAM_CHUNK = 256 * 1024  # tamaño de los fragmentos que se envían por streaming

def _parse_host_port(s: str, default_port: int = 50052):
    """Admite 'dns:///host:port', 'host:port', '[::1]:50052', 'localhost:50052'."""
    if not s: return ("127.0.0.1", default_port)
    t = s.strip()
    i = t.find("://")
    if i >= 0:
        t = t[i+3:]
        while t.startswith("/"): t = t[1:]
    if t.startswith("["):  # IPv6 [::1]:50052
        r = t.find("]")
        host = t[1:r] if r > 0 else t
        port = int(t[r+2:]) if r > 0 and r+1 < len(t) and t[r+1] == ":" else default_port
    else:
        # último ':' como separador de puerto
        j = t.rfind(":")
        if j > 0:
            host, ps = t[:j].strip(), t[j+1:].strip()
            try: port = int(ps)
            except: port = default_port
        else:
            host, port = t, default_port
    # normalizaciones
    if host in ("localhost", "::1", "0:0:0:0:0:0:0:1"): host = "127.0.0.1"
    # workaround: si viene puerto admin (50053/50055) usa IO (puerto-1)
    if port in (50053, 50055): port -= 1
    return host, port

def _target_from_assignment(asg):
    """Soporta planes que traen 'primary_dn' (string) o host/port separados."""
    if hasattr(asg, "primary_dn") and asg.primary_dn:
        return _parse_host_port(asg.primary_dn)
    # nombres alternativos frecuentes
    for h, p in (("host","port"), ("primary_host","primary_port"), ("io_host","io_port")):
        if hasattr(asg, h) and hasattr(asg, p):
            host = getattr(asg, h)
            port = getattr(asg, p)
            try: port = int(port)
            except: pass
            return _parse_host_port(f"{host}:{port}")
    # fallback
    return ("127.0.0.1", 50052)

def put(path: str, replication: int = 2):
    size = os.path.getsize(path)
    name = os.path.basename(path)
    remaining = size
    # pide plan al master
    with grpc.insecure_channel(MASTER_ADDR) as ch:
        plan = MasterServiceStub(ch).PutPlan(
            PutPlanRequest(filename=name, filesize=size, block_size=BLOCK_SIZE, replication=replication)
        )
    # envía bloques en streaming
    with open(path, "rb") as f:
        for i, asg in enumerate(plan.assignments):
            block_len = min(BLOCK_SIZE, remaining)
            host, port = _target_from_assignment(asg)
            print(f"[put] blk#{i} ({block_len} bytes) -> {host}:{port}")
            chan = grpc.insecure_channel(f"{host}:{port}")
            stub = DataNodeIOStub(chan)
            def gen():
                sent = 0
                seq = 0
                while sent < block_len:
                    chunk = f.read(min(STREAM_CHUNK, block_len - sent))
                    if not chunk: break
                    yield BlockChunk(block_id=asg.block_id, seq=seq, data=chunk, eof=False)
                    sent += len(chunk); seq += 1
                yield BlockChunk(block_id=asg.block_id, seq=seq, eof=True)
            ack = stub.WriteBlock(gen())
            print(f"OK {name}#blk{i} -> {host}:{port} ok={ack.ok} bytes={ack.bytes_received} checksum={ack.checksum}")
            remaining -= block_len
            if remaining <= 0:
                break

def get(remote_name: str, out_path: str | None):
    with grpc.insecure_channel(MASTER_ADDR) as ch:
        plan = MasterServiceStub(ch).GetPlan(GetPlanRequest(filename=remote_name))
    out_path = out_path or os.path.abspath(os.path.basename(remote_name))
    # recibe bloques en orden
    with open(out_path, "wb") as out:
        for i, loc in enumerate(plan.locations):
            host, port = _target_from_assignment(loc)
            print(f"[get] blk#{i} <- {host}:{port}")
            chan = grpc.insecure_channel(f"{host}:{port}")
            stub = DataNodeIOStub(chan)
            for chunk in stub.ReadBlock(BlockId(block_id=loc.block_id)):
                if chunk.eof: break
                out.write(chunk.data)
    print("Guardado en:", out_path)

def main():
    ap = argparse.ArgumentParser(prog="cli", description="GridFS CLI")
    sub = ap.add_subparsers(dest="cmd", required=True)

    ap_put = sub.add_parser("put", help="sube un archivo")
    ap_put.add_argument("path")
    ap_put.add_argument("--replication", "-r", type=int, default=2)

    ap_get = sub.add_parser("get", help="descarga un archivo")
    ap_get.add_argument("name")
    ap_get.add_argument("--out", "-o", default=None)

    args = ap.parse_args()

    if args.cmd == "put":
        put(args.path, replication=args.replication)
    elif args.cmd == "get":
        get(args.name, args.out)

if __name__ == "__main__":
    main()
