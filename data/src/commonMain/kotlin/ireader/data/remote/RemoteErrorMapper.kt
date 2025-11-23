package ireader.data.remote

import ireader.core.log.Log


/**
 * Maps remote errors to user-friendly messages and handles graceful degradation
 */
object RemoteErrorMapper {
    
    /**
     * Wraps Supabase calls with error handling that allows the app to continue
     * even when Supabase is unavailable (maintenance, network issues, etc.)
     */
    suspend fun <T> withErrorMapping(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: Exception) {
            // Log the error for debugging
            Log.warn("Supabase request failed: ${e.message}", e)
            
            // Check if this is a non-critical error that should be silently handled
            if (isNonCriticalError(e)) {
               Log.info("Non-critical Supabase error - app will continue normally")
                // Return failure but don't crash the app
                Result.failure(Exception(mapError(e), e))
            } else {
                val message = mapError(e)
                Result.failure(Exception(message, e))
            }
        }
    }
    
    /**
     * Determines if an error is non-critical and the app can continue without it
     */
    private fun isNonCriticalError(e: Exception): Boolean {
        val message = e.message?.lowercase() ?: ""
        return message.contains("maintenance") ||
               message.contains("service unavailable") ||
               message.contains("503") ||
               message.contains("502") ||
               message.contains("cancelled") ||
               message.contains("connection refused") ||
               message.contains("unable to resolve host") ||
               message.contains("timeout")
    }
    
    private fun mapError(e: Exception): String {
        val message = e.message?.lowercase() ?: ""
        return when {
            message.contains("maintenance") || message.contains("503") -> 
                "Service temporarily unavailable due to maintenance. Some features may be limited."
            message.contains("cancelled") -> 
                "Request was cancelled. This is normal during service maintenance."
            message.contains("network") || message.contains("unable to resolve host") -> 
                "Network error. Please check your internet connection."
            message.contains("timeout") -> 
                "Request timed out. The service may be temporarily unavailable."
            message.contains("unauthorized") || message.contains("401") -> 
                "Authentication failed. Please sign in again."
            message.contains("not found") || message.contains("404") -> 
                "Resource not found."
            message.contains("502") || message.contains("bad gateway") -> 
                "Service temporarily unavailable. Please try again later."
            else -> "Service temporarily unavailable: ${e.message ?: "Unknown error"}"
        }
    }
}
