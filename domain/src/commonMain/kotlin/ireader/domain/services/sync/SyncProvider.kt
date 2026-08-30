package ireader.domain.services.sync

import ireader.domain.models.sync.SyncProgressItem
import ireader.domain.models.sync.SyncProviderType
import ireader.domain.models.sync.UnifiedSyncManifest

/**
 * Common interface for all sync providers (Google Drive, Supabase, Local Wi-Fi, etc.)
 */
interface SyncProvider {
    /**
     * Unique identifier for the provider type
     */
    val type: SyncProviderType

    /**
     * User-facing display name of the provider
     */
    val name: String

    /**
     * Check if the user is authenticated and ready to sync with this provider
     */
    suspend fun isAuthenticated(): Boolean

    /**
     * Download the latest remote sync manifest
     */
    suspend fun fetchRemoteManifest(): Result<UnifiedSyncManifest?>

    /**
     * Upload the merged sync manifest to remote storage
     */
    suspend fun uploadManifest(manifest: UnifiedSyncManifest): Result<Unit>

    /**
     * Push a realtime single reading progress update (if supported by provider, e.g. Supabase)
     */
    suspend fun pushProgress(progress: SyncProgressItem): Result<Unit> {
        return Result.success(Unit)
    }
}
