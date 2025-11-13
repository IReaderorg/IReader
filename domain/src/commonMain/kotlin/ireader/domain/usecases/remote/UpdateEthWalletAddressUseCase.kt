package ireader.domain.usecases.remote

import ireader.domain.data.repository.RemoteRepository

class UpdateEthWalletAddressUseCase(
    private val remoteRepository: RemoteRepository
) {
    suspend operator fun invoke(userId: String, ethWalletAddress: String): Result<Unit> {
        return remoteRepository.updateEthWalletAddress(userId, ethWalletAddress)
    }
}
