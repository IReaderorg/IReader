package ireader.core.io

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import okio.BufferedSink
import okio.BufferedSource
import okio.FileSystem
import okio.Path
import okio.buffer

actual suspend fun FileSystem.withAsyncSink(path: Path, block: (BufferedSink) -> Unit) {
    withContext(Dispatchers.IO) {
        sink(path).buffer().use { block(it) }
    }
}

actual suspend fun FileSystem.withAsyncGzipSink(path: Path, block: (BufferedSink) -> Unit) {
    withContext(Dispatchers.IO) {
        // TODO: Implement gzip compression
        sink(path).buffer().use { block(it) }
    }
}

actual suspend fun <T> FileSystem.withAsyncSource(path: Path, block: (BufferedSource) -> T): T {
    return withContext(Dispatchers.IO) {
        source(path).buffer().use { block(it) }
    }
}

actual suspend fun <T> FileSystem.withAsyncGzipSource(path: Path, block: (BufferedSource) -> T): T {
    return withContext(Dispatchers.IO) {
        // TODO: Implement gzip decompression
        source(path).buffer().use { block(it) }
    }
}
