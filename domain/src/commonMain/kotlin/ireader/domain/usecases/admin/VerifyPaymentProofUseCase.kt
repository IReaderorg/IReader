package ireader.domain.usecases.admin

import ireader.domain.data.repository.BadgeRepository
import ireader.domain.models.remote.PaymentProofStatus

class VerifyPaymentProofUseCase(
    private val badgeRepository: BadgeRepository
) {
    suspend operator fun invoke(
        proofId: String,
        approve: Boolean,
        adminUserId: String
    ): Result<Unit> {
        val status = if (approve) PaymentProofStatus.APPROVED else PaymentProofStatus.REJECTED
        return badgeRepository.updatePaymentProofStatus(proofId, status, adminUserId)
    }
}
