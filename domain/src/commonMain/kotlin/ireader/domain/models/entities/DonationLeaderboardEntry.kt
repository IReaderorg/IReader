package ireader.domain.models.entities

import kotlinx.serialization.Serializable

@Serializable
data class DonationLeaderboardEntry(
    val id: String? = null,
    val userId: String,
    val username: String,
    val totalDonationAmount: Double,
    val badgeCount: Int,
    val rank: Int = 0,
    val avatarUrl: String? = null,
    val highestBadgeRarity: String? = null,
    val badges: List<DonorBadgeInfo> = emptyList(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class DonorBadgeInfo(
    val badgeId: String,
    val badgeName: String,
    val badgeIcon: String,
    val badgeRarity: String,
    val price: Double,
    val earnedAt: Long
)
