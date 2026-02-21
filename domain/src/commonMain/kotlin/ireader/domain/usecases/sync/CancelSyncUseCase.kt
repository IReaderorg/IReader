package ireader.domain.usecases.sync

import ireader.domain.repositories.SyncRepository

/**
 * Use case for cancelling an ongoing sync operation.
 *
 * @property syncRepository Repository for sync operations
 */
class CancelSyncUseCase(
    private val syncRepository: SyncRepository
) {
    /**
     * Cancel the current sync operation.
     *
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(): Result<Unit> {
        return syncRepository.cancelSync()
    }
}
