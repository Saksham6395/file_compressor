package com.example.compressor

object NativeLib {
    external fun compress(inputFd: Int, outputPath: String): Int

    init {
        System.loadLibrary("compressor")
    }
}
