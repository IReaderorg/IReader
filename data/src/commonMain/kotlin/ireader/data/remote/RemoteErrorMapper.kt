package ireader.data.remote

import ireader.domain.models.remote.RemoteError
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.exceptions.HttpRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.utils.io.errors.IOException

/**
 * Maps various exceptions to domain RemoteError types
 */
object RemoteErrorMapper {
    
    /**
     * Maps a throwable to the appropriate RemoteError type
     * 
     * @param throwable The exception to map
     * @return Corresponding RemoteError instance
     */
    fun mapError(throwable: Throwable): RemoteError {
        return when (throwable) {
            is RemoteError -> throwable
            
            is HttpRequestTimeoutException -> RemoteError.NetworkError(
                "Request timed out. Please check your connection."
            )
            
            is IOException -> RemoteError.NetworkError(
                "Network error: ${throwable.message ?: "Unable to connect"}"
            )
            
            is RestException -> {
                when (throwable.statusCode) {
                    401, 403 -> RemoteError.AuthenticationError(
                        throwable.message ?: "Authentication failed"
                    )
                    400, 422 -> RemoteError.ValidationError(
                        throwable.message ?: "Invalid request data"
                    )
                    in 500..599 -> RemoteError.ServerError(
                        throwable.message ?: "Server error occurred"
                    )
                    else -> RemoteError.UnknownError(throwable)
                }
            }
            
            is HttpRequestException -> RemoteError.NetworkError(
                "HTTP request failed: ${throwable.message}"
            )
            
            else -> RemoteError.UnknownError(throwable)
        }
    }
    
    /**
     * Wraps a suspend operation with error mapping
     * 
     * @param operation The operation to execute
     * @return Result with mapped errors
     */
    suspend fun <T> withErrorMapping(operation: suspend () -> T): Result<T> {
        return try {
            Result.success(operation())
        } catch (e: Exception) {
            Result.failure(mapError(e))
        }
    }
}
