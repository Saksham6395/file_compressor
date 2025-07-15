#include <jni.h>
#include <string>
#include "encoder.h"

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_compressor_NativeLib_compress(
        JNIEnv *env,
        jobject /* this */,
        jint inputFd,
        jstring outputPath) {

    const char* out_path = env->GetStringUTFChars(outputPath, nullptr);

    int result = compress(inputFd, std::string(out_path));

    env->ReleaseStringUTFChars(outputPath, out_path);

    return result;  // 0 = success, -1 = failure
}