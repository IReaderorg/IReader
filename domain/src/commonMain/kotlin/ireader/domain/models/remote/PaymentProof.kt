package ireader.domain.models.remote

import kotlinx.serialization.Serializable

@Serializable
data class PaymentProof(
    val id: String,
    val userId: String,
    val badgeId: String,
    val transactionId: String,
    val paymentMethod: String,
    val proofImageUrl: String?,
    val status: PaymentProofStatus,
    val submittedAt: String,
    val reviewedAt: String? = null,
    val reviewedBy: String? = null,
    // Additional fields for display
    val userEmail: String? = null,
    val username: String? = null,
    val badgeName: String? = null
)

@Serializable
enum class PaymentProofStatus {
    PENDING,
    APPROVED,
    REJECTED
}

@Serializable
data class PaymentProofWithDetails(
    val proof: PaymentProof,
    val badge: Badge,
    val user: User
)
