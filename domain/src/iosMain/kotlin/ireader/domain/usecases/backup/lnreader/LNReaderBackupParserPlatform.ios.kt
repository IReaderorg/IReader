package ireader.domain.usecases.backup.lnreader

/**
 * iOS implementation of LNReader backup parsing
 */
actual suspend fun parseBackupPlatform(bytes: ByteArray): LNReaderBackup {
    // TODO: Implement proper parsing
    return LNReaderBackup(
        novels = emptyList(),
        chapters = emptyList(),
        categories = emptyList()
    )
}

actual fun isLNReaderBackupPlatform(bytes: ByteArray): Boolean {
    // Check if it's a valid LNReader backup format
    return try {
        val header = bytes.take(4).toByteArray().decodeToString()
        header == "LNRB" || bytes.decodeToString().contains("\"novels\"")
    } catch (e: Exception) {
        false
    }
}
