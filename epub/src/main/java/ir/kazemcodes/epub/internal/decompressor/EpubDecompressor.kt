package ir.kazemcodes.epub.internal.decompressor

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

internal class EpubDecompressor {

    fun decompress(inputStream: InputStream): Map<String, Pair<ZipEntry, ByteArray>> {
        val steam = BufferedInputStream(inputStream)
        return ZipInputStream(steam).use { zipInputStream ->
            zipInputStream
                .entries()
                .filterNot { it.isDirectory }
                .associate { it.name to (it to zipInputStream.readBytes()) }

        }
    }

    private fun unpackToPathAndReturnResult(
        zipFile: ZipFile,
        outputPath: String
    ): List<ZipEntry> {

        val result = mutableListOf<ZipEntry>()
        File(outputPath).mkdir()

        zipFile.entries().asSequence().forEach { entry ->
            result.add(entry)
            val destinationFile = File(outputPath, entry.name)
            destinationFile.parentFile?.mkdirs()

            if (!entry.isDirectory) {
                val inputStream = BufferedInputStream(zipFile.getInputStream(entry))
                val outputStream = BufferedOutputStream(FileOutputStream(destinationFile))

                inputStream.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
        return result
    }
}
internal fun ZipInputStream.entries() = generateSequence { nextEntry }