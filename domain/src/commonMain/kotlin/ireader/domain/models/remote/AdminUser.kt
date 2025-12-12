package ireader.domain.models.remote

import kotlinx.serialization.Serializable

/**
 * Domain model representing a user for admin management
 */
@Serializable
data class AdminUser(
    val id: String,
    val email: String,
    val username: String?,
    val createdAt: String,
    val isAdmin: Boolean = false,
    val isSupporter: Boolean = false,
    val badges: List<String> = emptyList()
)

/**
 * Request to assign a badge to a user
 */
@Serializable
data class AssignBadgeRequest(
    val userId: String,
    val badgeId: String
)

/**
 * Request to reset a user's password
 */
@Serializable
data class ResetPasswordRequest(
    val userId: String,
    val email: String
)
