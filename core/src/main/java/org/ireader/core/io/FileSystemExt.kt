package org.ireader.core.io


import java.io.File


//
//import okio.BufferedSink
//import okio.FileSystem
//import okio.Path
//
//suspend fun FileSystem.withAsyncSink(path: Path, block: (BufferedSink) -> Unit) {
//
//}
//
//suspend fun FileSystem.withAsyncGzipSink(path: Path, block: (BufferedSink) -> Unit) {}
//
//
////suspend fun <T> FileSystem.withAsyncSource(path: Path, block: (BufferedSource) -> T): T {
////    return T
////}
////
////
////suspend fun <T> FileSystem.withAsyncGzipSource(path: Path, block: (BufferedSource) -> T): T {
////    return T
////}

fun File.calculateSizeRecursively(): Long {
    return walkBottomUp().fold(0L) { acc, file -> acc + file.length() }
}