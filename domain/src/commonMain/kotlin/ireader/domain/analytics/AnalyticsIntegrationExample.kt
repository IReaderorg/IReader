package ireader.domain.analytics

import ireader.core.log.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Example integration of analytics system
 * This file demonstrates how to integrate analytics throughout the application
 */
class AnalyticsIntegrationExample(
    private val analyticsManager: AnalyticsManager
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    /**
     * Example: Initialize analytics on app start
     * Call this in your Application.onCreate() or main() function
     */
    fun initializeAnalytics() {
        try {
            // Initialize analytics
            analyticsManager.initialize()
            
            // Set privacy mode based on user preferences
            // val privacyMode = userPreferences.getPrivacyMode()
            // analyticsManager.setPrivacyMode(privacyMode)
            
            Log.info { "Analytics initialized successfully" }
        } catch (e: Exception) {
            Log.error { "Failed to initialize analytics: ${e.message}" }
        }
    }
    
    /**
     * Example: Shutdown analytics on app exit
     * Call this in your Application.onTerminate() or app shutdown
     */
    fun shutdownAnalytics() {
        try {
            analyticsManager.shutdown()
            Log.info { "Analytics shutdown successfully" }
        } catch (e: Exception) {
            Log.error { "Failed to shutdown analytics: ${e.message}" }
        }
    }
    
    /**
     * Example: Track feature usage
     */
    fun trackFeatureUsage() {
        // Track when user opens a book
        analyticsManager.trackFeature("book_opened", mapOf(
            "source" to "library",
            "format" to "epub"
        ))
        
        // Track when user reads a chapter
        analyticsManager.trackFeature("chapter_read")
        
        // Track when user performs search
        analyticsManager.trackFeature("search_performed", mapOf(
            "query_length" to "15"
        ))
        
        // Track when user uses TTS
        analyticsManager.trackFeature("tts_started", mapOf(
            "engine" to "piper",
            "language" to "en"
        ))
    }
    
    /**
     * Example: Track performance metrics
     */
    suspend fun trackPerformanceMetrics() {
        // Track TTS synthesis time
        val audioData = analyticsManager.measureTimeSuspend(MetricType.SYNTHESIS_TIME) {
            // Simulate TTS synthesis
            synthesizeText("Hello world")
        }
        
        // Track database query time
        val books = analyticsManager.measureTimeSuspend(MetricType.DB_QUERY_TIME) {
            // Simulate database query
            fetchBooksFromDatabase()
        }
        
        // Track network request time
        val bookDetails = analyticsManager.measureTimeSuspend(MetricType.NETWORK_LATENCY) {
            // Simulate network request
            fetchBookDetailsFromNetwork()
        }
    }
    
    /**
     * Example: Track errors
     */
    fun trackErrors() {
        try {
            // Some risky operation
            riskyOperation()
        } catch (e: Exception) {
            // Track the error with context
            analyticsManager.trackError(
                error = e,
                screen = "BookDetailScreen",
                userAction = "load_chapters",
                appState = mapOf(
                    "bookId" to "123",
                    "sourceId" to "456"
                )
            )
        }
    }
    
    /**
     * Example: Generate and export performance report
     */
    fun generatePerformanceReport() {
        scope.launch {
            try {
                // Generate report
                val report = analyticsManager.performanceReporter.generateReport()
                
                // Log report to console
                analyticsManager.logPerformanceReport()
                
                // Export as JSON
                val json = analyticsManager.performanceReporter.exportReportAsJson()
                Log.info { "Performance report JSON: $json" }
                
                // Export as CSV
                val csv = analyticsManager.performanceReporter.exportReportAsCsv()
                Log.info { "Performance report CSV: $csv" }
                
                // Access specific metrics
                report.insights.forEach { insight ->
                    Log.info { "Insight: $insight" }
                }
            } catch (e: Exception) {
                Log.error { "Failed to generate performance report: ${e.message}" }
            }
        }
    }
    
    /**
     * Example: Monitor session statistics
     */
    fun monitorSessionStatistics() {
        val sessionStats = analyticsManager.usageAnalytics.getSessionStatistics()
        
        Log.info { "Total sessions: ${sessionStats.totalSessions}" }
        Log.info { "Average session duration: ${sessionStats.averageDuration / 1000}s" }
        Log.info { "Sessions per day: ${sessionStats.sessionsPerDay}" }
    }
    
    /**
     * Example: Monitor error statistics
     */
    fun monitorErrorStatistics() {
        val errorStats = analyticsManager.errorTracker.getErrorStatistics()
        
        Log.info { "Total errors: ${errorStats.totalErrors}" }
        Log.info { "Error rate: ${errorStats.errorRate} errors/hour" }
        Log.info { "Most common error: ${errorStats.mostCommonError}" }
        
        errorStats.errorsByType.forEach { (type, count) ->
            Log.info { "  $type: $count occurrences" }
        }
    }
    
    /**
     * Example: Clear analytics data
     */
    fun clearAnalyticsData() {
        analyticsManager.clearAllData()
        Log.info { "All analytics data cleared" }
    }
    
    // Simulated methods for examples
    private suspend fun synthesizeText(text: String): ByteArray {
        kotlinx.coroutines.delay(150) // Simulate synthesis time
        return ByteArray(0)
    }
    
    private suspend fun fetchBooksFromDatabase(): List<String> {
        kotlinx.coroutines.delay(45) // Simulate database query
        return emptyList()
    }
    
    private suspend fun fetchBookDetailsFromNetwork(): String {
        kotlinx.coroutines.delay(250) // Simulate network request
        return ""
    }
    
    private fun riskyOperation() {
        throw Exception("Something went wrong")
    }
}

/**
 * Example: Application-level integration
 */
object ApplicationAnalytics {
    private lateinit var analyticsManager: AnalyticsManager
    
    /**
     * Initialize analytics on app start
     */
    fun initialize(privacyMode: PrivacyMode = PrivacyMode.BALANCED) {
        analyticsManager = AnalyticsManager(privacyMode)
        analyticsManager.initialize()
    }
    
    /**
     * Get analytics manager instance
     */
    fun getInstance(): AnalyticsManager {
        if (!::analyticsManager.isInitialized) {
            throw IllegalStateException("Analytics not initialized. Call initialize() first.")
        }
        return analyticsManager
    }
    
    /**
     * Shutdown analytics on app exit
     */
    fun shutdown() {
        if (::analyticsManager.isInitialized) {
            analyticsManager.shutdown()
        }
    }
}
