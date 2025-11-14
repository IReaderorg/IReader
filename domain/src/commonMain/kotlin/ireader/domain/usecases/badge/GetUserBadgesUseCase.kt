package ireader.domain.usecases.badge

import ireader.domain.data.repository.BadgeRepository
import ireader.domain.models.remote.UserBadge

class GetUserBadgesUseCase(
    private val badgeRepository: BadgeRepository
) {
    suspend operator fun invoke(
        userId: String? = null,
        filterPrimary: Boolean = false,
        filterFeatured: Boolean = false
    ): Result<List<UserBadge>> {
        return badgeRepository.getUserBadges(userId)
            .map { badges ->
                var filteredBadges = badges
                
                // Filter by primary flag if requested
                if (filterPrimary) {
                    filteredBadges = filteredBadges.filter { it.isPrimary }
                }
                
                // Filter by featured flag if requested
                if (filterFeatured) {
                    filteredBadges = filteredBadges.filter { it.isFeatured }
                }
                
                filteredBadges
            }
    }
}
