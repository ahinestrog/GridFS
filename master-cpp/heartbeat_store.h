#pragma once
#include <string>
#include <vector>
#include <sqlite3.h>

class HeartbeatStore {
public:
    explicit HeartbeatStore(const std::string& db_path);
    ~HeartbeatStore();

    void Upsert(const std::string& node_id,
        const std::string& key,
        const std::string& value,
        long long ts_unix_ms);

    // Consultar información de espacio
    std::vector<std::string> GetActiveNodes();
    long long GetFreeSpace(const std::string& node_id);
    bool IsNodeActive(const std::string& node_id, long long timeout_ms = 10000);

private:
    void InitSchema(); 
    sqlite3* db_{};
};