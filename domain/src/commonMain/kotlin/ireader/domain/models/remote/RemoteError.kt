package ireader.domain.models.remote

/**
 * Sealed class representing different types of remote operation errors
 */
sealed class RemoteError : Exception() {
    data class NetworkError(override val message: String) : RemoteError()
    data class AuthenticationError(override val message: String) : RemoteError()
    data class ValidationError(override val message: String) : RemoteError()
    data class ServerError(override val message: String) : RemoteError()
    data class UnknownError(override val cause: Throwable?) : RemoteError()
}
