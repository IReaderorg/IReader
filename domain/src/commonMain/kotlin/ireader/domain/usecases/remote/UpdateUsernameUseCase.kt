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
     * Update the username for the current user
     * @param username The new username
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(username: String): Result<Unit> {
        return try {
            // Get current authenticated user
            val user = remoteRepository.getCurrentUser().getOrNull()
                ?: return Result.failure(Exception("User not authenticated"))
            
            // Update username
            remoteRepository.updateUsername(user.walletAddress, username)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
