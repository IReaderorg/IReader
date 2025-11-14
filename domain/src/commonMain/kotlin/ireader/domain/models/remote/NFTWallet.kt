package ireader.domain.models.remote

import kotlinx.serialization.Serializable

@Serializable
data class NFTWallet(
    val userId: String,
    val walletAddress: String,
    val lastVerified: Long,
    val ownsNFT: Boolean,
    val nftTokenId: String? = null,
    val cacheExpiresAt: Long
)

@Serializable
data class NFTVerificationResult(
    val ownsNFT: Boolean,
    val tokenId: String? = null,
    val verifiedAt: Long,
    val cacheExpiresAt: Long
)
