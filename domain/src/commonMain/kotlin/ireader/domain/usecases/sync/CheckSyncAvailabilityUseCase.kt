package ireader.domain.usecases.sync

import ireader.domain.data.repository.RemoteRepository

/**
 * Use case for checking if sync is available
 */
class CheckSyncAvailabilityUseCase(
    private val remoteRepository: RemoteRepository?
) {
    /**
     * Check if sync functionality is available
     */
    operator fun invoke(): Boolean {
        return remoteRepository != null
    }
    
    /**
     * Check if user is authenticated for sync
     */
    suspend fun isAuthenticated(): Boolean {
        return remoteRepository?.getCurrentUser()?.getOrNull() != null
    }
}
