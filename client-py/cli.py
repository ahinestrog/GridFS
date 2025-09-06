#!/usr/bin/env python3
import argparse, os, sys, zlib, grpc
from pathlib import Path

import master_pb2 as m_pb2
import master_pb2_grpc as m_grpc
import datanode_pb2 as dn_pb2
import datanode_pb2_grpc as dn_grpc

DEFAULT_MASTER = os.environ.get("MASTER_ADDR", "127.0.0.1:50051")

def human(n):
    for unit in ['B','KB','MB','GB','TB']:
        if n < 1024.0: return f"{n:.1f}{unit}"
        n /= 1024.0
    return f"{n:.1f}PB"

def master_channel(addr: str):
    # normaliza: si te dan "host:port", úsalo directo
    target = addr if "://" in addr else addr
    return grpc.insecure_channel(target)

def datanode_channel(host: str, port: int):
    return grpc.insecure_channel(f"{host}:{port}")

# ---------- ls ----------
def cmd_ls(args):
    with master_channel(args.master) as ch:
        stub = m_grpc.MasterStub(ch)
        resp = stub.Ls(m_pb2.LsRequest(path=args.path))
    if not resp.entries:
        print("(vacío)")
        return
    for e in resp.entries:
        t = "<DIR>" if e.is_dir else "     "
        size = "-" if e.is_dir else human(e.size)
        print(f"{t} {e.name:30} {size:>8}  blocks={e.blocks} repl={e.replication}")

# ---------- mkdir ----------
def cmd_mkdir(args):
    with master_channel(args.master) as ch:
        stub = m_grpc.MasterStub(ch)
        resp = stub.Mkdir(m_pb2.MkdirRequest(path=args.path))
    print("OK" if resp.ok else f"ERROR: {resp.msg}")

# ---------- rm ----------
def cmd_rm(args):
    with master_channel(args.master) as ch:
        stub = m_grpc.MasterStub(ch)
        resp = stub.Rm(m_pb2.RmRequest(path=args.path))
    print("OK" if resp.ok else f"ERROR: {resp.msg}")

# ---------- rmdir ----------
def cmd_rmdir(args):
    with master_channel(args.master) as ch:
        stub = m_grpc.MasterStub(ch)
        resp = stub.Rmdir(m_pb2.RmdirRequest(path=args.path, recursive=args.recursive))
    print("OK" if resp.ok else f"ERROR: {resp.msg}")

# ---------- put ----------
def iter_chunks(file_path: Path, block_id: int, chunk_size: int):
    with file_path.open("rb") as f:
        while True:
            buf = f.read(chunk_size)
            if not buf:
                break
            crc = zlib.crc32(buf) & 0xffffffff
            yield dn_pb2.Chunk(block_id=block_id, data=buf, crc32=crc)

def upload_block(block_plan: m_pb2.BlockPlan, local_path: Path, offset: int, length: int, chunk_size: int):
    # El cliente envía al PRIMER DN de la tubería; el DN se encarga de replicar al resto.
    head = block_plan.pipeline[0]
    with datanode_channel(head.host, head.port) as ch:
        stub = dn_grpc.DataNodeIOStub(ch)
        def gen():
            with local_path.open("rb") as f:
                f.seek(offset)
                remaining = length
                while remaining > 0:
                    to_read = min(chunk_size, remaining)
                    buf = f.read(to_read)
                    if not buf:
                        break
                    crc = zlib.crc32(buf) & 0xffffffff
                    yield dn_pb2.Chunk(block_id=block_plan.block_id, data=buf, crc32=crc)
                    remaining -= len(buf)
        ack = stub.Upload(gen())
        if not ack.ok:
            raise RuntimeError(f"Upload block {block_plan.block_id} failed: {ack.msg}")

def cmd_put(args):
    local = Path(args.src)
    if not local.is_file():
        print(f"ERROR: no existe archivo local: {local}", file=sys.stderr)
        sys.exit(2)

    # 1) Plan con Master
    with master_channel(args.master) as ch:
        mst = m_grpc.MasterStub(ch)
        st = local.stat()
        plan = mst.PlanWrite(m_pb2.PlanWriteRequest(
            path=args.dst,
            size=st.st_size,
            replication=args.replication))

    # 2) Subir bloque a bloque
    chunk_size = plan.chunk_size or 65536
    print(f"[put] size={st.st_size} bytes, chunk_size={chunk_size}, blocks={len(plan.blocks)}")
    offset = 0
    remaining = st.st_size
    # Si el Master planifica longitudes por bloque, podrías usarlas; aquí dividimos equitativo salvo el último
    # Suponemos bloques en plan.blocks ya representan “slots”; Master decide el tamaño de cada uno
    # Para simplicidad: asumimos tamaños iguales excepto el último
    # (Si tu Master ya manda length por bloque, usa eso y borra este cálculo.)
    block_size = chunk_size * 1024  # por ejemplo, 64MB si chunk=64KB (ajusta a tu Master si aplica)
    for i, bp in enumerate(plan.blocks):
        if i < len(plan.blocks) - 1:
            length = min(block_size, remaining)
        else:
            length = remaining
        print(f"  - block_id={bp.block_id} -> {bp.pipeline[0].host}:{bp.pipeline[0].port}  len={length}")
        upload_block(bp, local, offset, length, chunk_size)
        offset += length
        remaining -= length

    print("OK")

# ---------- get ----------
def cmd_get(args):
    # 1) Plan de lectura
    with master_channel(args.master) as ch:
        mst = m_grpc.MasterStub(ch)
        plan = mst.PlanRead(m_pb2.PlanReadRequest(path=args.src))
    chunk_size = plan.chunk_size or 65536

    out = Path(args.dst)
    out.parent.mkdir(parents=True, exist_ok=True)
    with out.open("wb") as f:
        for bl in plan.blocks:
            # Elegimos la primera réplica
            dn = bl.replicas[0]
            print(f"[get] block_id={bl.block_id} from {dn.host}:{dn.port} len={bl.length}")
            with datanode_channel(dn.host, dn.port) as chdn:
                stub = dn_grpc.DataNodeIOStub(chdn)
                stream = stub.Download(dn_pb2.DownloadRequest(block_id=bl.block_id))
                received = 0
                for chunk in stream:
                    # (Opcional) validar CRC
                    if chunk.crc32:
                        crc = zlib.crc32(chunk.data) & 0xffffffff
                        if crc != chunk.crc32:
                            raise RuntimeError(f"CRC mismatch on block {bl.block_id}")
                    f.write(chunk.data)
                    received += len(chunk.data)
                if bl.length and received != bl.length:
                    # Permite que el último bloque sea “corto”
                    pass
    print("OK")

# ---------- main ----------
def main():
    p = argparse.ArgumentParser(prog="gridfs-cli", description="CLI GridFS (put/get/ls/mkdir/rm/rmdir)")
    p.add_argument("--master", default=DEFAULT_MASTER, help="ADDR del Master (host:port), default: 127.0.0.1:50051")

    sub = p.add_subparsers(dest="cmd", required=True)

    sp = sub.add_parser("ls");      sp.add_argument("path"); sp.set_defaults(func=cmd_ls)
    sp = sub.add_parser("mkdir");   sp.add_argument("path"); sp.set_defaults(func=cmd_mkdir)
    sp = sub.add_parser("rm");      sp.add_argument("path"); sp.set_defaults(func=cmd_rm)
    sp = sub.add_parser("rmdir");   sp.add_argument("path"); sp.add_argument("-r","--recursive",action="store_true"); sp.set_defaults(func=cmd_rmdir)

    sp = sub.add_parser("put")
    sp.add_argument("src"); sp.add_argument("dst")
    sp.add_argument("-R","--replication", type=int, default=3)
    sp.set_defaults(func=cmd_put)

    sp = sub.add_parser("get")
    sp.add_argument("src"); sp.add_argument("dst")
    sp.set_defaults(func=cmd_get)

    args = p.parse_args()
    args.func(args)

if __name__ == "__main__":
    main()
