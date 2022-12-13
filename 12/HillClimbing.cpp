#include <fstream>
#include <iostream>
#include <map>
#include <queue>
#include <set>
#include <string>
#include <utility>
#include <vector>

using namespace std;

class HillClimbingAlgorithm {
public:
    void read_graph(string input) {
        int i = 0;

        // read in vertices and initialize empty adjacency lists for every vertex
        for (char c : input) {
            if (c == '\n') {
                if (field_width == 0) field_width = i;
                continue;
            }
            if (c == 'S') { start = i; c = 'a'; }
            if (c == 'E') { end = i; c = 'z'; }
            vertices.push_back(c);
            edges.push_back({});
            ++i;
        }

        // populate the adjacency lists
        for (i = 0; i < vertices.size(); ++i) {
            if (i >= field_width) {
                int up = i - field_width;
                if (vertices.at(up) <= vertices.at(i) + 1) edges.at(i).insert(up);
            }
            if (i <= vertices.size() - field_width - 1) {
                int down = i + field_width;
                if (vertices.at(down) <= vertices.at(i) + 1) edges.at(i).insert(down);
            }
            if (i % field_width > 0) {
                int left = i - 1;
                if (vertices.at(left) <= vertices.at(i) + 1) edges.at(i).insert(left);
            }
            if (i % field_width < field_width - 1) {
                int right = i + 1;
                if (vertices.at(right) <= vertices.at(i) + 1) edges.at(i).insert(right);
            }
        } 
    }

    // This is the shortest-path problem in a directed, unweighted graph.
    // A simple breadth first search does the trick.
    int find_shortest_path() {
        bfs();
        int v = end;
        int length = 0;
        if (parent[v] == -1) return INT_MAX; // didn't find it from this position
        while (parent[v] != -1) {
            v = parent[v];
            ++length;
        }
        return length;
    }

    int find_shortest_path_from_any_a() {
        int shortest = INT_MAX;
        for (int i = 0; i < vertices.size(); ++i) {
            if (vertices.at(i) == 'a') {
                start = i;
                int length = find_shortest_path();
                if (length < shortest) shortest = length;
            }
        }
        return shortest;
    }

private:
    void bfs() {
        parent.clear();
        vector<bool> discovered;
        queue<int> Q;
        for (int i = 0; i < vertices.size(); ++i) {
            discovered.push_back(false);
            parent.push_back(-1);
        }
        Q.push(start);
        discovered[start] = true;

        while (!Q.empty()) {
            int u = Q.front();
            Q.pop();
            for (int v : edges[u]) {
                if (!discovered[v]) {
                    discovered[v] = true;
                    Q.push(v);
                    parent[v] = u;
                }
                if (v == end) break; // we can stop early since we're not interested in the whole graph
            }
        }
    }

    int start;
    int end;
    int field_width = 0;
    vector<char> vertices;
    vector<set<int>> edges;
    vector<int> parent;
};


int main(int argc, char** argv) {
    ifstream is("input.txt");
    string input(istreambuf_iterator<char>(is), {});
    HillClimbingAlgorithm alg = HillClimbingAlgorithm();
    alg.read_graph(input);
    cout << "1. Length of the shortest path: " << alg.find_shortest_path() << endl;
    cout << "2. Length of the shortest path from any 'a': " << alg.find_shortest_path_from_any_a() << endl;
}
