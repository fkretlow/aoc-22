#include <fstream>
#include <functional>
#include <iostream>
#include <queue>
#include <regex>
#include <string>
#include <vector>

using namespace std;

class FSDir {
public:
    FSDir(FSDir* p, const string& n) : parent(p), name(n) { }
    ~FSDir() { for (auto child : children) delete child; }

    const string& get_name() { return name; }
    FSDir* get_parent() const { return parent; }
    const vector<FSDir*>& get_children() const { return children; }

    FSDir* get_child(const string& name) const {
        for (auto child : children) {
            if (child->get_name() == name) return child;
        }
        return nullptr;
    }

    size_t get_size() const {
        size_t size = file_size;
        for (auto child : children) {
            size += child->get_size();
        }
        return size;
    }

    void add_file(const size_t s) { file_size += s; }

    void add_sub_dir(const string& n) {
        FSDir* dir = new FSDir(this, n);
        children.push_back(dir);
    }

    void traverse(function<void (FSDir*)> f) {
        f(this);
        for (auto child : children) {
            child->traverse(f);
        }
    }

private:
    const string name;
    FSDir* parent;
    vector<FSDir*> children;
    size_t file_size = 0;
};


class FSScanner {
public:
    FSScanner() = default;
    ~FSScanner() { delete root; }

    void process(const string& line) {
        switch (state) {
            case INIT: init_state(line); break;
            case LS: ls_state(line); break;
        }
    }

    FSDir* const get_fs() const { return root; }

private:
    void init_state(const string& line) {
        smatch match;
        if (regex_match(line, match, command_regex)) {
            const string cmd = match[1];
            if (cmd == "ls") {
                state = LS;
                return;
            } else { // cmd == cd
                const string dest = match[3];
                cd(dest);
                return;
            }
        } else {
            throw invalid_argument("received invalid line " + line);
        }
    }

    void ls_state(const string& line) {
        if (line.starts_with('$')) {
            state = INIT;
            return init_state(line);
        }
        smatch match;
        if (regex_match(line, match, ls_dir_regex)) {
            const string name = match[1];
            work_dir->add_sub_dir(name);
        } else if (regex_match(line, match, ls_file_regex)) {
            size_t size;
            sscanf(line.c_str(), "%zu", &size);
            work_dir->add_file(size);
        } else {
            throw invalid_argument("cannot parse directory content '" + line + "'");
        }
    }

    void cd(const string& dest) {
        if (dest == "/") {
            work_dir = root;
        } else if (dest == "..") {
            work_dir = work_dir->get_parent();
        } else {
            FSDir* child = work_dir->get_child(dest);
            if (child) work_dir = child;
            else throw invalid_argument("no matching subdirectory: '" + dest + "'");
        }
    }

    FSDir* root = new FSDir(nullptr, "/");
    FSDir* work_dir = root;
    enum State { INIT, LS };
    State state = INIT;
    const regex command_regex = regex("\\$ (ls|cd)( (..|/|[\\w.-]+))?");
    const regex ls_dir_regex = regex("dir ([\\w.-]+)");
    const regex ls_file_regex = regex("(\\d+) [\\w.-]+");
};

int main() {
    ifstream input("input2.txt");
    string line;
    FSScanner scanner = FSScanner();
    while (getline(input, line)) {
        scanner.process(line);
    }
    FSDir* fs = scanner.get_fs();

    size_t r1 = 0;
    fs->traverse([&r1](FSDir* dir) {
                     size_t size = dir->get_size();
                     if (size <= 100000) r1 += size;
                 });
    cout << "result 1: " << r1 << endl;

    size_t r2 = SIZE_T_MAX;
    size_t diff = 30000000 - (70000000 - fs->get_size());
    fs->traverse([&r2, &diff](FSDir *dir) {
                     size_t size = dir->get_size();
                     if (size >= diff && size < r2) r2 = size; 
                 });
    cout << "result 2: " << r2 << endl;
}
