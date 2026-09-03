package ireader.domain.models.sync

import kotlinx.serialization.Serializable

/**
 * Presets for Quick Share / ShareMe style transfers.
 */
@Serializable
enum class TransferPreset {
    /** Mirror everything: Library books, full offline downloaded chapters, history, and settings */
    EVERYTHING,

    /** Transfer books metadata and reading progress/history without heavy offline chapter text */
    LIBRARY_AND_PROGRESS,

    /** Fast catch-up: Reading progress and history only */
    PROGRESS_ONLY,

    /** Custom selection chosen by user */
    CUSTOM
}

/**
 * Granular transfer scope configuration for Quick Share / ShareMe local sync.
 *
 * @property transferLibrary Whether to sync library books and categories
 * @property transferReadingProgress Whether to sync reading history and chapter progress
 * @property transferDownloadedChapters Whether to transfer full offline chapter content (List<Page>)
 * @property transferSettings Whether to transfer reader and app preferences
 * @property preset Selected preset mode
 */
@Serializable
data class SyncTransferScope(
    val transferLibrary: Boolean = true,
    val transferReadingProgress: Boolean = true,
    val transferDownloadedChapters: Boolean = false,
    val transferSettings: Boolean = false,
    val preset: TransferPreset = TransferPreset.LIBRARY_AND_PROGRESS
) {
    companion object {
        val Everything = SyncTransferScope(
            transferLibrary = true,
            transferReadingProgress = true,
            transferDownloadedChapters = true,
            transferSettings = true,
            preset = TransferPreset.EVERYTHING
        )

        val LibraryAndProgress = SyncTransferScope(
            transferLibrary = true,
            transferReadingProgress = true,
            transferDownloadedChapters = false,
            transferSettings = false,
            preset = TransferPreset.LIBRARY_AND_PROGRESS
        )

        val ProgressOnly = SyncTransferScope(
            transferLibrary = false,
            transferReadingProgress = true,
            transferDownloadedChapters = false,
            transferSettings = false,
            preset = TransferPreset.PROGRESS_ONLY
        )
    }
}
