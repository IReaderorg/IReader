package ireader.domain.usecases.backup.v2

import android.content.Context
import ireader.domain.models.common.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class AndroidBackupArchiveStreamer(
    private val context: Context
) : BackupArchiveStreamer {

    override suspend fun createArchive(
        uri: Uri,
        metadataBytes: ByteArray,
        totalBooks: Int,
        writeBookEntries: suspend (emitEntry: suspend (entryName: String, bytes: ByteArray) -> Unit) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            val outputStream = context.contentResolver.openOutputStream(uri.androidUri, "w")
                ?: throw BackupException.WriteFailed(uri.toString(), IllegalStateException("Cannot open output stream"))

            ZipOutputStream(BufferedOutputStream(outputStream)).use { zipOut ->
                // 1. Write metadata entry
                val metaEntry = ZipEntry("metadata.pb")
                metaEntry.size = metadataBytes.size.toLong()
                zipOut.putNextEntry(metaEntry)
                zipOut.write(metadataBytes)
                zipOut.closeEntry()

                // 2. Stream book entries one-by-one
                writeBookEntries { entryName, bytes ->
                    val bookEntry = ZipEntry(entryName)
                    bookEntry.size = bytes.size.toLong()
                    zipOut.putNextEntry(bookEntry)
                    zipOut.write(bytes)
                    zipOut.closeEntry()
                    zipOut.flush()
                }
            }
        }
    }

    override suspend fun extractArchive(
        uri: Uri,
        onMetadata: suspend (ByteArray) -> Unit,
        onBookContent: suspend (entryName: String, bytes: ByteArray) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            val inputStream = context.contentResolver.openInputStream(uri.androidUri)
                ?: throw BackupException.ReadFailed(uri.toString(), IllegalStateException("Cannot open input stream"))

            ZipInputStream(BufferedInputStream(inputStream)).use { zipIn ->
                while (true) {
                    val entry = zipIn.nextEntry ?: break
                    val bytes = zipIn.readBytes()
                    if (entry.name == "metadata.pb" || entry.name == "metadata.proto.gz") {
                        onMetadata(bytes)
                    } else if (entry.name.startsWith("chapters/") || entry.name.endsWith(".pb")) {
                        onBookContent(entry.name, bytes)
                    }
                    zipIn.closeEntry()
                }
            }
        }
    }

    override suspend fun isZipArchive(uri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri.androidUri)?.use { input ->
                    val header = ByteArray(4)
                    val read = input.read(header)
                    read == 4 && header[0] == 0x50.toByte() && header[1] == 0x4B.toByte() &&
                            header[2] == 0x03.toByte() && header[3] == 0x04.toByte()
                } ?: false
            } catch (_: Exception) {
                false
            }
        }
    }
}
