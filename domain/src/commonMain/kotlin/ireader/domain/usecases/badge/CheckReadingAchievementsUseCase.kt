package ireader.domain.usecases.badge

import ireader.domain.data.repository.BadgeRepository

/**
 * Use case to check and award reading achievement badges based on user statistics
 */
class CheckReadingAchievementsUseCase(
    private val badgeRepository: BadgeRepository,
    private val checkAndAwardBadge: CheckAndAwardAchievementBadgeUseCase
) {
    suspend operator fun invoke(
        chaptersRead: Int,
        booksCompleted: Int,
        reviewsWritten: Int,
        readingStreak: Int
    ): Result<List<String>> {
        val awardedBadges = mutableListOf<String>()
        
        // Check chapter reading badges
        when {
            chaptersRead >= 1000 -> checkBadge("master_reader", awardedBadges)
            chaptersRead >= 500 -> checkBadge("bookworm", awardedBadges)
            chaptersRead >= 100 -> checkBadge("avid_reader", awardedBadges)
            chaptersRead >= 10 -> checkBadge("novice_reader", awardedBadges)
        }
        
        // Check book completion badges
        when {
            booksCompleted >= 100 -> checkBadge("legendary_collector", awardedBadges)
            booksCompleted >= 50 -> checkBadge("library_master", awardedBadges)
            booksCompleted >= 10 -> checkBadge("book_collector", awardedBadges)
            booksCompleted >= 1 -> checkBadge("first_finish", awardedBadges)
        }
        
        // Check review badges
        when {
            reviewsWritten >= 100 -> checkBadge("legendary_critic", awardedBadges)
            reviewsWritten >= 50 -> checkBadge("master_critic", awardedBadges)
            reviewsWritten >= 10 -> checkBadge("thoughtful_critic", awardedBadges)
            reviewsWritten >= 1 -> checkBadge("first_critic", awardedBadges)
        }
        
        // Check reading streak badges
        when {
            readingStreak >= 365 -> checkBadge("year_legend", awardedBadges)
            readingStreak >= 30 -> checkBadge("month_master", awardedBadges)
            readingStreak >= 7 -> checkBadge("week_warrior", awardedBadges)
        }
        
        return Result.success(awardedBadges)
    }
    
    private suspend fun checkBadge(badgeId: String, awardedList: MutableList<String>) {
        checkAndAwardBadge(badgeId).onSuccess { awarded ->
            if (awarded) {
                awardedList.add(badgeId)
            }
        }
    }
}
