package ireader.domain.usecases.badge

import ireader.domain.data.repository.BadgeRepository
import ireader.domain.models.remote.Badge
import ireader.domain.models.remote.BadgeRarity
import ireader.domain.models.remote.BadgeType

class GetAvailableBadgesUseCase(
    private val badgeRepository: BadgeRepository
) {
    suspend operator fun invoke(): Result<List<Badge>> {
        return badgeRepository.getAvailableBadges()
            .map { badges ->
                badges
                    // Only show PURCHASABLE badges in the store
                    .filter { it.type == BadgeType.PURCHASABLE }
                    // Sort by rarity (LEGENDARY > EPIC > RARE > COMMON)
                    .sortedWith(
                        compareByDescending<Badge> { it.badgeRarity.ordinal }
                    )
            }
    }
}
