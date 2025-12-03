package ireader.domain.plugins

import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Data class representing trial period information for premium plugins
 * Requirements: 8.4, 8.5
 */
data class TrialInfo(
    val pluginId: String,
    val startDate: Long,
    val expirationDate: Long,
    val isActive: Boolean
) {
    /**
     * Check if the trial has expired
     */
    fun isExpired(): Boolean {
        return currentTimeToLong() > expirationDate
    }
    
    /**
     * Get remaining days in trial
     */
    fun getRemainingDays(): Int {
        val remaining = expirationDate - currentTimeToLong()
        return (remaining / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
    }
}

/**
 * Repository interface for managing trial periods
 */
interface TrialRepository {
    /**
     * Start a trial period for a plugin
     */
    suspend fun startTrial(pluginId: String, durationDays: Int): Result<TrialInfo>
    
    /**
     * Get trial information for a plugin
     */
    suspend fun getTrialInfo(pluginId: String, userId: String): TrialInfo?
    
    /**
     * Check if a user has an active trial for a plugin
     */
    suspend fun hasActiveTrial(pluginId: String, userId: String): Boolean
    
    /**
     * End a trial period
     */
    suspend fun endTrial(pluginId: String, userId: String): Result<Unit>
}
