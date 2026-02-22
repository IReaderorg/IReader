package ireader.domain.repositories

import ireader.domain.models.sync.TrustedDevice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Fake implementation of TrustedDeviceRepository for testing.
 * 
 * Provides an in-memory storage for trusted devices that can be used
 * in unit tests without requiring actual database or network access.
 */
class FakeTrustedDeviceRepository : TrustedDeviceRepository {
    
    private val devices = mutableMapOf<String, TrustedDevice>()
    private val devicesFlow = MutableStateFlow<Map<String, TrustedDevice>>(emptyMap())
    
    override suspend fun getTrustedDevice(deviceId: String): TrustedDevice? {
        return devices[deviceId]
    }
    
    override suspend fun upsertTrustedDevice(device: TrustedDevice) {
        devices[device.deviceId] = device
        devicesFlow.value = devices.toMap()
    }
    
    override fun getActiveTrustedDevices(): Flow<List<TrustedDevice>> {
        return devicesFlow.map { devicesMap ->
            devicesMap.values.filter { it.isActive }
        }
    }
    
    override suspend fun deactivateTrustedDevice(deviceId: String) {
        devices[deviceId]?.let { device ->
            devices[deviceId] = device.copy(isActive = false)
            devicesFlow.value = devices.toMap()
        }
    }
    
    override suspend fun updateDeviceExpiration(deviceId: String, expiresAt: Long) {
        devices[deviceId]?.let { device ->
            devices[deviceId] = device.copy(expiresAt = expiresAt)
            devicesFlow.value = devices.toMap()
        }
    }
    
    override suspend fun deleteTrustedDevice(deviceId: String) {
        devices.remove(deviceId)
        devicesFlow.value = devices.toMap()
    }
    
    /**
     * Clear all devices from storage.
     * Useful for resetting state between tests.
     */
    fun clear() {
        devices.clear()
        devicesFlow.value = emptyMap()
    }
}
