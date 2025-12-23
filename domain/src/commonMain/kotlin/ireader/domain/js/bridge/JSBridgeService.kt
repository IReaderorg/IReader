package ireader.domain.js.bridge

import kotlinx.serialization.Serializable

/**
 * Bridge service that provides Kotlin functionality to JavaScript plugins.
 * This allows plugins to make HTTP requests and access other native features.
 */
interface JSBridgeService {
    /**
     * Make an HTTP request from JavaScript.
     * 
     * @param url The URL to fetch
     * @param options Request options (method, headers, body, etc.)
     * @return Response data
     */
    suspend fun fetch(url: String, options: FetchOptions?): FetchResponse
    
    /**
     * Get a preference value.
     */
    suspend fun getPreference(key: String, defaultValue: String): String
    
    /**
     * Set a preference value.
     */
    suspend fun setPreference(key: String, value: String)
}

/**
 * HTTP request options for fetch API.
 */
@Serializable
data class FetchOptions(
    val method: String = "GET",
    val headers: Map<String, String> = emptyMap(),
    val body: String? = null
)

/**
 * HTTP response from fetch API.
 */
@Serializable
data class FetchResponse(
    val ok: Boolean,
    val status: Int,
    val statusText: String,
    val headers: Map<String, String>,
    val text: String,
    val url: String? = null
)
