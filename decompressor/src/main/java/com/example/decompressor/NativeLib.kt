package com.example.decompressor

object NativeLib {
    external fun decompress(inputPath: Int, outputPath: String): Int

    init {
        System.loadLibrary("decompressor") // match your `.so` name
    }
}
