import os, sys, grpc
from master_pb2 import PutPlanRequest, GetPlanRequest
from master_pb2_grpc import MasterServiceStub
from common_pb2 import BlockChunk, BlockId
from datanode_pb2_grpc import DataNodeIOStub


MASTER_ADDR = os.environ.get("MASTER_ADDR", "localhost:50051")
BLOCK_SIZE = 1024 * 1024


def put(path: str):
    size = os.path.getsize(path); name = os.path.basename(path)
    with grpc.insecure_channel(MASTER_ADDR) as ch:
        plan = MasterServiceStub(ch).PutPlan(PutPlanRequest(filename=name, filesize=size, block_size=BLOCK_SIZE, replication=2))
    with open(path,'rb') as f:
        for asg in plan.assignments:
            with grpc.insecure_channel(asg.primary_dn) as chdn:
                dn = DataNodeIOStub(chdn)
                def gen():
                    remaining = min(BLOCK_SIZE, size); sent = 0; seq = 0
                    while sent < remaining:
                        chunk = f.read(min(remaining - sent, 1024*1024))
                        if not chunk: break
                        sent += len(chunk)
                        yield BlockChunk(block_id=asg.block_id, seq=seq, data=chunk, eof=False)
                        seq += 1
                    yield BlockChunk(block_id=asg.block_id, seq=seq, eof=True)
                ack = dn.WriteBlock(gen())
                print(f"OK {asg.block_id} -> {asg.primary_dn} ok={ack.ok} bytes={ack.bytes_received} checksum={ack.checksum}")


def get(name: str):
    with grpc.insecure_channel(MASTER_ADDR) as ch:
        plan = MasterServiceStub(ch).GetPlan(GetPlanRequest(filename=name))
    with open(name,'wb') as out:
        for loc in plan.locations:
            with grpc.insecure_channel(loc.primary_dn) as chdn:
                dn = DataNodeIOStub(chdn)
                for chunk in dn.ReadBlock(BlockId(block_id=loc.block_id)):
                    if chunk.eof: break
                    out.write(chunk.data)
    print("Listo: ", name)


if __name__ == '__main__':
    if len(sys.argv)<3: print("Uso: cli.py put <archivo> | get <archivo>"); sys.exit(1)
    cmd,arg=sys.argv[1],sys.argv[2]
    if cmd=='put': put(arg)
    elif cmd=='get': get(arg)
    else: print('Comando no soportado')