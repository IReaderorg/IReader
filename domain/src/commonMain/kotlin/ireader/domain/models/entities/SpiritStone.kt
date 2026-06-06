package ireader.domain.models.entities

/**
 * Represents a user's Spirit Stone balance and transaction history.
 * Spirit Stones are the in-app virtual currency earned through reading.
 * 
 * Earning rates:
 * - 1 Spirit Stone per 30 minutes of reading
 * - 5 Spirit Stones per book completed
 * - 10 Spirit Stones per 7-day streak milestone
 * - 3 Spirit Stones per chapter read
 * - 1 Spirit Stone per daily login
 */
data class SpiritStoneBalance(
    val userId: String,
    val balance: Long,
    val totalEarned: Long,
    val totalSpent: Long,
    val lastUpdated: Long
)

data class SpiritStoneTransaction(
    val id: String,
    val userId: String,
    val amount: Long,           // positive = earned, negative = spent
    val type: TransactionType,
    val description: String,
    val timestamp: Long
)

enum class TransactionType {
    READING_REWARD,
    BOOK_COMPLETED,
    STREAK_MILESTONE,
    CHAPTER_READ,
    DAILY_LOGIN,
    SHOP_PURCHASE,
    ACHIEVEMENT_REWARD
}

/**
 * Represents an item available in the Spirit Stone shop.
 */
data class ShopItem(
    val id: String,
    val name: String,
    val description: String,
    val type: ShopItemType,
    val price: Long,            // in Spirit Stones
    val icon: String,
    val previewUrl: String? = null,
    val isLimited: Boolean = false,
    val availableUntil: Long? = null
)

enum class ShopItemType {
    PROFILE_BADGE,
    PROFILE_FRAME,
    PROFILE_BACKGROUND,
    PROFILE_TITLE,
    SPECIAL_BADGE,
    ANIMATED_EFFECT
}

/**
 * Represents a user's inventory of purchased items.
 */
data class UserInventory(
    val userId: String,
    val ownedItems: List<OwnedItem>,
    val activeFrame: String? = null,
    val activeBackground: String? = null,
    val activeTitle: String? = null
)

data class OwnedItem(
    val itemId: String,
    val item: ShopItem,
    val purchasedAt: Long,
    val isEquipped: Boolean = false
)
