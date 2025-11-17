package ireader.domain.models.backup

import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Category
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.Track
import kotlinx.serialization.Serializable

/**
 * Comprehensive library backup data structure
 */
@Serializable
data class LibraryBackup(
    val version: Int = CURRENT_VERSION,
    val timestamp: Long = System.currentTimeMillis(),
    val books: List<BackupBook> = emptyList(),
    val categories: List<BackupCategory> = emptyList(),
    val preferences: BackupPreferences? = null,
    val statistics: BackupStatistics? = null,
    val metadata: BackupMetadata
) {
    companion object {
        const val CURRENT_VERSION = 2
    }
}

/**
 * Book data for backup
 */
@Serializable
data class BackupBook(
    val id: Long,
    val sourceId: Long,
    val title: String,
    val key: String,
    val author: String = "",
    val description: String = "",
    val genres: List<String> = emptyList(),
    val status: Long = 0,
    val cover: String = "",
    val customCover: String = "",
    val favorite: Boolean = false,
    val lastUpdate: Long = 0,
    val dateAdded: Long = 0,
    val viewer: Long = 0,
    val flags: Long = 0,
    val isPinned: Boolean = false,
    val pinnedOrder: Int = 0,
    val isArchived: Boolean = false,
    val chapters: List<BackupChapter> = emptyList(),
    val categories: List<Long> = emptyList(),
    val tracking: List<BackupTrack> = emptyList(),
    val customCoverData: ByteArray? = null
)

/**
 * Chapter data for backup
 */
@Serializable
data class BackupChapter(
    val id: Long,
    val bookId: Long,
    val key: String,
    val name: String,
    val scanlator: String? = null,
    val read: Boolean = false,
    val bookmark: Boolean = false,
    val lastPageRead: Long = 0,
    val chapterNumber: Float = 0f,
    val sourceOrder: Long = 0,
    val dateFetch: Long = 0,
    val dateUpload: Long = 0
)

/**
 * Category data for backup
 */
@Serializable
data class BackupCategory(
    val id: Long,
    val name: String,
    val order: Long = 0,
    val flags: Long = 0
)

/**
 * Tracking data for backup
 */
@Serializable
data class BackupTrack(
    val siteId: Int,
    val entryId: Long,
    val mediaId: Long = 0,
    val mediaUrl: String = "",
    val title: String = "",
    val lastRead: Float = 0f,
    val totalChapters: Int = 0,
    val score: Float = 0f,
    val status: Int = 1,
    val startReadTime: Long = 0,
    val endReadTime: Long = 0
)

/**
 * Preferences data for backup
 */
@Serializable
data class BackupPreferences(
    val librarySort: String = "",
    val libraryFilters: String = "",
    val readerSettings: Map<String, String> = emptyMap(),
    val downloadSettings: Map<String, String> = emptyMap(),
    val trackingSettings: Map<String, String> = emptyMap()
)

/**
 * Statistics data for backup
 */
@Serializable
data class BackupStatistics(
    val totalReadingTime: Long = 0,
    val chaptersRead: Int = 0,
    val booksCompleted: Int = 0,
    val readingStreak: Int = 0,
    val longestStreak: Int = 0
)

/**
 * Backup metadata
 */
@Serializable
data class BackupMetadata(
    val appVersion: String,
    val deviceName: String = "",
    val backupType: BackupType = BackupType.FULL,
    val isIncremental: Boolean = false,
    val previousBackupTimestamp: Long = 0,
    val totalSize: Long = 0,
    val bookCount: Int = 0,
    val chapterCount: Int = 0
)

enum class BackupType {
    FULL,
    LIBRARY_ONLY,
    SETTINGS_ONLY,
    INCREMENTAL
}

/**
 * Backup restore options
 */
data class RestoreOptions(
    val restoreBooks: Boolean = true,
    val restoreChapters: Boolean = true,
    val restoreCategories: Boolean = true,
    val restoreTracking: Boolean = true,
    val restorePreferences: Boolean = false,
    val restoreStatistics: Boolean = false,
    val mergeMode: MergeMode = MergeMode.REPLACE_EXISTING,
    val skipExisting: Boolean = false
)

enum class MergeMode {
    REPLACE_EXISTING,
    KEEP_EXISTING,
    MERGE_NEWER
}

/**
 * Backup restore progress
 */
data class RestoreProgress(
    val totalItems: Int,
    val processedItems: Int,
    val currentItem: String = "",
    val errors: List<RestoreError> = emptyList(),
    val isCompleted: Boolean = false
) {
    val progressPercentage: Float
        get() = if (totalItems > 0) (processedItems.toFloat() / totalItems) * 100f else 0f
}

/**
 * Restore error information
 */
data class RestoreError(
    val itemType: String,
    val itemName: String,
    val error: String,
    val canSkip: Boolean = true
)