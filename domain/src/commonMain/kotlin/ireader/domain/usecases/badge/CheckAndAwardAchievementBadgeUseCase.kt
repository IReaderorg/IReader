package ireader.domain.usecases.badge

import ireader.domain.data.repository.BadgeRepository

/**
 * Use case to check if a user qualifies for an achievement badge and award it
 */
class CheckAndAwardAchievementBadgeUseCase(
    private val badgeRepository: BadgeRepository
) {
    suspend operator fun invoke(badgeId: String): Result<Boolean> {
        return badgeRepository.checkAndAwardAchievementBadge(badgeId)
    }
}
