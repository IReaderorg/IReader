package ireader.domain.models.remote

import kotlinx.serialization.Serializable

/**
 * Domain model for user badges
 */
@Serializable
data class UserBadge(
    val userId: String,
    val badgeId: String,
    val earnedAt: Long,
    val metadata: Map<String, String>? = null
)
