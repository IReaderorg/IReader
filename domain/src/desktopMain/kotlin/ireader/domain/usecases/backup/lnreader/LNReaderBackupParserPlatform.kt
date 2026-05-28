package ireader.domain.usecases.backup.lnreader

import ireader.domain.models.lnreader.LNReaderBackup
import ireader.domain.models.lnreader.LNReaderVersion

/**
 * Desktop implementation of LNReader backup parsing.
 *
 * For Desktop/JVM, we provide a stub implementation that returns empty results
 * since LNReader backup import is primarily an Android feature.
 */

/**
 * Desktop stub implementation - returns empty backup since Desktop doesn't support ZIP parsing.
 */
actual suspend fun parseBackupPlatform(bytes: ByteArray): LNReaderBackup {
    ireader.core.log.Log.warn { "LNReader: Desktop platform does not support LNReader backup parsing" }
    return LNReaderBackup(
        version = LNReaderVersion("unknown"),
        novels = emptyList(),
        categories = emptyList(),
        settings = emptyMap()
    )
}

/**
 * Desktop stub implementation - always returns false.
 */
actual fun isLNReaderBackupPlatform(bytes: ByteArray): Boolean {
    return false
}

/**
 * Desktop stub implementation - returns empty map since Desktop doesn't support ZIP parsing.
 */
actual fun extractChapterContentPlatform(backupBytes: ByteArray): Map<Int, String> {
    ireader.core.log.Log.warn { "LNReader: Desktop platform does not support chapter content extraction from download.zip" }
    return emptyMap()
}
