#include <iostream>
#include <fstream>
#include <unordered_map>
#include <vector>
#include <unistd.h>  // for dup()
#include <cstdio>
#include <fcntl.h>
#include <sstream>
using namespace std;

class Node {
public:
    char ch;
    Node *left, *right;

    Node(char c = '\0') {
        ch = c;
        left = right = nullptr;
    }
};

void insertCode(Node* root, char ch, const string& code) {
    Node* curr = root;
    for (char bit : code) {
        if (bit == '0') {
            if (!curr->left) curr->left = new Node();
            curr = curr->left;
        } else {
            if (!curr->right) curr->right = new Node();
            curr = curr->right;
        }
    }
    curr->ch = ch;
}

string decode(Node* root, const string& bitstring, uint32_t bitLength) {
    string result = "";
    Node* curr = root;
    for (uint32_t i = 0; i < bitLength; ++i) {
        curr = (bitstring[i] == '0') ? curr->left : curr->right;
        if (!curr->left && !curr->right) {
            result += curr->ch;
            curr = root;
        }
    }
    return result;
}

void readCompressedFile(const string& filename, string& bitstring, Node*& root, uint32_t& bitLength) {
    ifstream in(filename, ios::binary);
    if (!in) {
        cerr << "Failed to open " << filename << endl;
        exit(1);
    }

    uint16_t mapSize;
    in.read(reinterpret_cast<char*>(&mapSize), sizeof(mapSize));//reade 2 byte(16 bits) as size of mapSize is 16
    root = new Node();

    for (int i = 0; i < mapSize; ++i) {
        char ch;
        in.get(ch);

        uint8_t codeLen;
        in.get(reinterpret_cast<char&>(codeLen));//reade 1 byte(8 bits)

        int byteCount = (codeLen + 7) / 8; // ceil of size 8 to get the number of bytes to store the hauffmann code
        string bits = "";
        for (int j = 0; j < byteCount; ++j) {
            unsigned char byte;
            in.get(reinterpret_cast<char&>(byte));//reade 1 byte(8 bits)
            for (int k = 7; k >= 0 && bits.size() < codeLen; --k) {
                bits += ((byte >> k) & 1) ? '1' : '0';
            }
        }

        insertCode(root, ch, bits);
    }

    in.read(reinterpret_cast<char*>(&bitLength), sizeof(bitLength));//inputs size of data of length 4byte(32 bits)

    unsigned char byte;
    while (in.get(reinterpret_cast<char&>(byte))) {
        for (int i = 7; i >= 0; --i) {
            bitstring += ((byte >> i) & 1) ? '1' : '0';
        }
    }

    in.close();
}

void deleteTree(Node* root) {
    if (!root) return;
    deleteTree(root->left);
    deleteTree(root->right);
    delete root;
}

int decompress(int inputFd, const string& outputFile) {
    lseek(inputFd, 0, SEEK_SET);
    string data;
    char buffer[4096];
    ssize_t bytesRead;

    while ((bytesRead = read(inputFd, buffer, sizeof(buffer))) > 0) {
        data.append(buffer, bytesRead);
    }

    if (bytesRead < 0) {
        cerr << "Error reading from file descriptor.\n";
        return 1;
    }
    std::istringstream in(data, ios::binary);


    uint16_t mapSize;
    in.read(reinterpret_cast<char*>(&mapSize), sizeof(mapSize));
    Node* root = new Node();

    for (int i = 0; i < mapSize; ++i) {
        char ch;
        in.get(ch);

        uint8_t codeLen;
        in.get(reinterpret_cast<char&>(codeLen));

        int byteCount = (codeLen + 7) / 8;
        string bits = "";
        for (int j = 0; j < byteCount; ++j) {
            unsigned char byte;
            in.get(reinterpret_cast<char&>(byte));
            for (int k = 7; k >= 0 && bits.size() < codeLen; --k) {
                bits += ((byte >> k) & 1) ? '1' : '0';
            }
        }

        insertCode(root, ch, bits);
    }

    uint32_t bitLength;
    in.read(reinterpret_cast<char*>(&bitLength), sizeof(bitLength));

    string bitstring = "";
    unsigned char byte;
    while (in.get(reinterpret_cast<char&>(byte))) {
        for (int i = 7; i >= 0; --i) {
            bitstring += ((byte >> i) & 1) ? '1' : '0';
        }
    }


    string decoded = decode(root, bitstring, bitLength);

    ofstream out(outputFile);
    if (!out) {
        cerr << "Failed to open output file: " << outputFile << endl;
        deleteTree(root);
        return 1;
    }
    out << decoded;
    out.close();

    deleteTree(root);
    return 0;
}
