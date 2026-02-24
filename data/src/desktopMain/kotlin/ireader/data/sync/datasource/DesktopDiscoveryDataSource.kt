package ireader.data.sync.datasource

import ireader.domain.models.sync.DeviceInfo
import ireader.domain.models.sync.DeviceType
import ireader.domain.models.sync.DiscoveredDevice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceInfo
import javax.jmdns.ServiceListener

/**
 * Desktop implementation of DiscoveryDataSource using JmDNS.
 * 
 * Uses JmDNS library for mDNS service discovery on Desktop platforms.
 * Service type: "_ireader-sync._tcp.local."
 */
class DesktopDiscoveryDataSource : DiscoveryDataSource {
    
    private var jmdns: JmDNS? = null
    private var serviceInfo: ServiceInfo? = null
    
    private val discoveredDevicesFlow = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
    private val discoveredDevicesMap = mutableMapOf<String, DiscoveredDevice>()
    
    private var currentDeviceInfo: DeviceInfo? = null
    private var serviceListener: ServiceListener? = null
    
    companion object {
        private const val SERVICE_TYPE = "_ireader-sync._tcp.local."
        private const val SERVICE_NAME_PREFIX = "IReader"
    }
    
    override suspend fun startBroadcasting(deviceInfo: DeviceInfo): Result<Unit> {
        return try {
            currentDeviceInfo = deviceInfo
            
            // Create JmDNS instance if not exists
            if (jmdns == null) {
                jmdns = JmDNS.create(InetAddress.getLocalHost())
            }
            
            // Create service info
            val serviceName = "$SERVICE_NAME_PREFIX-${deviceInfo.deviceName}"
            val info = ServiceInfo.create(
                SERVICE_TYPE,
                serviceName,
                deviceInfo.port,
                0,
                0,
                mapOf(
                    "deviceId" to deviceInfo.deviceId,
                    "deviceName" to deviceInfo.deviceName,
                    "deviceType" to deviceInfo.deviceType.name,
                    "appVersion" to deviceInfo.appVersion
                )
            )
            
            serviceInfo = info
            jmdns?.registerService(info)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun stopBroadcasting(): Result<Unit> {
        return try {
            serviceInfo?.let { info ->
                jmdns?.unregisterService(info)
                serviceInfo = null
            }
            currentDeviceInfo = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun startDiscovery(): Result<Unit> {
        return try {
            // Stop any existing discovery first to clear cache
            serviceListener?.let { listener ->
                try {
                    jmdns?.removeServiceListener(SERVICE_TYPE, listener)
                } catch (e: Exception) {
                    // Ignore - might not be running
                }
            }
            
            // Clear old discovered devices when starting new discovery
            synchronized(discoveredDevicesMap) {
                discoveredDevicesMap.clear()
                discoveredDevicesFlow.value = emptyList()
            }
            
            // Create JmDNS instance if not exists
            if (jmdns == null) {
                jmdns = JmDNS.create(InetAddress.getLocalHost())
            }
            
            val listener = object : ServiceListener {
                override fun serviceAdded(event: ServiceEvent?) {
                    event?.let {
                        // Request service info to get full details
                        jmdns?.requestServiceInfo(it.type, it.name, 5000)
                    }
                }
                
                override fun serviceRemoved(event: ServiceEvent?) {
                    event?.let {
                        val info = it.info
                        val deviceId = info.getPropertyString("deviceId")
                        deviceId?.let { id ->
                            synchronized(discoveredDevicesMap) {
                                discoveredDevicesMap.remove(id)
                                discoveredDevicesFlow.value = discoveredDevicesMap.values.toList()
                            }
                        }
                    }
                }
                
                override fun serviceResolved(event: ServiceEvent?) {
                    event?.let {
                        val device = parseServiceInfo(it.info)
                        device?.let { discovered ->
                            // Don't add self
                            if (discovered.deviceInfo.deviceId != currentDeviceInfo?.deviceId) {
                                synchronized(discoveredDevicesMap) {
                                    discoveredDevicesMap[discovered.deviceInfo.deviceId] = discovered
                                    discoveredDevicesFlow.value = discoveredDevicesMap.values.toList()
                                }
                            }
                        }
                    }
                }
            }
            
            serviceListener = listener
            jmdns?.addServiceListener(SERVICE_TYPE, listener)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun stopDiscovery(): Result<Unit> {
        return try {
            serviceListener?.let { listener ->
                jmdns?.removeServiceListener(SERVICE_TYPE, listener)
                serviceListener = null
            }
            synchronized(discoveredDevicesMap) {
                discoveredDevicesMap.clear()
                discoveredDevicesFlow.value = emptyList()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun observeDiscoveredDevices(): Flow<List<DiscoveredDevice>> {
        return discoveredDevicesFlow
    }
    
    override suspend fun verifyDevice(deviceInfo: DeviceInfo): Result<Boolean> {
        return try {
            // Try to establish a TCP connection to verify reachability
            val socket = Socket()
            socket.connect(InetSocketAddress(deviceInfo.ipAddress, deviceInfo.port), 5000)
            socket.close()
            Result.success(true)
        } catch (e: IOException) {
            Result.success(false)
        }
    }
    
    /**
     * Clean up resources when done.
     */
    fun close() {
        try {
            serviceInfo?.let { info ->
                jmdns?.unregisterService(info)
            }
            serviceListener?.let { listener ->
                jmdns?.removeServiceListener(SERVICE_TYPE, listener)
            }
            jmdns?.close()
            jmdns = null
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }
    
    private fun parseServiceInfo(info: ServiceInfo): DiscoveredDevice? {
        return try {
            val deviceId = info.getPropertyString("deviceId") ?: return null
            val deviceName = info.getPropertyString("deviceName") ?: return null
            val deviceTypeStr = info.getPropertyString("deviceType") ?: return null
            val appVersion = info.getPropertyString("appVersion") ?: return null
            
            val deviceType = try {
                DeviceType.valueOf(deviceTypeStr)
            } catch (e: Exception) {
                DeviceType.DESKTOP
            }
            
            val addresses = info.inet4Addresses
            val ipAddress = if (addresses.isNotEmpty()) {
                addresses[0].hostAddress
            } else {
                return null
            }
            
            val port = info.port
            
            DiscoveredDevice(
                deviceInfo = DeviceInfo(
                    deviceId = deviceId,
                    deviceName = deviceName,
                    deviceType = deviceType,
                    appVersion = appVersion,
                    ipAddress = ipAddress,
                    port = port,
                    lastSeen = System.currentTimeMillis()
                ),
                isReachable = true,
                discoveredAt = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            null
        }
    }
}
