package ireader.data.repository

import ireader.domain.data.repository.BadgeRepository
import ireader.domain.models.remote.Badge
import ireader.domain.models.remote.PaymentProof
import ireader.domain.models.remote.UserBadge
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * No-op implementation of BadgeRepository used when Supabase is not configured.
 * Returns empty results and failures with descriptive messages.
 */
class NoOpBadgeRepository : BadgeRepository {
    
    private val unavailableMessage = "Badges require Supabase configuration. " +
            "Please configure Supabase credentials in Settings → Supabase Configuration."
    
    override suspend fun getUserBadges(userId: String?): Result<List<UserBadge>> {
        return Result.success(emptyList())
    }
    
    override fun observeUserBadges(userId: String?): Flow<List<UserBadge>> {
        return flowOf(emptyList())
    }
    
    override suspend fun getAvailableBadges(): Result<List<Badge>> {
        return Result.success(emptyList())
    }
    
    override suspend fun submitPaymentProof(
        badgeId: String,
        proof: PaymentProof
    ): Result<Unit> {
        return Result.failure(UnsupportedOperationException(unavailableMessage))
    }
    
    override suspend fun setPrimaryBadge(badgeId: String): Result<Unit> {
        return Result.failure(UnsupportedOperationException(unavailableMessage))
    }
    
    override suspend fun setFeaturedBadges(badgeIds: List<String>): Result<Unit> {
        return Result.failure(UnsupportedOperationException(unavailableMessage))
    }
    
    override suspend fun getPrimaryBadge(): Result<Badge?> {
        return Result.success(null)
    }
    
    override suspend fun getFeaturedBadges(): Result<List<Badge>> {
        return Result.success(emptyList())
    }
    
    override suspend fun getPaymentProofsByStatus(
        status: ireader.domain.models.remote.PaymentProofStatus
    ): Result<List<PaymentProof>> {
        return Result.success(emptyList())
    }
    
    override suspend fun updatePaymentProofStatus(
        proofId: String,
        status: ireader.domain.models.remote.PaymentProofStatus,
        adminUserId: String
    ): Result<Unit> {
        return Result.failure(UnsupportedOperationException(unavailableMessage))
    }
}
