package ireader.domain.models.sync

/**
 * Exception thrown during sync operations.
 */
class SyncException(
    val errorType: SyncErrorType,
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

enum class SyncErrorType {
    NETWORK_ERROR,
    TIMEOUT,
    AUTHENTICATION_FAILED,
    TOO_MANY_ATTEMPTS,
    CERTIFICATE_MISMATCH,
    TRUST_EXPIRED,
    SECURITY_VIOLATION,
    TRANSFER_FAILED,
    NOT_PAIRED,
    DEVICE_NOT_FOUND,
    UNKNOWN
}
