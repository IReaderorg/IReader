package ireader.domain.analytics

import ireader.core.log.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.abs
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Usage analytics for tracking app-wide feature usage
 * Implements privacy-preserving data collection
 */
class UsageAnalytics(
    private val privacyMode: PrivacyMode = PrivacyMode.BALANCED
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val featureUsage = mutableMapOf<String, FeatureUsageData>()
    private val sessions = mutableListOf<SessionData>()
    private var currentSessionStart: Long? = null
    
    /**
     * Record session start
     */
    fun recordSessionStart() {
        if (privacyMode == PrivacyMode.STRICT) return
        
        try {
            currentSessionStart = currentTimeToLong()
            Log.info { "Analytics: Session started" }
        } catch (e: Exception) {
            Log.error { "Failed to record session start: ${e.message}" }
        }
    }
    
    /**
     * Record session end
     */
    fun recordSessionEnd() {
        if (privacyMode == PrivacyMode.STRICT) return
        
        try {
            val startTime = currentSessionStart ?: return
            val endTime = currentTimeToLong()
            val duration = endTime - startTime
            
            sessions.add(
                SessionData(
                    startTime = startTime,
                    endTime = endTime,
                    duration = duration
                )
            )
            
            currentSessionStart = null
            Log.info { "Analytics: Session ended (duration: ${duration}ms)" }
        } catch (e: Exception) {
            Log.error { "Failed to record session end: ${e.message}" }
        }
    }
    
    /**
     * Record feature usage event
     * Examples: book_opened, chapter_read, search_performed, etc.
     */
    fun recordFeatureUsage(featureName: String, metadata: Map<String, String> = emptyMap()) {
        if (privacyMode == PrivacyMode.STRICT) return
        
        try {
            // Remove PII from metadata
            val sanitizedMetadata = if (privacyMode == PrivacyMode.BALANCED || privacyMode == PrivacyMode.FULL) {
                removePII(metadata)
            } else {
                emptyMap()
            }
            
            val usage = featureUsage.getOrPut(featureName) {
                FeatureUsageData(featureName)
            }
            
            usage.recordUsage(sanitizedMetadata)
            
            if (privacyMode == PrivacyMode.FULL) {
                Log.debug { "Analytics: Feature used - $featureName" }
            }
        } catch (e: Exception) {
            Log.error { "Failed to record feature usage: ${e.message}" }
        }
    }
    
    /**
     * Get feature usage statistics
     */
    fun getFeatureUsage(featureName: String): FeatureUsageData? {
        return try {
            featureUsage[featureName]
        } catch (e: Exception) {
            Log.error { "Failed to get feature usage: ${e.message}" }
            null
        }
    }
    
    /**
     * Get all feature usage data
     */
    fun getAllFeatureUsage(): Map<String, FeatureUsageData> {
        return try {
            featureUsage.toMap()
        } catch (e: Exception) {
            Log.error { "Failed to get all feature usage: ${e.message}" }
            emptyMap()
        }
    }
    
    /**
     * Get session statistics
     */
    fun getSessionStatistics(): SessionStatistics {
        return try {
            if (sessions.isEmpty()) {
                SessionStatistics(
                    totalSessions = 0,
                    averageDuration = 0L,
                    totalDuration = 0L,
                    sessionsPerDay = 0.0
                )
            } else {
                val totalDuration = sessions.sumOf { it.duration }
                val averageDuration = totalDuration / sessions.size
                
                // Calculate sessions per day
                val firstSession = sessions.minOf { it.startTime }
                val lastSession = sessions.maxOf { it.endTime }
                val daysDiff = ((lastSession - firstSession) / (1000 * 60 * 60 * 24)).coerceAtLeast(1)
                val sessionsPerDay = sessions.size.toDouble() / daysDiff
                
                SessionStatistics(
                    totalSessions = sessions.size,
                    averageDuration = averageDuration,
                    totalDuration = totalDuration,
                    sessionsPerDay = sessionsPerDay
                )
            }
        } catch (e: Exception) {
            Log.error { "Failed to get session statistics: ${e.message}" }
            SessionStatistics(0, 0L, 0L, 0.0)
        }
    }
    
    /**
     * Clear all analytics data
     */
    fun clear() {
        try {
            featureUsage.clear()
            sessions.clear()
            currentSessionStart = null
        } catch (e: Exception) {
            Log.error { "Failed to clear analytics: ${e.message}" }
        }
    }
    
    /**
     * Remove PII from metadata
     * Removes names, emails, addresses, phone numbers, etc.
     */
    private fun removePII(metadata: Map<String, String>): Map<String, String> {
        val piiKeys = setOf("name", "email", "address", "phone", "user", "username", "userid")
        return metadata.filterKeys { key ->
            !piiKeys.any { piiKey -> key.lowercase().contains(piiKey) }
        }
    }
    
    /**
     * Hash user ID for privacy
     */
    fun hashUserId(userId: String): String {
        return try {
            // Simple hash for privacy (in production, use proper hashing)
            abs(userId.hashCode()).toString()
        } catch (e: Exception) {
            Log.error { "Failed to hash user ID: ${e.message}" }
            "unknown"
        }
    }
}

/**
 * Feature usage data
 */
data class FeatureUsageData(
    val featureName: String,
    var usageCount: Int = 0,
    var lastUsed: Long = 0L,
    val usageHistory: MutableList<Long> = mutableListOf()
) {
    fun recordUsage(metadata: Map<String, String> = emptyMap()) {
        usageCount++
        lastUsed = currentTimeToLong()
        usageHistory.add(lastUsed)
        
        // Keep only last 1000 usage timestamps to prevent memory issues
        if (usageHistory.size > 1000) {
            usageHistory.removeAt(0)
        }
    }
}

/**
 * Session data
 */
data class SessionData(
    val startTime: Long,
    val endTime: Long,
    val duration: Long
)

/**
 * Session statistics
 */
@kotlinx.serialization.Serializable
data class SessionStatistics(
    val totalSessions: Int,
    val averageDuration: Long,
    val totalDuration: Long,
    val sessionsPerDay: Double
)
