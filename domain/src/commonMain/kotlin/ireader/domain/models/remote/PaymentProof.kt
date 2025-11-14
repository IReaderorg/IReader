package ireader.domain.models.remote

import kotlinx.serialization.Serializable

@Serializable
data class PaymentProof(
    val transactionId: String,
    val paymentMethod: String,
    val timestamp: Long,
    val proofImageUrl: String? = null,
    val status: PaymentStatus = PaymentStatus.PENDING
)

@Serializable
enum class PaymentStatus {
    PENDING,
    APPROVED,
    REJECTED
}
