package ireader.domain.usecases.admin

import ireader.domain.data.repository.BadgeRepository
import ireader.domain.models.remote.PaymentProof
import ireader.domain.models.remote.PaymentProofStatus

class GetPendingPaymentProofsUseCase(
    private val badgeRepository: BadgeRepository
) {
    suspend operator fun invoke(): Result<List<PaymentProof>> {
        return badgeRepository.getPaymentProofsByStatus(PaymentProofStatus.PENDING)
    }
}
