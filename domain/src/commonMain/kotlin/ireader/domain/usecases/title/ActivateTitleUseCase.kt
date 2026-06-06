package ireader.domain.usecases.title

import ireader.domain.models.entities.ActiveTitle
import ireader.domain.models.entities.UserTitle
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Use case for activating a user title.
 * When activated, the title's effect starts and will expire after the duration.
 */
class ActivateTitleUseCase {

    /**
     * Activate a title for the user.
     * Returns an ActiveTitle with expiration time.
     */
    fun invoke(title: UserTitle): ActiveTitle {
        val now = currentTimeToLong()
        val durationMs = title.effect.durationHours * 60 * 60 * 1000
        val expiresAt = now + durationMs

        return ActiveTitle(
            title = title,
            activatedAt = now,
            expiresAt = expiresAt,
            remainingHours = title.effect.durationHours
        )
    }

    /**
     * Check if an active title's effect should be applied to an XP event.
     */
    fun shouldApplyEffect(activeTitle: ActiveTitle, xpSource: String): Boolean {
        if (!activeTitle.isActive) return false
        if (activeTitle.isTimeRestricted && !activeTitle.isCurrentlyEffective()) return false

        return when (activeTitle.title.effect) {
            is ireader.domain.models.entities.TitleEffect.ReadingTimeBonus ->
                xpSource == "READING_TIME"
            is ireader.domain.models.entities.TitleEffect.ChapterBonus ->
                xpSource == "CHAPTER_READ"
            is ireader.domain.models.entities.TitleEffect.BookCompletionBonus ->
                xpSource == "BOOK_COMPLETED"
            is ireader.domain.models.entities.TitleEffect.StreakBonus ->
                xpSource == "STREAK_MILESTONE"
            is ireader.domain.models.entities.TitleEffect.TimeRestrictedBonus ->
                xpSource == "READING_TIME"
        }
    }

    /**
     * Calculate the XP multiplier from an active title.
     */
    fun getXpMultiplier(activeTitle: ActiveTitle): Double {
        if (!activeTitle.isActive) return 1.0
        if (activeTitle.isTimeRestricted && !activeTitle.isCurrentlyEffective()) return 1.0
        return activeTitle.title.effect.multiplier
    }
}
