package ireader.plugin.api

import kotlinx.serialization.Serializable

/**
 * Plugin interface for data synchronization.
 * Supports local server sync and cloud sync services.
 * 
 * Example:
 * ```kotlin
 * class LocalSyncPlugin : SyncPlugin {
 *     override val manifest = PluginManifest(
 *         id = "com.example.local-sync",
 *         name = "Local Server Sync",
 *         type = PluginType.SYNC,
 *         permissions = listOf(PluginPermission.LOCAL_SERVER, PluginPermission.SYNC_DATA),
 *         // ... other manifest fields
 *     )
 *     
 *     override val syncType = SyncType.LOCAL_SERVER
 *     
 *     override suspend fun sync(data: SyncData): SyncResult<SyncResponse> {
 *         // Sync to local server
 *     }
 * }
 * ```
 */
interface SyncPlugin : Plugin {
    /**
     * Type of sync service.
     */
    val syncType: SyncType
    
    /**
     * Sync configuration.
     */
    val syncConfig: SyncConfig
    
    /**
     * Data types this plugin can sync.
     */
    val supportedDataTypes: List<SyncDataType>
    
    /**
     * Check connection to sync server.
     */
    suspend fun checkConnection(): SyncResult<SyncServerStatus>
    
    /**
     * Authenticate with sync server.
     */
    suspend fun authenticate(credentials: SyncCredentials): SyncResult<SyncAuthResponse>
    
    /**
     * Sync data to server.
     */
    suspend fun sync(data: SyncData): SyncResult<SyncResponse>
    
    /**
     * Pull data from server.
     */
    suspend fun pull(dataTypes: List<SyncDataType>, since: Long? = null): SyncResult<SyncData>
    
    /**
     * Push data to server.
     */
    suspend fun push(data: SyncData): SyncResult<SyncResponse>
    
    /**
     * Resolve sync conflicts.
     */
    suspend fun resolveConflicts(conflicts: List<SyncConflict>, resolution: ConflictResolution): SyncResult<SyncResponse>
    
    /**
     * Get sync status.
     */
    fun getSyncStatus(): SyncStatus
    
    /**
     * Get last sync timestamp.
     */
    fun getLastSyncTime(): Long?
    
    /**
     * Enable/disable auto sync.
     */
    fun setAutoSync(enabled: Boolean, intervalMs: Long = 3600000)
    
    /**
     * Configure server endpoint.
     */
    fun setServerEndpoint(endpoint: String)
    
    /**
     * Logout and clear sync data.
     */
    suspend fun logout(): SyncResult<Unit>
}

/**
 * Type of sync service.
 */
@Serializable
enum class SyncType {
    /** Local server (LAN, localhost) */
    LOCAL_SERVER,
    /** Cloud service (Supabase, Firebase, etc.) */
    CLOUD,
    /** WebDAV server */
    WEBDAV,
    /** Custom sync protocol */
    CUSTOM
}

/**
 * Sync configuration.
 */
@Serializable
data class SyncConfig(
    /** Default server endpoint */
    val defaultEndpoint: String,
    /** Whether custom endpoints are supported */
    val supportsCustomEndpoint: Boolean = true,
    /** Whether auto sync is supported */
    val supportsAutoSync: Boolean = true,
    /** Minimum sync interval in milliseconds */
    val minSyncIntervalMs: Long = 60000,
    /** Whether conflict resolution is supported */
    val supportsConflictResolution: Boolean = true,
    /** Whether incremental sync is supported */
    val supportsIncrementalSync: Boolean = true,
    /** Maximum data size per sync in bytes */
    val maxSyncSizeBytes: Long = 50 * 1024 * 1024,
    /** Connection timeout in milliseconds */
    val connectionTimeoutMs: Long = 30000
)

/**
 * Data types that can be synced.
 */
@Serializable
enum class SyncDataType {
    /** Reading progress */
    READING_PROGRESS,
    /** Library (books, manga) */
    LIBRARY,
    /** Bookmarks */
    BOOKMARKS,
    /** Reading history */
    HISTORY,
    /** App settings */
    SETTINGS,
    /** Categories/collections */
    CATEGORIES,
    /** Custom sources */
    SOURCES,
    /** Themes */
    THEMES,
    /** Glossaries */
    GLOSSARIES,
    /** Character database */
    CHARACTERS,
    /** Notes and annotations */
    NOTES
}

/**
 * Sync credentials.
 */
@Serializable
data class SyncCredentials(
    /** Username or email */
    val username: String? = null,
    /** Password */
    val password: String? = null,
    /** API key */
    val apiKey: String? = null,
    /** OAuth token */
    val oauthToken: String? = null,
    /** Server endpoint */
    val endpoint: String? = null
)

/**
 * Sync authentication response.
 */
@Serializable
data class SyncAuthResponse(
    /** Whether authentication succeeded */
    val success: Boolean,
    /** User ID */
    val userId: String? = null,
    /** Access token */
    val accessToken: String? = null,
    /** Refresh token */
    val refreshToken: String? = null,
    /** Token expiry timestamp */
    val expiresAt: Long? = null,
    /** User display name */
    val displayName: String? = null
)

/**
 * Data to sync.
 */
@Serializable
data class SyncData(
    /** Reading progress entries */
    val readingProgress: List<ReadingProgressEntry> = emptyList(),
    /** Library entries */
    val libraryEntries: List<LibraryEntry> = emptyList(),
    /** Bookmarks */
    val bookmarks: List<BookmarkEntry> = emptyList(),
    /** History entries */
    val historyEntries: List<HistoryEntry> = emptyList(),
    /** Settings */
    val settings: Map<String, String> = emptyMap(),
    /** Categories */
    val categories: List<CategoryEntry> = emptyList(),
    /** Timestamp of this sync data */
    val timestamp: Long,
    /** Device ID */
    val deviceId: String
)

@Serializable
data class ReadingProgressEntry(
    val bookId: String,
    val chapterId: String,
    val position: Int,
    val percentage: Float,
    val lastRead: Long
)

@Serializable
data class LibraryEntry(
    val id: String,
    val sourceId: String,
    val title: String,
    val coverUrl: String?,
    val author: String?,
    val status: String,
    val categoryIds: List<String>,
    val addedAt: Long,
    val lastUpdated: Long
)

@Serializable
data class BookmarkEntry(
    val id: String,
    val bookId: String,
    val chapterId: String,
    val position: Int,
    val note: String?,
    val createdAt: Long
)

@Serializable
data class HistoryEntry(
    val bookId: String,
    val chapterId: String,
    val readAt: Long
)

@Serializable
data class CategoryEntry(
    val id: String,
    val name: String,
    val order: Int
)

/**
 * Sync response.
 */
@Serializable
data class SyncResponse(
    /** Whether sync succeeded */
    val success: Boolean,
    /** Number of items synced */
    val itemsSynced: Int,
    /** Conflicts detected */
    val conflicts: List<SyncConflict> = emptyList(),
    /** Server timestamp */
    val serverTimestamp: Long,
    /** Message */
    val message: String? = null
)

/**
 * Sync conflict.
 */
@Serializable
data class SyncConflict(
    /** Conflict ID */
    val id: String,
    /** Data type */
    val dataType: SyncDataType,
    /** Item ID */
    val itemId: String,
    /** Local value (JSON) */
    val localValue: String,
    /** Server value (JSON) */
    val serverValue: String,
    /** Local timestamp */
    val localTimestamp: Long,
    /** Server timestamp */
    val serverTimestamp: Long
)

/**
 * Conflict resolution strategy.
 */
@Serializable
enum class ConflictResolution {
    /** Use local version */
    USE_LOCAL,
    /** Use server version */
    USE_SERVER,
    /** Use newer version */
    USE_NEWER,
    /** Merge (if possible) */
    MERGE
}

/**
 * Sync status.
 */
@Serializable
data class SyncStatus(
    /** Whether sync is in progress */
    val isSyncing: Boolean,
    /** Current sync progress (0-100) */
    val progress: Float,
    /** Current status message */
    val statusMessage: String,
    /** Last sync timestamp */
    val lastSyncTime: Long?,
    /** Whether auto sync is enabled */
    val autoSyncEnabled: Boolean,
    /** Pending changes count */
    val pendingChanges: Int
)

/**
 * Sync server status.
 */
@Serializable
data class SyncServerStatus(
    /** Whether server is online */
    val isOnline: Boolean,
    /** Server endpoint */
    val endpoint: String,
    /** Server version */
    val serverVersion: String? = null,
    /** Whether authentication is required */
    val authRequired: Boolean,
    /** Supported data types */
    val supportedDataTypes: List<SyncDataType> = emptyList(),
    /** Server latency in milliseconds */
    val latencyMs: Long? = null
)

/**
 * Result wrapper for sync operations.
 */
sealed class SyncResult<out T> {
    data class Success<T>(val data: T) : SyncResult<T>()
    data class Error(val error: SyncError) : SyncResult<Nothing>()
    
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }
    
    inline fun <R> map(transform: (T) -> R): SyncResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }
}

/**
 * Sync errors.
 */
@Serializable
sealed class SyncError {
    data class ConnectionFailed(val endpoint: String, val reason: String) : SyncError()
    data class AuthenticationFailed(val reason: String) : SyncError()
    data class Unauthorized(val message: String) : SyncError()
    data class ConflictDetected(val conflicts: List<SyncConflict>) : SyncError()
    data class DataTooLarge(val maxSizeBytes: Long, val actualSizeBytes: Long) : SyncError()
    data class ServerError(val statusCode: Int, val message: String) : SyncError()
    data class Timeout(val timeoutMs: Long) : SyncError()
    data object Cancelled : SyncError()
    data class Unknown(val message: String) : SyncError()
}
