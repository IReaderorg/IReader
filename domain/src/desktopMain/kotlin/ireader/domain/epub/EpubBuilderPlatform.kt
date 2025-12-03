package ireader.domain.epub

import java.io.ByteArrayOutputStream
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Desktop implementation of EPUB ZIP creation using java.util.zip
 */
actual fun createEpubZipPlatform(entries: List<EpubBuilder.EpubZipEntry>): ByteArray {
    val byteArrayOutputStream = ByteArrayOutputStream()
    
    ZipOutputStream(byteArrayOutputStream).use { zipOut ->
        for (entry in entries) {
            val zipEntry = ZipEntry(entry.path)
            
            if (!entry.compressed) {
                // For mimetype, store without compression
                zipEntry.method = ZipEntry.STORED
                zipEntry.size = entry.content.size.toLong()
                zipEntry.compressedSize = entry.content.size.toLong()
                val crc = CRC32()
                crc.update(entry.content)
                zipEntry.crc = crc.value
            }
            
            zipOut.putNextEntry(zipEntry)
            zipOut.write(entry.content)
            zipOut.closeEntry()
        }
    }
    
    return byteArrayOutputStream.toByteArray()
}
