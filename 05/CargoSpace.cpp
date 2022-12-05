#include <cctype>
#include <deque>
#include <fstream>
#include <iostream>
#include <sstream>
#include <string>
#include <vector>
#include <regex>

using namespace std;

class CargoSpace {
public:
    string process_input(istream& input, const bool one_by_one) {
        string line;
        getline(input, line);

        do {
            add_layer(line);
            getline(input, line);
        } while (!(line.at(0) != '[' && isdigit(line.at(1))) && !input.eof());

        do {
            getline(input, line);
        } while (!line.starts_with("move") && !input.eof());

        do {
            execute_move(line, one_by_one);
            getline(input, line);
        } while (!input.eof());

        return get_topmost_crates();
    }

private:
    vector<deque<char>> stacks;
    const regex move_regex = regex("move (\\d+) from (\\d+) to (\\d)");

    void add_layer(const string& layer) {
        deque<char>::size_type i;
        string::size_type j;
        for (i = 0, j = 1; j < layer.size(); ++i, j += 4) {
            char c = layer.at(j);
            if (i >= stacks.size()) {
                stacks.push_back(deque<char>());
            }
            if (isalpha(c)) {
                stacks.at(i).push_front(c);
            }
        }
    }

    void execute_move(const string& move, const bool one_by_one) {
        int count, from, to;
        smatch matches;
        if (regex_match(move, matches, move_regex)) {
            count = stoi(matches[1]);
            from = stoi(matches[2]) - 1;
            to = stoi(matches[3]) - 1;
        } else {
            cerr << "couldn't parse move string: " << move << endl;
            return;
        }

        if (one_by_one) {
            while (count--) {
                char c = stacks.at(from).back();
                stacks.at(from).pop_back();
                stacks.at(to).push_back(c);
            }
        } else {
            const auto start = stacks.at(from).end() - count;
            const auto end = stacks.at(from).end();
            stacks.at(to).insert(stacks.at(to).end(), start, end);
            stacks.at(from).erase(start, end);
        }
    }

    inline string get_topmost_crates() const {
        stringstream s;
        for (auto stack : stacks) {
            s << (!stack.empty() ? stack.back() : ' ');
        }   
        return s.str();
    }
};

int main() {
    ifstream input("input.txt");

    cout << "Topmost crates when moved by CrateMover 9000: "
        << CargoSpace().process_input(input, true) << endl;

    input.clear(); input.seekg(0);

    cout << "Topmost crates when moved by CrateMover 9001: "
        << CargoSpace().process_input(input, false) << endl;

    return 0;
}

