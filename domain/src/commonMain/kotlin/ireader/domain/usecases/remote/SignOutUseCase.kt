package ireader.domain.usecases.remote

import ireader.domain.data.repository.RemoteRepository

/**
 * Use case for signing out the current user
 * 
 * Requirements: 2.4
 */
class SignOutUseCase(
    private val remoteRepository: RemoteRepository
) {
    /**
     * Sign out the current user
     */
    suspend operator fun invoke() {
        remoteRepository.signOut()
    }
}
