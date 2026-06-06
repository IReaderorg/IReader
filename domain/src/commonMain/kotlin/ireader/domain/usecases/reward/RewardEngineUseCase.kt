package ireader.domain.usecases.reward

import ireader.domain.models.entities.AchievementCategory
import ireader.domain.models.entities.Reward
import ireader.domain.models.entities.RewardType
import ireader.domain.models.entities.UserAchievement
import ireader.domain.models.entities.XpEvent
import ireader.domain.models.entities.XpSource
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Core reward engine that calculates XP, levels, and achievements.
 * 
 * XP Rules:
 * - 1 XP per minute of reading
 * - 5 XP per chapter read
 * - 50 XP per book completed
 * - 10 XP per streak day milestone
 * - 5 XP per daily login
 * 
 * Level System:
 * - Each level requires 60 XP (60 minutes of reading)
 * - Level = floor(total_minutes / 60) + 1
 */
class RewardEngineUseCase {

    /**
     * Calculate XP for a reading session.
     */
    fun calculateReadingXp(minutes: Long): XpEvent {
        return XpEvent(
            source = XpSource.READING_TIME,
            amount = minutes.toInt(),
            timestamp = currentTimeToLong()
        )
    }

    /**
     * Calculate XP for reading a chapter.
     */
    fun calculateChapterXp(): XpEvent {
        return XpEvent(
            source = XpSource.CHAPTER_READ,
            amount = 5,
            timestamp = currentTimeToLong()
        )
    }

    /**
     * Calculate XP for completing a book.
     */
    fun calculateBookCompletionXp(): XpEvent {
        return XpEvent(
            source = XpSource.BOOK_COMPLETED,
            amount = 50,
            timestamp = currentTimeToLong()
        )
    }

    /**
     * Calculate XP for a streak milestone.
     */
    fun calculateStreakXp(streakDays: Int): XpEvent {
        return XpEvent(
            source = XpSource.STREAK_MILESTONE,
            amount = streakDays * 10,
            timestamp = currentTimeToLong()
        )
    }

    /**
     * Check if a new achievement should be awarded based on statistics.
     */
    fun checkAchievements(
        totalMinutes: Long,
        totalChapters: Int,
        totalBooks: Int,
        currentStreak: Int,
        existingAchievements: List<UserAchievement>
    ): List<UserAchievement> {
        val newAchievements = mutableListOf<UserAchievement>()
        val existingIds = existingAchievements.map { it.id }.toSet()

        // Reading time achievements
        val timeAchievements = listOf(
            Pair(60L, "first_hour"),
            Pair(600L, "ten_hours"),
            Pair(3600L, "bookworm_time"),
            Pair(14400L, "reading_marathon")
        )
        timeAchievements.forEach { (threshold, id) ->
            if (totalMinutes >= threshold && id !in existingIds) {
                newAchievements.add(
                    UserAchievement(
                        id = id,
                        name = getAchievementName(id),
                        description = getAchievementDescription(id),
                        icon = "⏱",
                        earnedAt = currentTimeToLong(),
                        category = AchievementCategory.READING_TIME
                    )
                )
            }
        }

        // Chapter achievements
        val chapterAchievements = listOf(
            Pair(10, "first_chapters"),
            Pair(100, "chapter_reader"),
            Pair(500, "chapter_master"),
            Pair(1000, "chapter_legend")
        )
        chapterAchievements.forEach { (threshold, id) ->
            if (totalChapters >= threshold && id !in existingIds) {
                newAchievements.add(
                    UserAchievement(
                        id = id,
                        name = getAchievementName(id),
                        description = getAchievementDescription(id),
                        icon = "📖",
                        earnedAt = currentTimeToLong(),
                        category = AchievementCategory.CHAPTERS
                    )
                )
            }
        }

        // Book achievements
        val bookAchievements = listOf(
            Pair(1, "first_book"),
            Pair(10, "book_collector"),
            Pair(50, "library_builder"),
            Pair(100, "book_master")
        )
        bookAchievements.forEach { (threshold, id) ->
            if (totalBooks >= threshold && id !in existingIds) {
                newAchievements.add(
                    UserAchievement(
                        id = id,
                        name = getAchievementName(id),
                        description = getAchievementDescription(id),
                        icon = "📚",
                        earnedAt = currentTimeToLong(),
                        category = AchievementCategory.BOOKS
                    )
                )
            }
        }

        // Streak achievements
        val streakAchievements = listOf(
            Pair(7, "week_warrior"),
            Pair(30, "month_master"),
            Pair(365, "year_legend")
        )
        streakAchievements.forEach { (threshold, id) ->
            if (currentStreak >= threshold && id !in existingIds) {
                newAchievements.add(
                    UserAchievement(
                        id = id,
                        name = getAchievementName(id),
                        description = getAchievementDescription(id),
                        icon = "🔥",
                        earnedAt = currentTimeToLong(),
                        category = AchievementCategory.STREAK
                    )
                )
            }
        }

        return newAchievements
    }

    /**
     * Create a level-up reward.
     */
    fun createLevelUpReward(newLevel: Int): Reward {
        return Reward(
            id = "level_up_$newLevel",
            name = "Level $newLevel Reached!",
            description = "You've reached level $newLevel",
            type = RewardType.LEVEL_UP,
            icon = "⭐",
            earnedAt = currentTimeToLong(),
            xpValue = 0
        )
    }

    private fun getAchievementName(id: String): String {
        return when (id) {
            "first_hour" -> "First Hour"
            "ten_hours" -> "Ten Hours"
            "bookworm_time" -> "Bookworm Time"
            "reading_marathon" -> "Reading Marathon"
            "first_chapters" -> "First Chapters"
            "chapter_reader" -> "Chapter Reader"
            "chapter_master" -> "Chapter Master"
            "chapter_legend" -> "Chapter Legend"
            "first_book" -> "First Book"
            "book_collector" -> "Book Collector"
            "library_builder" -> "Library Builder"
            "book_master" -> "Book Master"
            "week_warrior" -> "Week Warrior"
            "month_master" -> "Month Master"
            "year_legend" -> "Year Legend"
            else -> id
        }
    }

    private fun getAchievementDescription(id: String): String {
        return when (id) {
            "first_hour" -> "Read for 1 hour total"
            "ten_hours" -> "Read for 10 hours total"
            "bookworm_time" -> "Read for 100 hours total"
            "reading_marathon" -> "Read for 400 hours total"
            "first_chapters" -> "Read 10 chapters"
            "chapter_reader" -> "Read 100 chapters"
            "chapter_master" -> "Read 500 chapters"
            "chapter_legend" -> "Read 1000 chapters"
            "first_book" -> "Complete your first book"
            "book_collector" -> "Complete 10 books"
            "library_builder" -> "Complete 50 books"
            "book_master" -> "Complete 100 books"
            "week_warrior" -> "Maintain a 7-day streak"
            "month_master" -> "Maintain a 30-day streak"
            "year_legend" -> "Maintain a 365-day streak"
            else -> ""
        }
    }
}
