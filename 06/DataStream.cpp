#include <fstream>
#include <iostream>
#include <string>
#include <tuple>
#include <vector>

using namespace std;

class DataStream {
public:
    DataStream(const string& src) : data(src) {}

    string::size_type findStartOfPacket() const {
        return findFirstUniqueWindow(PACKET_MARKER_SIZE);
    }

    string::size_type findStartOfMessage() const {
        return findFirstUniqueWindow(MESSAGE_MARKER_SIZE);
    }
    
private:
    string::size_type findFirstUniqueWindow(const string::size_type width) const {
        for (auto start = data.cbegin(), end = start + width; end <= data.cend(); ++start, ++end) {
            if (areCharactersUnique(start, end)) return end - data.cbegin();
        }
        return data.size();
    }

    bool areCharactersUnique(string::const_iterator start, string::const_iterator end) const {
        for (auto i = start; i != end - 1; ++i) {
            for (auto j = i + 1; j != end; ++j) {
                if (*i == *j) return false;
            }
        }
        return true;
    }

    string data;
    static const string::size_type PACKET_MARKER_SIZE = 4;
    static const string::size_type MESSAGE_MARKER_SIZE = 14;
};

int main() {
    ifstream input_stream("input.txt");
    string input(istreambuf_iterator<char>(input_stream), {});
    DataStream ds(input);
    cout << "Start of packet: " << ds.findStartOfPacket() << endl;
    cout << "Start of message: " << ds.findStartOfMessage() << endl;

    return 0;
}
