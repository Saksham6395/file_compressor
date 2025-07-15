#ifndef DECODER_H
#define DECODER_H

#include <string>

// Declaration for the decompression function
int decompress(int inputFd, const std::string& outputPath);

#endif // DECODER_H
