package ireader.domain.preferences.prefs

import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore

class NetworkPreferences(
    private val preferenceStore: PreferenceStore,
) {
    companion object PreferenceKeys {
        const val KEY_CUSTOM_USER_AGENT = "network_custom_user_agent"
        const val KEY_USE_DEFAULT_USER_AGENT = "network_use_default_user_agent"
        const val KEY_PROXY_ENABLED = "network_proxy_enabled"
        const val KEY_PROXY_HOST = "network_proxy_host"
        const val KEY_PROXY_PORT = "network_proxy_port"
        const val DEFAULT_USER_AGENT = "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.6478.71 Mobile Safari/537.36"
    }

    fun customUserAgent(): Preference<String> {
        return preferenceStore.getString(KEY_CUSTOM_USER_AGENT, DEFAULT_USER_AGENT)
    }

    fun useDefaultUserAgent(): Preference<Boolean> {
        return preferenceStore.getBoolean(KEY_USE_DEFAULT_USER_AGENT, true)
    }

    fun proxyEnabled(): Preference<Boolean> {
        return preferenceStore.getBoolean(KEY_PROXY_ENABLED, false)
    }

    fun proxyHost(): Preference<String> {
        return preferenceStore.getString(KEY_PROXY_HOST, "")
    }

    fun proxyPort(): Preference<Int> {
        return preferenceStore.getInt(KEY_PROXY_PORT, 0)
    }
}