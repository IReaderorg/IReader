package ireader.domain.services.download

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import ireader.core.log.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Android implementation of NetworkStateProvider.
 * Uses ConnectivityManager with NetworkCallback for real-time network state monitoring.
 */
class AndroidNetworkStateProvider(
    context: Context
) : NetworkStateProvider {
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    private val _networkState = MutableStateFlow(getCurrentNetworkType())
    override val networkState: StateFlow<NetworkType> = _networkState.asStateFlow()
    
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            updateNetworkState()
            Log.debug { "NetworkStateProvider: Network available" }
        }
        
        override fun onLost(network: Network) {
            updateNetworkState()
            Log.debug { "NetworkStateProvider: Network lost" }
        }
        
        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            updateNetworkState()
        }
    }
    
    init {
        registerNetworkCallback()
    }
    
    private fun registerNetworkCallback() {
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager.registerNetworkCallback(request, networkCallback)
            Log.debug { "NetworkStateProvider: Registered network callback" }
        } catch (e: Exception) {
            Log.error { "NetworkStateProvider: Failed to register network callback - ${e.message}" }
        }
    }
    
    private fun updateNetworkState() {
        val newState = getCurrentNetworkType()
        if (_networkState.value != newState) {
            _networkState.value = newState
            Log.debug { "NetworkStateProvider: State changed to $newState" }
        }
    }
    
    private fun getCurrentNetworkType(): NetworkType {
        val activeNetwork = connectivityManager.activeNetwork
            ?: return NetworkType.NONE
        
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            ?: return NetworkType.NONE
        
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.MOBILE
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.OTHER
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> {
                // VPN can be over WiFi or mobile, check underlying transport
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    NetworkType.WIFI
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    NetworkType.MOBILE
                } else {
                    NetworkType.OTHER
                }
            }
            else -> NetworkType.OTHER
        }
    }
    
    /**
     * Unregister the network callback when no longer needed.
     * Call this when the provider is being disposed.
     */
    fun unregister() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
            Log.debug { "NetworkStateProvider: Unregistered network callback" }
        } catch (e: Exception) {
            Log.error { "NetworkStateProvider: Failed to unregister network callback - ${e.message}" }
        }
    }
}
