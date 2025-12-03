package ireader.domain.usecases.backup.lnreader

import ireader.domain.models.lnreader.LNReaderBackup
import ireader.domain.models.lnreader.LNReaderVersion

/**
 * iOS implementation of LNReader backup parsing
 * 
 * TODO: Full implementation using iOS ZIP libraries
 */
actual suspend fun parseBackupPlatform(bytes: ByteArray): LNReaderBackup {
    // TODO: Implement proper ZIP parsing for iOS
    return LNReaderBackup(
        version = LNReaderVersion("unknown"),
        novels = emptyList(),
        categories = emptyList(),
        settings = emptyMap()
    )
}

actual fun isLNReaderBackupPlatform(bytes: ByteArray): Boolean {
    // Check if it's a valid ZIP file (LNReader backups are ZIP files)
    return try {
        // ZIP files start with PK (0x50 0x4B)
        bytes.size >= 4 && bytes[0] == 0x50.toByte() && bytes[1] == 0x4B.toByte()
    } catch (e: Exception) {
        false
    }
}
