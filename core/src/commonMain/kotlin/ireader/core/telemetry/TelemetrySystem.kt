package ireader.core.telemetry

import ireader.core.log.IReaderLog
import ireader.core.prefs.PrivacyPreferences
import ireader.core.system.SystemCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

/**
 * Telemetry system for tracking app usage and performance
 * Respects user privacy preferences
 */
@OptIn(ExperimentalTime::class)
class TelemetrySystem(
    private val privacyPreferences: PrivacyPreferences,
    private val scope: CoroutineScope
) {
    
    private val events = mutableListOf<TelemetryEvent>()
    
    /**
     * Track an event
     */
    fun trackEvent(
        eventName: String,
        properties: Map<String, Any> = emptyMap()
    ) {
        scope.launch {
            // Check if telemetry is enabled (simplified for now)
            // TODO: Implement proper privacy preferences check
            
            val event = TelemetryEvent(
                name = eventName,
                timestamp = kotlin.time.Clock.System.now(),
                properties = properties
            )
            
            events.add(event)
            IReaderLog.debug("Telemetry event: $eventName", tag = "Telemetry")
        }
    }
    
    /**
     * Track a metric
     */
    fun trackMetric(
        metricName: String,
        value: Double,
        properties: Map<String, Any> = emptyMap()
    ) {
        scope.launch {
            // Check if telemetry is enabled (simplified for now)
            // TODO: Implement proper privacy preferences check
            
            val event = TelemetryEvent(
                name = metricName,
                timestamp = kotlin.time.Clock.System.now(),
                value = value,
                properties = properties
            )
            
            events.add(event)
            IReaderLog.debug("Telemetry metric: $metricName = $value", tag = "Telemetry")
        }
    }
    
    /**
     * Track an error
     */
    fun trackError(
        error: Throwable,
        context: String? = null,
        properties: Map<String, Any> = emptyMap()
    ) {
        scope.launch {
            // Check if crash reporting is enabled (simplified for now)
            // TODO: Implement proper privacy preferences check
            
            val errorProperties = properties.toMutableMap()
            errorProperties["error_type"] = error::class.simpleName ?: "Unknown"
            errorProperties["error_message"] = error.message ?: "No message"
            if (context != null) {
                errorProperties["context"] = context
            }
            
            val event = TelemetryEvent(
                name = "error",
                timestamp = kotlin.time.Clock.System.now(),
                properties = errorProperties
            )
            
            events.add(event)
            IReaderLog.error("Telemetry error tracked: ${error.message}", error, tag = "Telemetry")
        }
    }
    
    /**
     * Track screen view
     */
    fun trackScreenView(screenName: String) {
        trackEvent("screen_view", mapOf("screen_name" to screenName))
    }
    
    /**
     * Track user action
     */
    fun trackUserAction(
        actionName: String,
        properties: Map<String, Any> = emptyMap()
    ) {
        trackEvent("user_action", properties + mapOf("action_name" to actionName))
    }
    
    /**
     * Get all events
     */
    fun getEvents(): List<TelemetryEvent> = events.toList()
    
    /**
     * Clear all events
     */
    fun clearEvents() {
        events.clear()
        IReaderLog.info("Telemetry events cleared", tag = "Telemetry")
    }
    
    /**
     * Get events count
     */
    fun getEventsCount(): Int = events.size
    
    /**
     * Export events to string
     */
    fun exportEvents(): String {
        return buildString {
            appendLine("=== Telemetry Events ===")
            appendLine("Total Events: ${events.size}")
            appendLine()
            
            events.forEach { event ->
                appendLine("Event: ${event.name}")
                appendLine("  Timestamp: ${event.timestamp}")
                if (event.value != null) {
                    appendLine("  Value: ${event.value}")
                }
                if (event.properties.isNotEmpty()) {
                    appendLine("  Properties:")
                    event.properties.forEach { (key, value) ->
                        appendLine("    $key: $value")
                    }
                }
                appendLine()
            }
        }
    }
    
    /**
     * Log telemetry summary
     */
    fun logSummary() {
        val summary = buildString {
            appendLine("Telemetry Summary:")
            appendLine("  Total Events: ${events.size}")
            appendLine("  Event Types: ${events.map { it.name }.distinct().size}")
        }
        IReaderLog.info(summary, tag = "Telemetry")
    }
    
    /**
     * Measure and track operation duration
     */
    inline fun <T> measureAndTrack(
        operationName: String,
        properties: Map<String, Any> = emptyMap(),
        block: () -> T
    ): T {
        val startTime = ireader.core.system.SystemCompat.currentTimeMillis()
        val result = block()
        val endTime = ireader.core.system.SystemCompat.currentTimeMillis()
        val duration = endTime - startTime
        
        trackMetric(
            metricName = "${operationName}_duration",
            value = duration.toDouble(),
            properties = properties
        )
        
        return result
    }
}

/**
 * Telemetry event data class
 */
data class TelemetryEvent @OptIn(ExperimentalTime::class) constructor(
    val name: String,
    val timestamp: kotlin.time.Instant,
    val value: Double? = null,
    val properties: Map<String, Any> = emptyMap()
)
