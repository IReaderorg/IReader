//package org.ireader.core.io
//
//import io.ktor.utils.io.*
//import io.ktor.utils.io.bits.*
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import java.io.File
//
//suspend fun ByteReadChannel.peek(bytes: Int, buffer: ByteArray): ByteArray {
//    val memory = Memory.of(buffer, 0, bytes)
//    peekTo(memory, 0, 0, bytes.toLong(), bytes.toLong())
//    return buffer
//}
//
//@Suppress("BlockingMethodInNonBlockingContext")
//suspend fun ByteReadChannel.saveTo(file: File) {
//    withContext(Dispatchers.IO) {
//        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
//        file.outputStream().use { output ->
//            do {
//                val read = readAvailable(buffer, 0, DEFAULT_BUFFER_SIZE)
//                if (read > 0) {
//                    output.write(buffer, 0, read)
//                }
//            } while (read >= 0)
//        }
//    }
//}
