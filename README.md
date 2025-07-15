# 📦 FileCompressor – Huffman-Based Android File Compressor  

An Android app that compresses and decompresses `.txt` files using **Huffman Coding**, powered by **C++ for speed**, **JNI for bridging**, and **Jetpack Compose** for a smooth UI.

---

## 🚀  What’s This All About?
- **DSA + Android + Native C++ = 🔥**
- Compress large `.txt` files directly on your phone  
- Share/download compressed files easily  
- Powered by a **custom Huffman encoder/decoder** (not some library shortcut!)

---

## 📲 Try It Out

> Link to APK :
🎥 [Google Drive](https://drive.google.com/drive/folders/1MjPU8uwgaTjUpJE0Uf-LhGFauMyU8bU9)

---

## 🧠 Why Huffman?

Huffman Coding is a classic greedy algorithm from **DSA land** used for lossless data compression. It's optimal for symbol-by-symbol coding based on frequency.  

In this app, we:
- Build a frequency map of characters  
- Use a **priority queue** to build the Huffman tree  
- Generate binary codes  
- Store them efficiently in the compressed file  

And yes, it’s all **written in C++**, from scratch, by hand, like true warriors.

---

## ⚙️ Features

| Feature | Description |
|--------|-------------|
| 📚 Compress `.txt` files | Uses native Huffman coding via C++ |
| 🔓 Decompress `.sks` files | Fast native decompression |
| 📂 Save to Downloads | Public download folder access |
| 📤 Share compressed files | Share via any installed app |
| 🔄 Realtime UI updates | Built with Jetpack Compose |
| 🧠 Efficient | Handles large files (MBs) using file descriptors |
| 🤝 C++ ↔ Kotlin | Uses JNI for native interfacing |

---

## 🛠️ Tech Stack

| Layer | Tool |
|------|------|
| 🧠 Compression Engine | **C++**, Custom Huffman Logic |
| 🔗 Native Bridge | **JNI**, NDK |
| 📱 UI | Jetpack Compose |
| 🗃️ File I/O | Android Storage APIs |
| 📤 Sharing | FileProvider + Intents |
| 💾 Caching | ViewModel + State management |

---

## 📂 Project Structure

```bash
filecompressor/
├── app/
│   └── src/main/java/com/example/filecompressor/
│       ├── navigation/           # Jetpack Navigation setup
│       ├── screen/               # All Compose screens
│       ├── viewmodel/            # ViewModels for state mgmt
│       ├── utils/                # Helpers like sharedPref
│       └── MainActivity.kt
│
├── compressor/                   # Native Huffman Compressor
│   ├── cpp/
│   │   ├── compressor.cpp        # Entry point
│   │   ├── encoder.cpp/.h        # Huffman logic
│   │   └── CMakeLists.txt
│   └── kotlin+java/
│       └── com/example/compressor/NativeLib.kt
│
├── decompressor/                 # Native Huffman Decompressor
│   ├── cpp/
│   │   ├── decompressor.cpp
│   │   ├── decoder.cpp/.h
│   │   └── CMakeLists.txt
│   └── kotlin+java/
│       └── com/example/decompressor/NativeLib.kt

```

## 🔩 How It Works (Under the Hood)

Compression Flow:
- User picks a file using system picker

- We grab the file descriptor (fd) — not the entire file content

- Pass the fd to native C++ using JNI

🧵 In C++:

- Read and build frequency map

- Build Huffman tree with a min-heap

- Encode and write the compressed data to disk

- Success! File saved in Downloads

Decompression Flow:
- User picks .sks file

- Again, pass fd to native C++

- Decode and reconstruct the original file

- All while keeping the memory footprint low and performance high 🧠⚡

## 🧪 Sample Huffman Code
cpp
Copy
Edit
```bash
priority_queue<Node*, vector<Node*>, Compare> pq;

for (auto& [ch, freq] : freqMap) {
    pq.push(new Node(ch, freq));
}

while (pq.size() > 1) {
    Node* left = pq.top(); pq.pop();
    Node* right = pq.top(); pq.pop();

    Node* merged = new Node('\0', left->freq + right->freq);
    merged->left = left;
    merged->right = right;

    pq.push(merged);
}
```
Pure DSA. No STL trickery. No libraries. Just logic.

## 🚀 Setup Guide
Clone the repo:

bash
Copy
Edit
git clone https://github.com/Saksham6395/filecompressor.git
Open in Android Studio

Make sure NDK + CMake are installed

Build the app

Run it on a real device (file access needs real storage)


🙌 Author
Saksham Samarth
📍 ECE @ SVNIT
⚔️ Competitive Programmer | ⚙️ Android Native Dev | 🤖 ML Enthusiast
