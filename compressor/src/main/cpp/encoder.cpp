#include <iostream>
#include <fstream>
#include <unordered_map>
#include <queue>
#include <vector>
#include <iomanip>
#include <unistd.h>
#include <fcntl.h>
using namespace std;

class Node {

public:
    char ch;
    int freq;
    Node *left, *right;
    Node(char c, int f){
        ch=c;
        freq=f;
        left=nullptr;
        right=nullptr;
    }
};

struct Compare {
    bool operator()(Node* a, Node* b) {
        return a->freq > b->freq;
    }
};

void buildCodeMap(Node* root, string str, unordered_map<char, string>& huffmanCode) {
    if (!root) return;

    // Leaf node
    if (!root->left && !root->right) {
        huffmanCode[root->ch] = str;
        return;
    }

    buildCodeMap(root->left, str + "0", huffmanCode);
    buildCodeMap(root->right, str + "1", huffmanCode);
}

void deleteTree(Node* root) {
    if (!root) return;
    deleteTree(root->left);
    deleteTree(root->right);
    delete root;
}

string encode(const string& text, unordered_map<char, string>& huffmanCode) {
    string encoded = "";
    for (char ch : text) {
        encoded += huffmanCode[ch];
    }
    return encoded;
}

string decode(Node* root, const string& encoded) {
    string decoded = "";
    Node* curr = root;

    for (char bit : encoded) {
        if (bit == '0') curr = curr->left;
        else curr = curr->right;

        if (!curr->left && !curr->right) {
            decoded += curr->ch;
            curr = root;
        }
    }

    return decoded;
}

void writeCompressedFile(const string& encodedText, unordered_map<char, string>& huffmanCode, const string& filename) {
    ofstream out(filename, ios::binary);
    if (!out) {
        cerr << "Failed to open " << filename << "\n";
        return;
    }

    // Step 1: Write Huffman Map Size
    uint16_t mapSize = huffmanCode.size();
    out.write(reinterpret_cast<const char*>(&mapSize), sizeof(mapSize));

    // Step 2: Write Huffman Map: [char][code size][code as bits]
    for (auto& [ch, code] : huffmanCode) {
        out.put(ch); // write the char

        uint8_t codeLength = code.size();
        out.put(codeLength); // write the code length

        // Pack the bits of the code string
        unsigned char byte = 0;
        int bitCount = 0;
        for (char bit : code) {
            byte = byte << 1 | (bit - '0');
            bitCount++;

            if (bitCount == 8) {
                out.put(byte);
                bitCount = 0;
                byte = 0;
            }
        }
        if (bitCount > 0) {
            byte <<= (8 - bitCount);
            out.put(byte);
        }
    }

    // Step 3: Write original encodedText length in bits
    uint32_t bitLength = encodedText.size();
    out.write(reinterpret_cast<const char*>(&bitLength), sizeof(bitLength));

    // Step 4: Write compressed bit string
    unsigned char byte = 0;
    int bitCount = 0;

    for (char bit : encodedText) {
        byte = byte << 1 | (bit - '0');
        bitCount++;

        if (bitCount == 8) {
            out.put(byte);
            bitCount = 0;
            byte = 0;
        }
    }

    if (bitCount > 0) {
        byte <<= (8 - bitCount);
        out.put(byte);
    }

    out.close();
}

int compress(int inputFd, const string& outputPath) {
    // Move to start of file
    lseek(inputFd, 0, SEEK_SET);

    // Read the file content
    string text;
    char buffer[4096];
    ssize_t bytesRead;

    while ((bytesRead = read(inputFd, buffer, sizeof(buffer))) > 0) {
        text.append(buffer, bytesRead);
    }

    if (bytesRead < 0) {
        cerr << "Error reading from file descriptor.\n";
        return -1;
    }

    // rest of your logic remains the same...
    unordered_map<char, int> freq;
    for (char ch : text) freq[ch]++;

    priority_queue<Node*, vector<Node*>, Compare> pq;
    for (auto& entry : freq) {
        pq.push(new Node(entry.first, entry.second));
    }

    while (pq.size() > 1) {
        Node* left = pq.top(); pq.pop();
        Node* right = pq.top(); pq.pop();
        Node* merged = new Node('\0', left->freq + right->freq);
        merged->left = left;
        merged->right = right;
        pq.push(merged);
    }

    Node* root = pq.top();
    unordered_map<char, string> huffmanCode;
    buildCodeMap(root, "", huffmanCode);

    string encodedText = encode(text, huffmanCode);
    writeCompressedFile(encodedText, huffmanCode, outputPath);
    deleteTree(root);
    return 0; // success
}