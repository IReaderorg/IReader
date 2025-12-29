package ireader.domain.http

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import ireader.core.http.cloudflare.CloudflareBypassPluginManager
import ireader.core.http.cloudflare.CloudflareChallenge
import ireader.core.http.cloudflare.PluginBypassResult
import ireader.core.log.Log
import kotlinx.coroutines.delay

/**
 * Native Cloudflare bypass implementation without external dependencies.
 * 
 * This implementation uses proper headers, cookie management, and retry logic
 * to bypass most Cloudflare protections without requiring external tools.
 * 
 * Strategies:
 * 1. Proper browser headers (User-Agent, Accept, etc.)
 * 2. Cookie persistence across requests
 * 3. Retry with exponential backoff for 403/503 errors
 * 4. JavaScript challenge detection and handling
 * 5. Plugin-based bypass (FlareSolverr with auto-start) when challenge is detected
 */
class CloudflareBypass(
    private val httpClient: HttpClient,
    private val pluginManager: CloudflareBypassPluginManager? = null
) {
    
    companion object {
        // Common browser User-Agents that work well with Cloudflare
        private val USER_AGENTS = listOf(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
        )
        
        private const val MAX_RETRIES = 3
        private const val INITIAL_DELAY = 2000L // 2 seconds
    }
    
    /**
     * Fetches a URL with Cloudflare bypass strategies.
     * 
     * @param url The URL to fetch
     * @param method HTTP method (GET or POST)
     * @param body Optional request body for POST
     * @param customHeaders Optional custom headers
     * @param retryOn403 Whether to retry on 403 errors
     * @return CloudflareResponse with status and content
     */
    suspend fun fetch(
        url: String,
        method: String = "GET",
        body: String? = null,
        customHeaders: Map<String, String> = emptyMap(),
        retryOn403: Boolean = true
    ): CloudflareResponse {
        var lastError: Exception? = null
        var retries = 0
        var delayMs = INITIAL_DELAY
        
        while (retries <= MAX_RETRIES) {
            try {
                val response = makeRequest(url, method, body, customHeaders, retries)
                
                // Check if we got a Cloudflare challenge
                when (response.status.value) {
                    403 -> {
                        val bodyText = response.bodyAsText()
                        val isCloudflare = bodyText.contains("cf-browser-verification") || 
                                          bodyText.contains("Checking your browser") ||
                                          bodyText.contains("Just a moment") ||
                                          bodyText.contains("cloudflare")
                        
                        // If it's definitely Cloudflare, try plugin-based bypass
                        if (isCloudflare) {
                            Log.warn { "[CloudflareBypass] Cloudflare challenge detected, trying plugin bypass" }
                            
                            // Try plugin-based bypass (FlareSolverr with auto-start)
                            val pluginResult = tryPluginBypass(url, customHeaders)
                            if (pluginResult != null && pluginResult.success) {
                                return pluginResult
                            }
                            
                            return CloudflareResponse(
                                success = false,
                                statusCode = 403,
                                statusText = "Forbidden - Cloudflare Protection",
                                body = bodyText,
                                headers = response.headers.entries().associate { it.key to it.value.joinToString(", ") },
                                error = pluginResult?.error ?: "Site is protected by Cloudflare. Configure FlareSolverr in Settings > Cloudflare Bypass.",
                                isCloudflareChallenge = true
                            )
                        }
                        
                        // For other 403s, retry
                        if (retryOn403 && retries < MAX_RETRIES) {
                            Log.warn { "[CloudflareBypass] Got 403, retrying in ${delayMs}ms (attempt ${retries + 1}/$MAX_RETRIES)" }
                            delay(delayMs)
                            delayMs *= 2 // Exponential backoff
                            retries++
                            continue
                        }
                        
                        return CloudflareResponse(
                            success = false,
                            statusCode = 403,
                            statusText = "Forbidden",
                            body = bodyText,
                            headers = response.headers.entries().associate { it.key to it.value.joinToString(", ") },
                            error = "Access forbidden. Site may require authentication.",
                            isCloudflareChallenge = false
                        )
                    }
                    
                    503 -> {
                        val bodyText = response.bodyAsText()
                        if (bodyText.contains("cf-browser-verification") || bodyText.contains("Checking your browser") || bodyText.contains("Just a moment")) {
                            Log.warn { "[CloudflareBypass] Cloudflare 503 challenge detected, trying plugin bypass" }
                            
                            // Try plugin-based bypass (FlareSolverr with auto-start)
                            val pluginResult = tryPluginBypass(url, customHeaders)
                            if (pluginResult != null && pluginResult.success) {
                                return pluginResult
                            }
                            
                            if (retries < MAX_RETRIES) {
                                Log.warn { "[CloudflareBypass] Plugin bypass failed, waiting ${delayMs}ms before retry" }
                                delay(delayMs)
                                delayMs *= 2
                                retries++
                                continue
                            }
                            
                            return CloudflareResponse(
                                success = false,
                                statusCode = 503,
                                statusText = "Service Unavailable - Cloudflare Challenge",
                                body = bodyText,
                                headers = response.headers.entries().associate { it.key to it.value.joinToString(", ") },
                                error = pluginResult?.error ?: "Cloudflare challenge detected. Configure FlareSolverr in Settings > Cloudflare Bypass.",
                                isCloudflareChallenge = true
                            )
                        }
                    }
                    
                    in 200..299 -> {
                        val bodyText = response.bodyAsText()
                        Log.info { "[CloudflareBypass] Success: ${response.status.value} (${bodyText.length} bytes)" }
                        return CloudflareResponse(
                            success = true,
                            statusCode = response.status.value,
                            statusText = response.status.description,
                            body = bodyText,
                            headers = response.headers.entries().associate { it.key to it.value.joinToString(", ") },
                            cookies = extractCookies(response)
                        )
                    }
                    
                    else -> {
                        val bodyText = response.bodyAsText()
                        return CloudflareResponse(
                            success = false,
                            statusCode = response.status.value,
                            statusText = response.status.description,
                            body = bodyText,
                            headers = response.headers.entries().associate { it.key to it.value.joinToString(", ") },
                            error = "HTTP ${response.status.value}: ${response.status.description}"
                        )
                    }
                }
            } catch (e: Exception) {
                lastError = e
                if (retries < MAX_RETRIES) {
                    Log.warn { "[CloudflareBypass] Request failed: ${e.message}, retrying in ${delayMs}ms" }
                    delay(delayMs)
                    delayMs *= 2
                    retries++
                    continue
                }
            }
        }
        
        return CloudflareResponse(
            success = false,
            statusCode = 0,
            statusText = "Network Error",
            body = "",
            headers = emptyMap(),
            error = "Failed after $MAX_RETRIES retries: ${lastError?.message}"
        )
    }
    
    /**
     * Try to bypass Cloudflare using the plugin manager (FlareSolverr with auto-start).
     */
    private suspend fun tryPluginBypass(
        url: String,
        customHeaders: Map<String, String>
    ): CloudflareResponse? {
        val manager = pluginManager ?: return null
        
        try {
            Log.info { "[CloudflareBypass] Attempting plugin-based bypass for: $url" }
            
            val result = manager.bypass(
                url = url,
                challenge = CloudflareChallenge.JSChallenge(),
                headers = customHeaders,
                userAgent = USER_AGENTS[0],
                timeoutMs = 60000
            )
            
            return when (result) {
                is PluginBypassResult.Success -> {
                    Log.info { "[CloudflareBypass] Plugin bypass successful!" }
                    CloudflareResponse(
                        success = true,
                        statusCode = result.statusCode,
                        statusText = "OK",
                        body = result.content,
                        headers = emptyMap(),
                        cookies = result.cookies.map { "${it.name}=${it.value}" },
                        isCloudflareChallenge = false
                    )
                }
                is PluginBypassResult.Failed -> {
                    Log.warn { "[CloudflareBypass] Plugin bypass failed: ${result.reason}" }
                    CloudflareResponse(
                        success = false,
                        statusCode = 403,
                        statusText = "Bypass Failed",
                        body = "",
                        headers = emptyMap(),
                        error = "Plugin bypass failed: ${result.reason}",
                        isCloudflareChallenge = true
                    )
                }
                is PluginBypassResult.UserInteractionRequired -> {
                    Log.warn { "[CloudflareBypass] User interaction required: ${result.message}" }
                    CloudflareResponse(
                        success = false,
                        statusCode = 403,
                        statusText = "User Interaction Required",
                        body = "",
                        headers = emptyMap(),
                        error = "Manual verification required: ${result.message}",
                        isCloudflareChallenge = true
                    )
                }
                is PluginBypassResult.ServiceUnavailable -> {
                    Log.warn { "[CloudflareBypass] Plugin service unavailable: ${result.reason}" }
                    CloudflareResponse(
                        success = false,
                        statusCode = 503,
                        statusText = "Service Unavailable",
                        body = "",
                        headers = emptyMap(),
                        error = "Bypass service unavailable: ${result.reason}. ${result.setupInstructions ?: ""}",
                        isCloudflareChallenge = true
                    )
                }
            }
        } catch (e: Exception) {
            Log.error(e, "[CloudflareBypass] Plugin bypass error")
            return null
        }
    }
    
    /**
     * Makes the actual HTTP request with proper headers.
     */
    private suspend fun makeRequest(
        url: String,
        method: String,
        body: String?,
        customHeaders: Map<String, String>,
        attempt: Int
    ): HttpResponse {
        // Rotate User-Agent on retries
        val userAgent = USER_AGENTS[attempt % USER_AGENTS.size]
        
        return when (method.uppercase()) {
            "POST" -> httpClient.post(url) {
                headers {
                    append("User-Agent", userAgent)
                    append("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    append("Accept-Language", "en-US,en;q=0.9")
                    // Don't set Accept-Encoding - let OkHttp handle compression automatically
                    append("DNT", "1")
                    append("Connection", "keep-alive")
                    append("Upgrade-Insecure-Requests", "1")
                    append("Sec-Fetch-Dest", "document")
                    append("Sec-Fetch-Mode", "navigate")
                    append("Sec-Fetch-Site", "none")
                    append("Sec-Fetch-User", "?1")
                    append("Cache-Control", "max-age=0")
                    
                    // Apply custom headers (can override defaults)
                    customHeaders.forEach { (key, value) ->
                        append(key, value)
                    }
                }
                body?.let {
                    contentType(ContentType.Application.FormUrlEncoded)
                    setBody(it)
                }
            }
            
            else -> httpClient.get(url) {
                headers {
                    append("User-Agent", userAgent)
                    append("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    append("Accept-Language", "en-US,en;q=0.9")
                    // Don't set Accept-Encoding - let OkHttp handle compression automatically
                    append("DNT", "1")
                    append("Connection", "keep-alive")
                    append("Upgrade-Insecure-Requests", "1")
                    append("Sec-Fetch-Dest", "document")
                    append("Sec-Fetch-Mode", "navigate")
                    append("Sec-Fetch-Site", "none")
                    append("Sec-Fetch-User", "?1")
                    append("Cache-Control", "max-age=0")
                    
                    // Apply custom headers (can override defaults)
                    customHeaders.forEach { (key, value) ->
                        append(key, value)
                    }
                }
            }
        }
    }
    
    /**
     * Extracts cookies from response headers.
     */
    private fun extractCookies(response: HttpResponse): List<String> {
        return response.headers.getAll("Set-Cookie") ?: emptyList()
    }
}

/**
 * Response from Cloudflare bypass attempt.
 */
data class CloudflareResponse(
    val success: Boolean,
    val statusCode: Int,
    val statusText: String,
    val body: String,
    val headers: Map<String, String>,
    val cookies: List<String> = emptyList(),
    val error: String? = null,
    val isCloudflareChallenge: Boolean = false
)
