package ireader.core.http.cloudflare

import io.ktor.client.HttpClient
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
 */
class FlareSolverrProvider(
    private val httpClient: HttpClient,
    private val getServerUrl: () -> String = { "http://localhost:8191/v1" }
) : CloudflareBypassProvider {
    
    override val id: String = "ireader.builtin.flaresolverr"
    override val name: String = "FlareSolverr"
    override val priority: Int = 100
    
    private val json = kotlinx.serialization.json.Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    private var cachedAvailability: Boolean? = null
    private var lastAvailabilityCheck: Long = 0
    private val availabilityCacheDuration = 60_000L // 1 minute
    
    override suspend fun isAvailable(): Boolean {
        val now = currentTimeMillis()
        
        // Use cached result if recent
        if (cachedAvailability != null && (now - lastAvailabilityCheck) < availabilityCacheDuration) {
            return cachedAvailability!!
        }
        
        return try {
            val serverUrl = getServerUrl()
            val response = httpClient.post(serverUrl) {
                contentType(ContentType.Application.Json)
                setBody("""{"cmd":"sessions.list"}""")
            }
            val available = response.status.value in 200..299
            cachedAvailability = available
            lastAvailabilityCheck = now
            
            if (available) {
                Log.info { "[FlareSolverrProvider] Service available at $serverUrl" }
            }
            available
        } catch (e: Exception) {
            Log.debug { "[FlareSolverrProvider] Service not available: ${e.message}" }
            cachedAvailability = false
            lastAvailabilityCheck = now
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
        if (!isAvailable()) {
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
            val serverUrl = getServerUrl()
            val solverrRequest = FlareSolverrRequest(
                cmd = if (request.postData != null) "request.post" else "request.get",
                url = request.url,
                maxTimeout = request.timeoutMs.toInt().coerceAtMost(180000),
                postData = request.postData,
                returnOnlyCookies = request.cookiesOnly
            )
            
            val requestJson = json.encodeToString(FlareSolverrRequest.serializer(), solverrRequest)
            Log.debug { "[FlareSolverrProvider] Sending request to $serverUrl for ${request.url}" }
            
            val response = httpClient.post(serverUrl) {
                contentType(ContentType.Application.Json)
                setBody(requestJson)
            }
            
            val responseText = response.bodyAsText()
            val solverrResponse = json.decodeFromString(FlareSolverrResponse.serializer(), responseText)
            
            when {
                solverrResponse.status == "ok" && solverrResponse.solution != null -> {
                    val solution = solverrResponse.solution
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
                    
                    Log.info { "[FlareSolverrProvider] Bypass successful for ${request.url}" }
                    
                    PluginBypassResult.Success(
                        content = solution.response ?: "",
                        cookies = cookies,
                        userAgent = solution.userAgent,
                        finalUrl = solution.url,
                        statusCode = solution.status
                    )
                }
                solverrResponse.message.contains("timeout", ignoreCase = true) -> {
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
                    PluginBypassResult.Failed(
                        reason = "FlareSolverr error: ${solverrResponse.message}",
                        canRetry = true
                    )
                }
            }
        } catch (e: Exception) {
            Log.error(e, "[FlareSolverrProvider] Bypass error")
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
