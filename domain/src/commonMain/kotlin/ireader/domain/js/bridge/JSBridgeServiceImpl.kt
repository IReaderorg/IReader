package ireader.domain.js.bridge

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.core.*
import ireader.core.http.cloudflare.CloudflareBypassPluginManager
import ireader.core.log.Log
import ireader.core.prefs.PreferenceStore
import ireader.domain.http.CloudflareBypass
import okio.Buffer
import okio.GzipSource
import okio.buffer

/**
 * Implementation of JSBridgeService that provides HTTP and preference access to plugins.
 * Uses Okio for KMP-compatible gzip decompression.
 * Includes Cloudflare bypass support with auto-start FlareSolverr.
 */
class JSBridgeServiceImpl(
    private val httpClient: HttpClient,
    private val preferenceStore: PreferenceStore,
    private val pluginId: String = "unknown",
    private val pluginManager: CloudflareBypassPluginManager? = null
) : JSBridgeService {
    
    // Cloudflare bypass with plugin manager for auto-start support
    private val cloudflareBypass = CloudflareBypass(httpClient, pluginManager)
    
    override suspend fun fetch(url: String, options: FetchOptions?): FetchResponse {
        return try {
            Log.info("JSBridge: [$pluginId] Fetching $url with method ${options?.method ?: "GET"}")
            
            // Build headers map
            val headersMap = mutableMapOf<String, String>()
            headersMap[HttpHeaders.AcceptEncoding] = "gzip, deflate"
            options?.headers?.forEach { (key, value) ->
                if (!key.equals(HttpHeaders.AcceptEncoding, ignoreCase = true)) {
                    headersMap[key] = value
                }
            }
            
            // Use CloudflareBypass for automatic Cloudflare handling with auto-start
            val cfResponse = cloudflareBypass.fetch(
                url = url,
                method = options?.method ?: "GET",
                body = options?.body,
                customHeaders = headersMap
            )
            
            if (cfResponse.success) {
                Log.info("JSBridge: Fetch complete - status ${cfResponse.statusCode}, ${cfResponse.body.length} chars")
                
                FetchResponse(
                    ok = true,
                    status = cfResponse.statusCode,
                    statusText = cfResponse.statusText,
                    headers = cfResponse.headers,
                    text = cfResponse.body,
                    url = url
                )
            } else {
                Log.warn("JSBridge: Fetch failed - ${cfResponse.error}")
                
                FetchResponse(
                    ok = false,
                    status = cfResponse.statusCode,
                    statusText = cfResponse.statusText,
                    headers = cfResponse.headers,
                    text = cfResponse.body,
                    url = url
                )
            }
        } catch (e: Exception) {
            Log.error("JSBridge: Fetch error for $url", e)
            FetchResponse(
                ok = false,
                status = 0,
                statusText = e.message ?: "Unknown error",
                headers = emptyMap(),
                text = "",
                url = url
            )
        }
    }
    
    override suspend fun getPreference(key: String, defaultValue: String): String {
        return preferenceStore.getString(key).get() ?: defaultValue
    }
    
    override suspend fun setPreference(key: String, value: String) {
        preferenceStore.getString(key).set(value)
    }
}
