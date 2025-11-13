package ireader.domain.data.repository

import ireader.domain.models.remote.UserBadge
import kotlinx.coroutines.flow.Flow

interface BadgeRepository {
    suspend fun getUserBadges(userId: String? = null): Result<List<UserBadge>>
    fun observeUserBadges(userId: String? = null): Flow<List<UserBadge>>
}
