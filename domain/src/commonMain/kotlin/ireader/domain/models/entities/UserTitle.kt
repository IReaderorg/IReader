package ireader.domain.models.entities

/**
 * Represents a user title with a passive effect.
 * Titles can be earned through achievements or purchased from the shop.
 * Effects are limited-duration to encourage active engagement.
 */
data class UserTitle(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val rarity: TitleRarity,
    val effect: TitleEffect,
    val isEarned: Boolean = false,
    val earnedAt: Long? = null
)

enum class TitleRarity {
    COMMON,
    RARE,
    EPIC,
    LEGENDARY
}

/**
 * Sealed class representing different types of title effects.
 */
sealed class TitleEffect {
    abstract val multiplier: Double
    abstract val durationHours: Long

    /**
     * Bonus XP from reading time.
     */
    data class ReadingTimeBonus(
        override val multiplier: Double = 2.0,
        override val durationHours: Long = 24
    ) : TitleEffect()

    /**
     * Bonus XP from reading chapters.
     */
    data class ChapterBonus(
        override val multiplier: Double = 2.0,
        override val durationHours: Long = 12
    ) : TitleEffect()

    /**
     * Bonus XP from completing books.
     */
    data class BookCompletionBonus(
        override val multiplier: Double = 3.0,
        override val durationHours: Long = 24
    ) : TitleEffect()

    /**
     * Bonus to streak XP.
     */
    data class StreakBonus(
        override val multiplier: Double = 2.0,
        override val durationHours: Long = 48
    ) : TitleEffect()

    /**
     * Time-restricted bonus (e.g., only during night hours).
     */
    data class TimeRestrictedBonus(
        override val multiplier: Double = 3.0,
        override val durationHours: Long = 168, // 7 days
        val startHour: Int = 22,
        val endHour: Int = 6
    ) : TitleEffect()
}

/**
 * Represents an active title with expiration time.
 */
data class ActiveTitle(
    val title: UserTitle,
    val activatedAt: Long,
    val expiresAt: Long,
    val remainingHours: Long
) {
    val isActive: Boolean
        get() = System.currentTimeMillis() < expiresAt

    val isTimeRestricted: Boolean
        get() = title.effect is TitleEffect.TimeRestrictedBonus

    fun isCurrentlyEffective(): Boolean {
        if (!isActive) return false
        if (!isTimeRestricted) return true

        val effect = title.effect as TitleEffect.TimeRestrictedBonus
        val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return if (effect.startHour > effect.endHour) {
            // Wraps midnight (e.g., 22:00 - 06:00)
            currentHour >= effect.startHour || currentHour < effect.endHour
        } else {
            currentHour in effect.startHour until effect.endHour
        }
    }
}
