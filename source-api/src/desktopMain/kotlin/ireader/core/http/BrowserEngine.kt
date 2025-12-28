package ireader.core.http

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.bodyAsText
import ireader.core.http.cloudflare.CloudflareBypassPluginManager
import ireader.core.http.cloudflare.CloudflareChallenge
import ireader.core.http.cloudflare.PluginBypassResult
import ireader.core.log.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * Desktop implementation of BrowserEngine
 * 
 * Uses a multi-strategy approach:
 * 1. CloudflareBypassPluginManager - Plugin-based bypass (FlareSolverr, etc.)
 * 2. Basic HTTP fetch - For simple pages without JavaScript requirements
 * 
 * To enable full browser capabilities on desktop, install FlareSolverr:
 * - Docker: docker run -d -p 8191:8191 ghcr.io/flaresolverr/flaresolverr:latest
 * - Or download from: https://github.com/FlareSolverr/FlareSolverr/releases
 */
actual class BrowserEngine actual constructor() : BrowserEngineInterface {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    // Lazy HTTP client for basic fetching
    private val httpClient by lazy {
        HttpClient(OkHttp) {
            engine {
                config {
                    followRedirects(true)
                    followSslRedirects(true)
                }
            }
        }
    }
    
    // Plugin manager for Cloudflare bypass (injected via setter for DI compatibility)
    private var bypassManager: CloudflareBypassPluginManager? = null
    
    /**
     * Set the CloudflareBypassPluginManager for plugin-based bypass.
     * Called by DI after construction.
     */
    fun setBypassManager(manager: CloudflareBypassPluginManager) {
        bypassManager = manager
        Log.info { "[DesktopBrowserEngine] Bypass manager configured" }
    }
    
    actual override fun isAvailable(): Boolean {
        // Desktop browser engine is always "available" but with limited capabilities
        // Full capabilities require bypass plugins
        return true
    }
    
    /**
     * Check if any bypass provider is available for full browser capabilities
     */
    suspend fun hasBypassCapability(): Boolean {
        return bypassManager?.hasAvailableProvider() ?: false
    }
    
    actual override suspend fun fetch(
        url: String,
        selector: String?,
        headers: Headers,
        timeout: Long,
        userAgent: String,
    ): BrowserResult = withContext(Dispatchers.IO) {
        // First try basic HTTP fetch
        val basicResult = fetchWithHttpClient(url, headers, userAgent, selector)
        
        // Check if we hit Cloudflare protection
        if (basicResult.statusCode in listOf(403, 503) && 
            basicResult.error?.contains("Cloudflare", ignoreCase = true) == true) {
            
            // Try plugin-based bypass
            val manager = bypassManager
            if (manager != null && manager.hasAvailableProvider()) {
                return@withContext fetchWithBypassManager(url, headers, timeout, userAgent, basicResult.responseBody)
            }
        }
        
        return@withContext basicResult
    }
    
    /**
     * Fetch using CloudflareBypassPluginManager for plugin-based bypass
     */
    private suspend fun fetchWithBypassManager(
        url: String,
        headers: Headers,
        timeout: Long,
        userAgent: String,
        responseBody: String
    ): BrowserResult {
        val manager = bypassManager ?: return BrowserResult(
            responseBody = "",
            cookies = emptyList(),
            statusCode = 500,
            error = "Bypass manager not configured"
        )
        
        try {
            // Detect challenge type from the response
            val challenge = CloudflareChallenge.JSChallenge() // Default to JS challenge for 403/503
            
            Log.info { "[DesktopBrowserEngine] Attempting plugin-based bypass for: $url" }
            
            val result = manager.bypass(
                url = url,
                challenge = challenge,
                headers = headers,
                userAgent = userAgent,
                timeoutMs = timeout
            )
            
            return when (result) {
                is PluginBypassResult.Success -> {
                    Log.info { "[DesktopBrowserEngine] Plugin bypass successful for: $url" }
                    
                    val cookies = result.cookies.map { cookie ->
                        Cookie(
                            name = cookie.name,
                            value = cookie.value,
                            domain = cookie.domain,
                            path = cookie.path,
                            expiresAt = cookie.expiresAt,
                            secure = cookie.secure,
                            httpOnly = cookie.httpOnly
                        )
                    }
                    
                    BrowserResult(
                        responseBody = result.content,
                        cookies = cookies,
                        statusCode = result.statusCode
                    )
                }
                is PluginBypassResult.Failed -> {
                    Log.warn { "[DesktopBrowserEngine] Plugin bypass failed: ${result.reason}" }
                    BrowserResult(
                        responseBody = "",
                        cookies = emptyList(),
                        statusCode = 500,
                        error = "Bypass failed: ${result.reason}"
                    )
                }
                is PluginBypassResult.UserInteractionRequired -> {
                    Log.warn { "[DesktopBrowserEngine] User interaction required: ${result.message}" }
                    BrowserResult(
                        responseBody = "",
                        cookies = emptyList(),
                        statusCode = 403,
                        error = "User interaction required: ${result.message}"
                    )
                }
                is PluginBypassResult.ServiceUnavailable -> {
                    Log.warn { "[DesktopBrowserEngine] Bypass service unavailable: ${result.reason}" }
                    BrowserResult(
                        responseBody = "",
                        cookies = emptyList(),
                        statusCode = 503,
                        error = "Bypass service unavailable: ${result.reason}\n${result.setupInstructions ?: ""}"
                    )
                }
            }
        } catch (e: Exception) {
            Log.error(e, "[DesktopBrowserEngine] Plugin bypass error")
            return BrowserResult(
                responseBody = "",
                cookies = emptyList(),
                statusCode = 500,
                error = "Plugin bypass error: ${e.message}"
            )
        }
    }
    
    /**
     * Basic HTTP fetch without JavaScript rendering
     * Limited capabilities - won't work for JavaScript-heavy sites or Cloudflare protected sites
     */
    private suspend fun fetchWithHttpClient(
        url: String,
        headers: Headers,
        userAgent: String,
        selector: String?
    ): BrowserResult {
        try {
            Log.debug { "[DesktopBrowserEngine] Basic HTTP fetch: $url" }
            
            val response = httpClient.get(url) {
                headers {
                    append("User-Agent", userAgent)
                    headers.forEach { (key, value) ->
                        append(key, value)
                    }
                }
            }
            
            val body = response.bodyAsText()
            val statusCode = response.status.value
            
            // Check if we hit Cloudflare protection
            if (statusCode == 403 || statusCode == 503) {
                val isCloudflare = body.contains("cloudflare", ignoreCase = true) ||
                        body.contains("cf-browser-verification", ignoreCase = true) ||
                        body.contains("Just a moment", ignoreCase = true)
                
                if (isCloudflare) {
                    return BrowserResult(
                        responseBody = body,
                        cookies = emptyList(),
                        statusCode = statusCode,
                        error = "Cloudflare protection detected. Configure a bypass plugin in Settings > Cloudflare Bypass."
                    )
                }
            }
            
            // Check if selector is required but page needs JavaScript
            if (selector != null && !body.contains(selector)) {
                Log.warn { "[DesktopBrowserEngine] Selector '$selector' not found - page may require JavaScript" }
            }
            
            return BrowserResult(
                responseBody = body,
                cookies = emptyList(), // Basic HTTP doesn't extract cookies the same way
                statusCode = statusCode
            )
        } catch (e: Exception) {
            Log.error(e, "[DesktopBrowserEngine] HTTP fetch error")
            return BrowserResult(
                responseBody = "",
                cookies = emptyList(),
                statusCode = 500,
                error = "HTTP fetch failed: ${e.message}"
            )
        }
    }
}
