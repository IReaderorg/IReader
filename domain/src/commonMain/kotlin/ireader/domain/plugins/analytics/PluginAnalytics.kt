package ireader.domain.plugins.analytics

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Plugin Analytics Dashboard
 * 
 * Features:
 * - Usage statistics for developers
 * - Crash reports and error tracking
 * - A/B testing support
 * - Performance metrics
 * - User engagement analytics
 */

/**
 * Analytics event types.
 */
@Serializable
enum class AnalyticsEventType {
    PLUGIN_INSTALLED,
    PLUGIN_UNINSTALLED,
    PLUGIN_ENABLED,
    PLUGIN_DISABLED,
    PLUGIN_USED,
    PLUGIN_ERROR,
    PLUGIN_CRASH,
    FEATURE_USED,
    SETTING_CHANGED,
    AB_TEST_EXPOSURE,
    AB_TEST_CONVERSION,
    PERFORMANCE_METRIC,
    USER_FEEDBACK
}

/**
 * Analytics event data.
 */
@Serializable
data class AnalyticsEvent(
    val id: String,
    val pluginId: String,
    val eventType: AnalyticsEventType,
    val timestamp: Long,
    val sessionId: String,
    val userId: String?,
    val properties: Map<String, String> = emptyMap(),
    val metrics: Map<String, Double> = emptyMap(),
    val deviceInfo: DeviceInfo? = null
)

/**
 * Device information for analytics.
 */
@Serializable
data class DeviceInfo(
    val platform: String,
    val osVersion: String,
    val appVersion: String,
    val deviceModel: String?,
    val locale: String,
    val timezone: String
)

/**
 * Plugin usage statistics.
 */
@Serializable
data class PluginUsageStats(
    val pluginId: String,
    val totalInstalls: Long,
    val activeInstalls: Long,
    val totalUninstalls: Long,
    val dailyActiveUsers: Int,
    val weeklyActiveUsers: Int,
    val monthlyActiveUsers: Int,
    val averageSessionDuration: Long,
    val totalSessions: Long,
    val retentionRate: Float,
    val crashFreeRate: Float,
    val averageRating: Float,
    val ratingCount: Int,
    val lastUpdated: Long
)

/**
 * Time-series data point.
 */
@Serializable
data class TimeSeriesDataPoint(
    val timestamp: Long,
    val value: Double,
    val label: String? = null
)

/**
 * Usage trend data.
 */
@Serializable
data class UsageTrend(
    val pluginId: String,
    val metricName: String,
    val period: TrendPeriod,
    val dataPoints: List<TimeSeriesDataPoint>,
    val changePercent: Float,
    val trend: TrendDirection
)

@Serializable
enum class TrendPeriod {
    HOURLY,
    DAILY,
    WEEKLY,
    MONTHLY
}

@Serializable
enum class TrendDirection {
    UP,
    DOWN,
    STABLE
}

/**
 * Crash report data.
 */
@Serializable
data class CrashReport(
    val id: String,
    val pluginId: String,
    val pluginVersion: String,
    val timestamp: Long,
    val errorType: String,
    val errorMessage: String,
    val stackTrace: String,
    val deviceInfo: DeviceInfo,
    val breadcrumbs: List<Breadcrumb> = emptyList(),
    val customData: Map<String, String> = emptyMap(),
    val occurrenceCount: Int = 1,
    val affectedUsers: Int = 1,
    val status: CrashStatus = CrashStatus.NEW,
    val assignedTo: String? = null,
    val resolvedInVersion: String? = null
)

@Serializable
data class Breadcrumb(
    val timestamp: Long,
    val category: String,
    val message: String,
    val level: BreadcrumbLevel
)

@Serializable
enum class BreadcrumbLevel {
    DEBUG,
    INFO,
    WARNING,
    ERROR
}

@Serializable
enum class CrashStatus {
    NEW,
    INVESTIGATING,
    IN_PROGRESS,
    RESOLVED,
    WONT_FIX,
    DUPLICATE
}

/**
 * Crash group for aggregating similar crashes.
 */
@Serializable
data class CrashGroup(
    val id: String,
    val pluginId: String,
    val errorType: String,
    val errorMessage: String,
    val firstOccurrence: Long,
    val lastOccurrence: Long,
    val occurrenceCount: Int,
    val affectedUsers: Int,
    val affectedVersions: List<String>,
    val status: CrashStatus,
    val sampleCrashId: String
)

/**
 * A/B test configuration.
 */
@Serializable
data class ABTest(
    val id: String,
    val pluginId: String,
    val name: String,
    val description: String,
    val variants: List<ABTestVariant>,
    val targetAudience: TargetAudience,
    val startDate: Long,
    val endDate: Long?,
    val status: ABTestStatus,
    val primaryMetric: String,
    val secondaryMetrics: List<String> = emptyList(),
    val minimumSampleSize: Int = 1000,
    val confidenceLevel: Float = 0.95f
)

@Serializable
data class ABTestVariant(
    val id: String,
    val name: String,
    val description: String,
    val weight: Float,
    val config: Map<String, String> = emptyMap()
)

@Serializable
data class TargetAudience(
    val percentage: Float = 100f,
    val platforms: List<String> = emptyList(),
    val appVersions: List<String> = emptyList(),
    val locales: List<String> = emptyList(),
    val customFilters: Map<String, String> = emptyMap()
)

@Serializable
enum class ABTestStatus {
    DRAFT,
    RUNNING,
    PAUSED,
    COMPLETED,
    ARCHIVED
}

/**
 * A/B test results.
 */
@Serializable
data class ABTestResults(
    val testId: String,
    val variantResults: List<VariantResult>,
    val winner: String?,
    val statisticalSignificance: Float,
    val sampleSize: Int,
    val duration: Long,
    val lastUpdated: Long
)

@Serializable
data class VariantResult(
    val variantId: String,
    val variantName: String,
    val sampleSize: Int,
    val conversionRate: Float,
    val averageValue: Double,
    val confidenceInterval: ConfidenceInterval,
    val improvement: Float?
)

@Serializable
data class ConfidenceInterval(
    val lower: Float,
    val upper: Float,
    val level: Float
)

/**
 * Performance metric data.
 */
@Serializable
data class PerformanceMetric(
    val pluginId: String,
    val metricName: String,
    val value: Double,
    val unit: String,
    val timestamp: Long,
    val percentile: Int? = null,
    val tags: Map<String, String> = emptyMap()
)

/**
 * Performance summary for a plugin.
 */
@Serializable
data class PerformanceSummary(
    val pluginId: String,
    val period: TrendPeriod,
    val loadTimeP50: Long,
    val loadTimeP95: Long,
    val loadTimeP99: Long,
    val memoryUsageAvg: Long,
    val memoryUsageMax: Long,
    val cpuUsageAvg: Float,
    val networkRequestsAvg: Int,
    val networkLatencyP50: Long,
    val errorRate: Float,
    val crashRate: Float
)

/**
 * User engagement metrics.
 */
@Serializable
data class EngagementMetrics(
    val pluginId: String,
    val period: TrendPeriod,
    val sessionsPerUser: Float,
    val averageSessionLength: Long,
    val featureUsage: Map<String, Int>,
    val retentionDay1: Float,
    val retentionDay7: Float,
    val retentionDay30: Float,
    val churnRate: Float,
    val npsScore: Float?
)

/**
 * Dashboard summary for developers.
 */
@Serializable
data class DeveloperDashboard(
    val developerId: String,
    val plugins: List<PluginDashboardSummary>,
    val totalDownloads: Long,
    val totalActiveUsers: Int,
    val totalRevenue: Double,
    val overallRating: Float,
    val topCrashes: List<CrashGroup>,
    val recentActivity: List<AnalyticsEvent>,
    val lastUpdated: Long
)

@Serializable
data class PluginDashboardSummary(
    val pluginId: String,
    val pluginName: String,
    val usageStats: PluginUsageStats,
    val performanceSummary: PerformanceSummary,
    val crashFreeRate: Float,
    val rating: Float,
    val revenue: Double,
    val activeABTests: Int
)
