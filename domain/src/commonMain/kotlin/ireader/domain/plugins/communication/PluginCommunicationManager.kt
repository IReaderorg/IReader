package ireader.domain.plugins.communication

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import ireader.core.util.createICoroutineScope
import ireader.domain.plugins.Plugin
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Manager for cross-plugin communication.
 */
class PluginCommunicationManager(
    private val eventBus: PluginEventBus,
    private val serviceRegistry: PluginServiceRegistry
) {
    private val scope: CoroutineScope = createICoroutineScope()
    private val mutex = Mutex()
    
    private val apiProviders = mutableMapOf<String, ApiProvider>()
    private val registeredApis = mutableMapOf<String, PluginApi>()
    private val pendingRequests = mutableMapOf<String, CompletableDeferred<PluginResponse>>()
    
    private val _availableApis = MutableStateFlow<List<PluginApi>>(emptyList())
    val availableApis: StateFlow<List<PluginApi>> = _availableApis.asStateFlow()
    
    init {
        // Listen for plugin responses
        scope.launch {
            eventBus.getEventsFlow(setOf("plugin.response")).collect { event ->
                val requestId = event.payload["request_id"] ?: return@collect
                val deferred = mutex.withLock { pendingRequests.remove(requestId) }
                deferred?.complete(
                    PluginResponse(
                        requestId = requestId,
                        sourcePluginId = event.sourcePluginId,
                        success = event.payload["success"] == "true",
                        data = event.payload.filterKeys { it != "request_id" && it != "success" },
                        errorMessage = event.payload["error"],
                        timestamp = event.timestamp
                    )
                )
            }
        }
    }

    /**
     * Register a plugin as an API provider.
     */
    suspend fun registerApiProvider(plugin: Plugin, provider: ApiProvider) {
        mutex.withLock {
            apiProviders[plugin.manifest.id] = provider
            provider.getExposedApis().forEach { api ->
                registeredApis[api.apiId] = api
            }
            updateAvailableApis()
        }
        
        // Register services
        provider.getExposedApis().forEach { api ->
            serviceRegistry.registerService(
                PluginService(
                    serviceId = api.apiId,
                    providerId = api.providerId,
                    serviceName = api.apiId,
                    description = api.description,
                    version = api.version,
                    capabilities = api.methods.map { it.name }
                )
            )
        }
        
        // Emit API available event
        eventBus.emit(
            sourcePluginId = plugin.manifest.id,
            eventType = "api.available",
            payload = mapOf(
                "apis" to provider.getExposedApis().joinToString(",") { it.apiId }
            ),
            isSticky = true
        )
    }
    
    /**
     * Unregister a plugin's APIs.
     */
    suspend fun unregisterApiProvider(pluginId: String) {
        mutex.withLock {
            val provider = apiProviders.remove(pluginId)
            provider?.getExposedApis()?.forEach { api ->
                registeredApis.remove(api.apiId)
            }
            updateAvailableApis()
        }
        
        serviceRegistry.unregisterProvider(pluginId)
        
        eventBus.emit(
            sourcePluginId = pluginId,
            eventType = "api.unavailable",
            payload = mapOf("provider_id" to pluginId)
        )
    }
    
    /**
     * Call an API method.
     */
    suspend fun callApi(
        callerPluginId: String,
        apiId: String,
        method: String,
        parameters: Map<String, String> = emptyMap(),
        timeoutMs: Long = 30000
    ): ApiCallResult<Map<String, String>> {
        val api = mutex.withLock { registeredApis[apiId] }
            ?: return ApiCallResult.Error(ApiError.ApiNotFound(apiId))
        
        val apiMethod = api.methods.find { it.name == method }
            ?: return ApiCallResult.Error(ApiError.MethodNotFound(apiId, method))
        
        // Validate required parameters
        val missingParams = apiMethod.parameters
            .filter { it.isRequired }
            .filter { !parameters.containsKey(it.name) }
        
        if (missingParams.isNotEmpty()) {
            return ApiCallResult.Error(
                ApiError.InvalidParameters("Missing required parameters: ${missingParams.map { it.name }}")
            )
        }
        
        val provider = mutex.withLock { apiProviders[api.providerId] }
            ?: return ApiCallResult.Error(ApiError.ApiNotFound(apiId))
        
        return try {
            withTimeout(timeoutMs) {
                provider.handleApiCall(apiId, method, parameters)
            }
        } catch (e: Exception) {
            ApiCallResult.Error(ApiError.ExecutionFailed(e.message ?: "Unknown error"))
        }
    }
    
    /**
     * Send a request to another plugin.
     */
    suspend fun sendRequest(
        sourcePluginId: String,
        targetPluginId: String,
        method: String,
        parameters: Map<String, String> = emptyMap(),
        timeoutMs: Long = 30000
    ): Result<PluginResponse> {
        val requestId = generateRequestId()
        val deferred = CompletableDeferred<PluginResponse>()
        
        mutex.withLock {
            pendingRequests[requestId] = deferred
        }
        
        val request = PluginRequest(
            id = requestId,
            sourcePluginId = sourcePluginId,
            targetPluginId = targetPluginId,
            method = method,
            parameters = parameters,
            timestamp = currentTimeToLong(),
            timeoutMs = timeoutMs
        )
        
        eventBus.emit(
            sourcePluginId = sourcePluginId,
            eventType = "plugin.request",
            payload = mapOf(
                "request_id" to requestId,
                "method" to method,
                "target" to targetPluginId
            ) + parameters,
            targetPluginId = targetPluginId
        )
        
        return try {
            val response = withTimeout(timeoutMs) { deferred.await() }
            Result.success(response)
        } catch (e: Exception) {
            mutex.withLock { pendingRequests.remove(requestId) }
            Result.failure(e)
        }
    }
    
    /**
     * Send a response to a request.
     */
    suspend fun sendResponse(
        sourcePluginId: String,
        requestId: String,
        success: Boolean,
        data: Map<String, String>? = null,
        errorMessage: String? = null
    ) {
        val payload = mutableMapOf(
            "request_id" to requestId,
            "success" to success.toString()
        )
        data?.let { payload.putAll(it) }
        errorMessage?.let { payload["error"] = it }
        
        eventBus.emit(
            sourcePluginId = sourcePluginId,
            eventType = "plugin.response",
            payload = payload
        )
    }
    
    /**
     * Get available APIs.
     */
    suspend fun getAvailableApis(): List<PluginApi> {
        return mutex.withLock { registeredApis.values.toList() }
    }
    
    /**
     * Get API by ID.
     */
    suspend fun getApi(apiId: String): PluginApi? {
        return mutex.withLock { registeredApis[apiId] }
    }
    
    /**
     * Check if an API is available.
     */
    suspend fun isApiAvailable(apiId: String): Boolean {
        return mutex.withLock { registeredApis.containsKey(apiId) }
    }
    
    /**
     * Discover services matching a query.
     */
    suspend fun discoverServices(query: ServiceQuery): List<PluginService> {
        return serviceRegistry.queryServices(query)
    }
    
    /**
     * Subscribe to events.
     */
    fun subscribeToEvents(
        subscriberId: String,
        eventTypes: Set<String>,
        handler: suspend (PluginEvent) -> Unit
    ): EventSubscription {
        return eventBus.subscribe(subscriberId, eventTypes, handler = handler)
    }
    
    /**
     * Emit an event.
     */
    suspend fun emitEvent(
        sourcePluginId: String,
        eventType: String,
        payload: Map<String, String> = emptyMap(),
        targetPluginId: String? = null
    ) {
        eventBus.emit(sourcePluginId, eventType, payload, targetPluginId)
    }
    
    /**
     * Get events flow.
     */
    fun getEventsFlow(eventTypes: Set<String>? = null): Flow<PluginEvent> {
        return eventBus.getEventsFlow(eventTypes)
    }
    
    private fun updateAvailableApis() {
        _availableApis.value = registeredApis.values.toList()
    }
    
    private fun generateRequestId(): String = "req_${currentTimeToLong()}_${(0..999999).random()}"
}
