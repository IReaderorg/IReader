package ireader.domain.analytics

import ireader.core.log.Log

/**
 * Network analytics interceptor for tracking request latency
 * Can be integrated with HTTP clients (Ktor, OkHttp, etc.)
 */
class NetworkAnalyticsInterceptor(
    private val analyticsManager: AnalyticsManager
) {
    /**
     * Track network request
     * Call this before making a request, and call complete() after
     */
    fun trackRequest(url: String, method: String): NetworkRequestTracker {
        return NetworkRequestTracker(
            url = url,
            method = method,
            startTime = System.currentTimeMillis(),
            analyticsManager = analyticsManager
        )
    }
}

/**
 * Tracker for individual network requests
 */
class NetworkRequestTracker(
    private val url: String,
    private val method: String,
    private val startTime: Long,
    private val analyticsManager: AnalyticsManager
) {
    /**
     * Mark request as complete and record latency
     */
    fun complete(success: Boolean = true, statusCode: Int? = null) {
        try {
            val duration = System.currentTimeMillis() - startTime
            
            val context = buildMap {
                put("method", method)
                put("success", success.toString())
                statusCode?.let { put("status_code", it.toString()) }
                // Don't include full URL to avoid PII
                put("domain", extractDomain(url))
            }
            
            analyticsManager.performanceMonitor.recordNetworkLatency(duration, context)
            
            if (!success) {
                Log.warn { "Network request failed: $method $url (${duration}ms)" }
            }
        } catch (e: Exception) {
            // Never throw from analytics
            Log.error { "Failed to complete network tracking: ${e.message}" }
        }
    }
    
    /**
     * Mark request as failed with error
     */
    fun fail(error: Throwable) {
        try {
            val duration = System.currentTimeMillis() - startTime
            
            val context = buildMap {
                put("method", method)
                put("success", "false")
                put("error", error::class.simpleName ?: "Unknown")
                put("domain", extractDomain(url))
            }
            
            analyticsManager.performanceMonitor.recordNetworkLatency(duration, context)
            analyticsManager.trackError(error, userAction = "network_request")
        } catch (e: Exception) {
            // Never throw from analytics
            Log.error { "Failed to track network failure: ${e.message}" }
        }
    }
    
    private fun extractDomain(url: String): String {
        return try {
            val domain = url.substringAfter("://").substringBefore("/")
            domain.substringAfter("www.")
        } catch (e: Exception) {
            "unknown"
        }
    }
}

/**
 * Extension function for easy network tracking
 */
suspend inline fun <T> NetworkAnalyticsInterceptor.trackRequest(
    url: String,
    method: String,
    crossinline block: suspend () -> T
): T {
    val tracker = trackRequest(url, method)
    return try {
        val result = block()
        tracker.complete(success = true)
        result
    } catch (e: Exception) {
        tracker.fail(e)
        throw e
    }
}
