package ireader.domain.usecases.remote

import ireader.domain.data.repository.RemoteRepository
import ireader.domain.models.remote.User

/**
 * Use case for getting the currently authenticated user
 * 
 * Requirements: 2.4
 */
class GetCurrentUserUseCase(
    private val remoteRepository: RemoteRepository
) {
    /**
     * Get the currently authenticated user
     * @return Result containing the User or null if not authenticated
     */
    suspend operator fun invoke(): Result<User?> {
        return remoteRepository.getCurrentUser()
    }
}
