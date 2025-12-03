package ireader.domain.analytics

import ireader.core.log.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Performance reporter for generating reports and insights
 */
class PerformanceReporter(
    private val performanceMonitor: PerformanceMonitor,
    private val usageAnalytics: UsageAnalytics,
    private val errorTracker: ErrorTracker
) {
    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    /**
     * Generate comprehensive performance report
     */
    fun generateReport(): PerformanceReport {
        return try {
            val now = currentTimeToLong()
            val oneDayAgo = now - (24 * 60 * 60 * 1000)
            val oneWeekAgo = now - (7 * 24 * 60 * 60 * 1000)
            
            // Collect performance metrics
            val synthesisStats = performanceMonitor.getStatistics(MetricType.SYNTHESIS_TIME)
            val uiStats = performanceMonitor.getStatistics(MetricType.UI_FRAME_TIME)
            val networkStats = performanceMonitor.getStatistics(MetricType.NETWORK_LATENCY)
            val dbStats = performanceMonitor.getStatistics(MetricType.DB_QUERY_TIME)
            
            // Collect usage statistics
            val sessionStats = usageAnalytics.getSessionStatistics()
            val featureUsage = usageAnalytics.getAllFeatureUsage()
            
            // Collect error statistics
            val errorStats = errorTracker.getErrorStatistics()
            
            // Generate insights
            val insights = generateInsights(
                synthesisStats, uiStats, networkStats, dbStats,
                sessionStats, errorStats, oneDayAgo, oneWeekAgo
            )
            
            PerformanceReport(
                generatedAt = now,
                performanceMetrics = PerformanceMetricsReport(
                    synthesisTime = synthesisStats?.toMetricSummary(),
                    uiFrameTime = uiStats?.toMetricSummary(),
                    networkLatency = networkStats?.toMetricSummary(),
                    databaseQueryTime = dbStats?.toMetricSummary()
                ),
                usageMetrics = UsageMetricsReport(
                    sessionStatistics = sessionStats,
                    topFeatures = featureUsage.entries
                        .sortedByDescending { it.value.usageCount }
                        .take(10)
                        .associate { it.key to it.value.usageCount }
                ),
                errorMetrics = ErrorMetricsReport(
                    totalErrors = errorStats.totalErrors,
                    errorsByType = errorStats.errorsByType,
                    mostCommonError = errorStats.mostCommonError?.name,
                    errorRate = errorStats.errorRate
                ),
                insights = insights
            )
        } catch (e: Exception) {
            Log.error { "Failed to generate performance report: ${e.message}" }
            PerformanceReport(
                generatedAt = currentTimeToLong(),
                performanceMetrics = PerformanceMetricsReport(),
                usageMetrics = UsageMetricsReport(
                    sessionStatistics = SessionStatistics(0, 0L, 0L, 0.0),
                    topFeatures = emptyMap()
                ),
                errorMetrics = ErrorMetricsReport(0, emptyMap(), null, 0.0),
                insights = emptyList()
            )
        }
    }
    
    /**
     * Generate actionable insights from metrics
     */
    private fun generateInsights(
        synthesisStats: PerformanceStatistics?,
        uiStats: PerformanceStatistics?,
        networkStats: PerformanceStatistics?,
        dbStats: PerformanceStatistics?,
        sessionStats: SessionStatistics,
        errorStats: ErrorStatistics,
        oneDayAgo: Long,
        oneWeekAgo: Long
    ): List<String> {
        val insights = mutableListOf<String>()
        
        try {
            // TTS synthesis insights
            synthesisStats?.let { stats ->
                if (stats.p95 > 500) {
                    insights.add("TTS synthesis is slow: 95th percentile is ${stats.p95.toInt()}ms (target: <500ms)")
                }
                if (stats.average > 200) {
                    insights.add("Average TTS synthesis time is ${stats.average.toInt()}ms (target: <200ms)")
                }
            }
            
            // UI performance insights
            uiStats?.let { stats ->
                if (stats.p95 > 16.67) { // 60 FPS = 16.67ms per frame
                    insights.add("UI frame drops detected: 95th percentile is ${stats.p95.toInt()}ms (target: <17ms for 60 FPS)")
                }
            }
            
            // Network performance insights
            networkStats?.let { stats ->
                if (stats.p95 > 3000) {
                    insights.add("Network latency is high: 95th percentile is ${stats.p95.toInt()}ms")
                }
                
                // Check for trend
                val recentStats = performanceMonitor.getStatistics(MetricType.NETWORK_LATENCY, oneWeekAgo, oneDayAgo)
                val currentStats = performanceMonitor.getStatistics(MetricType.NETWORK_LATENCY, oneDayAgo, currentTimeToLong())
                
                if (recentStats != null && currentStats != null) {
                    val change = ((currentStats.average - recentStats.average) / recentStats.average) * 100
                    if (change > 20) {
                        insights.add("Network latency increased ${change.toInt()}% this week")
                    } else if (change < -20) {
                        insights.add("Network latency improved ${(-change).toInt()}% this week")
                    }
                }
            }
            
            // Database performance insights
            dbStats?.let { stats ->
                if (stats.p95 > 100) {
                    insights.add("Database queries are slow: 95th percentile is ${stats.p95.toInt()}ms (consider indexing)")
                }
            }
            
            // Session insights
            if (sessionStats.averageDuration < 60000) { // Less than 1 minute
                insights.add("Average session duration is short: ${sessionStats.averageDuration / 1000}s (users may be experiencing issues)")
            }
            
            // Error insights
            if (errorStats.errorRate > 5) {
                insights.add("High error rate: ${errorStats.errorRate.toInt()} errors per hour")
            }
            
            errorStats.mostCommonError?.let { errorType ->
                val count = errorStats.errorsByType[errorType] ?: 0
                insights.add("Most common error: ${errorType.name} ($count occurrences)")
            }
            
            // Positive insights
            if (insights.isEmpty()) {
                insights.add("All metrics are within acceptable ranges")
            }
        } catch (e: Exception) {
            Log.error { "Failed to generate insights: ${e.message}" }
        }
        
        return insights
    }
    
    /**
     * Export report as JSON
     */
    fun exportReportAsJson(): String {
        return try {
            val report = generateReport()
            json.encodeToString(report)
        } catch (e: Exception) {
            Log.error { "Failed to export report as JSON: ${e.message}" }
            "{\"error\": \"Failed to generate report\"}"
        }
    }
    
    /**
     * Export report as CSV
     */
    fun exportReportAsCsv(): String {
        return try {
            val report = generateReport()
            buildString {
                appendLine("Metric Type,Average,P50,P95,P99,Min,Max,Count")
                
                report.performanceMetrics.synthesisTime?.let {
                    appendLine("Synthesis Time,${it.average},${it.p50},${it.p95},${it.p99},${it.min},${it.max},${it.count}")
                }
                
                report.performanceMetrics.uiFrameTime?.let {
                    appendLine("UI Frame Time,${it.average},${it.p50},${it.p95},${it.p99},${it.min},${it.max},${it.count}")
                }
                
                report.performanceMetrics.networkLatency?.let {
                    appendLine("Network Latency,${it.average},${it.p50},${it.p95},${it.p99},${it.min},${it.max},${it.count}")
                }
                
                report.performanceMetrics.databaseQueryTime?.let {
                    appendLine("Database Query Time,${it.average},${it.p50},${it.p95},${it.p99},${it.min},${it.max},${it.count}")
                }
                
                appendLine()
                appendLine("Feature,Usage Count")
                report.usageMetrics.topFeatures.forEach { (feature, count) ->
                    appendLine("$feature,$count")
                }
                
                appendLine()
                appendLine("Error Type,Count")
                report.errorMetrics.errorsByType.forEach { (type, count) ->
                    appendLine("$type,$count")
                }
            }
        } catch (e: Exception) {
            Log.error { "Failed to export report as CSV: ${e.message}" }
            "Error,Failed to generate report"
        }
    }
}

/**
 * Extension function to convert PerformanceStatistics to MetricSummary
 */
private fun PerformanceStatistics.toMetricSummary(): MetricSummary {
    return MetricSummary(
        average = this.average,
        p50 = this.p50,
        p95 = this.p95,
        p99 = this.p99,
        min = this.min,
        max = this.max,
        count = this.count
    )
}

/**
 * Performance report data classes
 */
@Serializable
data class PerformanceReport(
    val generatedAt: Long,
    val performanceMetrics: PerformanceMetricsReport,
    val usageMetrics: UsageMetricsReport,
    val errorMetrics: ErrorMetricsReport,
    val insights: List<String>
)

@Serializable
data class PerformanceMetricsReport(
    val synthesisTime: MetricSummary? = null,
    val uiFrameTime: MetricSummary? = null,
    val networkLatency: MetricSummary? = null,
    val databaseQueryTime: MetricSummary? = null
)

@Serializable
data class MetricSummary(
    val average: Double,
    val p50: Double,
    val p95: Double,
    val p99: Double,
    val min: Double,
    val max: Double,
    val count: Int
)

@Serializable
data class UsageMetricsReport(
    val sessionStatistics: SessionStatistics,
    val topFeatures: Map<String, Int>
)

@Serializable
data class ErrorMetricsReport(
    val totalErrors: Int,
    val errorsByType: Map<ErrorType, Int>,
    val mostCommonError: String?,
    val errorRate: Double
)
