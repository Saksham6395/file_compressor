cmake_minimum_required(VERSION 3.22.1)
project("compressor")

add_library(
        compressor
        SHARED
        encoder.cpp
        compressor.cpp
)

find_library(
        log-lib
        log
)

target_link_libraries(
        compressor
        ${log-lib}
)
