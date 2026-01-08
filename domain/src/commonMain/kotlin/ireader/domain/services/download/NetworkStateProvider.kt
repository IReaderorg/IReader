package ireader.domain.services.download

import kotlinx.coroutines.flow.StateFlow

/**
 * Enum representing network connection types.
 */
enum class NetworkType {
    /**
     * No network connection available.
     */
    NONE,
    
    /**
     * Connected via WiFi.
     */
    WIFI,
    
    /**
     * Connected via mobile data (cellular).
     */
    MOBILE,
    
    /**
     * Connected via other means (ethernet, VPN, etc.).
     */
    OTHER
}

/**
 * Interface for monitoring network state.
 * Used for network-aware downloads (WiFi-only option).
 */
interface NetworkStateProvider {
    
    /**
     * Current network state as a StateFlow.
     * Emits updates when network state changes.
     */
    val networkState: StateFlow<NetworkType>
    
    /**
     * Returns true if any network connection is available.
     */
    val isConnected: Boolean
        get() = networkState.value != NetworkType.NONE
    
    /**
     * Returns true if connected via WiFi.
     */
    val isWifi: Boolean
        get() = networkState.value == NetworkType.WIFI
    
    /**
     * Returns true if connected via mobile data.
     */
    val isMobile: Boolean
        get() = networkState.value == NetworkType.MOBILE
    
    /**
     * Returns true if downloads should proceed based on current network and settings.
     * @param wifiOnly If true, only allow downloads on WiFi.
     */
    fun shouldAllowDownload(wifiOnly: Boolean): Boolean {
        return when {
            !isConnected -> false
            wifiOnly -> isWifi
            else -> true
        }
    }
}
