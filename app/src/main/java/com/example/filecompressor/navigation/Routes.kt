package com.example.filecompressor.navigation

sealed class Routes(val routes:String) {
    data object Compress : Routes("compress")
    data object Decompress : Routes("decompress")
    data object Selection : Routes("selection")
    data object Sharing : Routes("sharing")
    data object Decompressed_Sharing : Routes("decompressed_sharing")

}