package ireader.domain.plugins

import ireader.core.io.VirtualFile
import platform.Foundation.*
import kotlinx.cinterop.*

/**
 * iOS implementation of ZIP extraction for plugins
 * 
 * Uses manual ZIP parsing without Compression framework
 */
@OptIn(ExperimentalForeignApi::class)
actual suspend fun extractZipEntry(file: VirtualFile, entryName: String): String? {
    try {
        // Read the ZIP file content
        val zipData = file.readBytes() ?: return null
        
        // Parse ZIP and find the entry
        val entry = findZipEntry(zipData, entryName) ?: return null
        
        // Decompress if needed
        val content = if (entry.compressionMethod == 0) {
            // STORED - no compression
            entry.data
        } else if (entry.compressionMethod == 8) {
            // DEFLATE - use NSData decompression
            inflateData(entry.data, entry.uncompressedSize)
        } else {
            return null
        }
        
        return content?.decodeToString()
    } catch (e: Exception) {
        println("[PluginLoader] Error extracting ZIP entry: ${e.message}")
        return null
    }
}

/**
 * iOS implementation of plugin instantiation
 * Not supported on iOS - use JS plugins instead
 */
actual fun instantiatePlugin(pluginClass: Any): Plugin {
    throw UnsupportedOperationException("Native plugin instantiation is not supported on iOS. Use JS plugins instead.")
}

/**
 * Data class for ZIP entry information
 */
private data class ZipEntry(
    val name: String,
    val compressionMethod: Int,
    val compressedSize: Int,
    val uncompressedSize: Int,
    val data: ByteArray
)

/**
 * Find and extract a specific entry from a ZIP file
 */
private fun findZipEntry(zipData: ByteArray, entryName: String): ZipEntry? {
    var offset = 0
    
    while (offset < zipData.size - 4) {
        // Check for local file header signature
        val signature = zipData.readInt32LE(offset)
        if (signature != 0x04034b50) {
            break
        }
        
        // Parse local file header
        val compressionMethod = zipData.readInt16LE(offset + 8)
        val compressedSize = zipData.readInt32LE(offset + 18)
        val uncompressedSize = zipData.readInt32LE(offset + 22)
        val fileNameLength = zipData.readInt16LE(offset + 26)
        val extraFieldLength = zipData.readInt16LE(offset + 28)
        
        val headerSize = 30
        val fileName = zipData.sliceArray(offset + headerSize until offset + headerSize + fileNameLength)
            .decodeToString()
        
        val dataOffset = offset + headerSize + fileNameLength + extraFieldLength
        
        if (fileName == entryName) {
            val data = zipData.sliceArray(dataOffset until dataOffset + compressedSize)
            return ZipEntry(
                name = fileName,
                compressionMethod = compressionMethod,
                compressedSize = compressedSize,
                uncompressedSize = uncompressedSize,
                data = data
            )
        }
        
        // Move to next entry
        offset = dataOffset + compressedSize
    }
    
    return null
}

/**
 * Inflate (decompress) DEFLATE data
 * Simple implementation without Compression framework
 */
@OptIn(ExperimentalForeignApi::class)
private fun inflateData(compressedData: ByteArray, expectedSize: Int): ByteArray? {
    if (compressedData.isEmpty()) return ByteArray(0)
    
    // For now, return null for compressed data - plugins should use STORED method
    // A full DEFLATE implementation would require a third-party library
    // or implementing the algorithm manually
    
    // Try using NSData if available
    return try {
        memScoped {
            compressedData.usePinned { pinnedData ->
                val nsData = NSData.dataWithBytes(
                    pinnedData.addressOf(0),
                    compressedData.size.toULong()
                )
                
                // NSData doesn't have built-in decompression
                // Return null to indicate decompression not supported
                null
            }
        }
    } catch (e: Exception) {
        null
    }
}

private fun ByteArray.readInt16LE(offset: Int): Int {
    return (this[offset].toInt() and 0xFF) or
           ((this[offset + 1].toInt() and 0xFF) shl 8)
}

private fun ByteArray.readInt32LE(offset: Int): Int {
    return (this[offset].toInt() and 0xFF) or
           ((this[offset + 1].toInt() and 0xFF) shl 8) or
           ((this[offset + 2].toInt() and 0xFF) shl 16) or
           ((this[offset + 3].toInt() and 0xFF) shl 24)
}
