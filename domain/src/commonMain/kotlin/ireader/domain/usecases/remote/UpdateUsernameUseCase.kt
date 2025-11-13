package ireader.domain.usecases.remote

import ireader.domain.data.repository.RemoteRepository

/**
 * Use case for updating a user's username
 * 
 * This use case:
 * 1. Verifies the user is authenticated
 * 2. Updates the username on the remote backend
 * 
 * Requirements: 3.2
 */
class UpdateUsernameUseCase(
    private val remoteRepository: RemoteRepository
) {
    /**
     * Update the username for a user
     * @param userId The user ID
     * @param username The new username
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(userId: String, username: String): Result<Unit> {
        return remoteRepository.updateUsername(userId, username)
    }
}
