package ireader.domain.data.repository

import ireader.domain.models.remote.Badge
import ireader.domain.models.remote.PaymentProof
import ireader.domain.models.remote.UserBadge
import kotlinx.coroutines.flow.Flow

interface BadgeRepository {
    // Existing methods
    suspend fun getUserBadges(userId: String? = null): Result<List<UserBadge>>
    fun observeUserBadges(userId: String? = null): Flow<List<UserBadge>>
    
    // New methods for monetization system
    suspend fun getAvailableBadges(): Result<List<Badge>>
    suspend fun submitPaymentProof(badgeId: String, proof: PaymentProof): Result<Unit>
    suspend fun setPrimaryBadge(badgeId: String): Result<Unit>
    suspend fun setFeaturedBadges(badgeIds: List<String>): Result<Unit>
    suspend fun getPrimaryBadge(): Result<Badge?>
    suspend fun getFeaturedBadges(): Result<List<Badge>>
}
