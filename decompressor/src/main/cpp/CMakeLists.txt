cmake_minimum_required(VERSION 3.22.1)
project("decompressor")

add_library(
        decompressor
        SHARED
        decompressor.cpp
        decoder.cpp
)

target_link_libraries(
        decompressor
        android
        log
)