#include "heartbeat_store.h"
#include <stdexcept>
static const char* kCreate =
"CREATE TABLE IF NOT EXISTS heartbeats (node_id TEXT, key TEXT, value TEXT, ts_unix_ms INTEGER, PRIMARY KEY(node_id,key));";
static const char* kUpsert =
"INSERT INTO heartbeats(node_id,key,value,ts_unix_ms) VALUES(?1,?2,?3,?4) ON CONFLICT(node_id,key) DO UPDATE SET value=excluded.value, ts_unix_ms=excluded.ts_unix_ms;";
HeartbeatStore::HeartbeatStore(const std::string& db_path){ if(sqlite3_open(db_path.c_str(),&db_)!=SQLITE_OK) throw std::runtime_error("No DB"); InitSchema(); }
HeartbeatStore::~HeartbeatStore(){ if(db_) sqlite3_close(db_); }
void HeartbeatStore::InitSchema(){ char* err=nullptr; if(sqlite3_exec(db_,kCreate,nullptr,nullptr,&err)!=SQLITE_OK){ std::string m=err?err:"err"; sqlite3_free(err); throw std::runtime_error(m);} }
void HeartbeatStore::Upsert(const std::string& node_id,const std::string& key,const std::string& value,long long ts){ sqlite3_stmt* s=nullptr; sqlite3_prepare_v2(db_,kUpsert,-1,&s,nullptr); sqlite3_bind_text(s,1,node_id.c_str(),-1,SQLITE_TRANSIENT); sqlite3_bind_text(s,2,key.c_str(),-1,SQLITE_TRANSIENT); sqlite3_bind_text(s,3,value.c_str(),-1,SQLITE_TRANSIENT); sqlite3_bind_int64(s,4,(sqlite3_int64)ts); if(sqlite3_step(s)!=SQLITE_DONE){ sqlite3_finalize(s); throw std::runtime_error("upsert fail"); } sqlite3_finalize(s);}