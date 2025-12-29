package ireader.core.http.cloudflare

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import ireader.core.log.Log
import ireader.core.util.currentTimeMillis

/**
 * Built-in FlareSolverr bypass provider.
 * 
 * FlareSolverr is a proxy server that solves Cloudflare challenges using a real browser.
 * 
 * Setup:
 * - Docker: docker run -d -p 8191:8191 ghcr.io/flaresolverr/flaresolverr:latest
 * - Or download from: https://github.com/FlareSolverr/FlareSolverr/releases
 * 
 * Configuration:
 * - Default URL: http://localhost:8191/v1
 * - Can be configured via preferences
 * 
 * Auto-start:
 * - If FlareSolverr is downloaded but not running, it will be auto-started when needed
 */
class FlareSolverrProvider(
    private val httpClient: HttpClient,
    private val getServerUrl: () -> String = { "http://localhost:8191/v1" },
    private val autoStarter: FlareSolverrAutoStarter = NoOpFlareSolverrAutoStarter
) : CloudflareBypassProvider {
    
    override val id: String = "ireader.builtin.flaresolverr"
    override val name: String = "FlareSolverr"
    override val priority: Int = 100
    
    private val instanceId = System.identityHashCode(this)
    
    init {
        Log.debug { "[FlareSolverrProvider] Instance created: $instanceId" }
    }
    
    private val json = kotlinx.serialization.json.Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true  // Important: include fields with default values like 'cmd'
    }
    
    private var cachedAvailability: Boolean? = null
    private var lastAvailabilityCheck: Long = 0
    private val availabilityCacheDuration = 5_000L // 5 seconds - short cache to quickly detect server start
    private var autoStartAttempted = false
    
    /**
     * Force refresh availability check on next call.
     */
    fun invalidateAvailabilityCache() {
        cachedAvailability = null
        lastAvailabilityCheck = 0
        autoStartAttempted = false
    }
    
    override suspend fun isAvailable(): Boolean {
        val now = currentTimeMillis()
        
        Log.debug { "[FlareSolverrProvider:$instanceId] isAvailable() called, cached=$cachedAvailability, autoStartAttempted=$autoStartAttempted" }
        
        // Use cached result if recent
        if (cachedAvailability != null && (now - lastAvailabilityCheck) < availabilityCacheDuration) {
            Log.debug { "[FlareSolverrProvider:$instanceId] Using cached availability: $cachedAvailability" }
            return cachedAvailability!!
        }
        
        // First, check if server is already running
        Log.debug { "[FlareSolverrProvider] Checking if FlareSolverr server is running..." }
        val serverRunning = checkServerRunning()
        if (serverRunning) {
            Log.debug { "[FlareSolverrProvider] Server is running!" }
            cachedAvailability = true
            lastAvailabilityCheck = now
            return true
        }
        
        Log.debug { "[FlareSolverrProvider] Server not running, checking if downloaded..." }
        
        // Server not running - try auto-start if downloaded and not already attempted
        if (!autoStartAttempted && autoStarter.isDownloaded()) {
            autoStartAttempted = true
            Log.debug { "[FlareSolverrProvider] FlareSolverr not running but downloaded - attempting auto-start" }
            
            if (autoStarter.startServer()) {
                // Wait for server to become ready (up to 60 seconds - FlareSolverr can take a while to start)
                val startTime = currentTimeMillis()
                val maxWaitTime = 60_000L
                
                while (currentTimeMillis() - startTime < maxWaitTime) {
                    kotlinx.coroutines.delay(2000) // Check every 2 seconds
                    if (checkServerRunning()) {
                        Log.debug { "[FlareSolverrProvider] FlareSolverr auto-started successfully" }
                        cachedAvailability = true
                        lastAvailabilityCheck = currentTimeMillis()
                        return true
                    }
                }
                Log.debug { "[FlareSolverrProvider] FlareSolverr auto-start timed out after ${maxWaitTime/1000}s" }
            } else {
                Log.debug { "[FlareSolverrProvider] Failed to auto-start FlareSolverr" }
            }
        } else if (!autoStarter.isDownloaded()) {
            Log.debug { "[FlareSolverrProvider] FlareSolverr not downloaded" }
        }
        
        cachedAvailability = false
        lastAvailabilityCheck = now
        return false
    }
    
    /**
     * Check if the FlareSolverr server is running and responding.
     */
    private suspend fun checkServerRunning(): Boolean {
        return try {
            val serverUrl = getServerUrl()
            // Use 127.0.0.1 instead of localhost to avoid DNS resolution issues
            val baseUrl = serverUrl
                .removeSuffix("/v1")
                .removeSuffix("/")
                .replace("localhost", "127.0.0.1")
            Log.debug { "[FlareSolverrProvider] Checking server at $baseUrl" }
            
            val response = httpClient.get(baseUrl)
            val available = response.status.value in 200..299
            
            Log.debug { "[FlareSolverrProvider] Server check result: $available (status: ${response.status.value})" }
            available
        } catch (e: Exception) {
            Log.debug { "[FlareSolverrProvider] Service not available: ${e.message}" }
            false
        }
    }
    
    override suspend fun canHandle(challenge: CloudflareChallenge): Boolean {
        return when (challenge) {
            is CloudflareChallenge.JSChallenge -> true
            is CloudflareChallenge.ManagedChallenge -> true
            is CloudflareChallenge.TurnstileChallenge -> true // FlareSolverr can handle some Turnstile
            is CloudflareChallenge.CaptchaChallenge -> false // Requires manual solving
            is CloudflareChallenge.BlockedIP -> false // Can't bypass IP blocks
            is CloudflareChallenge.RateLimited -> false // Should wait instead
            is CloudflareChallenge.Unknown -> true // Try anyway
            CloudflareChallenge.None -> true
        }
    }
    
    override suspend fun bypass(request: BypassRequest): PluginBypassResult {
        Log.debug { "[FlareSolverrProvider] bypass() called for ${request.url}" }
        
        if (!isAvailable()) {
            Log.debug { "[FlareSolverrProvider] Service not available in bypass()" }
            return PluginBypassResult.ServiceUnavailable(
                reason = "FlareSolverr service is not running",
                setupInstructions = """
                    To use FlareSolverr, you need to run the service:
                    
                    Docker (recommended):
                    docker run -d -p 8191:8191 ghcr.io/flaresolverr/flaresolverr:latest
                    
                    Or download from:
                    https://github.com/FlareSolverr/FlareSolverr/releases
                """.trimIndent()
            )
        }
        
        return try {
            var serverUrl = getServerUrl().replace("localhost", "127.0.0.1")
            // Ensure URL ends with /v1 for the API endpoint (no trailing slash)
            serverUrl = serverUrl.trimEnd('/')
            if (!serverUrl.endsWith("/v1")) {
                serverUrl = "$serverUrl/v1"
            }
            Log.debug { "[FlareSolverrProvider] Sending bypass POST request to $serverUrl" }
            
            val solverrRequest = FlareSolverrRequest(
                cmd = if (request.postData != null) "request.post" else "request.get",
                url = request.url,
                maxTimeout = 120000,  // 2 minutes - some challenges take longer
                postData = request.postData,
                returnOnlyCookies = request.cookiesOnly
            )
            
            val requestJson = json.encodeToString(FlareSolverrRequest.serializer(), solverrRequest)
            Log.debug { "[FlareSolverrProvider] Request JSON: $requestJson" }
            
            val response = httpClient.post(serverUrl) {
                contentType(ContentType.Application.Json)
                setBody(io.ktor.http.content.TextContent(requestJson, ContentType.Application.Json))
            }
            
            Log.debug { "[FlareSolverrProvider] HTTP response status: ${response.status}" }
            val responseText = response.bodyAsText()
            Log.debug { "[FlareSolverrProvider] Response (first 500 chars): ${responseText.take(500)}" }
            
            // Check for error response format (different from success format)
            if (responseText.contains("\"error\"") && responseText.contains("405")) {
                Log.debug { "[FlareSolverrProvider] Got 405 Method Not Allowed - check if URL is correct: $serverUrl" }
                return PluginBypassResult.Failed(
                    reason = "FlareSolverr API error: Method not allowed. Make sure URL ends with /v1",
                    canRetry = false
                )
            }
            
            val solverrResponse = json.decodeFromString(FlareSolverrResponse.serializer(), responseText)
            Log.debug { "[FlareSolverrProvider] FlareSolverr response - status: ${solverrResponse.status}, message: '${solverrResponse.message}', hasSolution: ${solverrResponse.solution != null}" }
            
            when {
                solverrResponse.status == "ok" && solverrResponse.solution != null -> {
                    val solution = solverrResponse.solution
                    Log.debug { "[FlareSolverrProvider] Solution - url: ${solution.url}, status: ${solution.status}, cookies: ${solution.cookies.size}" }
                    
                    val cookies = solution.cookies.map { cookie ->
                        BypassCookie(
                            name = cookie.name,
                            value = cookie.value,
                            domain = cookie.domain ?: extractDomain(request.url),
                            path = cookie.path ?: "/",
                            expiresAt = cookie.expires?.toLong() ?: 0L,
                            secure = cookie.secure ?: false,
                            httpOnly = cookie.httpOnly ?: false
                        )
                    }
                    
                    Log.debug { "[FlareSolverrProvider] Bypass successful for ${request.url}" }
                    
                    PluginBypassResult.Success(
                        content = solution.response ?: "",
                        cookies = cookies,
                        userAgent = solution.userAgent,
                        finalUrl = solution.url,
                        statusCode = solution.status
                    )
                }
                solverrResponse.message.contains("timeout", ignoreCase = true) -> {
                    Log.debug { "[FlareSolverrProvider] Timeout: ${solverrResponse.message}" }
                    PluginBypassResult.Failed(
                        reason = "FlareSolverr timeout: ${solverrResponse.message}",
                        canRetry = true,
                        retryAfterMs = 5000
                    )
                }
                solverrResponse.message.contains("captcha", ignoreCase = true) ||
                solverrResponse.message.contains("challenge", ignoreCase = true) -> {
                    PluginBypassResult.UserInteractionRequired(
                        message = "Manual verification required: ${solverrResponse.message}",
                        verificationUrl = request.url
                    )
                }
                else -> {
                    val reason = if (solverrResponse.message.isNotEmpty()) {
                        "FlareSolverr error: ${solverrResponse.message}"
                    } else {
                        "FlareSolverr returned status '${solverrResponse.status}' without solution"
                    }
                    Log.debug { "[FlareSolverrProvider] $reason" }
                    PluginBypassResult.Failed(
                        reason = reason,
                        canRetry = true
                    )
                }
            }
        } catch (e: Exception) {
            Log.debug { "[FlareSolverrProvider] Bypass error: ${e.message}" }
            // Reset availability cache on error
            cachedAvailability = null
            
            PluginBypassResult.Failed(
                reason = "FlareSolverr error: ${e.message}",
                canRetry = true
            )
        }
    }
    
    override fun getStatusDescription(): String {
        return when {
            cachedAvailability == true -> "Connected to ${getServerUrl()}"
            cachedAvailability == false -> "Not connected - service unavailable"
            else -> "Not checked"
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
