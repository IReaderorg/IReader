package org.ireader.core.io

import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path
import okio.buffer
import okio.use

private const val BUFFER_SIZE = 8096


suspend fun ByteReadChannel.peek(bytes: Int, buffer: ByteArray = ByteArray(bytes)): ByteArray {
    return ByteArray(0)
}

suspend fun ByteReadChannel.saveTo(path: Path, fileSystem: FileSystem) {
    withContext(Dispatchers.IO) {
        val buffer = ByteArray(BUFFER_SIZE)
        fileSystem.sink(path).buffer().use { sink ->
            do {
                val read = readAvailable(buffer, 0, BUFFER_SIZE)
                if (read > 0) {
                    sink.write(buffer, 0, read)
                }
            } while (read >= 0)
        }
    }
}

