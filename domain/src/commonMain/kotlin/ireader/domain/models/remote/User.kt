package ireader.domain.models.remote

/**
 * Domain model representing a user authenticated via Web3 wallet
 */
data class User(
    val walletAddress: String,
    val username: String?,
    val createdAt: Long,
    val isSupporter: Boolean
)
