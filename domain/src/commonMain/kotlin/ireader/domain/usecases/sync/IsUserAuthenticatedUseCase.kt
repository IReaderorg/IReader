package ireader.domain.usecases.sync

import ireader.domain.data.repository.RemoteRepository
import ireader.core.log.Log

/**
 * Use case to check if user is authenticated
 * Used to determine if sync features should be available
 */
class IsUserAuthenticatedUseCase(
    private val remoteRepository: RemoteRepository
) {
    
    suspend operator fun invoke(): Boolean {
        return try {
            val user = remoteRepository.getCurrentUser().getOrNull()
            user != null
        } catch (e: Exception) {
            Log.error(e, "Failed to check authentication status")
            false
        }
    }
}
