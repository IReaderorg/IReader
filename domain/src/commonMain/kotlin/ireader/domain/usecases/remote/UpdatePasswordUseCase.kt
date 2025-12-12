package ireader.domain.usecases.remote

import ireader.domain.data.repository.RemoteRepository

class UpdatePasswordUseCase(
    private val remoteRepository: RemoteRepository
) {
    suspend operator fun invoke(newPassword: String): Result<Unit> {
        return remoteRepository.updatePassword(newPassword)
    }
}
