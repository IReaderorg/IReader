package ireader.data.repository

import ireader.data.repository.base.NoOpRepositoryBase
import ireader.domain.data.repository.BadgeRepository
import ireader.domain.models.remote.Badge
import ireader.domain.models.remote.PaymentProof
import ireader.domain.models.remote.PaymentProofStatus
import ireader.domain.models.remote.UserBadge
import kotlinx.coroutines.flow.Flow

/**
 * No-op implementation of BadgeRepository used when Supabase is not configured.
 * Returns empty results and failures with descriptive messages.
 * 
 * Implemented as a singleton object since it is stateless.
 * @see Requirements 2.1, 2.2, 2.3, 2.4
 */
object NoOpBadgeRepository : NoOpRepositoryBase(), BadgeRepository {
    
    private const val FEATURE_NAME = "Badges"
    
    override suspend fun getUserBadges(userId: String?): Result<List<UserBadge>> =
        emptyListResult()
    
    override fun observeUserBadges(userId: String?): Flow<List<UserBadge>> =
        emptyListFlow()
    
    override suspend fun getAvailableBadges(): Result<List<Badge>> =
        emptyListResult()
    
    override suspend fun submitPaymentProof(
        badgeId: String,
        proof: PaymentProof
    ): Result<Unit> = unavailableResult(FEATURE_NAME)
    
    override suspend fun setPrimaryBadge(badgeId: String): Result<Unit> =
        unavailableResult(FEATURE_NAME)
    
    override suspend fun setFeaturedBadges(badgeIds: List<String>): Result<Unit> =
        unavailableResult(FEATURE_NAME)
    
    override suspend fun getPrimaryBadge(): Result<Badge?> =
        emptyResult()
    
    override suspend fun getFeaturedBadges(): Result<List<Badge>> =
        emptyListResult()
    
    override suspend fun checkAndAwardAchievementBadge(badgeId: String): Result<Boolean> =
        unavailableResult(FEATURE_NAME)
    
    override suspend fun getPaymentProofsByStatus(
        status: PaymentProofStatus
    ): Result<List<PaymentProof>> = emptyListResult()
    
    override suspend fun updatePaymentProofStatus(
        proofId: String,
        status: PaymentProofStatus,
        adminUserId: String
    ): Result<Unit> = unavailableResult(FEATURE_NAME)
}
