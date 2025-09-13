#pragma once
#include <string>
#include <vector>
#include <sqlite3.h>
#include "common.pb.h"
struct FileBlockRow { std::string filename; int64_t idx{}; std::string block_id; std::string primary_dn; std::string replicas_csv; };
class MetaStore {
public:
	explicit MetaStore(const std::string& db_path); ~MetaStore();
	void SavePutPlan(const std::string& filename, const std::vector<com::gridfs::proto::BlockAssignment>& asgs, const std::string& owner);
	std::vector<FileBlockRow> GetFileLayout(const std::string& filename, const std::string& owner);
	std::vector<std::string> ListFiles(const std::string& owner);
	std::vector<std::string> ListDirs(const std::string& owner);
	bool CreateDir(const std::string& dirname, const std::string& owner);
	bool RemoveDir(const std::string& dirname, const std::string& owner);
	bool RemoveFile(const std::string& filename, const std::string& owner);
	bool RegisterUser(const std::string& user, const std::string& pass);
	bool ValidateUser(const std::string& user, const std::string& pass);
private:
	void InitSchema();
	sqlite3* db_{};
};