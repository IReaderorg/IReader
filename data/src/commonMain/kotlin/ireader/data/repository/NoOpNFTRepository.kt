package ireader.data.repository

import ireader.data.repository.base.NoOpRepositoryBase
import ireader.domain.data.repository.NFTRepository
import ireader.domain.models.remote.NFTVerificationResult
import ireader.domain.models.remote.NFTWallet

/**
 * No-op implementation of NFTRepository used when Supabase is not configured.
 * Returns empty results and failures with descriptive messages.
 * 
 * Implemented as a singleton object since it is stateless.
 * @see Requirements 2.1, 2.2, 2.3, 2.4
 */
object NoOpNFTRepository : NoOpRepositoryBase(), NFTRepository {
    
    private const val FEATURE_NAME = "NFT features"
    
    override suspend fun saveWalletAddress(address: String): Result<Unit> =
        unavailableResult(FEATURE_NAME)
    
    override suspend fun getWalletAddress(): Result<String?> =
        emptyResult()
    
    override suspend fun verifyNFTOwnership(address: String): Result<NFTVerificationResult> =
        unavailableResult(FEATURE_NAME)
    
    override suspend fun getCachedVerification(): Result<NFTWallet?> =
        emptyResult()
    
    override suspend fun isVerificationExpired(): Boolean = true
}
