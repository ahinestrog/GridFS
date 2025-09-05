#pragma once
#include <string>
#include <vector>
#include <sqlite3.h>
#include "common.pb.h"
struct FileBlockRow { std::string filename; int64_t idx{}; std::string block_id; std::string primary_dn; std::string replicas_csv; };
class MetaStore {
public:
explicit MetaStore(const std::string& db_path); ~MetaStore();
void SavePutPlan(const std::string& filename,const std::vector<gridfs::BlockAssignment>& asgs);
std::vector<FileBlockRow> GetFileLayout(const std::string& filename);
private:
void InitSchema(); sqlite3* db_{};
};