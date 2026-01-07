package ireader.domain.services.common

import kotlinx.coroutines.flow.StateFlow

/**
 * Common sync service for cloud synchronization
 */
interface SyncService : PlatformService {
    /**
     * Current sync state
     */
    val syncState: StateFlow<SyncState>
    
    /**
     * Last sync timestamp
     */
    val lastSyncTime: StateFlow<Long?>
    
    /**
     * Sync progress
     */
    val syncProgress: StateFlow<SyncProgress?>
    
    /**
     * Check if user is authenticated
     */
    fun isAuthenticated(): Boolean
    
    /**
     * Authenticate with sync service
     */
    suspend fun authenticate(
        provider: SyncProvider,
        credentials: Map<String, String>
    ): ServiceResult<Unit>
    
    /**
     * Sign out
     */
    suspend fun signOut(): ServiceResult<Unit>
    
    /**
     * Sync library to cloud
     */
    suspend fun syncToCloud(
        syncOptions: SyncOptions = SyncOptions()
    ): ServiceResult<SyncResult>
    
    /**
     * Sync from cloud
     */
    suspend fun syncFromCloud(
        syncOptions: SyncOptions = SyncOptions()
    ): ServiceResult<SyncResult>
    
    /**
     * Full bidirectional sync
     */
    suspend fun fullSync(
        syncOptions: SyncOptions = SyncOptions()
    ): ServiceResult<SyncResult>
    
    /**
     * Enable automatic sync
     */
    suspend fun enableAutoSync(
        intervalMinutes: Int = 30
    ): ServiceResult<Unit>
    
    /**
     * Disable automatic sync
     */
    suspend fun disableAutoSync(): ServiceResult<Unit>
    
    /**
     * Get sync conflicts
     */
    suspend fun getSyncConflicts(): ServiceResult<List<SyncConflict>>
    
    /**
     * Resolve sync conflict
     */
    suspend fun resolveConflict(
        conflictId: String,
        resolution: ConflictResolution
    ): ServiceResult<Unit>
}

/**
 * Sync provider
 */
enum class SyncProvider {
    GOOGLE_DRIVE,
    CUSTOM
}

/**
 * Sync state
 */
enum class SyncState {
    IDLE,
    SYNCING_UP,
    SYNCING_DOWN,
    SYNCING_BOTH,
    COMPLETED,
    ERROR,
    CONFLICT
}

/**
 * Sync options
 */
data class SyncOptions(
    val syncLibrary: Boolean = true,
    val syncProgress: Boolean = true,
    val syncSettings: Boolean = true,
    val syncCategories: Boolean = true,
    val conflictResolution: ConflictResolution = ConflictResolution.ASK
)

/**
 * Conflict resolution strategy
 */
enum class ConflictResolution {
    USE_LOCAL,
    USE_REMOTE,
    MERGE,
    ASK
}

/**
 * Sync progress
 */
data class SyncProgress(
    val currentStep: SyncStep,
    val progress: Float = 0f,
    val itemsProcessed: Int = 0,
    val totalItems: Int = 0,
    val message: String = ""
)

/**
 * Sync step
 */
enum class SyncStep {
    AUTHENTICATING,
    FETCHING_REMOTE,
    COMPARING,
    UPLOADING,
    DOWNLOADING,
    RESOLVING_CONFLICTS,
    FINALIZING
}

/**
 * Sync result
 */
data class SyncResult(
    val itemsUploaded: Int,
    val itemsDownloaded: Int,
    val conflicts: Int,
    val errors: List<String> = emptyList(),
    val timestamp: Long
)

/**
 * Sync conflict
 */
data class SyncConflict(
    val id: String,
    val type: ConflictType,
    val localData: Any,
    val remoteData: Any,
    val localTimestamp: Long,
    val remoteTimestamp: Long
)

/**
 * Conflict type
 */
enum class ConflictType {
    BOOK_METADATA,
    READING_PROGRESS,
    CATEGORY,
    SETTINGS
}
