package ireader.domain.usecases.badge

import ireader.domain.data.repository.BadgeRepository
import ireader.domain.models.remote.UserBadge

class GetUserBadgesUseCase(
    private val badgeRepository: BadgeRepository
) {
    suspend operator fun invoke(userId: String? = null): Result<List<UserBadge>> {
        return badgeRepository.getUserBadges(userId)
    }
}
