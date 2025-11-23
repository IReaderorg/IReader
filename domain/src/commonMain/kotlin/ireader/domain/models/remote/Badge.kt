package ireader.domain.models.remote

import kotlinx.serialization.Serializable

@Serializable
data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val category: String,
    val rarity: String,
    // New fields for monetization system
    val price: Double? = null,  // null for NFT badges
    val type: BadgeType = BadgeType.ACHIEVEMENT,
    val badgeRarity: BadgeRarity = BadgeRarity.COMMON,
    val imageUrl: String = icon,  // Default to icon for backward compatibility
    val isAvailable: Boolean = true
)

@Serializable
enum class BadgeType {
    PURCHASABLE,
    NFT_EXCLUSIVE,
    ACHIEVEMENT
}

@Serializable
enum class BadgeRarity {
    COMMON,
    RARE,
    EPIC,
    LEGENDARY
}

@Serializable
data class UserBadge(
    val badgeId: String,
    val badgeName: String,
    val badgeDescription: String,
    val badgeIcon: String,
    val badgeCategory: String,
    val badgeRarity: String,
    val earnedAt: String,
    val metadata: Map<String, String>? = null,
    // New fields for monetization system
    val isPrimary: Boolean = false,  // For review display
    val isFeatured: Boolean = false,  // For profile display (max 3)
    val acquiredAt: String = earnedAt,  // Timestamp field
    val imageUrl: String? = null,  // Badge image URL from Supabase storage
    val badgeType: String? = null  // Badge type (PURCHASABLE, NFT_EXCLUSIVE, ACHIEVEMENT)
)

@Serializable
sealed class BadgeError {
    @Serializable
    object InvalidWalletAddress : BadgeError()
    
    @Serializable
    object PaymentProofRequired : BadgeError()
    
    @Serializable
    object BadgeAlreadyOwned : BadgeError()
    
    @Serializable
    object NetworkError : BadgeError()
    
    @Serializable
    object VerificationFailed : BadgeError()
}
