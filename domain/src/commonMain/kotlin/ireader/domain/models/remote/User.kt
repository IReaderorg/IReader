package ireader.domain.models.remote

/**
 * Domain model representing an authenticated user
 */
data class User(
    val id: String,
    val email: String,
    val username: String?,
    val ethWalletAddress: String?,
    val createdAt: Long,
    val isSupporter: Boolean
)
