package ireader.data.sync.datasource

import ireader.domain.models.sync.DeviceInfo
import ireader.domain.models.sync.DiscoveredDevice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Fake implementation of DiscoveryDataSource for testing.
 */
class FakeDiscoveryDataSource : DiscoveryDataSource {
    
    var isBroadcasting = false
        private set
    
    var isDiscovering = false
        private set
    
    private val _discoveredDevices = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
    
    override suspend fun startBroadcasting(deviceInfo: DeviceInfo): Result<Unit> {
        isBroadcasting = true
        return Result.success(Unit)
    }
    
    override suspend fun stopBroadcasting(): Result<Unit> {
        isBroadcasting = false
        return Result.success(Unit)
    }
    
    override suspend fun startDiscovery(): Result<Unit> {
        isDiscovering = true
        return Result.success(Unit)
    }
    
    override suspend fun stopDiscovery(): Result<Unit> {
        isDiscovering = false
        return Result.success(Unit)
    }
    
    override fun observeDiscoveredDevices(): Flow<List<DiscoveredDevice>> {
        return _discoveredDevices.asStateFlow()
    }
    
    // Test helper methods
    fun addDiscoveredDevice(device: DiscoveredDevice) {
        _discoveredDevices.value = _discoveredDevices.value + device
    }
    
    fun removeDiscoveredDevice(deviceId: String) {
        _discoveredDevices.value = _discoveredDevices.value.filter { it.deviceInfo.deviceId != deviceId }
    }
    
    fun clearDiscoveredDevices() {
        _discoveredDevices.value = emptyList()
    }
    
    private val reachableDevices = mutableMapOf<String, Boolean>()
    
    fun setDeviceReachable(deviceId: String, reachable: Boolean) {
        reachableDevices[deviceId] = reachable
    }
    
    override suspend fun verifyDevice(deviceInfo: DeviceInfo): Result<Boolean> {
        val isReachable = reachableDevices[deviceInfo.deviceId] ?: false
        return Result.success(isReachable)
    }
}
