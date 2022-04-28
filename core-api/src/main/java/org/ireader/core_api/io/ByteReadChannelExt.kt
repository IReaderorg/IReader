

package org.ireader.core_api.io

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.bits.Memory
import io.ktor.utils.io.bits.of
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path
import okio.buffer
import okio.use
import java.io.File

private const val BUFFER_SIZE = 8096

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

suspend fun ByteReadChannel.peek(bytes: Int, buffer: ByteArray): ByteArray {
    val memory = Memory.of(buffer, 0, bytes)
    peekTo(memory, 0, 0, bytes.toLong(), bytes.toLong())
    return buffer
}

@Suppress("BlockingMethodInNonBlockingContext")
suspend fun ByteReadChannel.saveTo(file: File) {
    withContext(Dispatchers.IO) {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        file.outputStream().use { output ->
            do {
                val read = readAvailable(buffer, 0, DEFAULT_BUFFER_SIZE)
                if (read > 0) {
                    output.write(buffer, 0, read)
                }
            } while (read >= 0)
        }
    }
}
