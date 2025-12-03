package ireader.domain.epub

import kotlinx.cinterop.*

/**
 * iOS implementation of EPUB ZIP creation
 * 
 * Creates a valid ZIP file with proper EPUB structure
 */
@OptIn(ExperimentalForeignApi::class)
actual fun createEpubZipPlatform(entries: List<EpubBuilder.EpubZipEntry>): ByteArray {
    val zipBuilder = ZipBuilder()
    
    entries.forEach { entry ->
        if (entry.compressed) {
            zipBuilder.addCompressedEntry(entry.path, entry.content)
        } else {
            zipBuilder.addStoredEntry(entry.path, entry.content)
        }
    }
    
    return zipBuilder.build()
}

/**
 * Simple ZIP file builder
 */
private class ZipBuilder {
    private val localHeaders = mutableListOf<ByteArray>()
    private val centralHeaders = mutableListOf<ByteArray>()
    private var currentOffset = 0
    
    fun addStoredEntry(path: String, content: ByteArray) {
        addEntry(path, content, compressed = false)
    }
    
    fun addCompressedEntry(path: String, content: ByteArray) {
        // For simplicity, store all entries uncompressed
        // Full DEFLATE compression would require zlib
        addEntry(path, content, compressed = false)
    }
    
    private fun addEntry(path: String, content: ByteArray, compressed: Boolean) {
        val pathBytes = path.encodeToByteArray()
        val crc32 = calculateCrc32(content)
        val compressionMethod = 0 // STORED
        
        val localHeaderOffset = currentOffset
        
        val localHeader = buildLocalFileHeader(
            pathBytes = pathBytes,
            crc32 = crc32,
            compressedSize = content.size,
            uncompressedSize = content.size,
            compressionMethod = compressionMethod
        )
        
        localHeaders.add(localHeader)
        localHeaders.add(content)
        currentOffset += localHeader.size + content.size
        
        val centralHeader = buildCentralDirectoryHeader(
            pathBytes = pathBytes,
            crc32 = crc32,
            compressedSize = content.size,
            uncompressedSize = content.size,
            compressionMethod = compressionMethod,
            localHeaderOffset = localHeaderOffset
        )
        
        centralHeaders.add(centralHeader)
    }
    
    fun build(): ByteArray {
        val centralDirOffset = currentOffset
        var centralDirSize = 0
        
        centralHeaders.forEach { centralDirSize += it.size }
        
        val eocd = buildEndOfCentralDirectory(
            numEntries = centralHeaders.size,
            centralDirSize = centralDirSize,
            centralDirOffset = centralDirOffset
        )
        
        val totalSize = currentOffset + centralDirSize + eocd.size
        val result = ByteArray(totalSize)
        var offset = 0
        
        localHeaders.forEach { part ->
            part.copyInto(result, offset)
            offset += part.size
        }
        
        centralHeaders.forEach { header ->
            header.copyInto(result, offset)
            offset += header.size
        }
        
        eocd.copyInto(result, offset)
        
        return result
    }
    
    private fun buildLocalFileHeader(
        pathBytes: ByteArray,
        crc32: Long,
        compressedSize: Int,
        uncompressedSize: Int,
        compressionMethod: Int
    ): ByteArray {
        val header = ByteArray(30 + pathBytes.size)
        var offset = 0
        
        header.writeInt32LE(offset, 0x04034b50); offset += 4
        header.writeInt16LE(offset, 20); offset += 2
        header.writeInt16LE(offset, 0); offset += 2
        header.writeInt16LE(offset, compressionMethod); offset += 2
        header.writeInt16LE(offset, 0); offset += 2
        header.writeInt16LE(offset, 0); offset += 2
        header.writeInt32LE(offset, crc32.toInt()); offset += 4
        header.writeInt32LE(offset, compressedSize); offset += 4
        header.writeInt32LE(offset, uncompressedSize); offset += 4
        header.writeInt16LE(offset, pathBytes.size); offset += 2
        header.writeInt16LE(offset, 0); offset += 2
        pathBytes.copyInto(header, offset)
        
        return header
    }
    
    private fun buildCentralDirectoryHeader(
        pathBytes: ByteArray,
        crc32: Long,
        compressedSize: Int,
        uncompressedSize: Int,
        compressionMethod: Int,
        localHeaderOffset: Int
    ): ByteArray {
        val header = ByteArray(46 + pathBytes.size)
        var offset = 0
        
        header.writeInt32LE(offset, 0x02014b50); offset += 4
        header.writeInt16LE(offset, 20); offset += 2
        header.writeInt16LE(offset, 20); offset += 2
        header.writeInt16LE(offset, 0); offset += 2
        header.writeInt16LE(offset, compressionMethod); offset += 2
        header.writeInt16LE(offset, 0); offset += 2
        header.writeInt16LE(offset, 0); offset += 2
        header.writeInt32LE(offset, crc32.toInt()); offset += 4
        header.writeInt32LE(offset, compressedSize); offset += 4
        header.writeInt32LE(offset, uncompressedSize); offset += 4
        header.writeInt16LE(offset, pathBytes.size); offset += 2
        header.writeInt16LE(offset, 0); offset += 2
        header.writeInt16LE(offset, 0); offset += 2
        header.writeInt16LE(offset, 0); offset += 2
        header.writeInt16LE(offset, 0); offset += 2
        header.writeInt32LE(offset, 0); offset += 4
        header.writeInt32LE(offset, localHeaderOffset); offset += 4
        pathBytes.copyInto(header, offset)
        
        return header
    }
    
    private fun buildEndOfCentralDirectory(
        numEntries: Int,
        centralDirSize: Int,
        centralDirOffset: Int
    ): ByteArray {
        val eocd = ByteArray(22)
        var offset = 0
        
        eocd.writeInt32LE(offset, 0x06054b50); offset += 4
        eocd.writeInt16LE(offset, 0); offset += 2
        eocd.writeInt16LE(offset, 0); offset += 2
        eocd.writeInt16LE(offset, numEntries); offset += 2
        eocd.writeInt16LE(offset, numEntries); offset += 2
        eocd.writeInt32LE(offset, centralDirSize); offset += 4
        eocd.writeInt32LE(offset, centralDirOffset); offset += 4
        eocd.writeInt16LE(offset, 0)
        
        return eocd
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
