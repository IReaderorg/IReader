package ireader.domain.usecases.backup.lnreader

import ireader.domain.models.lnreader.*
import platform.Foundation.*
import kotlinx.cinterop.*
import kotlinx.serialization.json.Json

/**
 * iOS implementation of LNReader backup parsing
 */
@OptIn(ExperimentalForeignApi::class)
actual suspend fun parseBackupPlatform(bytes: ByteArray): LNReaderBackup {
    val backupJson = extractBackupJsonFromZip(bytes)
    
    if (backupJson == null) {
        return LNReaderBackup(
            version = LNReaderVersion("unknown"),
            novels = emptyList(),
            categories = emptyList(),
            settings = emptyMap()
        )
    }
    
    return try {
        val json = Json { ignoreUnknownKeys = true }
        json.decodeFromString<LNReaderBackup>(backupJson)
    } catch (e: Exception) {
        println("[LNReaderBackup] Parse error: ${e.message}")
        LNReaderBackup(
            version = LNReaderVersion("unknown"),
            novels = emptyList(),
            categories = emptyList(),
            settings = emptyMap()
        )
    }
}

actual fun isLNReaderBackupPlatform(bytes: ByteArray): Boolean {
    if (bytes.size < 4) return false
    if (bytes[0] != 0x50.toByte() || bytes[1] != 0x4B.toByte()) return false
    return findZipEntryData(bytes, "backup.json") != null || findZipEntryData(bytes, "novels.json") != null
}

private fun extractBackupJsonFromZip(zipData: ByteArray): String? {
    val possibleNames = listOf("backup.json", "novels.json", "data.json")
    for (name in possibleNames) {
        val data = findZipEntryData(zipData, name)
        if (data != null) return data.decodeToString()
    }
    return null
}


private fun findZipEntryData(zipData: ByteArray, entryName: String): ByteArray? {
    var offset = 0
    
    while (offset < zipData.size - 4) {
        val signature = readInt32LE(zipData, offset)
        if (signature != 0x04034b50) break
        
        val compressionMethod = readInt16LE(zipData, offset + 8)
        val compressedSize = readInt32LE(zipData, offset + 18)
        val fileNameLength = readInt16LE(zipData, offset + 26)
        val extraFieldLength = readInt16LE(zipData, offset + 28)
        
        val headerSize = 30
        val fileName = zipData.sliceArray(offset + headerSize until offset + headerSize + fileNameLength).decodeToString()
        val dataOffset = offset + headerSize + fileNameLength + extraFieldLength
        
        if (fileName == entryName || fileName.endsWith("/$entryName")) {
            // Only support uncompressed (STORED) entries
            if (compressionMethod == 0) {
                return zipData.sliceArray(dataOffset until dataOffset + compressedSize)
            }
            return null // Compressed data not supported without decompression library
        }
        
        offset = dataOffset + compressedSize
    }
    return null
}

private fun readInt16LE(data: ByteArray, offset: Int): Int =
    (data[offset].toInt() and 0xFF) or ((data[offset + 1].toInt() and 0xFF) shl 8)

private fun readInt32LE(data: ByteArray, offset: Int): Int =
    (data[offset].toInt() and 0xFF) or
    ((data[offset + 1].toInt() and 0xFF) shl 8) or
    ((data[offset + 2].toInt() and 0xFF) shl 16) or
    ((data[offset + 3].toInt() and 0xFF) shl 24)
