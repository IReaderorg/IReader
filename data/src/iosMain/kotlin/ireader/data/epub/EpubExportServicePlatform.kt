package ireader.data.epub

import kotlinx.cinterop.*

/**
 * iOS implementation of EPUB ZIP creation
 * 
 * Creates a valid ZIP file with proper EPUB structure:
 * - mimetype file stored uncompressed as first entry
 * - All other files stored uncompressed (DEFLATE requires native library)
 * 
 * Note: For full DEFLATE compression, consider using a KMP compression library
 * or implementing pure Kotlin DEFLATE (complex but possible).
 */
@OptIn(ExperimentalForeignApi::class)
actual fun createEpubZip(entries: List<EpubExportServiceImpl.EpubZipEntry>): ByteArray {
    val zipBuilder = EpubZipBuilder()
    
    entries.forEach { entry ->
        // Store all entries uncompressed for iOS
        // EPUB readers handle uncompressed entries fine
        zipBuilder.addStoredEntry(entry.path, entry.content)
    }
    
    return zipBuilder.build()
}

/**
 * ZIP file builder for EPUB creation
 * Uses STORED (uncompressed) method for all entries
 */
@OptIn(ExperimentalForeignApi::class)
private class EpubZipBuilder {
    private val localHeaders = mutableListOf<ByteArray>()
    private val centralHeaders = mutableListOf<ByteArray>()
    private var currentOffset = 0
    
    fun addStoredEntry(path: String, content: ByteArray) {
        addEntry(path, content)
    }
    
    private fun addEntry(path: String, content: ByteArray) {
        val pathBytes = path.encodeToByteArray()
        val crc32 = calculateCrc32(content)
        val size = content.size
        
        // Local file header (30 bytes + path + content)
        val localHeader = ByteArray(30 + pathBytes.size + size)
        var offset = 0
        
        // Signature
        localHeader.writeInt32LE(offset, 0x04034b50)
        offset += 4
        
        // Version needed (2.0)
        localHeader.writeInt16LE(offset, 20)
        offset += 2
        
        // General purpose bit flag
        localHeader.writeInt16LE(offset, 0)
        offset += 2
        
        // Compression method (0 = STORED)
        localHeader.writeInt16LE(offset, 0)
        offset += 2
        
        // Last mod time/date (use fixed value)
        localHeader.writeInt16LE(offset, 0)
        offset += 2
        localHeader.writeInt16LE(offset, 0)
        offset += 2
        
        // CRC-32
        localHeader.writeInt32LE(offset, crc32.toInt())
        offset += 4
        
        // Compressed size
        localHeader.writeInt32LE(offset, size)
        offset += 4
        
        // Uncompressed size
        localHeader.writeInt32LE(offset, size)
        offset += 4
        
        // File name length
        localHeader.writeInt16LE(offset, pathBytes.size)
        offset += 2
        
        // Extra field length
        localHeader.writeInt16LE(offset, 0)
        offset += 2
        
        // File name
        pathBytes.copyInto(localHeader, offset)
        offset += pathBytes.size
        
        // File content
        content.copyInto(localHeader, offset)
        
        localHeaders.add(localHeader)
        
        // Central directory header (46 bytes + path)
        val centralHeader = ByteArray(46 + pathBytes.size)
        offset = 0
        
        // Signature
        centralHeader.writeInt32LE(offset, 0x02014b50)
        offset += 4
        
        // Version made by
        centralHeader.writeInt16LE(offset, 20)
        offset += 2
        
        // Version needed
        centralHeader.writeInt16LE(offset, 20)
        offset += 2
        
        // General purpose bit flag
        centralHeader.writeInt16LE(offset, 0)
        offset += 2
        
        // Compression method (0 = STORED)
        centralHeader.writeInt16LE(offset, 0)
        offset += 2
        
        // Last mod time/date
        centralHeader.writeInt16LE(offset, 0)
        offset += 2
        centralHeader.writeInt16LE(offset, 0)
        offset += 2
        
        // CRC-32
        centralHeader.writeInt32LE(offset, crc32.toInt())
        offset += 4
        
        // Compressed size
        centralHeader.writeInt32LE(offset, size)
        offset += 4
        
        // Uncompressed size
        centralHeader.writeInt32LE(offset, size)
        offset += 4
        
        // File name length
        centralHeader.writeInt16LE(offset, pathBytes.size)
        offset += 2
        
        // Extra field length
        centralHeader.writeInt16LE(offset, 0)
        offset += 2
        
        // File comment length
        centralHeader.writeInt16LE(offset, 0)
        offset += 2
        
        // Disk number start
        centralHeader.writeInt16LE(offset, 0)
        offset += 2
        
        // Internal file attributes
        centralHeader.writeInt16LE(offset, 0)
        offset += 2
        
        // External file attributes
        centralHeader.writeInt32LE(offset, 0)
        offset += 4
        
        // Relative offset of local header
        centralHeader.writeInt32LE(offset, currentOffset)
        offset += 4
        
        // File name
        pathBytes.copyInto(centralHeader, offset)
        
        centralHeaders.add(centralHeader)
        currentOffset += localHeader.size
    }
    
    fun build(): ByteArray {
        val centralDirOffset = currentOffset
        val centralDirSize = centralHeaders.sumOf { it.size }
        
        // End of central directory record (22 bytes)
        val eocd = ByteArray(22)
        var offset = 0
        
        // Signature
        eocd.writeInt32LE(offset, 0x06054b50)
        offset += 4
        
        // Disk number
        eocd.writeInt16LE(offset, 0)
        offset += 2
        
        // Disk with central directory
        eocd.writeInt16LE(offset, 0)
        offset += 2
        
        // Number of entries on this disk
        eocd.writeInt16LE(offset, centralHeaders.size)
        offset += 2
        
        // Total number of entries
        eocd.writeInt16LE(offset, centralHeaders.size)
        offset += 2
        
        // Size of central directory
        eocd.writeInt32LE(offset, centralDirSize)
        offset += 4
        
        // Offset of central directory
        eocd.writeInt32LE(offset, centralDirOffset)
        offset += 4
        
        // Comment length
        eocd.writeInt16LE(offset, 0)
        
        // Combine all parts
        val totalSize = currentOffset + centralDirSize + 22
        val result = ByteArray(totalSize)
        var pos = 0
        
        // Local headers with content
        localHeaders.forEach { header ->
            header.copyInto(result, pos)
            pos += header.size
        }
        
        // Central directory
        centralHeaders.forEach { header ->
            header.copyInto(result, pos)
            pos += header.size
        }
        
        // End of central directory
        eocd.copyInto(result, pos)
        
        return result
    }
    
    private fun ByteArray.writeInt16LE(offset: Int, value: Int) {
        this[offset] = (value and 0xFF).toByte()
        this[offset + 1] = ((value shr 8) and 0xFF).toByte()
    }
    
    private fun ByteArray.writeInt32LE(offset: Int, value: Int) {
        this[offset] = (value and 0xFF).toByte()
        this[offset + 1] = ((value shr 8) and 0xFF).toByte()
        this[offset + 2] = ((value shr 16) and 0xFF).toByte()
        this[offset + 3] = ((value shr 24) and 0xFF).toByte()
    }
    
    private fun calculateCrc32(data: ByteArray): Long {
        var crc = 0xFFFFFFFFL
        for (byte in data) {
            val index = ((crc xor byte.toLong()) and 0xFF).toInt()
            crc = (crc shr 8) xor CRC32_TABLE[index]
        }
        return crc xor 0xFFFFFFFFL
    }
    
    companion object {
        private val CRC32_TABLE = LongArray(256) { n ->
            var c = n.toLong()
            repeat(8) {
                c = if ((c and 1L) != 0L) {
                    0xEDB88320L xor (c shr 1)
                } else {
                    c shr 1
                }
            }
            c
        }
    }
}
