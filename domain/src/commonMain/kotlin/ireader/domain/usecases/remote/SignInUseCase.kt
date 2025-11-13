package ireader.domain.usecases.remote

import ireader.domain.data.repository.RemoteRepository
import ireader.domain.models.remote.User

class SignInUseCase(
    private val remoteRepository: RemoteRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        return remoteRepository.signIn(email, password)
    }
}
