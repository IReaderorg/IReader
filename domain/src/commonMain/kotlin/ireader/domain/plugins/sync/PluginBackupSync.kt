package ireader.domain.plugins.sync

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Plugin Backup/Sync System
 * 
 * Features:
 * - Sync installed plugins across devices
 * - Backup and restore plugin settings
 * - Conflict resolution
 * - Selective sync
 */

/**
 * Plugin backup data.
 */
@Serializable
data class PluginBackup(
    val id: String,
    val createdAt: Long,
    val deviceId: String,
    val deviceName: String,
    val plugins: List<PluginBackupEntry>,
    val settings: Map<String, PluginSettingsBackup>,
    val pipelines: List<PipelineBackupEntry>,
    val collections: List<CollectionBackupEntry>,
    val version: Int = BACKUP_VERSION
) {
    companion object {
        const val BACKUP_VERSION = 1
    }
}

/**
 * Backup entry for a single plugin.
 */
@Serializable
data class PluginBackupEntry(
    val pluginId: String,
    val pluginName: String,
    val version: String,
    val versionCode: Int,
    val repositoryUrl: String?,
    val isEnabled: Boolean,
    val installedAt: Long
)

/**
 * Backup of plugin settings.
 */
@Serializable
data class PluginSettingsBackup(
    val pluginId: String,
    val settings: Map<String, String>,
    val lastModified: Long
)

/**
 * Backup entry for a pipeline.
 */
@Serializable
data class PipelineBackupEntry(
    val pipelineId: String,
    val pipelineName: String,
    val definition: String, // JSON serialized
    val createdAt: Long
)

/**
 * Backup entry for a collection.
 */
@Serializable
data class CollectionBackupEntry(
    val collectionId: String,
    val collectionName: String,
    val pluginIds: List<String>,
    val isPublic: Boolean,
    val createdAt: Long
)

/**
 * Sync status for a device.
 */
@Serializable
data class DeviceSyncStatus(
    val deviceId: String,
    val deviceName: String,
    val lastSyncTime: Long?,
    val syncState: SyncState,
    val pendingChanges: Int,
    val conflictCount: Int
)

@Serializable
enum class SyncState {
    IDLE,
    SYNCING,
    UPLOADING,
    DOWNLOADING,
    RESOLVING_CONFLICTS,
    ERROR,
    OFFLINE
}

/**
 * Sync conflict information.
 */
@Serializable
data class SyncConflict(
    val id: String,
    val pluginId: String,
    val conflictType: ConflictType,
    val localValue: String,
    val remoteValue: String,
    val localTimestamp: Long,
    val remoteTimestamp: Long,
    val deviceId: String
)

@Serializable
enum class ConflictType {
    SETTING_CHANGED,
    PLUGIN_VERSION_MISMATCH,
    PLUGIN_ENABLED_STATE,
    PIPELINE_MODIFIED,
    COLLECTION_MODIFIED
}

/**
 * Resolution strategy for conflicts.
 */
@Serializable
enum class ConflictResolution {
    USE_LOCAL,
    USE_REMOTE,
    USE_NEWEST,
    MERGE,
    MANUAL
}

/**
 * Sync configuration.
 */
@Serializable
data class SyncConfig(
    val enabled: Boolean = true,
    val autoSync: Boolean = true,
    val syncIntervalMinutes: Int = 30,
    val syncOnWifiOnly: Boolean = false,
    val syncPlugins: Boolean = true,
    val syncSettings: Boolean = true,
    val syncPipelines: Boolean = true,
    val syncCollections: Boolean = true,
    val conflictResolution: ConflictResolution = ConflictResolution.USE_NEWEST,
    val excludedPlugins: List<String> = emptyList()
)

/**
 * Sync operation result.
 */
@Serializable
data class SyncResult(
    val success: Boolean,
    val syncedPlugins: Int,
    val syncedSettings: Int,
    val syncedPipelines: Int,
    val syncedCollections: Int,
    val conflicts: List<SyncConflict>,
    val errors: List<SyncError>,
    val duration: Long,
    val timestamp: Long
)

@Serializable
data class SyncError(
    val pluginId: String?,
    val errorType: String,
    val message: String
)

/**
 * Change tracking for sync.
 */
@Serializable
data class SyncChange(
    val id: String,
    val changeType: ChangeType,
    val entityType: EntityType,
    val entityId: String,
    val oldValue: String?,
    val newValue: String?,
    val timestamp: Long,
    val synced: Boolean = false
)

@Serializable
enum class ChangeType {
    CREATED,
    UPDATED,
    DELETED
}

@Serializable
enum class EntityType {
    PLUGIN,
    SETTING,
    PIPELINE,
    COLLECTION
}

/**
 * Sync event for progress tracking.
 */
sealed class SyncEvent {
    data class Started(val totalItems: Int) : SyncEvent()
    data class Progress(val current: Int, val total: Int, val currentItem: String) : SyncEvent()
    data class ConflictDetected(val conflict: SyncConflict) : SyncEvent()
    data class ConflictResolved(val conflictId: String, val resolution: ConflictResolution) : SyncEvent()
    data class ItemSynced(val entityType: EntityType, val entityId: String) : SyncEvent()
    data class Error(val error: SyncError) : SyncEvent()
    data class Completed(val result: SyncResult) : SyncEvent()
}
