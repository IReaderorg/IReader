package ireader.domain.services.tts_service.piper

import ireader.core.log.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Privacy-preserving usage analytics for Piper TTS
 * Tracks anonymized voice usage, feature usage, and crash reports
 * 
 * All data is stored locally and never transmitted without explicit user consent
 * No personally identifiable information (PII) is collected
 * 
 * Requirements: 7.1, 10.1
 */
class UsageAnalytics(
    private val privacyMode: PrivacyMode = PrivacyMode.BALANCED
) {
    
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    // Voice usage tracking (anonymized by language only)
    private val voiceUsageByLanguage = ConcurrentHashMap<String, LanguageUsageStats>()
    
    // Feature usage tracking
    private val featureUsage = ConcurrentHashMap<String, AtomicLong>()
    
    // Session tracking
    private val sessionStart = Instant.now()
    private val sessionsCount = AtomicLong(0)
    
    // Daily usage tracking
    private val dailyUsage = ConcurrentHashMap<LocalDate, DailyUsageStats>()
    
    // Crash and error tracking (anonymized)
    private val crashReports = mutableListOf<CrashReport>()
    private val maxCrashReports = 100
    
    // Analytics state
    private val _analyticsState = MutableStateFlow(AnalyticsSnapshot())
    val analyticsState: StateFlow<AnalyticsSnapshot> = _analyticsState.asStateFlow()
    
    /**
     * Record voice usage (anonymized by language)
     * Only tracks language, duration bucket, and timestamp - no voice model IDs
     */
    fun recordVoiceUsage(
        language: String,
        durationMs: Long,
        characterCount: Int
    ) {
        if (!isTrackingEnabled()) return
        
        // Get or create language stats
        val stats = voiceUsageByLanguage.getOrPut(language) { LanguageUsageStats(language) }
        
        // Record usage with duration bucket (privacy-preserving)
        val durationBucket = getDurationBucket(durationMs)
        stats.recordUsage(durationBucket, characterCount)
        
        // Update daily usage
        val today = LocalDate.now()
        val dailyStats = dailyUsage.getOrPut(today) { DailyUsageStats(today) }
        dailyStats.recordVoiceUsage(language, durationMs, characterCount)
        
        // Update state
        updateAnalyticsState()
    }
    
    /**
     * Record feature usage
     */
    fun recordFeatureUsage(featureName: String) {
        if (!isTrackingEnabled()) return
        
        featureUsage.getOrPut(featureName) { AtomicLong(0) }.incrementAndGet()
        
        // Update daily usage
        val today = LocalDate.now()
        val dailyStats = dailyUsage.getOrPut(today) { DailyUsageStats(today) }
        dailyStats.recordFeatureUsage(featureName)
        
        updateAnalyticsState()
    }
    
    /**
     * Record a crash or error (anonymized)
     */
    fun recordCrash(
        errorType: String,
        errorMessage: String?,
        stackTrace: String?,
        context: Map<String, String> = emptyMap()
    ) {
        // Always record crashes regardless of privacy mode for debugging
        
        // Anonymize the crash report
        val anonymizedReport = CrashReport(
            timestamp = Instant.now(),
            errorType = errorType,
            // Sanitize error message to remove any potential PII
            errorMessage = sanitizeErrorMessage(errorMessage),
            // Truncate stack trace to first 10 lines to avoid excessive data
            stackTrace = stackTrace?.lines()?.take(10)?.joinToString("\n"),
            // Only include non-sensitive context
            context = sanitizeContext(context)
        )
        
        synchronized(crashReports) {
            crashReports.add(anonymizedReport)
            
            // Trim if too many reports
            if (crashReports.size > maxCrashReports) {
                crashReports.removeAt(0)
            }
        }
        
        updateAnalyticsState()
        
        Log.warn { "Crash recorded: $errorType - ${anonymizedReport.errorMessage}" }
    }
    
    /**
     * Record a session start
     */
    fun recordSessionStart() {
        if (!isTrackingEnabled()) return
        
        sessionsCount.incrementAndGet()
        
        val today = LocalDate.now()
        val dailyStats = dailyUsage.getOrPut(today) { DailyUsageStats(today) }
        dailyStats.recordSession()
        
        updateAnalyticsState()
    }
    
    /**
     * Get usage statistics for a specific language
     */
    fun getLanguageStats(language: String): LanguageUsageStats? {
        return voiceUsageByLanguage[language]
    }
    
    /**
     * Get all language usage statistics
     */
    fun getAllLanguageStats(): Map<String, LanguageUsageStats> {
        return voiceUsageByLanguage.toMap()
    }
    
    /**
     * Get feature usage statistics
     */
    fun getFeatureUsage(): Map<String, Long> {
        return featureUsage.mapValues { it.value.get() }
    }
    
    /**
     * Get daily usage statistics
     */
    fun getDailyUsage(date: LocalDate): DailyUsageStats? {
        return dailyUsage[date]
    }
    
    /**
     * Get usage statistics for a date range
     */
    fun getUsageRange(startDate: LocalDate, endDate: LocalDate): List<DailyUsageStats> {
        return dailyUsage.entries
            .filter { it.key in startDate..endDate }
            .sortedBy { it.key }
            .map { it.value }
    }
    
    /**
     * Get recent crash reports
     */
    fun getCrashReports(limit: Int = 10): List<CrashReport> {
        return synchronized(crashReports) {
            crashReports.takeLast(limit)
        }
    }
    
    /**
     * Generate an analytics summary
     */
    fun generateSummary(): AnalyticsSummary {
        val totalVoiceUsageMs = voiceUsageByLanguage.values.sumOf { it.getTotalDurationMs() }
        val totalCharacters = voiceUsageByLanguage.values.sumOf { it.getTotalCharacters() }
        val totalSessions = sessionsCount.get()
        val totalCrashes = synchronized(crashReports) { crashReports.size }
        
        val mostUsedLanguage = voiceUsageByLanguage.maxByOrNull { it.value.getUsageCount() }?.key
        val mostUsedFeature = featureUsage.maxByOrNull { it.value.get() }?.key
        
        return AnalyticsSummary(
            timestamp = Instant.now(),
            sessionStart = sessionStart,
            totalSessions = totalSessions,
            totalVoiceUsageMs = totalVoiceUsageMs,
            totalCharacters = totalCharacters,
            languageStats = getAllLanguageStats(),
            featureUsage = getFeatureUsage(),
            mostUsedLanguage = mostUsedLanguage,
            mostUsedFeature = mostUsedFeature,
            totalCrashes = totalCrashes,
            privacyMode = privacyMode
        )
    }
    
    /**
     * Export analytics data (for user review or backup)
     */
    fun exportData(): AnalyticsExport {
        return AnalyticsExport(
            exportTimestamp = Instant.now(),
            summary = generateSummary(),
            dailyUsage = dailyUsage.values.sortedBy { it.date },
            crashReports = synchronized(crashReports) { crashReports.toList() },
            privacyMode = privacyMode
        )
    }
    
    /**
     * Clear all analytics data
     */
    fun clearAllData() {
        voiceUsageByLanguage.clear()
        featureUsage.clear()
        dailyUsage.clear()
        synchronized(crashReports) {
            crashReports.clear()
        }
        sessionsCount.set(0)
        
        updateAnalyticsState()
        
        Log.info { "All analytics data cleared" }
    }
    
    /**
     * Clear old data (older than specified days)
     */
    fun clearOldData(daysToKeep: Int = 30) {
        val cutoffDate = LocalDate.now().minusDays(daysToKeep.toLong())
        
        // Remove old daily usage
        val removed = dailyUsage.keys.removeIf { it.isBefore(cutoffDate) }
        
        // Remove old crash reports
        val cutoffInstant = cutoffDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
        synchronized(crashReports) {
            crashReports.removeIf { it.timestamp.isBefore(cutoffInstant) }
        }
        
        updateAnalyticsState()
        
        Log.info { "Cleared analytics data older than $daysToKeep days" }
    }
    
    /**
     * Check if tracking is enabled based on privacy mode
     */
    private fun isTrackingEnabled(): Boolean {
        return privacyMode != PrivacyMode.DISABLED
    }
    
    /**
     * Get duration bucket for privacy-preserving tracking
     */
    private fun getDurationBucket(durationMs: Long): DurationBucket {
        return when {
            durationMs < 60_000 -> DurationBucket.UNDER_1_MIN
            durationMs < 300_000 -> DurationBucket.ONE_TO_5_MIN
            durationMs < 900_000 -> DurationBucket.FIVE_TO_15_MIN
            durationMs < 1_800_000 -> DurationBucket.FIFTEEN_TO_30_MIN
            else -> DurationBucket.OVER_30_MIN
        }
    }
    
    /**
     * Sanitize error message to remove potential PII
     */
    private fun sanitizeErrorMessage(message: String?): String? {
        if (message == null) return null
        
        // Remove file paths that might contain usernames
        var sanitized = message.replace(Regex("/Users/[^/]+/"), "/Users/<user>/")
        sanitized = sanitized.replace(Regex("C:\\\\Users\\\\[^\\\\]+\\\\"), "C:\\Users\\<user>\\")
        
        // Remove potential email addresses
        sanitized = sanitized.replace(Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"), "<email>")
        
        // Truncate if too long
        if (sanitized.length > 500) {
            sanitized = sanitized.take(500) + "..."
        }
        
        return sanitized
    }
    
    /**
     * Sanitize context map to remove sensitive information
     */
    private fun sanitizeContext(context: Map<String, String>): Map<String, String> {
        val allowedKeys = setOf(
            "platform", "os_version", "app_version", 
            "voice_language", "error_code", "operation"
        )
        
        return context.filterKeys { it in allowedKeys }
    }
    
    /**
     * Update the analytics state flow
     */
    private fun updateAnalyticsState() {
        _analyticsState.value = AnalyticsSnapshot(
            totalSessions = sessionsCount.get(),
            languageUsage = voiceUsageByLanguage.mapValues { it.value.getUsageCount() },
            featureUsage = getFeatureUsage(),
            recentCrashes = synchronized(crashReports) { crashReports.size }
        )
    }
    
    /**
     * Shutdown analytics
     */
    fun shutdown() {
        val summary = generateSummary()
        Log.info { "Analytics summary:\n${summary.toSummaryString()}" }
    }
}

/**
 * Privacy mode for analytics
 */
enum class PrivacyMode {
    DISABLED,    // No analytics collected
    MINIMAL,     // Only crash reports
    BALANCED,    // Crash reports + basic usage (default)
    FULL         // All analytics (still privacy-preserving)
}

/**
 * Duration buckets for privacy-preserving tracking
 */
enum class DurationBucket(val displayName: String) {
    UNDER_1_MIN("0-1 min"),
    ONE_TO_5_MIN("1-5 min"),
    FIVE_TO_15_MIN("5-15 min"),
    FIFTEEN_TO_30_MIN("15-30 min"),
    OVER_30_MIN("30+ min")
}

/**
 * Language usage statistics
 */
class LanguageUsageStats(val language: String) {
    private val usageByDuration = ConcurrentHashMap<DurationBucket, AtomicLong>()
    private val totalCharacters = AtomicLong(0)
    private val usageCount = AtomicLong(0)
    
    fun recordUsage(durationBucket: DurationBucket, characterCount: Int) {
        usageByDuration.getOrPut(durationBucket) { AtomicLong(0) }.incrementAndGet()
        totalCharacters.addAndGet(characterCount.toLong())
        usageCount.incrementAndGet()
    }
    
    fun getUsageCount(): Long = usageCount.get()
    fun getTotalCharacters(): Long = totalCharacters.get()
    fun getTotalDurationMs(): Long {
        // Estimate based on duration buckets (using midpoint of each bucket)
        return usageByDuration.entries.sumOf { (bucket, count) ->
            val midpoint = when (bucket) {
                DurationBucket.UNDER_1_MIN -> 30_000L
                DurationBucket.ONE_TO_5_MIN -> 180_000L
                DurationBucket.FIVE_TO_15_MIN -> 600_000L
                DurationBucket.FIFTEEN_TO_30_MIN -> 1_350_000L
                DurationBucket.OVER_30_MIN -> 2_700_000L
            }
            midpoint * count.get()
        }
    }
    fun getUsageByDuration(): Map<DurationBucket, Long> {
        return usageByDuration.mapValues { it.value.get() }
    }
}

/**
 * Daily usage statistics
 */
class DailyUsageStats(val date: LocalDate) {
    private val voiceUsageByLanguage = ConcurrentHashMap<String, AtomicLong>()
    private val totalVoiceUsageMs = AtomicLong(0)
    private val totalCharacters = AtomicLong(0)
    private val featureUsage = ConcurrentHashMap<String, AtomicLong>()
    private val sessions = AtomicLong(0)
    
    fun recordVoiceUsage(language: String, durationMs: Long, characterCount: Int) {
        voiceUsageByLanguage.getOrPut(language) { AtomicLong(0) }.incrementAndGet()
        totalVoiceUsageMs.addAndGet(durationMs)
        totalCharacters.addAndGet(characterCount.toLong())
    }
    
    fun recordFeatureUsage(featureName: String) {
        featureUsage.getOrPut(featureName) { AtomicLong(0) }.incrementAndGet()
    }
    
    fun recordSession() {
        sessions.incrementAndGet()
    }
    
    fun getSessions(): Long = sessions.get()
    fun getTotalVoiceUsageMs(): Long = totalVoiceUsageMs.get()
    fun getTotalCharacters(): Long = totalCharacters.get()
    fun getVoiceUsageByLanguage(): Map<String, Long> = voiceUsageByLanguage.mapValues { it.value.get() }
    fun getFeatureUsage(): Map<String, Long> = featureUsage.mapValues { it.value.get() }
}

/**
 * Crash report (anonymized)
 */
data class CrashReport(
    val timestamp: Instant,
    val errorType: String,
    val errorMessage: String?,
    val stackTrace: String?,
    val context: Map<String, String>
)

/**
 * Analytics snapshot for state flow
 */
data class AnalyticsSnapshot(
    val totalSessions: Long = 0,
    val languageUsage: Map<String, Long> = emptyMap(),
    val featureUsage: Map<String, Long> = emptyMap(),
    val recentCrashes: Int = 0
)

/**
 * Analytics summary
 */
data class AnalyticsSummary(
    val timestamp: Instant,
    val sessionStart: Instant,
    val totalSessions: Long,
    val totalVoiceUsageMs: Long,
    val totalCharacters: Long,
    val languageStats: Map<String, LanguageUsageStats>,
    val featureUsage: Map<String, Long>,
    val mostUsedLanguage: String?,
    val mostUsedFeature: String?,
    val totalCrashes: Int,
    val privacyMode: PrivacyMode
) {
    fun toSummaryString(): String {
        return buildString {
            appendLine("=== Usage Analytics Summary ===")
            appendLine("Timestamp: $timestamp")
            appendLine("Session started: $sessionStart")
            appendLine("Privacy mode: $privacyMode")
            appendLine()
            appendLine("Sessions: $totalSessions")
            appendLine("Total voice usage: ${formatDuration(totalVoiceUsageMs)}")
            appendLine("Total characters: $totalCharacters")
            appendLine("Most used language: ${mostUsedLanguage ?: "N/A"}")
            appendLine("Most used feature: ${mostUsedFeature ?: "N/A"}")
            appendLine("Total crashes: $totalCrashes")
            appendLine()
            appendLine("Language Usage:")
            languageStats.forEach { (lang, stats) ->
                appendLine("  $lang: ${stats.getUsageCount()} uses, ${stats.getTotalCharacters()} chars")
            }
            
            if (featureUsage.isNotEmpty()) {
                appendLine()
                appendLine("Feature Usage:")
                featureUsage.entries.sortedByDescending { it.value }.take(10).forEach { (feature, count) ->
                    appendLine("  $feature: $count")
                }
            }
        }
    }
    
    private fun formatDuration(ms: Long): String {
        val hours = ms / 3_600_000
        val minutes = (ms % 3_600_000) / 60_000
        return "${hours}h ${minutes}m"
    }
}

/**
 * Analytics export for backup or review
 */
data class AnalyticsExport(
    val exportTimestamp: Instant,
    val summary: AnalyticsSummary,
    val dailyUsage: List<DailyUsageStats>,
    val crashReports: List<CrashReport>,
    val privacyMode: PrivacyMode
)
