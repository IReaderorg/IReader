package ireader.domain.plugins.communication

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.serialization.Serializable
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Cross-Plugin Communication System
 * 
 * Features:
 * - Plugins can expose APIs to other plugins
 * - Event bus for plugin-to-plugin messaging
 * - Service discovery
 * - Request/response pattern
 * - Pub/sub pattern
 */

/**
 * API exposed by a plugin for other plugins to use.
 */
interface PluginApi {
    /**
     * Unique identifier for this API.
     */
    val apiId: String
    
    /**
     * Plugin that provides this API.
     */
    val providerId: String
    
    /**
     * API version for compatibility checking.
     */
    val version: Int
    
    /**
     * Description of the API.
     */
    val description: String
    
    /**
     * Available methods in this API.
     */
    val methods: List<ApiMethod>
}

/**
 * Method exposed by a plugin API.
 */
@Serializable
data class ApiMethod(
    val name: String,
    val description: String,
    val parameters: List<ApiParameter>,
    val returnType: String,
    val isAsync: Boolean = true
)

@Serializable
data class ApiParameter(
    val name: String,
    val type: String,
    val description: String,
    val isRequired: Boolean = true,
    val defaultValue: String? = null
)

/**
 * Result of an API call.
 */
sealed class ApiCallResult<out T> {
    data class Success<T>(val data: T) : ApiCallResult<T>()
    data class Error(val error: ApiError) : ApiCallResult<Nothing>()
    
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }
    
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw error.toException()
    }
}

/**
 * API call errors.
 */
@Serializable
sealed class ApiError {
    data class ApiNotFound(val apiId: String) : ApiError()
    data class MethodNotFound(val apiId: String, val methodName: String) : ApiError()
    data class PermissionDenied(val reason: String) : ApiError()
    data class InvalidParameters(val message: String) : ApiError()
    data class ExecutionFailed(val message: String) : ApiError()
    data class Timeout(val timeoutMs: Long) : ApiError()
    data class VersionMismatch(val required: Int, val actual: Int) : ApiError()
    
    fun toException(): Exception = when (this) {
        is ApiNotFound -> IllegalArgumentException("API not found: $apiId")
        is MethodNotFound -> IllegalArgumentException("Method not found: $apiId.$methodName")
        is PermissionDenied -> RuntimeException("Permission denied: $reason")
        is InvalidParameters -> IllegalArgumentException("Invalid parameters: $message")
        is ExecutionFailed -> RuntimeException("Execution failed: $message")
        is Timeout -> RuntimeException("API call timed out after ${timeoutMs}ms")
        is VersionMismatch -> IllegalStateException("Version mismatch: required $required, actual $actual")
    }
}

/**
 * Event for plugin-to-plugin communication.
 */
@Serializable
data class PluginEvent(
    val id: String,
    val sourcePluginId: String,
    val eventType: String,
    val timestamp: Long,
    val payload: Map<String, String> = emptyMap(),
    val targetPluginId: String? = null, // null = broadcast to all
    val priority: EventPriority = EventPriority.NORMAL,
    val isSticky: Boolean = false
)

@Serializable
enum class EventPriority {
    LOW,
    NORMAL,
    HIGH,
    CRITICAL
}

/**
 * Subscription to plugin events.
 */
data class EventSubscription(
    val subscriberId: String,
    val eventTypes: Set<String>,
    val sourceFilter: Set<String>? = null, // null = all sources
    val handler: suspend (PluginEvent) -> Unit
)

/**
 * Request for plugin-to-plugin communication.
 */
@Serializable
data class PluginRequest(
    val id: String,
    val sourcePluginId: String,
    val targetPluginId: String,
    val method: String,
    val parameters: Map<String, String> = emptyMap(),
    val timestamp: Long,
    val timeoutMs: Long = 30000
)

/**
 * Response to a plugin request.
 */
@Serializable
data class PluginResponse(
    val requestId: String,
    val sourcePluginId: String,
    val success: Boolean,
    val data: Map<String, String>? = null,
    val errorMessage: String? = null,
    val timestamp: Long
)

/**
 * Service registration for discovery.
 */
@Serializable
data class PluginService(
    val serviceId: String,
    val providerId: String,
    val serviceName: String,
    val description: String,
    val version: Int,
    val capabilities: List<String>,
    val metadata: Map<String, String> = emptyMap(),
    val isAvailable: Boolean = true
)

/**
 * Service query for discovery.
 */
@Serializable
data class ServiceQuery(
    val serviceName: String? = null,
    val capabilities: List<String> = emptyList(),
    val providerId: String? = null,
    val minVersion: Int? = null
)

/**
 * Interface for plugins that expose APIs.
 */
interface ApiProvider {
    /**
     * Get the APIs exposed by this plugin.
     */
    fun getExposedApis(): List<PluginApi>
    
    /**
     * Handle an API call.
     */
    suspend fun handleApiCall(
        apiId: String,
        method: String,
        parameters: Map<String, String>
    ): ApiCallResult<Map<String, String>>
}

/**
 * Interface for plugins that consume APIs.
 */
interface ApiConsumer {
    /**
     * Called when a required API becomes available.
     */
    fun onApiAvailable(api: PluginApi)
    
    /**
     * Called when a required API becomes unavailable.
     */
    fun onApiUnavailable(apiId: String)
    
    /**
     * Get the APIs required by this plugin.
     */
    fun getRequiredApis(): List<String>
    
    /**
     * Get the APIs optionally used by this plugin.
     */
    fun getOptionalApis(): List<String>
}

/**
 * Interface for plugins that handle events.
 */
interface EventHandler {
    /**
     * Get event types this plugin subscribes to.
     */
    fun getSubscribedEventTypes(): Set<String>
    
    /**
     * Handle an incoming event.
     */
    suspend fun handleEvent(event: PluginEvent)
}

/**
 * Interface for plugins that emit events.
 */
interface EventEmitter {
    /**
     * Get event types this plugin can emit.
     */
    fun getEmittedEventTypes(): Set<String>
}

/**
 * Common event types for standardization.
 */
object CommonEventTypes {
    const val BOOK_OPENED = "book.opened"
    const val BOOK_CLOSED = "book.closed"
    const val CHAPTER_CHANGED = "chapter.changed"
    const val TEXT_SELECTED = "text.selected"
    const val READING_PROGRESS = "reading.progress"
    const val TRANSLATION_COMPLETED = "translation.completed"
    const val TTS_STARTED = "tts.started"
    const val TTS_STOPPED = "tts.stopped"
    const val AI_RESPONSE_READY = "ai.response.ready"
    const val THEME_CHANGED = "theme.changed"
    const val PLUGIN_ENABLED = "plugin.enabled"
    const val PLUGIN_DISABLED = "plugin.disabled"
    const val USER_PREFERENCE_CHANGED = "user.preference.changed"
}
