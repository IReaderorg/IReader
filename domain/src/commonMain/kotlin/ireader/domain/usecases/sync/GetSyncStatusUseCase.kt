package ireader.domain.usecases.sync

import ireader.domain.models.sync.SyncStatus
import ireader.domain.repositories.SyncRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for observing the current sync status.
 *
 * @property syncRepository Repository for sync operations
 */
class GetSyncStatusUseCase(
    private val syncRepository: SyncRepository
) {
    /**
     * Observe the current sync status.
     *
     * @return Flow of sync status updates
     */
    operator fun invoke(): Flow<SyncStatus> {
        return syncRepository.observeSyncStatus()
    }
}
