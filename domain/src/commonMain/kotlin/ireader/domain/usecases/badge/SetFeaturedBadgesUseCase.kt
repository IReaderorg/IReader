package ireader.domain.usecases.badge

import ireader.domain.data.repository.BadgeRepository

class SetFeaturedBadgesUseCase(
    private val badgeRepository: BadgeRepository
) {
    suspend operator fun invoke(badgeIds: List<String>): Result<Unit> {
        // Validate max 3 badges selected
        if (badgeIds.size > 3) {
            return Result.failure(
                IllegalArgumentException("You can only select up to 3 featured badges")
            )
        }
        
        // Validate user owns all badges by fetching their badges
        val userBadgesResult = badgeRepository.getUserBadges()
        
        return userBadgesResult.fold(
            onSuccess = { userBadges ->
                val ownedBadgeIds = userBadges.map { it.badgeId }.toSet()
                
                // Check if user owns all selected badges
                val ownsAllBadges = badgeIds.all { it in ownedBadgeIds }
                
                if (!ownsAllBadges) {
                    Result.failure(
                        IllegalArgumentException("You don't own all selected badges")
                    )
                } else {
                    // User owns all badges, proceed to set them as featured
                    badgeRepository.setFeaturedBadges(badgeIds)
                }
            },
            onFailure = { error ->
                // Return the error from getUserBadges
                Result.failure(error)
            }
        )
    }
}
