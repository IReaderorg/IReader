package ireader.data.remote

/**
 * Maps remote errors to user-friendly messages
 */
object RemoteErrorMapper {
    
    suspend fun <T> withErrorMapping(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: Exception) {
            val message = mapError(e)
            Result.failure(Exception(message, e))
        }
    }
    
    private fun mapError(e: Exception): String {
        return when {
            e.message?.contains("network", ignoreCase = true) == true -> 
                "Network error. Please check your internet connection."
            e.message?.contains("timeout", ignoreCase = true) == true -> 
                "Request timed out. Please try again."
            e.message?.contains("unauthorized", ignoreCase = true) == true -> 
                "Authentication failed. Please sign in again."
            e.message?.contains("not found", ignoreCase = true) == true -> 
                "Resource not found."
            else -> e.message ?: "An unknown error occurred"
        }
    }
}
