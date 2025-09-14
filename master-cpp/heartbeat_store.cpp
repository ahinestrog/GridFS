#include "heartbeat_store.h"
#include <stdexcept>
#include <vector>
#include <chrono>

static const char* kCreate =
    "CREATE TABLE IF NOT EXISTS heartbeats ("
    "node_id TEXT,"
    "key TEXT,"
    "value TEXT,"
    "ts_unix_ms INTEGER," 
    "PRIMARY KEY(node_id,key)"
    ");";

static const char* kUpsert =
    "INSERT INTO heartbeats(node_id,key,value,ts_unix_ms)"
    "VALUES(?1,?2,?3,?4)"
    "ON CONFLICT(node_id,key) DO UPDATE SET value=excluded.value, ts_unix_ms=excluded.ts_unix_ms;";

HeartbeatStore::HeartbeatStore(const std::string& db_path){ 
    if(sqlite3_open(db_path.c_str(),&db_)!=SQLITE_OK){ 
        throw std::runtime_error("No DB");
    }
    InitSchema();
}

HeartbeatStore::~HeartbeatStore(){
    if(db_) sqlite3_close(db_);
}

void HeartbeatStore::InitSchema(){
    char* err=nullptr;
    if(sqlite3_exec(db_,kCreate,nullptr,nullptr,&err)!=SQLITE_OK){
        std::string m=err?err:"err";
        sqlite3_free(err);
        throw std::runtime_error(m);
    }
}

void HeartbeatStore::Upsert(const std::string& node_id,
                            const std::string& key,
                            const std::string& value,
                            long long ts){
    sqlite3_stmt* s=nullptr;
    sqlite3_prepare_v2(db_,kUpsert,-1,&s,nullptr);
    sqlite3_bind_text(s,1,node_id.c_str(),-1,SQLITE_TRANSIENT);
    sqlite3_bind_text(s,2,key.c_str(),-1,SQLITE_TRANSIENT);
    sqlite3_bind_text(s,3,value.c_str(),-1,SQLITE_TRANSIENT);
    sqlite3_bind_int64(s,4,(sqlite3_int64)ts); 
    
    if(sqlite3_step(s)!=SQLITE_DONE){
        sqlite3_finalize(s);
        throw std::runtime_error("upsert fail");
    }
    sqlite3_finalize(s);
}

std::vector<std::string> HeartbeatStore::GetActiveNodes() {
    std::vector<std::string> nodes;
    const char* query = "SELECT DISTINCT node_id FROM heartbeats WHERE key='heartbeat' AND ts_unix_ms > ?";
    
    sqlite3_stmt* stmt = nullptr;
    if (sqlite3_prepare_v2(db_, query, -1, &stmt, nullptr) != SQLITE_OK) {
        return nodes;
    }
    
    // Solo nodos que han enviado heartbeat en los últimos 10 segundos
    long long cutoff = std::chrono::duration_cast<std::chrono::milliseconds>(
        std::chrono::system_clock::now().time_since_epoch()).count() - 10000;
    
    sqlite3_bind_int64(stmt, 1, cutoff);
    
    while (sqlite3_step(stmt) == SQLITE_ROW) {
        const char* node_id = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 0));
        if (node_id) {
            nodes.push_back(std::string(node_id));
        }
    }
    
    sqlite3_finalize(stmt);
    return nodes;
}

long long HeartbeatStore::GetFreeSpace(const std::string& node_id) {
    const char* query = "SELECT value FROM heartbeats WHERE node_id=? AND key='free_space_bytes' ORDER BY ts_unix_ms DESC LIMIT 1";
    
    sqlite3_stmt* stmt = nullptr;
    if (sqlite3_prepare_v2(db_, query, -1, &stmt, nullptr) != SQLITE_OK) {
        return 0;
    }
    
    sqlite3_bind_text(stmt, 1, node_id.c_str(), -1, SQLITE_TRANSIENT);
    
    long long free_space = 0;
    if (sqlite3_step(stmt) == SQLITE_ROW) {
        const char* value = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 0));
        if (value) {
            try {
                free_space = std::stoll(value);
            } catch (...) {
                free_space = 0;
            }
        }
    }
    
    sqlite3_finalize(stmt);
    return free_space;
}

bool HeartbeatStore::IsNodeActive(const std::string& node_id, long long timeout_ms) {
    const char* query = "SELECT ts_unix_ms FROM heartbeats WHERE node_id=? AND key='heartbeat' ORDER BY ts_unix_ms DESC LIMIT 1";
    
    sqlite3_stmt* stmt = nullptr;
    if (sqlite3_prepare_v2(db_, query, -1, &stmt, nullptr) != SQLITE_OK) {
        return false;
    }
    
    sqlite3_bind_text(stmt, 1, node_id.c_str(), -1, SQLITE_TRANSIENT);
    
    bool is_active = false;
    if (sqlite3_step(stmt) == SQLITE_ROW) {
        long long last_heartbeat = sqlite3_column_int64(stmt, 0);
        long long now = std::chrono::duration_cast<std::chrono::milliseconds>(
            std::chrono::system_clock::now().time_since_epoch()).count();
        
        is_active = (now - last_heartbeat) <= timeout_ms;
    }
    
    sqlite3_finalize(stmt);
    return is_active;
}