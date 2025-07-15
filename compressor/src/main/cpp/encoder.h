#ifndef ENCODER_H
#define ENCODER_H

#include <string>

// Declaration for the compression function
int compress(int inputFd, const std::string& outputPath);

#endif // ENCODER_H
