package ireader.data.sync.datasource

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import ireader.domain.models.sync.DeviceInfo
import ireader.domain.models.sync.DeviceType
import ireader.domain.models.sync.DiscoveredDevice
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.coroutines.resume

/**
 * Android implementation of DiscoveryDataSource using NsdManager.
 * 
 * Uses Android's Network Service Discovery (NSD) API for mDNS service discovery.
 * Service type: "_ireader-sync._tcp"
 */
class AndroidDiscoveryDataSource(
    private val context: Context
) : DiscoveryDataSource {
    
    private val nsdManager: NsdManager by lazy {
        context.getSystemService(Context.NSD_SERVICE) as NsdManager
    }
    
    private val discoveredDevicesFlow = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
    private val discoveredDevicesMap = mutableMapOf<String, DiscoveredDevice>()
    
    private var registrationListener: NsdManager.RegistrationListener? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null
    
    private var currentDeviceInfo: DeviceInfo? = null
    
    companion object {
        private const val SERVICE_TYPE = "_ireader-sync._tcp"
        private const val SERVICE_NAME_PREFIX = "IReader"
    }
    
    override suspend fun startBroadcasting(deviceInfo: DeviceInfo): Result<Unit> {
        return suspendCancellableCoroutine { continuation ->
            try {
                currentDeviceInfo = deviceInfo
                
                val serviceInfo = NsdServiceInfo().apply {
                    serviceName = "$SERVICE_NAME_PREFIX-${deviceInfo.deviceName}"
                    serviceType = SERVICE_TYPE
                    port = deviceInfo.port
                    
                    // Add device info as TXT records
                    setAttribute("deviceId", deviceInfo.deviceId)
                    setAttribute("deviceName", deviceInfo.deviceName)
                    setAttribute("deviceType", deviceInfo.deviceType.name)
                    setAttribute("appVersion", deviceInfo.appVersion)
                }
                
                val listener = object : NsdManager.RegistrationListener {
                    override fun onRegistrationFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
                        if (continuation.isActive) {
                            continuation.resume(Result.failure(Exception("Registration failed with code: $errorCode")))
                        }
                    }
                    
                    override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
                        // Not relevant for startBroadcasting
                    }
                    
                    override fun onServiceRegistered(serviceInfo: NsdServiceInfo?) {
                        if (continuation.isActive) {
                            continuation.resume(Result.success(Unit))
                        }
                    }
                    
                    override fun onServiceUnregistered(serviceInfo: NsdServiceInfo?) {
                        // Not relevant for startBroadcasting
                    }
                }
                
                registrationListener = listener
                nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, listener)
                
                continuation.invokeOnCancellation {
                    try {
                        nsdManager.unregisterService(listener)
                    } catch (e: Exception) {
                        // Ignore
                    }
                }
                
            } catch (e: Exception) {
                if (continuation.isActive) {
                    continuation.resume(Result.failure(e))
                }
            }
        }
    }
    
    override suspend fun stopBroadcasting(): Result<Unit> {
        return try {
            registrationListener?.let { listener ->
                nsdManager.unregisterService(listener)
                registrationListener = null
            }
            currentDeviceInfo = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun startDiscovery(): Result<Unit> {
        return suspendCancellableCoroutine { continuation ->
            try {
                val listener = object : NsdManager.DiscoveryListener {
                    override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
                        if (continuation.isActive) {
                            continuation.resume(Result.failure(Exception("Discovery start failed with code: $errorCode")))
                        }
                    }
                    
                    override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {
                        // Not relevant for startDiscovery
                    }
                    
                    override fun onDiscoveryStarted(serviceType: String?) {
                        if (continuation.isActive) {
                            continuation.resume(Result.success(Unit))
                        }
                    }
                    
                    override fun onDiscoveryStopped(serviceType: String?) {
                        // Not relevant for startDiscovery
                    }
                    
                    override fun onServiceFound(serviceInfo: NsdServiceInfo?) {
                        serviceInfo?.let { info ->
                            // Resolve the service to get full details
                            nsdManager.resolveService(info, object : NsdManager.ResolveListener {
                                override fun onResolveFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
                                    // Ignore resolution failures
                                }
                                
                                override fun onServiceResolved(serviceInfo: NsdServiceInfo?) {
                                    serviceInfo?.let { resolved ->
                                        val device = parseServiceInfo(resolved)
                                        device?.let {
                                            // Don't add self
                                            if (it.deviceInfo.deviceId != currentDeviceInfo?.deviceId) {
                                                synchronized(discoveredDevicesMap) {
                                                    discoveredDevicesMap[it.deviceInfo.deviceId] = it
                                                    discoveredDevicesFlow.value = discoveredDevicesMap.values.toList()
                                                }
                                            }
                                        }
                                    }
                                }
                            })
                        }
                    }
                    
                    override fun onServiceLost(serviceInfo: NsdServiceInfo?) {
                        serviceInfo?.let { info ->
                            val deviceId = info.attributes?.get("deviceId")?.decodeToString()
                            deviceId?.let {
                                synchronized(discoveredDevicesMap) {
                                    discoveredDevicesMap.remove(it)
                                    discoveredDevicesFlow.value = discoveredDevicesMap.values.toList()
                                }
                            }
                        }
                    }
                }
                
                discoveryListener = listener
                nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, listener)
                
                continuation.invokeOnCancellation {
                    try {
                        nsdManager.stopServiceDiscovery(listener)
                    } catch (e: Exception) {
                        // Ignore
                    }
                }
                
            } catch (e: Exception) {
                if (continuation.isActive) {
                    continuation.resume(Result.failure(e))
                }
            }
        }
    }
    
    override suspend fun stopDiscovery(): Result<Unit> {
        return try {
            discoveryListener?.let { listener ->
                nsdManager.stopServiceDiscovery(listener)
                discoveryListener = null
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
        } catch (e: Exception) {
            Result.success(false)
        }
    }
    
    private fun parseServiceInfo(serviceInfo: NsdServiceInfo): DiscoveredDevice? {
        return try {
            val deviceId = serviceInfo.attributes?.get("deviceId")?.decodeToString() ?: return null
            val deviceName = serviceInfo.attributes?.get("deviceName")?.decodeToString() ?: return null
            val deviceTypeStr = serviceInfo.attributes?.get("deviceType")?.decodeToString() ?: return null
            val appVersion = serviceInfo.attributes?.get("appVersion")?.decodeToString() ?: return null
            
            val deviceType = try {
                DeviceType.valueOf(deviceTypeStr)
            } catch (e: Exception) {
                DeviceType.ANDROID
            }
            
            val ipAddress = serviceInfo.host?.hostAddress ?: return null
            val port = serviceInfo.port
            
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
