package ireader.domain.models.sync

import kotlinx.serialization.Serializable

/**
 * Sync provider types supported by IReader
 */
@Serializable
enum class SyncProviderType {
    NONE,
    GOOGLE_DRIVE,
    SUPABASE,
    LOCAL_WIFI
}

/**
 * Universal sync item types for tombstone tracking
 */
@Serializable
enum class UniversalSyncItemType {
    BOOK,
    CATEGORY,
    HISTORY,
    BOOKMARK
}

/**
 * Canonical sync manifest for cloud and P2P synchronization.
 * Lightweight (<100KB compressed payload) - never contains full chapter bodies.
 */
@Serializable
data class UnifiedSyncManifest(
    val version: Int = 1,
    val deviceId: String = "",
    val timestamp: Long = 0L,
    val books: List<SyncBookItem> = emptyList(),
    val progress: List<SyncProgressItem> = emptyList(),
    val categories: List<SyncCategoryItem> = emptyList(),
    val tombstones: List<SyncTombstone> = emptyList()
)

/**
 * Universal representation of a book in the sync manifest
 */
@Serializable
data class SyncBookItem(
    val globalId: String,
    val sourceId: Long,
    val key: String,
    val title: String,
    val author: String = "",
    val description: String = "",
    val genres: List<String> = emptyList(),
    val status: Long = 0L,
    val coverUrl: String = "",
    val favorite: Boolean = true,
    val lastModified: Long = 0L,
    val categories: List<String> = emptyList(),
    val downloadedChapterCount: Int = 0
)

/**
 * Universal representation of reading progress in the sync manifest
 */
@Serializable
data class SyncProgressItem(
    val bookGlobalId: String,
    val chapterKey: String,
    val chapterGlobalId: String = "",
    val progress: Float = 0f,
    val lastRead: Long = 0L,
    val lastModified: Long = 0L
)

/**
 * Universal representation of a category in the sync manifest
 */
@Serializable
data class SyncCategoryItem(
    val categoryId: Long,
    val name: String,
    val order: Int = 0,
    val lastModified: Long = 0L
)

/**
 * Universal 30-day tombstone to ensure deletions on one device cleanly
 * remove items on other devices without resurrection.
 */
@Serializable
data class SyncTombstone(
    val itemType: UniversalSyncItemType,
    val globalId: String,
    val deletedAt: Long
)

/**
 * Live UI state for sync operations across all providers
 */
@Serializable
data class UnifiedSyncState(
    val provider: SyncProviderType = SyncProviderType.NONE,
    val isSyncing: Boolean = false,
    val progress: Float = 0f,
    val currentStep: String = "",
    val lastSyncTimestamp: Long = 0L,
    val booksSyncedCount: Int = 0,
    val progressSyncedCount: Int = 0,
    val errorMessage: String? = null
)
