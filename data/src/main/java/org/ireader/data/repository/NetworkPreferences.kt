package org.ireader.data.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.ireader.core.prefs.PreferenceStore
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class NetworkPreferences @Inject constructor(
    private val preferenceStore: PreferenceStore,

    ) {
    companion object PreferenceKeys {
        const val CONNECTION_TIME_OUT = "connection_time_out"
        const val WRITE_TIME_OUT = "write_time_out"
        const val READ_TIME_OUT = "read_time_out"
        const val MAX_REQUEST = "max_request"
        const val MAX_HOST_REQUEST = "max_host_request"
        const val USE_PROXY = "use_proxy"
    }

    val connectionTimeOut = preferenceStore.getLong(CONNECTION_TIME_OUT, 2)
    val writeTimeOut = preferenceStore.getLong(WRITE_TIME_OUT, 2)
    val readTimeOut = preferenceStore.getLong(READ_TIME_OUT, 2)
    val maxRequest = preferenceStore.getInt(MAX_REQUEST, 64)
    val maxHostRequest = preferenceStore.getInt(MAX_HOST_REQUEST, 10)
    val useProxy = preferenceStore.getBoolean(USE_PROXY, false)

}