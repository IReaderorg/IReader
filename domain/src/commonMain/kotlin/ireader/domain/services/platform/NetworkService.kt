package ireader.domain.services.platform

import ireader.domain.services.common.PlatformService
import ireader.domain.services.common.ServiceResult
import kotlinx.coroutines.flow.Flow
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Platform-agnostic network monitoring service
 * 
 * Provides network connectivity information and monitoring
 */
interface NetworkService : PlatformService {
    
    /**
     * Check if device is connected to any network
     * 
     * @return true if connected
     */
    fun isConnected(): Boolean
    
    /**
     * Check if device is connected to WiFi
     * 
     * @return true if connected to WiFi
     */
    fun isWiFi(): Boolean
    
    /**
     * Check if device is connected to mobile data
     * 
     * @return true if connected to mobile data
     */
    fun isMobile(): Boolean
    
    /**
     * Check if device is connected to ethernet
     * 
     * @return true if connected to ethernet
     */
    fun isEthernet(): Boolean
    
    /**
     * Check if connection is metered (has data limits)
     * 
     * @return true if connection is metered
     */
    fun isMetered(): Boolean
    
    /**
     * Get current network state
     * 
     * @return Current network state
     */
    fun getNetworkState(): NetworkState
    
    /**
     * Observe network state changes
     * 
     * @return Flow of network state changes
     */
    fun observeNetworkChanges(): Flow<NetworkState>
    
    /**
     * Measure network speed
     * 
     * @return Network speed information
     */
    suspend fun measureNetworkSpeed(): ServiceResult<NetworkSpeed>
    
    /**
     * Check if specific host is reachable
     * 
     * @param host Host to check (e.g., "google.com")
     * @param timeoutMs Timeout in milliseconds
     * @return true if host is reachable
     */
    suspend fun isHostReachable(host: String, timeoutMs: Long = 5000): Boolean
}

/**
 * Network state information
 */
data class NetworkState(
    val isConnected: Boolean,
    val type: NetworkType,
    val isMetered: Boolean,
    val signalStrength: SignalStrength = SignalStrength.UNKNOWN,
    val timestamp: Long = currentTimeToLong()
)

/**
 * Network type enumeration
 */
enum class NetworkType {
    WIFI,
    MOBILE,
    ETHERNET,
    BLUETOOTH,
    VPN,
    NONE,
    UNKNOWN
}

/**
 * Signal strength enumeration
 */
enum class SignalStrength {
    EXCELLENT, // 4-5 bars
    GOOD,      // 3 bars
    FAIR,      // 2 bars
    POOR,      // 1 bar
    NONE,      // 0 bars
    UNKNOWN    // Cannot determine
}

/**
 * Network speed information
 */
data class NetworkSpeed(
    val downloadMbps: Float,
    val uploadMbps: Float,
    val latencyMs: Long,
    val measuredAt: Long = currentTimeToLong()
) {
    val isFast: Boolean
        get() = downloadMbps >= 10f && latencyMs < 100
    
    val isSlow: Boolean
        get() = downloadMbps < 1f || latencyMs > 500
    
    val quality: NetworkQuality
        get() = when {
            downloadMbps >= 25f && latencyMs < 50 -> NetworkQuality.EXCELLENT
            downloadMbps >= 10f && latencyMs < 100 -> NetworkQuality.GOOD
            downloadMbps >= 3f && latencyMs < 200 -> NetworkQuality.FAIR
            downloadMbps >= 1f && latencyMs < 500 -> NetworkQuality.POOR
            else -> NetworkQuality.VERY_POOR
        }
}

/**
 * Network quality enumeration
 */
enum class NetworkQuality {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR,
    VERY_POOR
}
