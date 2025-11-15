package ireader.domain.plugins

/**
 * Sealed class representing payment errors
 * Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 9.1, 9.2, 9.3, 9.4, 9.5
 */
sealed class PaymentError : Exception() {
    object NetworkError : PaymentError()
    object PaymentCancelled : PaymentError()
    object PaymentFailed : PaymentError()
    object AlreadyPurchased : PaymentError()
    data class ServerError(val code: Int) : PaymentError()
}

/**
 * Convert PaymentError to user-friendly message
 */
fun PaymentError.toUserMessage(): String = when (this) {
    is PaymentError.NetworkError -> "Network error. Please check your connection."
    is PaymentError.PaymentCancelled -> "Payment cancelled"
    is PaymentError.PaymentFailed -> "Payment failed. Please try again."
    is PaymentError.AlreadyPurchased -> "You already own this plugin"
    is PaymentError.ServerError -> "Server error ($code). Please try again later."
}
