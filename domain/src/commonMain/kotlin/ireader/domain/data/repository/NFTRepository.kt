package ireader.domain.data.repository

import ireader.domain.models.remote.NFTVerificationResult
import ireader.domain.models.remote.NFTWallet

interface NFTRepository {
    suspend fun saveWalletAddress(address: String): Result<Unit>
    suspend fun getWalletAddress(): Result<String?>
    suspend fun verifyNFTOwnership(address: String): Result<NFTVerificationResult>
    suspend fun getCachedVerification(): Result<NFTWallet?>
    suspend fun isVerificationExpired(): Boolean
}
