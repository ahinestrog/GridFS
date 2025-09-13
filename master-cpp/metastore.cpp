#include "metastore.h"
#include <sstream>
#include <stdexcept>
#include <set>
#include <algorithm>
static const char* kCreate = "CREATE TABLE IF NOT EXISTS file_blocks (filename TEXT, idx INTEGER, block_id TEXT, primary_dn TEXT, replicas_csv TEXT, owner TEXT, PRIMARY KEY(filename,idx,owner));";
static const char* kInsert = "INSERT OR REPLACE INTO file_blocks(filename,idx,block_id,primary_dn,replicas_csv,owner) VALUES(?1,?2,?3,?4,?5,?6);";
static const char* kQuery = "SELECT filename,idx,block_id,primary_dn,replicas_csv FROM file_blocks WHERE filename=?1 AND owner=?2 ORDER BY idx;";
static const char* kCreateDir = "CREATE TABLE IF NOT EXISTS directories (dirname TEXT, owner TEXT, PRIMARY KEY(dirname,owner));";
static const char* kCreateUser = "CREATE TABLE IF NOT EXISTS users (user TEXT PRIMARY KEY, pass TEXT);";
MetaStore::MetaStore(const std::string& db){ if(sqlite3_open(db.c_str(),&db_)!=SQLITE_OK) throw std::runtime_error("No metadb"); InitSchema(); }
MetaStore::~MetaStore(){ if(db_) sqlite3_close(db_);}
void MetaStore::InitSchema(){ char* err=nullptr; if(sqlite3_exec(db_,kCreate,nullptr,nullptr,&err)!=SQLITE_OK){ std::string m=err?err:"err"; sqlite3_free(err); throw std::runtime_error(m);} if(sqlite3_exec(db_,kCreateDir,nullptr,nullptr,&err)!=SQLITE_OK){ std::string m=err?err:"err"; sqlite3_free(err); throw std::runtime_error(m);} if(sqlite3_exec(db_, kCreateUser, nullptr, nullptr, &err) != SQLITE_OK){ std::string m=err?err:"err"; sqlite3_free(err); throw std::runtime_error(m);} }
void MetaStore::SavePutPlan(const std::string& fn, const std::vector<com::gridfs::proto::BlockAssignment>& asgs, const std::string& owner) {
    sqlite3_exec(db_, "BEGIN;", nullptr, nullptr, nullptr);
    sqlite3_stmt* s = nullptr;
    sqlite3_prepare_v2(db_, kInsert, -1, &s, nullptr);
    for (size_t i = 0; i < asgs.size(); ++i) {
        const auto& a = asgs[i];
        std::ostringstream csv;
        for (int j = 0; j < a.replica_dns_size(); ++j) {
            if (j) csv << ",";
            csv << a.replica_dns(j);
        }
        sqlite3_bind_text(s, 1, fn.c_str(), -1, SQLITE_TRANSIENT);
        sqlite3_bind_int64(s, 2, (sqlite3_int64)i);
        sqlite3_bind_text(s, 3, a.block_id().c_str(), -1, SQLITE_TRANSIENT);
        sqlite3_bind_text(s, 4, a.primary_dn().c_str(), -1, SQLITE_TRANSIENT);
        auto scsv = csv.str();
        sqlite3_bind_text(s, 5, scsv.c_str(), -1, SQLITE_TRANSIENT);
        sqlite3_bind_text(s, 6, owner.c_str(), -1, SQLITE_TRANSIENT);
        if (sqlite3_step(s) != SQLITE_DONE) {
            sqlite3_finalize(s);
            sqlite3_exec(db_, "ROLLBACK;", nullptr, nullptr, nullptr);
            throw std::runtime_error("insert fail");
        }
        sqlite3_reset(s);
    }
    sqlite3_finalize(s);
    sqlite3_exec(db_, "COMMIT;", nullptr, nullptr, nullptr);
}
std::vector<FileBlockRow> MetaStore::GetFileLayout(const std::string& fn, const std::string& owner) {
    sqlite3_stmt* s = nullptr;
    sqlite3_prepare_v2(db_, kQuery, -1, &s, nullptr);
    sqlite3_bind_text(s, 1, fn.c_str(), -1, SQLITE_TRANSIENT);
    sqlite3_bind_text(s, 2, owner.c_str(), -1, SQLITE_TRANSIENT);
    std::vector<FileBlockRow> v;
    int rc;
    while ((rc = sqlite3_step(s)) == SQLITE_ROW) {
        FileBlockRow r;
        r.filename = (const char*)sqlite3_column_text(s, 0);
        r.idx = sqlite3_column_int64(s, 1);
        r.block_id = (const char*)sqlite3_column_text(s, 2);
        r.primary_dn = (const char*)sqlite3_column_text(s, 3);
        r.replicas_csv = (const char*)sqlite3_column_text(s, 4);
        v.push_back(std::move(r));
    }
    sqlite3_finalize(s);
    return v;
}
std::vector<std::string> MetaStore::ListFiles(const std::string& owner) {
    std::vector<std::string> files;
    std::set<std::string> unique_files;
    const char* sql = "SELECT DISTINCT filename FROM file_blocks WHERE owner=?1;";
    sqlite3_stmt* stmt = nullptr;
    int rc = sqlite3_prepare_v2(db_, sql, -1, &stmt, nullptr);
    if (rc == SQLITE_OK) {
        sqlite3_bind_text(stmt, 1, owner.c_str(), -1, SQLITE_TRANSIENT);
        while (sqlite3_step(stmt) == SQLITE_ROW) {
            const char* fname = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 0));
            if (fname && unique_files.insert(fname).second) {
                files.push_back(fname);
            }
        }
        sqlite3_finalize(stmt);
    }
    return files;
}
std::vector<std::string> MetaStore::ListDirs(const std::string& owner) {
    std::vector<std::string> dirs;
    const char* sql = "SELECT dirname FROM directories WHERE owner=?1;";
    sqlite3_stmt* stmt = nullptr;
    int rc = sqlite3_prepare_v2(db_, sql, -1, &stmt, nullptr);
    if (rc == SQLITE_OK) {
        sqlite3_bind_text(stmt, 1, owner.c_str(), -1, SQLITE_TRANSIENT);
        while (sqlite3_step(stmt) == SQLITE_ROW) {
            const char* dname = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 0));
            if (dname) dirs.push_back(dname);
        }
        sqlite3_finalize(stmt);
    }
    return dirs;
}

bool MetaStore::CreateDir(const std::string& dirname, const std::string& owner) {
    const char* sql = "INSERT INTO directories(dirname, owner) VALUES(?1, ?2);";
    sqlite3_stmt* stmt = nullptr;
    int rc = sqlite3_prepare_v2(db_, sql, -1, &stmt, nullptr);
    if (rc != SQLITE_OK) return false;
    sqlite3_bind_text(stmt, 1, dirname.c_str(), -1, SQLITE_TRANSIENT);
    sqlite3_bind_text(stmt, 2, owner.c_str(), -1, SQLITE_TRANSIENT);
    rc = sqlite3_step(stmt);
    sqlite3_finalize(stmt);
    return rc == SQLITE_DONE;
}

bool MetaStore::RemoveDir(const std::string& dirname, const std::string& owner) {
    const char* sql = "DELETE FROM directories WHERE dirname=?1 AND owner=?2;";
    sqlite3_stmt* stmt = nullptr;
        int rc = sqlite3_prepare_v2(db_, sql, -1, &stmt, nullptr);
        if (rc != SQLITE_OK) return false;
        sqlite3_bind_text(stmt, 1, dirname.c_str(), -1, SQLITE_TRANSIENT);
        sqlite3_bind_text(stmt, 2, owner.c_str(), -1, SQLITE_TRANSIENT);
        rc = sqlite3_step(stmt);
        int changes = sqlite3_changes(db_);
        sqlite3_finalize(stmt);
        return rc == SQLITE_DONE && changes > 0;
}

bool MetaStore::RemoveFile(const std::string& filename, const std::string& owner) {
    const char* sql = "DELETE FROM file_blocks WHERE filename=?1 AND owner=?2;";
    sqlite3_stmt* stmt = nullptr;
        int rc = sqlite3_prepare_v2(db_, sql, -1, &stmt, nullptr);
        if (rc != SQLITE_OK) return false;
        sqlite3_bind_text(stmt, 1, filename.c_str(), -1, SQLITE_TRANSIENT);
        sqlite3_bind_text(stmt, 2, owner.c_str(), -1, SQLITE_TRANSIENT);
        rc = sqlite3_step(stmt);
        int changes = sqlite3_changes(db_);
        sqlite3_finalize(stmt);
        return rc == SQLITE_DONE && changes > 0;
}

bool MetaStore::RegisterUser(const std::string& user, const std::string& pass) {
    const char* sql = "INSERT INTO users(user, pass) VALUES(?1, ?2);";
    sqlite3_stmt* stmt = nullptr;
    int rc = sqlite3_prepare_v2(db_, sql, -1, &stmt, nullptr);
    if (rc != SQLITE_OK) return false;
    sqlite3_bind_text(stmt, 1, user.c_str(), -1, SQLITE_TRANSIENT);
    sqlite3_bind_text(stmt, 2, pass.c_str(), -1, SQLITE_TRANSIENT);
    rc = sqlite3_step(stmt);
    sqlite3_finalize(stmt);
    return rc == SQLITE_DONE;
}

bool MetaStore::ValidateUser(const std::string& user, const std::string& pass) {
    const char* sql = "SELECT pass FROM users WHERE user=?1;";
    sqlite3_stmt* stmt = nullptr;
    int rc = sqlite3_prepare_v2(db_, sql, -1, &stmt, nullptr);
    if (rc != SQLITE_OK) return false;
    sqlite3_bind_text(stmt, 1, user.c_str(), -1, SQLITE_TRANSIENT);
    rc = sqlite3_step(stmt);
    bool valid = false;
    if (rc == SQLITE_ROW) {
        const char* dbpass = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 0));
        if (dbpass && pass == dbpass) valid = true;
    }
    sqlite3_finalize(stmt);
    return valid;
}