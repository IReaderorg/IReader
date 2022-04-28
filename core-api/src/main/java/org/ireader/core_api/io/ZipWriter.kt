

package org.ireader.core_api.io

import okio.FileSystem
import okio.Path
import okio.Source
import okio.buffer
import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

fun FileSystem.createZip(
    destination: Path,
    compress: Boolean,
    block: ZipWriterScope.() -> Unit
) {
    try {
        val outStream = ZipOutputStream(destination.toFile().outputStream())
        if (!compress) {
            outStream.setLevel(Deflater.NO_COMPRESSION)
        }

        outStream.use {
            block(ZipWriterScope(outStream))
        }
    } catch (e: Throwable) {
        delete(destination)
        throw e
    }
}

class ZipWriterScope(
    private val stream: ZipOutputStream
) {

    fun addFile(destination: String, source: Source) {
        val entry = ZipEntry(destination)
        stream.putNextEntry(entry)
        source.buffer().use { bufferedSource ->
            bufferedSource.inputStream().use { input ->
                input.copyTo(stream)
            }
        }
        stream.closeEntry()
    }

    fun addDirectory(destination: String) {
        val directory = if (destination.endsWith("/")) {
            destination
        } else {
            "$destination/"
        }
        val entry = ZipEntry(directory)
        stream.putNextEntry(entry)
        stream.closeEntry()
    }
}
