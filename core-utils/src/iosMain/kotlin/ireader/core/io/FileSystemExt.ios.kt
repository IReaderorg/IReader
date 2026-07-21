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
        val sink = sink(path).buffer()
        try {
            block(sink)
        } finally {
            sink.close()
        }
    }
}

actual suspend fun FileSystem.withAsyncGzipSink(path: Path, block: (BufferedSink) -> Unit) {
    withContext(Dispatchers.IO) {
        // TODO: Implement gzip compression using okio.GzipSink when available
        val sink = sink(path).buffer()
        try {
            block(sink)
        } finally {
            sink.close()
        }
    }
}

actual suspend fun <T> FileSystem.withAsyncSource(path: Path, block: (BufferedSource) -> T): T {
    return withContext(Dispatchers.IO) {
        val source = source(path).buffer()
        try {
            block(source)
        } finally {
            source.close()
        }
    }
}

actual suspend fun <T> FileSystem.withAsyncGzipSource(path: Path, block: (BufferedSource) -> T): T {
    return withContext(Dispatchers.IO) {
        // TODO: Implement gzip decompression using okio.GzipSource when available
        val source = source(path).buffer()
        try {
            block(source)
        } finally {
            source.close()
        }
    }
}
