//package ireader.domain.analytics
//
//import ireader.core.log.Log
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.SupervisorJob
//import kotlinx.coroutines.launch
//
///**
// * Central analytics manager that coordinates all analytics components
// * Provides a unified interface for performance monitoring, usage tracking, and error tracking
// */
//class AnalyticsManager(
//    private var privacyMode: PrivacyMode = PrivacyMode.BALANCED
//) {
//    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
//
//    val performanceMonitor = PerformanceMonitor(privacyMode)
//    val usageAnalytics = UsageAnalytics(privacyMode)
//    val errorTracker = ErrorTracker(privacyMode)
//    val performanceReporter = PerformanceReporter(performanceMonitor, usageAnalytics, errorTracker)
//
//    /**
//     * Update privacy mode for all analytics components
//     */
//    fun setPrivacyMode(mode: PrivacyMode) {
//        try {
//            privacyMode = mode
//            Log.info { "Analytics privacy mode set to: $mode" }
//
//            // If switching to STRICT mode, clear all data
//            if (mode == PrivacyMode.STRICT) {
//                clearAllData()
//            }
//        } catch (e: Exception) {
//            Log.error { "Failed to set privacy mode: ${e.message}" }
//        }
//    }
//
//    /**
//     * Get current privacy mode
//     */
//    fun getPrivacyMode(): PrivacyMode = privacyMode
//
//    /**
//     * Initialize analytics (called on app start)
//     */
//    fun initialize() {
//        try {
//            usageAnalytics.recordSessionStart()
//            Log.info { "Analytics initialized with privacy mode: $privacyMode" }
//        } catch (e: Exception) {
//            Log.error { "Failed to initialize analytics: ${e.message}" }
//        }
//    }
//
//    /**
//     * Shutdown analytics (called on app exit)
//     */
//    fun shutdown() {
//        try {
//            usageAnalytics.recordSessionEnd()
//            Log.info { "Analytics shutdown" }
//        } catch (e: Exception) {
//            Log.error { "Failed to shutdown analytics: ${e.message}" }
//        }
//    }
//
//    /**
//     * Clear all analytics data
//     */
//    fun clearAllData() {
//        try {
//            performanceMonitor.clear()
//            usageAnalytics.clear()
//            errorTracker.clear()
//            Log.info { "All analytics data cleared" }
//        } catch (e: Exception) {
//            Log.error { "Failed to clear analytics data: ${e.message}" }
//        }
//    }
//
//    /**
//     * Generate and log performance report
//     */
//    fun logPerformanceReport() {
//        scope.launch {
//            try {
//                val report = performanceReporter.generateReport()
//                Log.info { "=== Performance Report ===" }
//                Log.info { "Generated at: ${report.generatedAt}" }
//
//                report.performanceMetrics.synthesisTime?.let {
//                    Log.info { "TTS Synthesis - Avg: ${it.average.toInt()}ms, P95: ${it.p95.toInt()}ms" }
//                }
//
//                report.performanceMetrics.uiFrameTime?.let {
//                    Log.info { "UI Frame Time - Avg: ${it.average.toInt()}ms, P95: ${it.p95.toInt()}ms" }
//                }
//
//                report.performanceMetrics.networkLatency?.let {
//                    Log.info { "Network Latency - Avg: ${it.average.toInt()}ms, P95: ${it.p95.toInt()}ms" }
//                }
//
//                report.performanceMetrics.databaseQueryTime?.let {
//                    Log.info { "DB Query Time - Avg: ${it.average.toInt()}ms, P95: ${it.p95.toInt()}ms" }
//                }
//
//                Log.info { "Sessions: ${report.usageMetrics.sessionStatistics.totalSessions}, Avg Duration: ${report.usageMetrics.sessionStatistics.averageDuration / 1000}s" }
//                Log.info { "Errors: ${report.errorMetrics.totalErrors}, Rate: ${report.errorMetrics.errorRate.toInt()}/hour" }
//
//                if (report.insights.isNotEmpty()) {
//                    Log.info { "Insights:" }
//                    report.insights.forEach { insight ->
//                        Log.info { "  - $insight" }
//                    }
//                }
//
//                Log.info { "=========================" }
//            } catch (e: Exception) {
//                Log.error { "Failed to log performance report: ${e.message}" }
//            }
//        }
//    }
//
//    /**
//     * Track a feature usage event
//     */
//    fun trackFeature(featureName: String, metadata: Map<String, String> = emptyMap()) {
//        try {
//            usageAnalytics.recordFeatureUsage(featureName, metadata)
//        } catch (e: Exception) {
//            // Never throw from analytics
//            Log.error { "Failed to track feature: ${e.message}" }
//        }
//    }
//
//    /**
//     * Track an error
//     */
//    fun trackError(
//        error: Throwable,
//        screen: String? = null,
//        userAction: String? = null,
//        appState: Map<String, String> = emptyMap()
//    ) {
//        try {
//            errorTracker.trackError(error, screen, userAction, appState)
//        } catch (e: Exception) {
//            // Never throw from analytics
//            Log.error { "Failed to track error: ${e.message}" }
//        }
//    }
//
//    /**
//     * Measure and track execution time
//     */
//    inline fun <T> measureTime(
//        metricType: MetricType,
//        context: Map<String, String> = emptyMap(),
//        block: () -> T
//    ): T {
//        return try {
//            performanceMonitor.measureTime(metricType, context, block)
//        } catch (e: Exception) {
//            // If analytics fails, still execute the block
//            Log.error { "Failed to measure time: ${e.message}" }
//            block()
//        }
//    }
//
//    /**
//     * Measure and track execution time for suspend functions
//     */
//    suspend inline fun <T> measureTimeSuspend(
//        metricType: MetricType,
//        context: Map<String, String> = emptyMap(),
//        crossinline block: suspend () -> T
//    ): T {
//        return try {
//            performanceMonitor.measureTimeSuspend(metricType, context, block)
//        } catch (e: Exception) {
//            // If analytics fails, still execute the block
//            Log.error { "Failed to measure time: ${e.message}" }
//            block()
//        }
//    }
//}
