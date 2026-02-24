package ireader.data.sync.datasource

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.coroutines.resume

/**
 * Android implementation of DiscoveryDataSource using NsdManager.
 * 
 * Uses Android's Network Service Discovery (NSD) API for mDNS service discovery.
 * Service type: "_ireader-sync._tcp"
 * 
 * Handles network changes by automatically restarting discovery and broadcasting.
 */
class AndroidDiscoveryDataSource(
    private val context: Context
) : DiscoveryDataSource {
    
    private val nsdManager: NsdManager by lazy {
        context.getSystemService(Context.NSD_SERVICE) as NsdManager
    }
    
    private val connectivityManager: ConnectivityManager by lazy {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    
    private val discoveredDevicesFlow = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
    private val discoveredDevicesMap = mutableMapOf<String, DiscoveredDevice>()
    
    private var registrationListener: NsdManager.RegistrationListener? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    
    private var currentDeviceInfo: DeviceInfo? = null
    private var isDiscoveryActive = false
    private var isBroadcastingActive = false
    
    companion object {
        private const val SERVICE_TYPE = "_ireader-sync._tcp"
        private const val SERVICE_NAME_PREFIX = "IReader"
    }
    
    init {
        // Register network callback to handle network changes
        setupNetworkCallback()
    }
    
    private fun setupNetworkCallback() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        
        val callback = object : ConnectivityManager.NetworkCallback() {
            private var lastNetwork: Network? = null
            
            override fun onAvailable(network: Network) {
                println("[AndroidDiscovery] Network available: $network")
                
                // Check if this is a different network than before
                val isDifferentNetwork = lastNetwork != null && lastNetwork != network
                lastNetwork = network
                
                if (isDifferentNetwork) {
                    println("[AndroidDiscovery] Different network detected, clearing cache and restarting...")
                    // Clear all cached devices immediately
                    synchronized(discoveredDevicesMap) {
                        discoveredDevicesMap.clear()
                        discoveredDevicesFlow.value = emptyList()
                    }
                }
                
                // Restart services (with delay to ensure network is fully ready)
                GlobalScope.launch {
                    delay(2000) // Wait 2 seconds for network to stabilize
                    restartServicesIfNeeded()
                }
            }
            
            override fun onLost(network: Network) {
                println("[AndroidDiscovery] Network lost: $network")
                // Network lost - clear discovered devices immediately
                synchronized(discoveredDevicesMap) {
                    discoveredDevicesMap.clear()
                    discoveredDevicesFlow.value = emptyList()
                }
                lastNetwork = null
            }
            
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                // Network capabilities changed (e.g., WiFi SSID changed)
                println("[AndroidDiscovery] Network capabilities changed")
                // Clear cache and restart to pick up new IPs
                synchronized(discoveredDevicesMap) {
                    discoveredDevicesMap.clear()
                    discoveredDevicesFlow.value = emptyList()
                }
                GlobalScope.launch {
                    delay(2000)
                    restartServicesIfNeeded()
                }
            }
        }
        
        networkCallback = callback
        try {
            connectivityManager.registerNetworkCallback(networkRequest, callback)
        } catch (e: Exception) {
            println("[AndroidDiscovery] Failed to register network callback: ${e.message}")
        }
    }
    
    private fun restartServicesIfNeeded() {
        // Clear cache first
        synchronized(discoveredDevicesMap) {
            discoveredDevicesMap.clear()
            discoveredDevicesFlow.value = emptyList()
        }
        
        // Restart discovery if it was active
        if (isDiscoveryActive) {
            println("[AndroidDiscovery] Restarting discovery...")
            GlobalScope.launch {
                try {
                    stopDiscovery()
                    delay(1500) // Wait for cleanup
                    startDiscovery()
                } catch (e: Exception) {
                    println("[AndroidDiscovery] Error restarting discovery: ${e.message}")
                }
            }
        }
        
        // Restart broadcasting if it was active
        if (isBroadcastingActive && currentDeviceInfo != null) {
            println("[AndroidDiscovery] Restarting broadcasting...")
            GlobalScope.launch {
                try {
                    stopBroadcasting()
                    delay(1500) // Wait for cleanup
                    currentDeviceInfo?.let { startBroadcasting(it) }
                } catch (e: Exception) {
                    println("[AndroidDiscovery] Error restarting broadcasting: ${e.message}")
                }
            }
        }
    }
    
    override suspend fun startBroadcasting(deviceInfo: DeviceInfo): Result<Unit> {
        return suspendCancellableCoroutine { continuation ->
            try {
                currentDeviceInfo = deviceInfo
                isBroadcastingActive = true
                
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
                        println("[AndroidDiscovery] Service registered: ${serviceInfo?.serviceName}")
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
            isBroadcastingActive = false
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
                isDiscoveryActive = true
                
                // Stop any existing discovery first to clear cache
                discoveryListener?.let { listener ->
                    try {
                        nsdManager.stopServiceDiscovery(listener)
                    } catch (e: Exception) {
                        // Ignore - might not be running
                    }
                }
                
                // Clear old discovered devices when starting new discovery
                synchronized(discoveredDevicesMap) {
                    discoveredDevicesMap.clear()
                    discoveredDevicesFlow.value = emptyList()
                }
                
                println("[AndroidDiscovery] Starting discovery for service type: $SERVICE_TYPE")
                
                val listener = object : NsdManager.DiscoveryListener {
                    override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
                        println("[AndroidDiscovery] Discovery start failed with code: $errorCode")
                        if (continuation.isActive) {
                            continuation.resume(Result.failure(Exception("Discovery start failed with code: $errorCode")))
                        }
                    }
                    
                    override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {
                        // Not relevant for startDiscovery
                    }
                    
                    override fun onDiscoveryStarted(serviceType: String?) {
                        println("[AndroidDiscovery] Discovery started for: $serviceType")
                        if (continuation.isActive) {
                            continuation.resume(Result.success(Unit))
                        }
                    }
                    
                    override fun onDiscoveryStopped(serviceType: String?) {
                        println("[AndroidDiscovery] Discovery stopped")
                    }
                    
                    override fun onServiceFound(serviceInfo: NsdServiceInfo?) {
                        serviceInfo?.let { info ->
                            println("[AndroidDiscovery] Service found: ${info.serviceName}")
                            // Resolve the service to get full details
                            nsdManager.resolveService(info, object : NsdManager.ResolveListener {
                                override fun onResolveFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
                                    println("[AndroidDiscovery] Resolve failed for ${serviceInfo?.serviceName} with code: $errorCode")
                                }
                                
                                override fun onServiceResolved(serviceInfo: NsdServiceInfo?) {
                                    serviceInfo?.let { resolved ->
                                        println("[AndroidDiscovery] Service resolved: ${resolved.serviceName}")
                                        val device = parseServiceInfo(resolved)
                                        device?.let {
                                            // Don't add self
                                            if (it.deviceInfo.deviceId != currentDeviceInfo?.deviceId) {
                                                println("[AndroidDiscovery] Adding device: ${it.deviceInfo.deviceName} (${it.deviceInfo.deviceId})")
                                                synchronized(discoveredDevicesMap) {
                                                    discoveredDevicesMap[it.deviceInfo.deviceId] = it
                                                    discoveredDevicesFlow.value = discoveredDevicesMap.values.toList()
                                                }
                                            } else {
                                                println("[AndroidDiscovery] Skipping self device")
                                            }
                                        }
                                    }
                                }
                            })
                        }
                    }
                    
                    override fun onServiceLost(serviceInfo: NsdServiceInfo?) {
                        serviceInfo?.let { info ->
                            println("[AndroidDiscovery] Service lost: ${info.serviceName}")
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
            isDiscoveryActive = false
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
    
    /**
     * Force refresh discovery by clearing cache and restarting.
     * Useful when network changes and old IPs are cached.
     */
    suspend fun forceRefresh(): Result<Unit> {
        return try {
            println("[AndroidDiscovery] Force refresh requested")
            // Clear all cached devices
            synchronized(discoveredDevicesMap) {
                discoveredDevicesMap.clear()
                discoveredDevicesFlow.value = emptyList()
            }
            
            // Restart services
            if (isDiscoveryActive) {
                stopDiscovery()
                delay(1500)
                startDiscovery()
            }
            
            if (isBroadcastingActive && currentDeviceInfo != null) {
                stopBroadcasting()
                delay(1500)
                currentDeviceInfo?.let { startBroadcasting(it) }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun cleanup() {
        // Unregister network callback when done
        networkCallback?.let {
            try {
                connectivityManager.unregisterNetworkCallback(it)
            } catch (e: Exception) {
                println("[AndroidDiscovery] Failed to unregister network callback: ${e.message}")
            }
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
            
            println("[AndroidDiscovery] Parsed device: $deviceName at $ipAddress:$port (ID: $deviceId)")
            
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
            println("[AndroidDiscovery] Failed to parse service info: ${e.message}")
            null
        }
    }
}
