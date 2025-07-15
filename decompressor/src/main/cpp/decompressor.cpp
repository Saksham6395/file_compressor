// decompressor.cpp

#include <jni.h>
#include <string>
#include "decoder.h"  // Include the logic

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_decompressor_NativeLib_decompress(
        JNIEnv *env,
        jobject /* this */,
        jint inputFd,
        jstring outputPath) {

    const char* out_path = env->GetStringUTFChars(outputPath, nullptr);

    int result = decompress(inputFd, std::string(out_path));

    env->ReleaseStringUTFChars(outputPath, out_path);

    return result;
}
