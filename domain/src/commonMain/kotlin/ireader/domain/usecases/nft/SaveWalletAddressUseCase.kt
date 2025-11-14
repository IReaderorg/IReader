package ireader.domain.usecases.nft

import ireader.domain.data.repository.NFTRepository
import ireader.domain.models.remote.BadgeError

class SaveWalletAddressUseCase(
    private val nftRepository: NFTRepository
) {
    suspend operator fun invoke(address: String): Result<Unit> {
        // Validate Ethereum address format
        val ethereumAddressRegex = Regex("^0x[a-fA-F0-9]{40}$")
        if (!ethereumAddressRegex.matches(address)) {
            return Result.failure(Exception("InvalidWalletAddress"))
        }
        
        // Call repository to save wallet address
        return nftRepository.saveWalletAddress(address)
    }
}
