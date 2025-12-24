package ireader.core.http

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.bodyAsText
import ireader.core.log.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Desktop implementation of BrowserEngine
 * 
 * Uses a multi-strategy approach:
 * 1. FlareSolverr (if available) - Full browser capabilities with Cloudflare bypass
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
    
    // FlareSolverr configuration
    private var flareSolverrUrl: String = "http://localhost:8191/v1"
    private var flareSolverrAvailable: Boolean? = null
    
    /**
     * Configure FlareSolverr endpoint
     */
    fun configureFlareSolverr(url: String) {
        flareSolverrUrl = url
        flareSolverrAvailable = null // Reset availability check
    }
    
    actual override fun isAvailable(): Boolean {
        // Desktop browser engine is always "available" but with limited capabilities
        // Full capabilities require FlareSolverr
        return true
    }
    
    /**
     * Check if FlareSolverr is available for full browser capabilities
     */
    suspend fun isFlareSolverrAvailable(): Boolean {
        if (flareSolverrAvailable != null) return flareSolverrAvailable!!
        
        return try {
            val response = httpClient.post(flareSolverrUrl) {
                contentType(ContentType.Application.Json)
                setBody("""{"cmd":"sessions.list"}""")
            }
            flareSolverrAvailable = response.status.value in 200..299
            if (flareSolverrAvailable == true) {
                Log.info { "[DesktopBrowserEngine] FlareSolverr available at $flareSolverrUrl" }
            }
            flareSolverrAvailable!!
        } catch (e: Exception) {
            Log.debug { "[DesktopBrowserEngine] FlareSolverr not available: ${e.message}" }
            flareSolverrAvailable = false
            false
        }
    }
    
    actual override suspend fun fetch(
        url: String,
        selector: String?,
        headers: Headers,
        timeout: Long,
        userAgent: String,
    ): BrowserResult = withContext(Dispatchers.IO) {
        // Try FlareSolverr first for full browser capabilities
        if (isFlareSolverrAvailable()) {
            return@withContext fetchWithFlareSolverr(url, headers, timeout, userAgent)
        }
        
        // Fall back to basic HTTP fetch
        return@withContext fetchWithHttpClient(url, headers, userAgent, selector)
    }
    
    /**
     * Fetch using FlareSolverr for full browser capabilities
     */
    private suspend fun fetchWithFlareSolverr(
        url: String,
        headers: Headers,
        timeout: Long,
        userAgent: String
    ): BrowserResult {
        try {
            val request = FlareSolverrRequestDto(
                cmd = "request.get",
                url = url,
                maxTimeout = timeout.toInt().coerceAtMost(180000),
                headers = headers.takeIf { it.isNotEmpty() }
            )
            
            val requestJson = json.encodeToString(FlareSolverrRequestDto.serializer(), request)
            Log.debug { "[DesktopBrowserEngine] FlareSolverr request to: $url" }
            
            val response = httpClient.post(flareSolverrUrl) {
                contentType(ContentType.Application.Json)
                setBody(requestJson)
            }
            
            val responseText = response.bodyAsText()
            val solverrResponse = json.decodeFromString(FlareSolverrResponseDto.serializer(), responseText)
            
            if (solverrResponse.status == "ok" && solverrResponse.solution != null) {
                Log.info { "[DesktopBrowserEngine] FlareSolverr success for: $url" }
                
                val cookies = solverrResponse.solution.cookies?.map { cookie ->
                    Cookie(
                        name = cookie.name,
                        value = cookie.value,
                        domain = cookie.domain ?: extractDomain(url),
                        path = cookie.path ?: "/",
                        expiresAt = cookie.expires?.toLong() ?: 0L,
                        secure = cookie.secure ?: false,
                        httpOnly = cookie.httpOnly ?: false
                    )
                } ?: emptyList()
                
                return BrowserResult(
                    responseBody = solverrResponse.solution.response,
                    cookies = cookies,
                    statusCode = solverrResponse.solution.status
                )
            } else {
                Log.warn { "[DesktopBrowserEngine] FlareSolverr failed: ${solverrResponse.message}" }
                return BrowserResult(
                    responseBody = "",
                    cookies = emptyList(),
                    statusCode = 500,
                    error = "FlareSolverr failed: ${solverrResponse.message}"
                )
            }
        } catch (e: Exception) {
            Log.error(e, "[DesktopBrowserEngine] FlareSolverr error")
            // Mark as unavailable and fall back to HTTP client
            flareSolverrAvailable = false
            return BrowserResult(
                responseBody = "",
                cookies = emptyList(),
                statusCode = 500,
                error = "FlareSolverr error: ${e.message}"
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
                        error = "Cloudflare protection detected. Install FlareSolverr for bypass: " +
                                "docker run -d -p 8191:8191 ghcr.io/flaresolverr/flaresolverr:latest"
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
    
    private fun extractDomain(url: String): String {
        return try {
            url.substringAfter("://").substringBefore("/").substringBefore(":")
        } catch (e: Exception) {
            ""
        }
    }
}

// Internal DTOs for FlareSolverr communication
@Serializable
private data class FlareSolverrRequestDto(
    val cmd: String,
    val url: String,
    val maxTimeout: Int = 60000,
    val session: String? = null,
    val postData: String? = null,
    val headers: Map<String, String>? = null
)

@Serializable
private data class FlareSolverrResponseDto(
    val status: String,
    val message: String,
    val solution: FlareSolverrSolutionDto? = null
)

@Serializable
private data class FlareSolverrSolutionDto(
    val url: String,
    val status: Int,
    val headers: Map<String, String>? = null,
    val response: String,
    val cookies: List<FlareSolverrCookieDto>? = null,
    val userAgent: String? = null
)

@Serializable
private data class FlareSolverrCookieDto(
    val name: String,
    val value: String,
    val domain: String? = null,
    val path: String? = null,
    val expires: Double? = null,
    val httpOnly: Boolean? = null,
    val secure: Boolean? = null
)
