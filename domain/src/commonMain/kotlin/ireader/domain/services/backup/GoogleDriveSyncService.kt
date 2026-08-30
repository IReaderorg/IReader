package ireader.domain.services.backup

import ireader.domain.models.sync.CloudSyncResult
import ireader.domain.models.sync.SyncStatus
import kotlinx.coroutines.flow.StateFlow

/**
 * Service for delta-based cloud synchronization with Google Drive.
 * 
 * Synchronizes books, chapters reading progress, and deletion tombstones
 * via lightweight JSON manifests instead of transferring large 50MB+ archive files.
 */
interface GoogleDriveSyncService {
    /**
     * Observable sync status
     */
    val syncStatus: StateFlow<SyncStatus>

    /**
     * Perform a delta sync against Google Drive.
     * 
     * @return Result containing synchronization statistics on success
     */
    suspend fun sync(): Result<CloudSyncResult>

    /**
     * Cancel an in-progress sync operation.
     */
    suspend fun cancel()

    /**
     * Check if currently authenticated with Google Drive.
     */
    suspend fun isAuthenticated(): Boolean
}
