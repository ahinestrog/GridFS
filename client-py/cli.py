import os, sys, argparse, grpc
# Añadir la carpeta de stubs generados al PYTHONPATH
#sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '../generated/py-stubs')))
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '../proto')))

sys.path.append(os.path.abspath(".."))
from proto import master_pb2
from proto import master_pb2_grpc
from proto import datanode_pb2
from proto import datanode_pb2_grpc
from proto import common_pb2

from master_pb2 import (
    PutPlanRequest, PutPlanResponse, GetPlanRequest, GetPlanResponse,
    Auth, LsRequest, LsResponse, RmRequest, RmResponse, 
    MkdirRequest, MkdirResponse, RmdirRequest, RmdirResponse, 
    RegisterUserRequest, RegisterUserResponse
)
from master_pb2_grpc import MasterServiceStub
from common_pb2 import BlockChunk, BlockId
from datanode_pb2_grpc import DataNodeIOStub



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

def put(path: str, replication: int = 2, auth=None):
    size = os.path.getsize(path)
    name = os.path.basename(path)
    remaining = size
    # pide plan al master
    with grpc.insecure_channel(MASTER_ADDR) as ch:
        plan = MasterServiceStub(ch).PutPlan(
            PutPlanRequest(filename=name, filesize=size, block_size=BLOCK_SIZE, replication=replication, auth=auth)
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

def get(remote_name: str, out_path: str | None, auth=None):
    import os
    try:
        with grpc.insecure_channel(MASTER_ADDR) as ch:
            plan = MasterServiceStub(ch).GetPlan(GetPlanRequest(filename=remote_name, auth=auth))
        # Si out_path es un directorio o termina en barra, guarda el archivo ahí con el nombre original
        if out_path:
            # Normaliza ruta relativa
            out_path = os.path.expanduser(out_path)
            if out_path.endswith(os.sep) or os.path.isdir(out_path):
                out_path = os.path.join(out_path, os.path.basename(remote_name))
            else:
                # Si la carpeta padre no existe, créala
                parent = os.path.dirname(out_path)
                if parent and not os.path.exists(parent):
                    os.makedirs(parent, exist_ok=True)
        else:
            out_path = os.path.abspath(os.path.basename(remote_name))
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
    except grpc.RpcError as e:
        if e.code() == grpc.StatusCode.NOT_FOUND:
            print(f"No se pudo descargar '{remote_name}': No existe")
        else:
            print(f"Error al descargar '{remote_name}':", e)
    except Exception as e:
        print(f"Error al guardar el archivo: {e}")

def get_auth(args):
    # Evita conflicto con palabra reservada 'pass' usando setattr
    return Auth(user=args.user, **{"pass": getattr(args, "pass")})

def ls(path: str, auth):
    with grpc.insecure_channel(MASTER_ADDR) as ch:
        stub = MasterServiceStub(ch)
        resp = stub.Ls(LsRequest(auth=auth, path=path))
        print("\n".join(resp.entries))

def rm(path: str, auth):
    with grpc.insecure_channel(MASTER_ADDR) as ch:
        stub = MasterServiceStub(ch)
        resp = stub.Rm(RmRequest(auth=auth, path=path))
        if resp.ok:
            print(f"Eliminado: {path}")
        else:
            print(f"No se pudo eliminar '{path}': {resp.error or 'No existe'}")

def mkdir(path: str, auth):
    with grpc.insecure_channel(MASTER_ADDR) as ch:
        stub = MasterServiceStub(ch)
        resp = stub.Mkdir(MkdirRequest(auth=auth, path=path))
        if resp.ok:
            print("Directorio creado:", path)
        else:
            print("Error:", resp.error)

def rmdir(path: str, auth):
    with grpc.insecure_channel(MASTER_ADDR) as ch:
        stub = MasterServiceStub(ch)
        resp = stub.Rmdir(RmdirRequest(auth=auth, path=path))
        if resp.ok:
            print(f"Directorio eliminado: {path}")
        else:
            print(f"No se pudo eliminar el directorio '{path}': {resp.error or 'No existe'}")

def register(user, password):
    with grpc.insecure_channel(MASTER_ADDR) as ch:
        stub = MasterServiceStub(ch)
        # Suponiendo que agregas RegisterUserRequest/RegisterUserResponse en el proto
        resp = stub.RegisterUser(RegisterUserRequest(user=user, **{"pass": password}))
        if resp.ok:
            print("Usuario creado:", user)
        else:
            print("Error:", resp.error)

def main():
    ap = argparse.ArgumentParser(prog="cli", description="GridFS CLI")
    sub = ap.add_subparsers(dest="cmd", required=True)

    ap_register = sub.add_parser("register", help="registrar usuario")
    ap_register.add_argument("--user", required=True, help="Usuario")
    ap_register.add_argument("--pass", required=False, help="Contraseña")

    ap_put = sub.add_parser("put", help="sube un archivo")
    ap_put.add_argument("path")
    ap_put.add_argument("--replication", "-r", type=int, default=2)

    ap_get = sub.add_parser("get", help="descarga un archivo")
    ap_get.add_argument("name")
    ap_get.add_argument("--out", "-o", default=None)

    ap_ls = sub.add_parser("ls", help="lista archivos/directorios")
    ap_ls.add_argument("path", nargs="?", default=".")

    ap_rm = sub.add_parser("rm", help="elimina archivo")
    ap_rm.add_argument("path")

    ap_mkdir = sub.add_parser("mkdir", help="crea directorio")
    ap_mkdir.add_argument("path")

    ap_rmdir = sub.add_parser("rmdir", help="elimina directorio")
    ap_rmdir.add_argument("path")

    # Agrega login/logout
    sub.add_parser("login", help="iniciar sesión")
    sub.add_parser("logout", help="cerrar sesión")

    args = ap.parse_args()

    import getpass
    # Sesión global en archivo temporal
    import os, json
    SESSION_FILE = os.path.expanduser("~/.gridfs_session")

    def save_session(user, password):
        with open(SESSION_FILE, "w") as f:
            json.dump({"user": user, "pass": password}, f)

    def clear_session():
        if os.path.exists(SESSION_FILE):
            os.remove(SESSION_FILE)

    def get_auth_session():
        if os.path.exists(SESSION_FILE):
            with open(SESSION_FILE) as f:
                data = json.load(f)
                return Auth(user=data["user"], **{"pass": data["pass"]})
        print("No hay sesión activa. Usa 'login' primero.")
        sys.exit(1)

    if args.cmd == "register":
        user = args.user
        password = getattr(args, "pass") or getpass.getpass("Contraseña para registrar: ")
        register(user, password)
        return

    if args.cmd == "login":
        user = input("Usuario: ")
        password = getpass.getpass("Contraseña: ")
        # Opcional: validar credenciales con una operación segura, por ejemplo ls
        try:
            auth = Auth(user=user, **{"pass": password})
            with grpc.insecure_channel(MASTER_ADDR) as ch:
                stub = MasterServiceStub(ch)
                stub.Ls(LsRequest(auth=auth, path="."))
            save_session(user, password)
            print(f"Sesión iniciada como '{user}'")
        except Exception as e:
            print("Login fallido:", e)
            sys.exit(1)
        return

    if args.cmd == "logout":
        clear_session()
        print("Sesión cerrada.")
        return

    # Para las demás operaciones, usar sesión
    auth = get_auth_session()

    if args.cmd == "put":
        put(args.path, replication=args.replication, auth=auth)
    elif args.cmd == "get":
        get(args.name, args.out, auth=auth)
    elif args.cmd == "ls":
        ls(args.path, auth)
    elif args.cmd == "rm":
        rm(args.path, auth)
    elif args.cmd == "mkdir":
        mkdir(args.path, auth)
    elif args.cmd == "rmdir":
        rmdir(args.path, auth)

if __name__ == "__main__":
    main()
