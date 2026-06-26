package eu.kanade.tachiyomi.network

import okhttp3.OkHttpClient

/**
 * Minimal NetworkHelper shim for tsundoku extension compatibility.
 *
 * Tsundoku's HttpSource uses `by injectLazy()` to get NetworkHelper from Injekt.
 * This shim provides an OkHttpClient that extensions use for HTTP requests.
 */
class NetworkHelper(OkHttpClient: OkHttpClient) {
    /**
     * The main OkHttpClient used by extensions.
     */
    val client: OkHttpClient = OkHttpClient

    /**
     * Client with Cloudflare bypass support.
     * In the shim, this is the same as the regular client.
     */
    val cloudflareClient: OkHttpClient = OkHttpClient
}

/**
 * Network preferences shim.
 * Tsundoku's NetworkHelper constructor may require this.
 */
class NetworkPreferences {
    val verboseLogging = SimplePreference(false)
    val dohProvider = SimplePreference("")
    val defaultUserAgent = SimplePreference("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
}

/**
 * Minimal preference wrapper for NetworkPreferences shim.
 */
class SimplePreference<T>(private val defaultValue: T) {
    fun get(): T = defaultValue
}
