package ireader.domain.plugins.communication

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ireader.core.util.createICoroutineScope
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Event bus for plugin-to-plugin communication.
 */
class PluginEventBus {
    private val scope: CoroutineScope = createICoroutineScope()
    private val mutex = Mutex()
    
    private val _events = MutableSharedFlow<PluginEvent>(
        replay = 0,
        extraBufferCapacity = 100
    )
    val events: SharedFlow<PluginEvent> = _events.asSharedFlow()
    
    private val subscriptions = mutableMapOf<String, EventSubscription>()
    private val stickyEvents = mutableMapOf<String, PluginEvent>()
    
    /**
     * Emit an event to the bus.
     */
    suspend fun emit(event: PluginEvent) {
        if (event.isSticky) {
            mutex.withLock {
                stickyEvents[event.eventType] = event
            }
        }
        _events.emit(event)
        
        // Notify direct subscriptions
        notifySubscribers(event)
    }
    
    /**
     * Emit an event with builder pattern.
     */
    suspend fun emit(
        sourcePluginId: String,
        eventType: String,
        payload: Map<String, String> = emptyMap(),
        targetPluginId: String? = null,
        priority: EventPriority = EventPriority.NORMAL,
        isSticky: Boolean = false
    ) {
        val event = PluginEvent(
            id = generateEventId(),
            sourcePluginId = sourcePluginId,
            eventType = eventType,
            timestamp = currentTimeToLong(),
            payload = payload,
            targetPluginId = targetPluginId,
            priority = priority,
            isSticky = isSticky
        )
        emit(event)
    }

    /**
     * Subscribe to events.
     */
    fun subscribe(
        subscriberId: String,
        eventTypes: Set<String>,
        sourceFilter: Set<String>? = null,
        handler: suspend (PluginEvent) -> Unit
    ): EventSubscription {
        val subscription = EventSubscription(
            subscriberId = subscriberId,
            eventTypes = eventTypes,
            sourceFilter = sourceFilter,
            handler = handler
        )
        
        scope.launch {
            mutex.withLock {
                subscriptions[subscriberId] = subscription
            }
            
            // Deliver sticky events
            stickyEvents.values
                .filter { eventTypes.contains(it.eventType) }
                .filter { sourceFilter == null || sourceFilter.contains(it.sourcePluginId) }
                .forEach { handler(it) }
        }
        
        return subscription
    }
    
    /**
     * Unsubscribe from events.
     */
    suspend fun unsubscribe(subscriberId: String) {
        mutex.withLock {
            subscriptions.remove(subscriberId)
        }
    }
    
    /**
     * Get events as a flow with filtering.
     */
    fun getEventsFlow(
        eventTypes: Set<String>? = null,
        sourceFilter: Set<String>? = null
    ): Flow<PluginEvent> {
        return events.filter { event ->
            val typeMatch = eventTypes == null || eventTypes.contains(event.eventType)
            val sourceMatch = sourceFilter == null || sourceFilter.contains(event.sourcePluginId)
            typeMatch && sourceMatch
        }
    }
    
    /**
     * Get the latest sticky event of a type.
     */
    suspend fun getStickyEvent(eventType: String): PluginEvent? {
        return mutex.withLock {
            stickyEvents[eventType]
        }
    }
    
    /**
     * Remove a sticky event.
     */
    suspend fun removeStickyEvent(eventType: String) {
        mutex.withLock {
            stickyEvents.remove(eventType)
        }
    }
    
    /**
     * Clear all sticky events.
     */
    suspend fun clearStickyEvents() {
        mutex.withLock {
            stickyEvents.clear()
        }
    }
    
    private suspend fun notifySubscribers(event: PluginEvent) {
        val matchingSubscriptions = mutex.withLock {
            subscriptions.values.filter { subscription ->
                val typeMatch = subscription.eventTypes.contains(event.eventType)
                val sourceMatch = subscription.sourceFilter == null || 
                    subscription.sourceFilter.contains(event.sourcePluginId)
                val targetMatch = event.targetPluginId == null || 
                    event.targetPluginId == subscription.subscriberId
                typeMatch && sourceMatch && targetMatch
            }
        }
        
        // Sort by priority and notify
        matchingSubscriptions
            .sortedByDescending { event.priority.ordinal }
            .forEach { subscription ->
                scope.launch {
                    try {
                        subscription.handler(event)
                    } catch (e: Exception) {
                        // Log error but don't crash
                    }
                }
            }
    }
    
    private fun generateEventId(): String = "evt_${currentTimeToLong()}_${(0..999999).random()}"
}

/**
 * Service registry for plugin service discovery.
 */
class PluginServiceRegistry {
    private val mutex = Mutex()
    private val services = mutableMapOf<String, PluginService>()
    private val servicesByProvider = mutableMapOf<String, MutableSet<String>>()
    
    /**
     * Register a service.
     */
    suspend fun registerService(service: PluginService) {
        mutex.withLock {
            services[service.serviceId] = service
            servicesByProvider.getOrPut(service.providerId) { mutableSetOf() }
                .add(service.serviceId)
        }
    }
    
    /**
     * Unregister a service.
     */
    suspend fun unregisterService(serviceId: String) {
        mutex.withLock {
            val service = services.remove(serviceId)
            service?.let {
                servicesByProvider[it.providerId]?.remove(serviceId)
            }
        }
    }
    
    /**
     * Unregister all services from a provider.
     */
    suspend fun unregisterProvider(providerId: String) {
        mutex.withLock {
            servicesByProvider[providerId]?.forEach { serviceId ->
                services.remove(serviceId)
            }
            servicesByProvider.remove(providerId)
        }
    }
    
    /**
     * Query services.
     */
    suspend fun queryServices(query: ServiceQuery): List<PluginService> {
        return mutex.withLock {
            services.values.filter { service ->
                val nameMatch = query.serviceName == null || 
                    service.serviceName == query.serviceName
                val capabilityMatch = query.capabilities.isEmpty() || 
                    query.capabilities.all { service.capabilities.contains(it) }
                val providerMatch = query.providerId == null || 
                    service.providerId == query.providerId
                val versionMatch = query.minVersion == null || 
                    service.version >= query.minVersion
                
                nameMatch && capabilityMatch && providerMatch && versionMatch && service.isAvailable
            }
        }
    }
    
    /**
     * Get a specific service.
     */
    suspend fun getService(serviceId: String): PluginService? {
        return mutex.withLock {
            services[serviceId]
        }
    }
    
    /**
     * Get all services from a provider.
     */
    suspend fun getProviderServices(providerId: String): List<PluginService> {
        return mutex.withLock {
            servicesByProvider[providerId]?.mapNotNull { services[it] } ?: emptyList()
        }
    }
    
    /**
     * Update service availability.
     */
    suspend fun setServiceAvailability(serviceId: String, isAvailable: Boolean) {
        mutex.withLock {
            services[serviceId]?.let {
                services[serviceId] = it.copy(isAvailable = isAvailable)
            }
        }
    }
}
