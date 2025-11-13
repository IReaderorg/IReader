package ireader.domain.models.remote

import kotlinx.serialization.Serializable

@Serializable
data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val category: String,
    val rarity: String
)

@Serializable
data class UserBadge(
    val badgeId: String,
    val badgeName: String,
    val badgeDescription: String,
    val badgeIcon: String,
    val badgeCategory: String,
    val badgeRarity: String,
    val earnedAt: String,
    val metadata: Map<String, String>? = null
)
