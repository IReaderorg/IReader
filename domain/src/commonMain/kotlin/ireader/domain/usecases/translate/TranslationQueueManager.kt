package ireader.domain.usecases.translate

import ireader.core.log.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Priority levels for translation requests.
 * CONTENT (chapter translation) has higher priority than METADATA (name/description translation).
 */
enum class TranslationPriority {
    /**
     * Chapter content translation - highest priority.
     * When started, all METADATA translations are cancelled.
     */
    CONTENT,
    
    /**
     * Book metadata translation (name, description) - lower priority.
     * Will be cancelled when CONTENT translation starts.
     */
    METADATA
}

/**
 * Represents a translation request in the queue.
 */
data class TranslationRequest(
    val id: String,
    val priority: TranslationPriority,
    val description: String = ""
)

/**
 * Manages translation request queue with priority handling.
 * 
 * Key behaviors:
 * - CONTENT translation (reader chapters) has highest priority
 * - When CONTENT translation starts, all METADATA translations are cancelled
 * - METADATA translations run in parallel but yield to CONTENT translations
 * - Provides centralized state for all translation activities
 * 
 * This ensures that translating book names/descriptions doesn't block
 * reader content translation, and reader content translation takes precedence.
 */
class TranslationQueueManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mutex = Mutex()
    
    companion object {
        private const val TAG = "TranslationQueueManager"
    }
    
    // Current active translation jobs by priority
    private val activeJobs = mutableMapOf<TranslationPriority, MutableList<Job>>()
    
    // State flows for UI observation
    private val _isContentTranslating = MutableStateFlow(false)
    val isContentTranslating: StateFlow<Boolean> = _isContentTranslating.asStateFlow()
    
    private val _isMetadataTranslating = MutableStateFlow(false)
    val isMetadataTranslating: StateFlow<Boolean> = _isMetadataTranslating.asStateFlow()
    
    private val _activeRequests = MutableStateFlow<List<TranslationRequest>>(emptyList())
    val activeRequests: StateFlow<List<TranslationRequest>> = _activeRequests.asStateFlow()
    
    // Track active requests for cancellation
    private val requestMap = mutableMapOf<String, Job>()
    private var requestCounter = 0L
    
    /**
     * Check if any translation is currently active.
     */
    fun isAnyTranslationActive(): Boolean {
        return _isContentTranslating.value || _isMetadataTranslating.value
    }
    
    /**
     * Check if content translation is active.
     * Use this to determine if metadata translations should be skipped.
     */
    fun isContentTranslationActive(): Boolean {
        return _isContentTranslating.value
    }
    
    /**
     * Register a translation request and get a request ID.
     * Returns null if the request should be skipped (e.g., metadata when content is translating).
     * 
     * @param priority The priority level of the translation
     * @param description Human-readable description for debugging
     * @return Request ID if the translation should proceed, null if it should be skipped
     */
    suspend fun registerRequest(
        priority: TranslationPriority,
        description: String = ""
    ): String? {
        return mutex.withLock {
            // If CONTENT translation is starting, cancel all METADATA translations
            if (priority == TranslationPriority.CONTENT) {
                cancelMetadataTranslations()
                _isContentTranslating.value = true
            } else {
                // METADATA translation - check if CONTENT is active
                if (_isContentTranslating.value) {
                    Log.info { "$TAG: Skipping METADATA translation '$description' - CONTENT translation is active" }
                    return null
                }
                _isMetadataTranslating.value = true
            }
            
            val requestId = "trans_${priority.name}_${requestCounter++}"
            val request = TranslationRequest(requestId, priority, description)
            
            // Update active requests list
            val currentRequests = _activeRequests.value.toMutableList()
            currentRequests.add(request)
            _activeRequests.value = currentRequests
            
            Log.info { "$TAG: Registered $priority translation: $requestId - $description" }
            requestId
        }
    }
    
    /**
     * Unregister a translation request when it completes.
     */
    suspend fun unregisterRequest(requestId: String) {
        mutex.withLock {
            val currentRequests = _activeRequests.value.toMutableList()
            val request = currentRequests.find { it.id == requestId }
            
            if (request != null) {
                currentRequests.remove(request)
                _activeRequests.value = currentRequests
                
                // Update state flags
                val hasContent = currentRequests.any { it.priority == TranslationPriority.CONTENT }
                val hasMetadata = currentRequests.any { it.priority == TranslationPriority.METADATA }
                
                _isContentTranslating.value = hasContent
                _isMetadataTranslating.value = hasMetadata
                
                Log.info { "$TAG: Unregistered translation: $requestId" }
            }
            
            // Remove from job map
            requestMap.remove(requestId)
        }
    }
    
    /**
     * Execute a translation with proper queue management.
     * Handles cancellation and cleanup automatically.
     * 
     * @param priority The priority level of the translation
     * @param description Human-readable description
     * @param block The translation work to execute
     * @return Result of the translation (Unit on success), or null if skipped
     */
    suspend fun executeTranslation(
        priority: TranslationPriority,
        description: String = "",
        block: suspend () -> Unit
    ): Result<Unit>? {
        val requestId = registerRequest(priority, description) ?: return null
        
        return try {
            val job = scope.launch {
                block()
            }
            
            // Track the job for potential cancellation
            mutex.withLock {
                requestMap[requestId] = job
            }
            
            // Wait for completion
            job.join()
            
            if (job.isCancelled) {
                Log.info { "$TAG: Translation cancelled: $requestId" }
                Result.failure(CancellationException("Translation cancelled"))
            } else {
                Result.success(Unit)
            }
        } catch (e: CancellationException) {
            Log.info { "$TAG: Translation cancelled: $requestId - ${e.message}" }
            Result.failure(e)
        } catch (e: Exception) {
            Log.error { "$TAG: Translation error: $requestId - ${e.message}" }
            Result.failure(e)
        } finally {
            unregisterRequest(requestId)
        }
    }
    
    /**
     * Cancel all metadata translations.
     * Called when content translation starts.
     */
    private suspend fun cancelMetadataTranslations() {
        val metadataJobs = requestMap.entries
            .filter { it.key.contains("METADATA") }
            .map { it.key to it.value }
        
        if (metadataJobs.isNotEmpty()) {
            Log.info { "$TAG: Cancelling ${metadataJobs.size} METADATA translations for CONTENT priority" }
            
            metadataJobs.forEach { (id, job) ->
                job.cancel()
                requestMap.remove(id)
            }
            
            // Update state
            val currentRequests = _activeRequests.value
                .filter { !it.id.contains("METADATA") }
            _activeRequests.value = currentRequests
            _isMetadataTranslating.value = false
        }
    }
    
    /**
     * Cancel all translations (both CONTENT and METADATA).
     * Use for app shutdown or user-initiated cancellation.
     */
    suspend fun cancelAll() {
        mutex.withLock {
            Log.info { "$TAG: Cancelling all translations (${requestMap.size} active)" }
            
            requestMap.values.forEach { job ->
                job.cancel()
            }
            requestMap.clear()
            
            activeJobs.clear()
            _activeRequests.value = emptyList()
            _isContentTranslating.value = false
            _isMetadataTranslating.value = false
        }
    }
    
    /**
     * Get current translation state for debugging/logging.
     */
    fun getStateSnapshot(): TranslationQueueSnapshot {
        return TranslationQueueSnapshot(
            isContentTranslating = _isContentTranslating.value,
            isMetadataTranslating = _isMetadataTranslating.value,
            activeRequestCount = _activeRequests.value.size,
            activeRequests = _activeRequests.value.toList()
        )
    }
}

/**
 * Snapshot of translation queue state for debugging.
 */
data class TranslationQueueSnapshot(
    val isContentTranslating: Boolean,
    val isMetadataTranslating: Boolean,
    val activeRequestCount: Int,
    val activeRequests: List<TranslationRequest>
)
