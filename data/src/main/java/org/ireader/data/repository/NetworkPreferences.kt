package org.ireader.data.repository

import android.content.SharedPreferences
import com.fredporciuncula.flow.preferences.FlowSharedPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class NetworkPreferences @Inject constructor(
    prefs: SharedPreferences,
) {
    companion object PreferenceKeys {
        const val CONNECTION_TIME_OUT = "connection_time_out"
        const val WRITE_TIME_OUT = "write_time_out"
        const val READ_TIME_OUT = "read_time_out"
        const val MAX_REQUEST = "max_request"
        const val MAX_HOST_REQUEST = "max_host_request"
        const val USE_PROXY = "use_proxy"
    }

    private val flowPrefs = FlowSharedPreferences(prefs)

    val connectionTimeOut = flowPrefs.getLong(CONNECTION_TIME_OUT, 2)
    val writeTimeOut = flowPrefs.getLong(WRITE_TIME_OUT, 2)
    val readTimeOut = flowPrefs.getLong(READ_TIME_OUT, 2)
    val maxRequest = flowPrefs.getInt(MAX_REQUEST, 64)
    val maxHostRequest = flowPrefs.getInt(MAX_HOST_REQUEST, 10)
    val useProxy = flowPrefs.getBoolean(USE_PROXY, false)

}