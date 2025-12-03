package ireader.domain.analytics

import ireader.core.log.Log
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Error tracker for monitoring application errors
 * Tracks error frequency, context, and trends
 */
class ErrorTracker(
    private val privacyMode: PrivacyMode = PrivacyMode.BALANCED
) {
    private val errors = mutableListOf<ErrorEvent>()
    private val errorCounts = mutableMapOf<ErrorType, Int>()
    private val maxErrors = 1000 // Limit to prevent memory issues
    
    /**
     * Track an error
     */
    fun trackError(
        error: Throwable,
        screen: String? = null,
        userAction: String? = null,
        appState: Map<String, String> = emptyMap()
    ) {
        if (privacyMode == PrivacyMode.STRICT) return
        
        try {
            val errorType = classifyError(error)
            val errorEvent = ErrorEvent(
                errorType = errorType,
                message = error.message ?: "Unknown error",
                stackTrace = if (privacyMode == PrivacyMode.FULL) error.stackTraceToString() else null,
                screen = screen,
                userAction = userAction,
                appState = removePII(appState),
                timestamp = currentTimeToLong()
            )
            
            errors.add(errorEvent)
            errorCounts[errorType] = (errorCounts[errorType] ?: 0) + 1
            
            // Trim old errors if exceeding limit
            if (errors.size > maxErrors) {
                errors.removeAt(0)
            }
            
            Log.error { "Error tracked: ${errorType.name} - ${error.message}" }
        } catch (e: Exception) {
            // Never throw from error tracking
            Log.error { "Failed to track error: ${e.message}" }
        }
    }
    
    /**
     * Get error statistics
     */
    fun getErrorStatistics(): ErrorStatistics {
        return try {
            if (errors.isEmpty()) {
                ErrorStatistics(
                    totalErrors = 0,
                    errorsByType = emptyMap(),
                    mostCommonError = null,
                    errorRate = 0.0,
                    recentErrors = emptyList()
                )
            } else {
                val now = currentTimeToLong()
                val oneDayAgo = now - (24 * 60 * 60 * 1000)
                val recentErrors = errors.filter { it.timestamp >= oneDayAgo }
                
                // Calculate error rate (errors per hour in last 24 hours)
                val errorRate = if (recentErrors.isNotEmpty()) {
                    recentErrors.size / 24.0
                } else {
                    0.0
                }
                
                // Find most common error
                val mostCommon = errorCounts.maxByOrNull { it.value }
                
                ErrorStatistics(
                    totalErrors = errors.size,
                    errorsByType = errorCounts.toMap(),
                    mostCommonError = mostCommon?.key,
                    errorRate = errorRate,
                    recentErrors = recentErrors.takeLast(10)
                )
            }
        } catch (e: Exception) {
            Log.error { "Failed to get error statistics: ${e.message}" }
            ErrorStatistics(0, emptyMap(), null, 0.0, emptyList())
        }
    }
    
    /**
     * Get errors by type
     */
    fun getErrorsByType(type: ErrorType): List<ErrorEvent> {
        return try {
            errors.filter { it.errorType == type }
        } catch (e: Exception) {
            Log.error { "Failed to get errors by type: ${e.message}" }
            emptyList()
        }
    }
    
    /**
     * Get recent errors
     */
    fun getRecentErrors(count: Int = 10): List<ErrorEvent> {
        return try {
            errors.takeLast(count)
        } catch (e: Exception) {
            Log.error { "Failed to get recent errors: ${e.message}" }
            emptyList()
        }
    }
    
    /**
     * Get error rate trend
     * Returns error count per hour for the last N hours
     */
    fun getErrorRateTrend(hours: Int = 24): Map<Long, Int> {
        return try {
            val now = currentTimeToLong()
            val startTime = now - (hours * 60 * 60 * 1000)
            val hourlyErrors = mutableMapOf<Long, Int>()
            
            errors.filter { it.timestamp >= startTime }.forEach { error ->
                val hour = error.timestamp / (60 * 60 * 1000)
                hourlyErrors[hour] = (hourlyErrors[hour] ?: 0) + 1
            }
            
            hourlyErrors
        } catch (e: Exception) {
            Log.error { "Failed to get error rate trend: ${e.message}" }
            emptyMap()
        }
    }
    
    /**
     * Clear all error data
     */
    fun clear() {
        try {
            errors.clear()
            errorCounts.clear()
        } catch (e: Exception) {
            Log.error { "Failed to clear error tracker: ${e.message}" }
        }
    }
    
    /**
     * Classify error by type
     */
    private fun classifyError(error: Throwable): ErrorType {
        return when {
            error::class.simpleName?.contains("Network", ignoreCase = true) == true -> ErrorType.NETWORK_ERROR
            error::class.simpleName?.contains("IO", ignoreCase = true) == true -> ErrorType.IO_ERROR
            error::class.simpleName?.contains("SQL", ignoreCase = true) == true -> ErrorType.DATABASE_ERROR
            error::class.simpleName?.contains("Parse", ignoreCase = true) == true -> ErrorType.PARSE_ERROR
            error::class.simpleName?.contains("Auth", ignoreCase = true) == true -> ErrorType.AUTH_ERROR
            error is IllegalArgumentException -> ErrorType.VALIDATION_ERROR
            error is IllegalStateException -> ErrorType.STATE_ERROR
            error is NullPointerException -> ErrorType.NULL_POINTER_ERROR
            else -> ErrorType.UNKNOWN_ERROR
        }
    }
    
    /**
     * Remove PII from app state
     */
    private fun removePII(state: Map<String, String>): Map<String, String> {
        val piiKeys = setOf("name", "email", "address", "phone", "user", "username", "userid", "password", "token")
        return state.filterKeys { key ->
            !piiKeys.any { piiKey -> key.lowercase().contains(piiKey) }
        }
    }
}

/**
 * Error event data
 */
data class ErrorEvent(
    val errorType: ErrorType,
    val message: String,
    val stackTrace: String?,
    val screen: String?,
    val userAction: String?,
    val appState: Map<String, String>,
    val timestamp: Long
)

/**
 * Error types
 */
enum class ErrorType {
    NETWORK_ERROR,
    DATABASE_ERROR,
    IO_ERROR,
    PARSE_ERROR,
    AUTH_ERROR,
    VALIDATION_ERROR,
    STATE_ERROR,
    NULL_POINTER_ERROR,
    UNKNOWN_ERROR
}

/**
 * Error statistics
 */
data class ErrorStatistics(
    val totalErrors: Int,
    val errorsByType: Map<ErrorType, Int>,
    val mostCommonError: ErrorType?,
    val errorRate: Double, // Errors per hour
    val recentErrors: List<ErrorEvent>
)
