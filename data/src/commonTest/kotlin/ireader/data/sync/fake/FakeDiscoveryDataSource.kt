package ireader.data.sync.fake

import ireader.domain.models.sync.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Fake implementation of discovery data source for testing.
 * Simulates device discovery without real network operations.
 */
class FakeDiscoveryDataSource {
    
    private val _discoveredDevices = MutableStateFlow<List<DeviceInfo>>(emptyList())
    private val _isDiscovering = MutableStateFlow(false)
    
    private val availableDevices = mutableListOf<DeviceInfo>()
    private var discoveryTimeout: Long = Long.MAX_VALUE
    private var failureMode: FailureMode? = null
    private var maxFailures: Int = 0
    private var currentFailures: Int = 0
    private var retryCount: Int = 0
    
    fun observeDiscoveredDevices(): Flow<List<DeviceInfo>> = _discoveredDevices.asStateFlow()
    
    fun isDiscovering(): Boolean = _isDiscovering.value
    
    suspend fun startDiscovery() {
        _isDiscovering.value = true
        
        // Simulate failures if configured
        if (shouldFail()) {
            currentFailures++
            retryCount++
            if (currentFailures < maxFailures) {
                // Retry
                kotlinx.coroutines.delay(100)
                startDiscovery()
                return
            }
        }
        
        // Simulate discovery delay
        kotlinx.coroutines.delay(100)
        
        _discoveredDevices.value = availableDevices.toList()
        
        // Auto-stop after timeout
        if (discoveryTimeout < Long.MAX_VALUE) {
            kotlinx.coroutines.delay(discoveryTimeout)
            stopDiscovery()
        }
    }
    
    fun stopDiscovery() {
        _isDiscovering.value = false
    }
    
    fun addDiscoverableDevice(device: DeviceInfo) {
        availableDevices.add(device)
    }
    
    fun removeDevice(deviceId: String) {
        availableDevices.removeAll { it.deviceId == deviceId }
        _discoveredDevices.value = _discoveredDevices.value.filter { it.deviceId != deviceId }
    }
    
    fun setDiscoveryTimeout(timeoutMs: Long) {
        discoveryTimeout = timeoutMs
    }
    
    fun setFailureMode(mode: FailureMode, maxFailures: Int = 3) {
        this.failureMode = mode
        this.maxFailures = maxFailures
        this.currentFailures = 0
    }
    
    fun getRetryCount(): Int = retryCount
    
    fun cleanup() {
        availableDevices.clear()
        _discoveredDevices.value = emptyList()
        _isDiscovering.value = false
        currentFailures = 0
        retryCount = 0
    }
    
    private fun shouldFail(): Boolean {
        return when (failureMode) {
            FailureMode.TRANSIENT -> currentFailures < maxFailures
            FailureMode.PERSISTENT -> true
            else -> false
        }
    }
}
