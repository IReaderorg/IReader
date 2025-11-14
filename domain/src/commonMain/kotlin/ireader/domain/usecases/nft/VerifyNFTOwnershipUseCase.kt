package ireader.domain.usecases.nft

import ireader.domain.data.repository.NFTRepository
import ireader.domain.models.remote.NFTVerificationResult
import kotlinx.coroutines.delay

class VerifyNFTOwnershipUseCase(
    private val nftRepository: NFTRepository
) {
    suspend operator fun invoke(address: String): Result<NFTVerificationResult> {
        var lastError: Throwable? = null
        val maxRetries = 2
        
        // Retry logic with max 2 retries
        repeat(maxRetries + 1) { attempt ->
            try {
                val result = nftRepository.verifyNFTOwnership(address)
                if (result.isSuccess) {
                    return result
                }
                lastError = result.exceptionOrNull()
            } catch (e: Exception) {
                lastError = e
            }
            
            // Don't delay after the last attempt
            if (attempt < maxRetries) {
                // Exponential backoff: 1s, 2s
                delay(1000L * (attempt + 1))
            }
        }
        
        // Return the last error after all retries exhausted
        return Result.failure(lastError ?: Exception("NFT verification failed"))
    }
}
