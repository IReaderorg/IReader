

package org.ireader.core_api.io

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.BufferedSink
import okio.BufferedSource
import okio.FileSystem
import okio.Path
import okio.buffer
import okio.gzip
import kotlin.io.use

@Suppress("BlockingMethodInNonBlockingContext")
suspend fun FileSystem.withAsyncSink(path: Path, block: (BufferedSink) -> Unit) {
    withContext(Dispatchers.IO) {
        sink(path).buffer().use(block)
    }
}

@Suppress("BlockingMethodInNonBlockingContext")
suspend fun FileSystem.withAsyncGzipSink(path: Path, block: (BufferedSink) -> Unit) {
    withContext(Dispatchers.IO) {
        sink(path).gzip().buffer().use(block)
    }
}

@Suppress("BlockingMethodInNonBlockingContext")
suspend fun <T> FileSystem.withAsyncSource(path: Path, block: (BufferedSource) -> T): T {
    return withContext(Dispatchers.IO) {
        source(path).buffer().use(block)
    }
}

@Suppress("BlockingMethodInNonBlockingContext")
suspend fun <T> FileSystem.withAsyncGzipSource(path: Path, block: (BufferedSource) -> T): T {
    return withContext(Dispatchers.IO) {
        source(path).gzip().buffer().use(block)
    }
}
