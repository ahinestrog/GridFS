#pragma once
#include <string>
#include <sqlite3.h>
class HeartbeatStore {
public:
explicit HeartbeatStore(const std::string& db_path);
~HeartbeatStore();
void Upsert(const std::string& node_id,const std::string& key,const std::string& value,long long ts_unix_ms);
private:
void InitSchema(); sqlite3* db_{};
};