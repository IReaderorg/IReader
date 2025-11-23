package ireader.domain.models.remote

import kotlinx.serialization.Serializable

@Serializable
/**
 * Domain model representing an authenticated user
 */
data class User(
    val id: String,
    val email: String,
    val username: String?,
    val ethWalletAddress: String?,
    val createdAt: Long,
    val isSupporter: Boolean,
    val isAdmin: Boolean = false
)
