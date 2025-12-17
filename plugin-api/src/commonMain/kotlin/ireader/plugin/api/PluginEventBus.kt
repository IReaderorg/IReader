package ireader.plugin.api

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

/**
 * Event bus for plugin-to-plugin and plugin-to-app communication.
 * Enables loose coupling between plugins and the app.
 */
interface PluginEventBus {
    /**
     * Publish an event to the bus.
     * @param event The event to publish
     */
    suspend fun publish(event: PluginEvent)
    
    /**
     * Subscribe to events of a specific type.
     * @param eventType The type of events to subscribe to
     * @return Flow of events
     */
    fun subscribe(eventType: String): Flow<PluginEvent>
    
    /**
     * Subscribe to all events.
     * @return Flow of all events
     */
    fun subscribeAll(): Flow<PluginEvent>
    
    /**
     * Subscribe to events from a specific plugin.
     * @param pluginId The plugin ID to filter by
     * @return Flow of events from that plugin
     */
    fun subscribeFromPlugin(pluginId: String): Flow<PluginEvent>
    
    /**
     * Request data from another plugin.
     * @param targetPluginId The plugin to request from
     * @param requestType The type of request
     * @param data Request data
     * @return Response data or null if not handled
     */
    suspend fun request(
        targetPluginId: String,
        requestType: String,
        data: Map<String, Any> = emptyMap()
    ): PluginResponse?
    
    /**
     * Register a request handler.
     * @param requestType The type of requests to handle
     * @param handler The handler function
     */
    fun registerRequestHandler(
        requestType: String,
        handler: suspend (PluginRequest) -> PluginResponse
    )
    
    /**
     * Unregister a request handler.
     */
    fun unregisterRequestHandler(requestType: String)
}

/**
 * Plugin event for the event bus.
 */
@Serializable
data class PluginEvent(
    /** Event type identifier */
    val type: String,
    /** Source plugin ID */
    val sourcePluginId: String,
    /** Event timestamp */
    val timestamp: Long,
    /** Event data */
    val data: Map<String, String> = emptyMap(),
    /** Target plugin ID (null for broadcast) */
    val targetPluginId: String? = null,
    /** Event priority */
    val priority: EventPriority = EventPriority.NORMAL
)

@Serializable
enum class EventPriority {
    LOW,
    NORMAL,
    HIGH,
    CRITICAL
}

/**
 * Plugin request for inter-plugin communication.
 */
@Serializable
data class PluginRequest(
    val requestId: String,
    val requestType: String,
    val sourcePluginId: String,
    val data: Map<String, String> = emptyMap(),
    val timestamp: Long
)

/**
 * Plugin response for inter-plugin communication.
 */
@Serializable
data class PluginResponse(
    val requestId: String,
    val success: Boolean,
    val data: Map<String, String> = emptyMap(),
    val errorMessage: String? = null
)

/**
 * Common event types for standardization.
 */
object PluginEventTypes {
    // Reading events
    const val BOOK_OPENED = "book.opened"
    const val BOOK_CLOSED = "book.closed"
    const val CHAPTER_STARTED = "chapter.started"
    const val CHAPTER_FINISHED = "chapter.finished"
    const val TEXT_SELECTED = "text.selected"
    const val PAGE_TURNED = "page.turned"
    
    // Library events
    const val BOOK_ADDED = "library.book.added"
    const val BOOK_REMOVED = "library.book.removed"
    const val BOOK_UPDATED = "library.book.updated"
    const val CATEGORY_CHANGED = "library.category.changed"
    
    // Sync events
    const val SYNC_STARTED = "sync.started"
    const val SYNC_COMPLETED = "sync.completed"
    const val SYNC_FAILED = "sync.failed"
    const val SYNC_CONFLICT = "sync.conflict"
    
    // TTS events
    const val TTS_STARTED = "tts.started"
    const val TTS_STOPPED = "tts.stopped"
    const val TTS_PAUSED = "tts.paused"
    const val TTS_RESUMED = "tts.resumed"
    const val TTS_CHAPTER_COMPLETED = "tts.chapter.completed"
    
    // Translation events
    const val TRANSLATION_REQUESTED = "translation.requested"
    const val TRANSLATION_COMPLETED = "translation.completed"
    
    // AI events
    const val AI_PROCESSING_STARTED = "ai.processing.started"
    const val AI_PROCESSING_COMPLETED = "ai.processing.completed"
    const val CHARACTER_DETECTED = "ai.character.detected"
    const val SUMMARY_GENERATED = "ai.summary.generated"
    
    // Image events
    const val IMAGE_GENERATED = "image.generated"
    const val IMAGE_UPSCALED = "image.upscaled"
    
    // Glossary events
    const val GLOSSARY_TERM_ADDED = "glossary.term.added"
    const val GLOSSARY_APPLIED = "glossary.applied"
    
    // Plugin events
    const val PLUGIN_ENABLED = "plugin.enabled"
    const val PLUGIN_DISABLED = "plugin.disabled"
    const val PLUGIN_UPDATED = "plugin.updated"
    const val PLUGIN_ERROR = "plugin.error"
}

/**
 * Common request types for standardization.
 */
object PluginRequestTypes {
    // TTS requests
    const val TTS_SPEAK = "tts.speak"
    const val TTS_GET_VOICES = "tts.getVoices"
    
    // Translation requests
    const val TRANSLATE_TEXT = "translation.translate"
    const val TRANSLATE_BATCH = "translation.translateBatch"
    
    // AI requests
    const val AI_SUMMARIZE = "ai.summarize"
    const val AI_ANALYZE_CHARACTERS = "ai.analyzeCharacters"
    const val AI_GENERATE_IMAGE = "ai.generateImage"
    
    // Glossary requests
    const val GLOSSARY_LOOKUP = "glossary.lookup"
    const val GLOSSARY_APPLY = "glossary.apply"
    
    // Sync requests
    const val SYNC_NOW = "sync.now"
    const val SYNC_GET_STATUS = "sync.getStatus"
    
    // Image requests
    const val IMAGE_UPSCALE = "image.upscale"
    const val IMAGE_ENHANCE = "image.enhance"
}