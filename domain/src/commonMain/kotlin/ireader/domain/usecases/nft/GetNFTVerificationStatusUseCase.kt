package ireader.domain.usecases.nft

import ireader.domain.data.repository.NFTRepository
import ireader.domain.models.remote.NFTWallet
import ireader.domain.utils.extensions.currentTimeToLong

class GetNFTVerificationStatusUseCase(
    private val nftRepository: NFTRepository
) {
    suspend operator fun invoke(): Result<NFTWallet?> {
        // Get cached verification from repository
        val cachedResult = nftRepository.getCachedVerification()
        
        if (cachedResult.isFailure) {
            return cachedResult
        }
        
        val cachedWallet = cachedResult.getOrNull()
        
        // If no cached data, return null
        if (cachedWallet == null) {
            return Result.success(null)
        }
        
        // Check if cache is expired
        val isExpired = nftRepository.isVerificationExpired()
        
        if (!isExpired) {
            // Cache is still valid, return it
            return Result.success(cachedWallet)
        }
        
        // Cache is expired, check if within 7-day grace period
        val currentTime = currentTimeToLong()
        val gracePeriodEnd = cachedWallet.cacheExpiresAt + (7 * 24 * 60 * 60 * 1000L) // 7 days in milliseconds
        
        if (currentTime <= gracePeriodEnd) {
            // Within grace period, return cached data with warning
            // The warning can be handled by the UI layer
            return Result.success(cachedWallet)
        }
        
        // Beyond grace period, needs re-verification
        return Result.success(null)
    }
}
