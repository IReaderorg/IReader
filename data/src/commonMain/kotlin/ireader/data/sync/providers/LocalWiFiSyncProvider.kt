package ireader.data.sync.providers

import ireader.domain.models.sync.SyncProviderType
import ireader.domain.models.sync.UnifiedSyncManifest
import ireader.domain.repositories.SyncRepository
import ireader.domain.services.sync.SyncProvider

/**
 * Local Wi-Fi P2P implementation of SyncProvider.
 * Direct device-to-device synchronization over local socket connections.
 */
class LocalWiFiSyncProvider(
    private val syncRepository: SyncRepository
) : SyncProvider {

    override val type: SyncProviderType = SyncProviderType.LOCAL_WIFI
    override val name: String = "Local Wi-Fi"

    override suspend fun isAuthenticated(): Boolean {
        return true // P2P network sync is local and does not require cloud login
    }

    override suspend fun fetchRemoteManifest(): Result<UnifiedSyncManifest?> {
        return Result.success(null) // Manifest exchange is handled interactively via P2P sockets
    }

    override suspend fun uploadManifest(manifest: UnifiedSyncManifest): Result<Unit> {
        return Result.success(Unit)
    }
}
