package ireader.domain.usecases.backup.v2

import ireader.domain.models.common.Uri

/**
 * Platform-agnostic streaming ZIP archive provider for memory-safe full backups.
 * Streams individual book chapters one-by-one to/from disk without loading the entire
 * library into RAM simultaneously.
 */
interface BackupArchiveStreamer {
    /**
     * Creates a streaming ZIP archive directly to the given URI without holding all books in RAM.
     * [metadataBytes] is written as "metadata.pb".
     * [writeBookEntries] is called to write each book's chapters on-demand.
     */
    suspend fun createArchive(
        uri: Uri,
        metadataBytes: ByteArray,
        totalBooks: Int,
        writeBookEntries: suspend (emitEntry: suspend (entryName: String, bytes: ByteArray) -> Unit) -> Unit
    )

    /**
     * Reads a ZIP archive from the given URI entry by entry without loading the entire archive into memory.
     */
    suspend fun extractArchive(
        uri: Uri,
        onMetadata: suspend (ByteArray) -> Unit,
        onBookContent: suspend (entryName: String, bytes: ByteArray) -> Unit
    )

    /**
     * Checks if the file at the given URI is a ZIP archive (magic bytes 0x50 0x4B 0x03 0x04).
     */
    suspend fun isZipArchive(uri: Uri): Boolean
}
