package ireader.domain.models.remote

import kotlinx.serialization.Serializable

/**
 * Domain model for badges
 */
@Serializable
data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val category: BadgeCategory,
    val rarity: BadgeRarity,
    val createdAt: Long
)

@Serializable
enum class BadgeCategory {
    DONOR,
    CONTRIBUTOR,
    READER,
    REVIEWER,
    SPECIAL
}

@Serializable
enum class BadgeRarity {
    COMMON,
    RARE,
    EPIC,
    LEGENDARY
}
