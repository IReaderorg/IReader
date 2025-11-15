package ireader.data.repository

import ireader.domain.data.repository.NFTRepository
import ireader.domain.models.remote.NFTVerificationResult
import ireader.domain.models.remote.NFTWallet

/**
 * No-op implementation of NFTRepository used when Supabase is not configured.
 * Returns empty results and failures with descriptive messages.
 */
class NoOpNFTRepository : NFTRepository {
    
    private val unavailableMessage = "NFT features require Supabase configuration. " +
            "Please configure Supabase credentials in Settings → Supabase Configuration."
    
    override suspend fun saveWalletAddress(address: String): Result<Unit> {
        return Result.failure(UnsupportedOperationException(unavailableMessage))
    }
    
    override suspend fun getWalletAddress(): Result<String?> {
        return Result.success(null)
    }
    
    override suspend fun verifyNFTOwnership(address: String): Result<NFTVerificationResult> {
        return Result.failure(UnsupportedOperationException(unavailableMessage))
    }
    
    override suspend fun getCachedVerification(): Result<NFTWallet?> {
        return Result.success(null)
    }
    
    override suspend fun isVerificationExpired(): Boolean {
        return true
    }
}
