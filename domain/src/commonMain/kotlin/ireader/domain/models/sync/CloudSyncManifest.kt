package ireader.domain.models.sync

import kotlinx.serialization.Serializable

/**
 * Lightweight manifest stored in Google Drive (sync_manifest.json)
 * enabling delta synchronization across devices without transferring 50MB+ archives.
 */
@Serializable
data class CloudSyncManifest(
    val version: Int = 1,
    val deviceId: String,
    val lastUpdated: Long,
    val books: List<CloudSyncBookItem> = emptyList(),
    val progress: List<CloudSyncProgressItem> = emptyList(),
    val tombstones: List<CloudSyncTombstone> = emptyList()
)

@Serializable
data class CloudSyncBookItem(
    val globalId: String,
    val title: String,
    val sourceId: Long,
    val key: String,
    val author: String = "",
    val coverUrl: String? = null,
    val favorite: Boolean = true,
    val lastModified: Long,
    val totalChapters: Int = 0
)

@Serializable
data class CloudSyncProgressItem(
    val bookGlobalId: String,
    val chapterGlobalId: String,
    val lastRead: Long,
    val progress: Double = 0.0,
    val lastModified: Long
)

@Serializable
data class CloudSyncTombstone(
    val globalId: String,
    val itemType: SyncItemType,
    val deletedAt: Long
)

@Serializable
data class CloudSyncResult(
    val booksSynced: Int,
    val progressSynced: Int,
    val itemsDeleted: Int,
    val durationMs: Long,
    val timestamp: Long
)
