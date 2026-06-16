package ireader.presentation.ui.settings.network

import ireader.core.http.CookieSynchronizer
import ireader.domain.preferences.prefs.NetworkPreferences
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NetworkSettingsState(
    val customUserAgent: String = NetworkPreferences.DEFAULT_USER_AGENT,
    val useDefaultUserAgent: Boolean = true,
    val proxyEnabled: Boolean = false,
    val proxyHost: String = "",
    val proxyPort: String = "",
    val showClearCookiesDialog: Boolean = false,
    val showClearCacheDialog: Boolean = false
)

class NetworkSettingsViewModel(
    private val networkPreferences: NetworkPreferences,
    private val cookieSynchronizer: CookieSynchronizer
) : BaseViewModel() {

    private val _state = MutableStateFlow(NetworkSettingsState())
    val state: StateFlow<NetworkSettingsState> = _state.asStateFlow()

    init {
        launchInScope {
            networkPreferences.customUserAgent().changes().collect { ua ->
                _state.value = _state.value.copy(customUserAgent = ua)
            }
        }
        launchInScope {
            networkPreferences.useDefaultUserAgent().changes().collect { useDefault ->
                _state.value = _state.value.copy(useDefaultUserAgent = useDefault)
            }
        }
        launchInScope {
            networkPreferences.proxyEnabled().changes().collect { enabled ->
                _state.value = _state.value.copy(proxyEnabled = enabled)
            }
        }
        launchInScope {
            networkPreferences.proxyHost().changes().collect { host ->
                _state.value = _state.value.copy(proxyHost = host)
            }
        }
        launchInScope {
            networkPreferences.proxyPort().changes().collect { port ->
                _state.value = _state.value.copy(proxyPort = port.toString())
            }
        }
    }

    fun setCustomUserAgent(value: String) {
        networkPreferences.customUserAgent().set(value)
    }

    fun setUseDefaultUserAgent(value: Boolean) {
        networkPreferences.useDefaultUserAgent().set(value)
    }

    fun setProxyEnabled(value: Boolean) {
        networkPreferences.proxyEnabled().set(value)
    }

    fun setProxyHost(value: String) {
        networkPreferences.proxyHost().set(value)
    }

    fun setProxyPort(value: String) {
        val port = value.toIntOrNull() ?: 0
        networkPreferences.proxyPort().set(port)
    }

    fun showClearCookiesDialog() {
        _state.value = _state.value.copy(showClearCookiesDialog = true)
    }

    fun hideClearCookiesDialog() {
        _state.value = _state.value.copy(showClearCookiesDialog = false)
    }

    fun clearCookies() {
        cookieSynchronizer.clearAll()
        hideClearCookiesDialog()
    }

    fun showClearCacheDialog() {
        _state.value = _state.value.copy(showClearCacheDialog = true)
    }

    fun hideClearCacheDialog() {
        _state.value = _state.value.copy(showClearCacheDialog = false)
    }

    private fun launchInScope(block: suspend kotlinx.coroutines.CoroutineScope.() -> Unit) {
        scope.launch(block = block)
    }
}