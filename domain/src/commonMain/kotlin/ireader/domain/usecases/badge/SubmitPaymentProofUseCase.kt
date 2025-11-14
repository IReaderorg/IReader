package ireader.domain.usecases.badge

import ireader.domain.data.repository.BadgeRepository
import ireader.domain.models.remote.BadgeError
import ireader.domain.models.remote.PaymentProof

class SubmitPaymentProofUseCase(
    private val badgeRepository: BadgeRepository
) {
    suspend operator fun invoke(badgeId: String, proof: PaymentProof): Result<Unit> {
        // Validate payment proof fields
        if (proof.transactionId.isBlank()) {
            return Result.failure(
                IllegalArgumentException("Transaction ID cannot be empty")
            )
        }
        
        if (proof.paymentMethod.isBlank()) {
            return Result.failure(
                IllegalArgumentException("Payment method must be specified")
            )
        }
        
        // Call repository to submit payment proof
        return badgeRepository.submitPaymentProof(badgeId, proof)
    }
}
