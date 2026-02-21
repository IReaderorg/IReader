package ireader.domain.usecases.sync

import ireader.domain.repositories.SyncRepository

/**
 * Use case for stopping the sync service.
 * This stops device discovery and broadcasting.
 *
 * @property syncRepository Repository for sync operations
 */
class StopSyncUseCase(
    private val syncRepository: SyncRepository
) {
    /**
     * Stop the sync service.
     *
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(): Result<Unit> {
        return syncRepository.stopDiscovery()
    }
}
