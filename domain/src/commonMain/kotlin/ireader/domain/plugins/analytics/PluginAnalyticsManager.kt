package ireader.domain.plugins.analytics

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ireader.core.util.createICoroutineScope
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Manager for plugin analytics.
 */
class PluginAnalyticsManager(
    private val analyticsRepository: PluginAnalyticsRepository,
    private val crashReporter: CrashReporter,
    private val abTestManager: ABTestManager
) {
    private val scope = createICoroutineScope()
    
    private val _dashboard = MutableStateFlow<DeveloperDashboard?>(null)
    val dashboard: StateFlow<DeveloperDashboard?> = _dashboard.asStateFlow()
    
    private val _crashGroups = MutableStateFlow<List<CrashGroup>>(emptyList())
    val crashGroups: StateFlow<List<CrashGroup>> = _crashGroups.asStateFlow()
    
    private val _activeTests = MutableStateFlow<List<ABTest>>(emptyList())
    val activeTests: StateFlow<List<ABTest>> = _activeTests.asStateFlow()
    
    private var currentSessionId: String = generateSessionId()
    private var deviceInfo: DeviceInfo? = null
    
    /**
     * Initialize analytics with device info.
     */
    fun initialize(deviceInfo: DeviceInfo) {
        this.deviceInfo = deviceInfo
        currentSessionId = generateSessionId()
    }
    
    /**
     * Track an analytics event.
     */
    suspend fun trackEvent(
        pluginId: String,
        eventType: AnalyticsEventType,
        properties: Map<String, String> = emptyMap(),
        metrics: Map<String, Double> = emptyMap()
    ) {
        val event = AnalyticsEvent(
            id = generateEventId(),
            pluginId = pluginId,
            eventType = eventType,
            timestamp = currentTimeToLong(),
            sessionId = currentSessionId,
            userId = null, // Would be set from auth
            properties = properties,
            metrics = metrics,
            deviceInfo = deviceInfo
        )
        analyticsRepository.trackEvent(event)
    }

    /**
     * Track plugin usage.
     */
    suspend fun trackPluginUsage(pluginId: String, feature: String, durationMs: Long? = null) {
        val props = mutableMapOf("feature" to feature)
        val metrics = mutableMapOf<String, Double>()
        durationMs?.let { metrics["duration_ms"] = it.toDouble() }
        
        trackEvent(pluginId, AnalyticsEventType.PLUGIN_USED, props, metrics)
    }
    
    /**
     * Track plugin error.
     */
    suspend fun trackError(
        pluginId: String,
        errorType: String,
        errorMessage: String,
        stackTrace: String? = null
    ) {
        val props = mutableMapOf(
            "error_type" to errorType,
            "error_message" to errorMessage
        )
        stackTrace?.let { props["stack_trace"] = it }
        
        trackEvent(pluginId, AnalyticsEventType.PLUGIN_ERROR, props)
    }
    
    /**
     * Report a crash.
     */
    suspend fun reportCrash(
        pluginId: String,
        pluginVersion: String,
        error: Throwable,
        breadcrumbs: List<Breadcrumb> = emptyList(),
        customData: Map<String, String> = emptyMap()
    ) {
        val report = CrashReport(
            id = generateCrashId(),
            pluginId = pluginId,
            pluginVersion = pluginVersion,
            timestamp = currentTimeToLong(),
            errorType = error::class.simpleName ?: "Unknown",
            errorMessage = error.message ?: "No message",
            stackTrace = error.stackTraceToString(),
            deviceInfo = deviceInfo ?: DeviceInfo(
                platform = "unknown",
                osVersion = "unknown",
                appVersion = "unknown",
                deviceModel = null,
                locale = "en",
                timezone = "UTC"
            ),
            breadcrumbs = breadcrumbs,
            customData = customData
        )
        
        crashReporter.reportCrash(report)
        trackEvent(pluginId, AnalyticsEventType.PLUGIN_CRASH, mapOf(
            "error_type" to report.errorType,
            "error_message" to report.errorMessage
        ))
    }
    
    /**
     * Add a breadcrumb for crash context.
     */
    fun addBreadcrumb(
        category: String,
        message: String,
        level: BreadcrumbLevel = BreadcrumbLevel.INFO
    ) {
        crashReporter.addBreadcrumb(Breadcrumb(
            timestamp = currentTimeToLong(),
            category = category,
            message = message,
            level = level
        ))
    }
    
    // Dashboard
    
    /**
     * Load developer dashboard.
     */
    suspend fun loadDashboard(developerId: String) {
        _dashboard.value = analyticsRepository.getDeveloperDashboard(developerId)
    }
    
    /**
     * Get usage stats for a plugin.
     */
    suspend fun getPluginUsageStats(pluginId: String): PluginUsageStats? {
        return analyticsRepository.getPluginUsageStats(pluginId)
    }
    
    /**
     * Get usage trends for a plugin.
     */
    suspend fun getUsageTrends(
        pluginId: String,
        metricName: String,
        period: TrendPeriod
    ): UsageTrend? {
        return analyticsRepository.getUsageTrends(pluginId, metricName, period)
    }
    
    /**
     * Get performance summary for a plugin.
     */
    suspend fun getPerformanceSummary(
        pluginId: String,
        period: TrendPeriod
    ): PerformanceSummary? {
        return analyticsRepository.getPerformanceSummary(pluginId, period)
    }
    
    /**
     * Get engagement metrics for a plugin.
     */
    suspend fun getEngagementMetrics(
        pluginId: String,
        period: TrendPeriod
    ): EngagementMetrics? {
        return analyticsRepository.getEngagementMetrics(pluginId, period)
    }
    
    // Crash Reports
    
    /**
     * Load crash groups for a plugin.
     */
    suspend fun loadCrashGroups(pluginId: String) {
        _crashGroups.value = crashReporter.getCrashGroups(pluginId)
    }
    
    /**
     * Get crash details.
     */
    suspend fun getCrashDetails(crashId: String): CrashReport? {
        return crashReporter.getCrashDetails(crashId)
    }
    
    /**
     * Update crash status.
     */
    suspend fun updateCrashStatus(crashGroupId: String, status: CrashStatus): Result<Unit> {
        return crashReporter.updateCrashStatus(crashGroupId, status)
    }
    
    // A/B Testing
    
    /**
     * Load active A/B tests for a plugin.
     */
    suspend fun loadActiveTests(pluginId: String) {
        _activeTests.value = abTestManager.getActiveTests(pluginId)
    }
    
    /**
     * Create a new A/B test.
     */
    suspend fun createABTest(test: ABTest): Result<ABTest> {
        return abTestManager.createTest(test)
    }
    
    /**
     * Get variant for user in a test.
     */
    suspend fun getTestVariant(testId: String, userId: String): ABTestVariant? {
        val variant = abTestManager.getVariantForUser(testId, userId)
        if (variant != null) {
            trackEvent(
                pluginId = _activeTests.value.find { it.id == testId }?.pluginId ?: "",
                eventType = AnalyticsEventType.AB_TEST_EXPOSURE,
                properties = mapOf(
                    "test_id" to testId,
                    "variant_id" to variant.id
                )
            )
        }
        return variant
    }
    
    /**
     * Track A/B test conversion.
     */
    suspend fun trackConversion(testId: String, userId: String, value: Double? = null) {
        val test = _activeTests.value.find { it.id == testId } ?: return
        val metrics = mutableMapOf<String, Double>()
        value?.let { metrics["conversion_value"] = it }
        
        trackEvent(
            pluginId = test.pluginId,
            eventType = AnalyticsEventType.AB_TEST_CONVERSION,
            properties = mapOf("test_id" to testId),
            metrics = metrics
        )
        
        abTestManager.trackConversion(testId, userId, value)
    }
    
    /**
     * Get A/B test results.
     */
    suspend fun getTestResults(testId: String): ABTestResults? {
        return abTestManager.getTestResults(testId)
    }
    
    /**
     * Stop an A/B test.
     */
    suspend fun stopTest(testId: String): Result<Unit> {
        return abTestManager.stopTest(testId)
    }
    
    // Performance Tracking
    
    /**
     * Track a performance metric.
     */
    suspend fun trackPerformanceMetric(
        pluginId: String,
        metricName: String,
        value: Double,
        unit: String,
        tags: Map<String, String> = emptyMap()
    ) {
        val metric = PerformanceMetric(
            pluginId = pluginId,
            metricName = metricName,
            value = value,
            unit = unit,
            timestamp = currentTimeToLong(),
            tags = tags
        )
        analyticsRepository.trackPerformanceMetric(metric)
    }
    
    /**
     * Start a new session.
     */
    fun startNewSession() {
        currentSessionId = generateSessionId()
    }
    
    private fun generateSessionId(): String = "session_${currentTimeToLong()}_${(0..999999).random()}"
    private fun generateEventId(): String = "event_${currentTimeToLong()}_${(0..999999).random()}"
    private fun generateCrashId(): String = "crash_${currentTimeToLong()}_${(0..999999).random()}"
}

/**
 * Repository interface for analytics data.
 */
interface PluginAnalyticsRepository {
    suspend fun trackEvent(event: AnalyticsEvent)
    suspend fun trackPerformanceMetric(metric: PerformanceMetric)
    suspend fun getDeveloperDashboard(developerId: String): DeveloperDashboard?
    suspend fun getPluginUsageStats(pluginId: String): PluginUsageStats?
    suspend fun getUsageTrends(pluginId: String, metricName: String, period: TrendPeriod): UsageTrend?
    suspend fun getPerformanceSummary(pluginId: String, period: TrendPeriod): PerformanceSummary?
    suspend fun getEngagementMetrics(pluginId: String, period: TrendPeriod): EngagementMetrics?
}

/**
 * Interface for crash reporting.
 */
interface CrashReporter {
    suspend fun reportCrash(report: CrashReport)
    fun addBreadcrumb(breadcrumb: Breadcrumb)
    suspend fun getCrashGroups(pluginId: String): List<CrashGroup>
    suspend fun getCrashDetails(crashId: String): CrashReport?
    suspend fun updateCrashStatus(crashGroupId: String, status: CrashStatus): Result<Unit>
}

/**
 * Interface for A/B test management.
 */
interface ABTestManager {
    suspend fun getActiveTests(pluginId: String): List<ABTest>
    suspend fun createTest(test: ABTest): Result<ABTest>
    suspend fun getVariantForUser(testId: String, userId: String): ABTestVariant?
    suspend fun trackConversion(testId: String, userId: String, value: Double?)
    suspend fun getTestResults(testId: String): ABTestResults?
    suspend fun stopTest(testId: String): Result<Unit>
}
