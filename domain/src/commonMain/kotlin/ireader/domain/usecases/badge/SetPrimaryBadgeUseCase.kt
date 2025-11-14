package ireader.domain.usecases.badge

import ireader.domain.data.repository.BadgeRepository

class SetPrimaryBadgeUseCase(
    private val badgeRepository: BadgeRepository
) {
    suspend operator fun invoke(badgeId: String): Result<Unit> {
        // Validate user owns the badge by fetching their badges
        val userBadgesResult = badgeRepository.getUserBadges()
        
        return userBadgesResult.fold(
            onSuccess = { userBadges ->
                // Check if user owns the badge
                val ownsBadge = userBadges.any { it.badgeId == badgeId }
                
                if (!ownsBadge) {
                    Result.failure(
                        IllegalArgumentException("You don't own this badge")
                    )
                } else {
                    // User owns the badge, proceed to set it as primary
                    badgeRepository.setPrimaryBadge(badgeId)
                }
            },
            onFailure = { error ->
                // Return the error from getUserBadges
                Result.failure(error)
            }
        )
    }
}
