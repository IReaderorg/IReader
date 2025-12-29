package ireader.domain.http

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import ireader.core.log.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Client for FlareSolverr - a proxy server to bypass Cloudflare protection.
 * 
 * GitHub: https://github.com/FlareSolverr/FlareSolverr
 * 
 * FlareSolverr uses Selenium/undetected-chromedriver to bypass Cloudflare and DDoS-GUARD protection.
 * 
 * Setup Options for Windows:
 * 
 * Option 1 - Docker (Recommended):
 * 1. Install Docker Desktop for Windows: https://www.docker.com/products/docker-desktop/
 * 2. Run: docker run -d -p 8191:8191 ghcr.io/flaresolverr/flaresolverr:latest
 * 
 * Option 2 - Python (No Docker):
 * 1. Install Python 3.11+: https://www.python.org/downloads/
 * 2. Install Chrome/Chromium
 * 3. Clone: git clone https://github.com/FlareSolverr/FlareSolverr.git
 * 4. Install: pip install -r requirements.txt
 * 5. Run: python src/flaresolverr.py
 * 
 * Option 3 - Executable (Easiest for Windows):
 * 1. Download from: https://github.com/FlareSolverr/FlareSolverr/releases
 * 2. Extract and run FlareSolverr.exe
 * 3. Requires Chrome/Chromium installed
 * 
 * API Endpoint: http://localhost:8191/v1
 * 
 * Note: FlareSolverr is more comprehensive but heavier than Byparr.
 * Consider using Byparr first for better performance.
 */
class FlareSolverrClient(
    private val httpClient: HttpClient,
    private val endpoint: String = "http://localhost:8191/v1",
    private val maxTimeout: Int = 180000 // 3 minutes for complex challenges
) {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    // Cookie storage per domain
    private val cookieStore = mutableMapOf<String, List<FlareSolverrCookie>>()
    
    /**
     * Checks if FlareSolverr is available and responding.
     */
    suspend fun isAvailable(): Boolean {
        return try {
            val response = httpClient.post(endpoint) {
                contentType(ContentType.Application.Json)
                setBody("""{"cmd":"sessions.list"}""")
            }
            response.status.value in 200..299
        } catch (e: Exception) {
            Log.warn { "[FlareSolverr] Not available: ${e.message}" }
            false
        }
    }
    
    /**
     * Solves a Cloudflare challenge and returns the response.
     * 
     * @param url The URL to access
     * @param method HTTP method (GET or POST)
     * @param postData Optional POST data
     * @param headers Optional custom headers
     * @return FlareSolverr response with cookies and HTML content
     */
    suspend fun solve(
        url: String,
        method: String = "GET",
        postData: String? = null,
        headers: Map<String, String>? = null
    ): FlareSolverrResponse {
        try {
            val request = FlareSolverrRequest(
                cmd = "request.${method.lowercase()}",
                url = url,
                maxTimeout = maxTimeout,
                postData = postData,
                headers = headers
            )
            
            val requestJson = json.encodeToString(FlareSolverrRequest.serializer(), request)
            Log.debug { "[FlareSolverr] Sending request to: $url" }
            
            val response = httpClient.post(endpoint) {
                contentType(ContentType.Application.Json)
                setBody(requestJson)
            }
            
            val responseText = response.bodyAsText()
            val solverrResponse = json.decodeFromString(FlareSolverrResponse.serializer(), responseText)
            
            if (solverrResponse.status == "ok") {
                Log.info { "[FlareSolverr] Successfully solved challenge for: $url" }
                
                // Store cookies for future use
                solverrResponse.solution?.cookies?.let { cookies ->
                    val domain = extractDomain(url)
                    cookieStore[domain] = cookies
                    Log.info { "[FlareSolverr] Stored ${cookies.size} cookies for domain: $domain" }
                    cookies.forEach { cookie ->
                        Log.debug { "[FlareSolverr] Cookie: ${cookie.name}=${cookie.value.take(20)}... (domain=${cookie.domain})" }
                    }
                }
            } else {
                Log.error { "[FlareSolverr] Failed to solve: ${solverrResponse.message}" }
            }
            
            return solverrResponse
        } catch (e: Exception) {
            Log.error(e, "[FlareSolverr] Error solving challenge: ${e.message}")
            return FlareSolverrResponse(
                status = "error",
                message = "FlareSolverr error: ${e.message}",
                solution = null
            )
        }
    }
    
    /**
     * Extracts domain from URL for cookie storage.
     */
    private fun extractDomain(url: String): String {
        return try {
            val host = url.substringAfter("://").substringBefore("/")
            // Remove port if present
            host.substringBefore(":")
        } catch (e: Exception) {
            url
        }
    }
    
    /**
     * Gets stored cookies for a domain.
     */
    fun getCookies(url: String): List<FlareSolverrCookie> {
        val domain = extractDomain(url)
        return cookieStore[domain] ?: emptyList()
    }
    
    /**
     * Gets cookies as a Cookie header string.
     */
    fun getCookieHeader(url: String): String? {
        val cookies = getCookies(url)
        if (cookies.isEmpty()) return null
        
        return cookies.joinToString("; ") { "${it.name}=${it.value}" }
    }
    
    /**
     * Clears stored cookies for a domain.
     */
    fun clearCookies(url: String) {
        val domain = extractDomain(url)
        cookieStore.remove(domain)
        Log.info { "[FlareSolverr] Cleared cookies for domain: $domain" }
    }
    
    /**
     * Clears all stored cookies.
     */
    fun clearAllCookies() {
        cookieStore.clear()
        Log.info { "[FlareSolverr] Cleared all stored cookies" }
    }
    
    /**
     * Creates a session for reusing cookies across multiple requests.
     */
    suspend fun createSession(sessionId: String): Boolean {
        return try {
            val request = """{"cmd":"sessions.create","session":"$sessionId"}"""
            val response = httpClient.post(endpoint) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            response.status.value in 200..299
        } catch (e: Exception) {
            Log.error(e, "[FlareSolverr] Failed to create session: ${e.message}")
            false
        }
    }
    
    /**
     * Destroys a session.
     */
    suspend fun destroySession(sessionId: String): Boolean {
        return try {
            val request = """{"cmd":"sessions.destroy","session":"$sessionId"}"""
            val response = httpClient.post(endpoint) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            response.status.value in 200..299
        } catch (e: Exception) {
            Log.error(e, "[FlareSolverr] Failed to destroy session: ${e.message}")
            false
        }
    }
}

@Serializable
data class FlareSolverrRequest(
    val cmd: String,
    val url: String,
    val maxTimeout: Int = 60000,
    val session: String? = null,
    val postData: String? = null,
    val headers: Map<String, String>? = null
)

@Serializable
data class FlareSolverrResponse(
    val status: String = "error",
    val message: String = "",
    val solution: FlareSolverrSolution? = null,
    val startTimestamp: Long? = null,
    val endTimestamp: Long? = null,
    val version: String? = null
)

@Serializable
data class FlareSolverrSolution(
    val url: String,
    val status: Int,
    val headers: Map<String, String>? = null,
    val response: String? = null,
    val cookies: List<FlareSolverrCookie> = emptyList(),
    val userAgent: String? = null
)

@Serializable
data class FlareSolverrCookie(
    val name: String,
    val value: String,
    val domain: String? = null,
    val path: String? = null,
    val expires: Double? = null,
    val size: Int? = null,
    val httpOnly: Boolean? = null,
    val secure: Boolean? = null,
    val session: Boolean? = null,
    val sameSite: String? = null
)
