package ireader.domain.models.sync

/**
 * Exception thrown during sync operations.
 *
 * @property errorType Type of error that occurred
 * @param message Detailed error message
 * @param cause Optional underlying cause of the exception
 *
 * @throws IllegalArgumentException if message is blank
 */
class SyncException(
    val errorType: SyncErrorType,
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {
    init {
        require(message.isNotBlank()) { "Error message cannot be empty or blank" }
    }
}

/**
 * Types of errors that can occur during sync operations.
 */
enum class SyncErrorType {
    /** Network connectivity error */
    NETWORK_ERROR,
    
    /** Operation timed out */
    TIMEOUT,
    
    /** Device authentication failed */
    AUTHENTICATION_FAILED,
    
    /** Too many failed authentication attempts */
    TOO_MANY_ATTEMPTS,
    
    /** Device certificate doesn't match expected value */
    CERTIFICATE_MISMATCH,
    
    /** Device trust has expired */
    TRUST_EXPIRED,
    
    /** Security policy violation */
    SECURITY_VIOLATION,
    
    /** Data transfer failed */
    TRANSFER_FAILED,
    
    /** Device is not paired */
    NOT_PAIRED,
    
    /** Device not found on network */
    DEVICE_NOT_FOUND,
    
    /** Unknown or unexpected error */
    UNKNOWN
}
