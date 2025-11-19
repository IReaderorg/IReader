package ireader.domain.js.library

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking

/**
 * JavaScript fetch API implementation using Ktor HttpClient.
 * Provides a fetch() function compatible with the Fetch API standard.
 * Supports Cloudflare bypass with two strategies:
 * 1. Native bypass (built-in, automatic)
 * 2. FlareSolverr (comprehensive, for stubborn sites)
 */
class JSFetchApi(
    private val httpClient: HttpClient,
    private val pluginId: String,
    private val validator: ireader.domain.js.util.JSPluginValidator = ireader.domain.js.util.JSPluginValidator(),
    private val allowLocalhost: Boolean = false,
    private val flareSolverrClient: ireader.domain.http.FlareSolverrClient? = null
) {
    
    private val cloudflareBypass = ireader.domain.http.CloudflareBypass(httpClient)
    
    /**
     * Performs an HTTP request similar to the Fetch API.
     * Automatically handles Cloudflare protection with two strategies:
     * 1. Native bypass with proper headers and retry logic
     * 2. FlareSolverr fallback (if configured and available)
     * 
     * @param url The URL to fetch
     * @param init Request initialization options (method, headers, body)
     * @return A map representing the Response object
     */
    fun fetch(url: String, init: Map<String, Any?>? = null): Map<String, Any?> {
        return try {
            println("[JSFetchApi] [$pluginId] Fetching: $url")
            
            // Validate URL for security
            val validationResult = validator.validateNetworkRequest(url, allowLocalhost)
            if (!validationResult.isValid()) {
                println("[JSFetchApi] [$pluginId] URL validation failed: ${validationResult.getError()}")
                return mapOf(
                    "ok" to false,
                    "status" to 0,
                    "statusText" to "Security Error",
                    "url" to url,
                    "text" to "",
                    "error" to validationResult.getError()
                )
            }
            
            runBlocking {
                try {
                val method = (init?.get("method") as? String)?.uppercase() ?: "GET"
                val headersMap = init?.get("headers") as? Map<String, String> ?: emptyMap()
                
                // Handle different body types
                val bodyObj = init?.get("body")
                val body = when (bodyObj) {
                    is String -> bodyObj
                    is Map<*, *> -> {
                        val formData = bodyObj as? Map<String, Any?>
                        formData?.get("data")?.let { data ->
                            (data as? Map<*, *>)?.entries?.joinToString("&") { (key, values) ->
                                val valueList = values as? List<*> ?: listOf(values)
                                valueList.joinToString("&") { value ->
                                    "$key=${java.net.URLEncoder.encode(value.toString(), "UTF-8")}"
                                }
                            }
                        } ?: ""
                    }
                    else -> bodyObj?.toString()
                }
                
                println("[JSFetchApi] [$pluginId] Method: $method, Headers: $headersMap")
                println("[JSFetchApi] [$pluginId] FlareSolverr client initialized: ${flareSolverrClient != null}")
                
                // Check if we have stored cookies from FlareSolverr
                val storedCookies = flareSolverrClient?.getCookieHeader(url)
                val headersWithCookies = if (storedCookies != null) {
                    println("[JSFetchApi] [$pluginId] Using stored FlareSolverr cookies")
                    headersMap + ("Cookie" to storedCookies)
                } else {
                    headersMap
                }
                
                // Try native Cloudflare bypass first (with stored cookies if available)
                val cfResponse = cloudflareBypass.fetch(url, method, body, headersWithCookies)
                
                // If successful, return the response
                if (cfResponse.success) {
                    println("[JSFetchApi] [$pluginId] Success: ${cfResponse.statusCode} (${cfResponse.body.length} bytes)")
                    // Log a sample of the HTML to help debug parsing issues
                    val htmlSample = cfResponse.body.take(500).replace("\n", " ")
                    println("[JSFetchApi] [$pluginId] HTML sample: $htmlSample...")
                    return@runBlocking mapOf(
                        "ok" to true,
                        "status" to cfResponse.statusCode,
                        "statusText" to cfResponse.statusText,
                        "url" to url,
                        "text" to cfResponse.body,
                        "headers" to cfResponse.headers
                    )
                }
                
                // If we got a 403 error (likely Cloudflare), try FlareSolverr
                if (cfResponse.statusCode == 403 && flareSolverrClient != null) {
                    println("[JSFetchApi] [$pluginId] Got 403 error, trying FlareSolverr...")
                    println("[JSFetchApi] [$pluginId] FlareSolverr client: ${if (flareSolverrClient != null) "available" else "null"}")
                    
                    try {
                        if (flareSolverrClient.isAvailable()) {
                            // Use withTimeout to prevent hanging
                            val solverrResponse = kotlinx.coroutines.withTimeout(90000L) { // 90 second timeout
                                flareSolverrClient.solve(url, method, body, headersMap)
                            }
                            
                            if (solverrResponse.status == "ok" && solverrResponse.solution != null) {
                                println("[JSFetchApi] [$pluginId] FlareSolverr success!")
                                return@runBlocking mapOf(
                                    "ok" to (solverrResponse.solution.status in 200..299),
                                    "status" to solverrResponse.solution.status,
                                    "statusText" to "OK",
                                    "url" to solverrResponse.solution.url,
                                    "text" to solverrResponse.solution.response,
                                    "headers" to (solverrResponse.solution.headers ?: emptyMap())
                                )
                            } else {
                                println("[JSFetchApi] [$pluginId] FlareSolverr failed: ${solverrResponse.message}")
                                println("[JSFetchApi] [$pluginId] FlareSolverr timeout or error - falling back to manual bypass")
                            }
                        } else {
                            println("[JSFetchApi] [$pluginId] FlareSolverr not available")
                        }
                    } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                        println("[JSFetchApi] [$pluginId] FlareSolverr timeout after 90 seconds")
                    } catch (e: Exception) {
                        println("[JSFetchApi] [$pluginId] FlareSolverr error: ${e.message}")
                    }
                }
                
                // Return the error response
                println("[JSFetchApi] [$pluginId] Failed: ${cfResponse.statusCode} - ${cfResponse.error}")
                mapOf(
                    "ok" to false,
                    "status" to cfResponse.statusCode,
                    "statusText" to cfResponse.statusText,
                    "url" to url,
                    "text" to cfResponse.body,
                    "error" to (cfResponse.error ?: "Request failed"),
                    "headers" to cfResponse.headers
                )
                } catch (e: Exception) {
                    println("[JSFetchApi] [$pluginId] Exception: ${e.message}")
                    e.printStackTrace()
                    mapOf(
                        "ok" to false,
                        "status" to 0,
                        "statusText" to "Network Error",
                        "url" to url,
                        "text" to "",
                        "error" to e.message
                    )
                }
            }
        } catch (e: Exception) {
            println("[JSFetchApi] [$pluginId] Fatal exception: ${e.message}")
            e.printStackTrace()
            mapOf(
                "ok" to false,
                "status" to 0,
                "statusText" to "Fatal Error",
                "url" to url,
                "text" to "",
                "error" to "Fatal error: ${e.message}"
            )
        }
    }
    
    /**
     * Creates a JavaScript-compatible fetch function string for injection.
     */
    fun toJavaScriptFunction(): String {
        return """
            function fetch(url, init) {
                return __nativeFetch(url, init || {});
            }
        """.trimIndent()
    }
}
