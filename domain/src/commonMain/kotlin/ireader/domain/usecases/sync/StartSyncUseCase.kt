package ireader.domain.usecases.sync

import ireader.domain.repositories.SyncRepository

/**
 * Use case for starting the sync service.
 * This begins device discovery on the local network.
 *
 * @property syncRepository Repository for sync operations
 */
class StartSyncUseCase(
    private val syncRepository: SyncRepository
) {
    /**
     * Start the sync service and begin discovering devices.
     *
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(): Result<Unit> {
        return syncRepository.startDiscovery()
    }
}
