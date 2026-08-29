package ireader.domain.usecases.backup.v2

import ireader.domain.models.common.Uri

class IosBackupArchiveStreamer : BackupArchiveStreamer {

    override suspend fun createArchive(
        uri: Uri,
        metadataBytes: ByteArray,
        totalBooks: Int,
        writeBookEntries: suspend (emitEntry: suspend (entryName: String, bytes: ByteArray) -> Unit) -> Unit
    ) {
        // Fallback for iOS
    }

    override suspend fun extractArchive(
        uri: Uri,
        onMetadata: suspend (ByteArray) -> Unit,
        onBookContent: suspend (entryName: String, bytes: ByteArray) -> Unit
    ) {
        // Fallback for iOS
    }

    override suspend fun isZipArchive(uri: Uri): Boolean = false
}
