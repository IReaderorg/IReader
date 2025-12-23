package ireader.domain.js.bridge

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.core.*
import ireader.core.log.Log
import ireader.core.prefs.PreferenceStore
import okio.Buffer
import okio.GzipSource
import okio.buffer

/**
 * Implementation of JSBridgeService that provides HTTP and preference access to plugins.
 * Uses Okio for KMP-compatible gzip decompression.
 */
class JSBridgeServiceImpl(
    private val httpClient: HttpClient,
    private val preferenceStore: PreferenceStore,
    private val pluginId: String = "unknown"
) : JSBridgeService {
    
    override suspend fun fetch(url: String, options: FetchOptions?): FetchResponse {
        return try {
            Log.info("JSBridge: [$pluginId] Fetching $url with method ${options?.method ?: "GET"}")
            
            val response = httpClient.request(url) {
                method = when (options?.method?.uppercase()) {
                    "POST" -> HttpMethod.Post
                    "PUT" -> HttpMethod.Put
                    "DELETE" -> HttpMethod.Delete
                    "PATCH" -> HttpMethod.Patch
                    else -> HttpMethod.Get
                }
                
                // Always accept gzip/deflate encoding
                header(HttpHeaders.AcceptEncoding, "gzip, deflate")
                
                // Set headers
                options?.headers?.forEach { (key, value) ->
                    // Don't override Accept-Encoding if we already set it
                    if (key.equals(HttpHeaders.AcceptEncoding, ignoreCase = true)) {
                        return@forEach
                    }
                    header(key, value)
                }
                
                // Set body for POST/PUT/PATCH
                if (options?.body != null && method != HttpMethod.Get) {
                    setBody(options.body)
                }
            }
            
            // Get response body as bytes first
            val bodyBytes = response.readBytes()
            val contentEncoding = response.headers[HttpHeaders.ContentEncoding]
            
            // Decompress if gzipped using Okio
            val text = if (contentEncoding?.contains("gzip", ignoreCase = true) == true) {
                try {
                    Log.debug("JSBridge: Decompressing gzip response")
                    val buffer = Buffer().write(bodyBytes)
                    val gzipSource = GzipSource(buffer)
                    gzipSource.buffer().readUtf8()
                } catch (e: Exception) {
                    Log.warn("JSBridge: Failed to decompress gzip, using raw bytes: ${e.message}")
                    bodyBytes.decodeToString()
                }
            } else {
                bodyBytes.decodeToString()
            }
            
            val headers = response.headers.entries()
                .associate { (key, values) -> key to values.firstOrNull().orEmpty() }
            
            Log.info("JSBridge: Fetch complete - status ${response.status.value}, ${text.length} chars")
            
            // Get the final URL (after redirects)
            val finalUrl = response.request.url.toString()
            
            FetchResponse(
                ok = response.status.isSuccess(),
                status = response.status.value,
                statusText = response.status.description,
                headers = headers,
                text = text,
                url = finalUrl
            )
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
