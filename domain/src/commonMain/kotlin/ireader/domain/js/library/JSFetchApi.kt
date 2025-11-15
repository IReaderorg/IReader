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
 */
class JSFetchApi(
    private val httpClient: HttpClient,
    private val pluginId: String,
    private val validator: ireader.domain.js.util.JSPluginValidator = ireader.domain.js.util.JSPluginValidator(),
    private val allowLocalhost: Boolean = false
) {
    
    /**
     * Performs an HTTP request similar to the Fetch API.
     * @param url The URL to fetch
     * @param init Request initialization options (method, headers, body)
     * @return A map representing the Response object
     */
    fun fetch(url: String, init: Map<String, Any?>? = null): Map<String, Any?> {
        println("[JSFetchApi] [$pluginId] Fetching: $url")
        
        // Validate URL for security
        val validationResult = validator.validateNetworkRequest(url, allowLocalhost)
        if (!validationResult.isValid()) {
            println("[JSFetchApi] [$pluginId] URL validation failed: ${validationResult.getError()}")
            return mapOf(
                "ok" to false,
                "status" to 0,
                "statusText" to "Security Error",
                "text" to "",
                "error" to validationResult.getError()
            )
        }
        
        return runBlocking {
            try {
                val method = (init?.get("method") as? String)?.uppercase() ?: "GET"
                val headersMap = init?.get("headers") as? Map<String, String> ?: emptyMap()
                val body = init?.get("body") as? String
                
                println("[JSFetchApi] [$pluginId] Method: $method, Headers: $headersMap")
                
                val response: HttpResponse = when (method) {
                    "POST" -> httpClient.post(url) {
                        headers {
                            headersMap.forEach { (key, value) ->
                                append(key, value)
                            }
                        }
                        body?.let {
                            contentType(ContentType.Application.Json)
                            setBody(it)
                        }
                    }
                    else -> httpClient.get(url) {
                        headers {
                            headersMap.forEach { (key, value) ->
                                append(key, value)
                            }
                        }
                    }
                }
                
                val responseText = response.bodyAsText()
                
                println("[JSFetchApi] [$pluginId] Response: ${response.status.value} ${response.status.description}, Body length: ${responseText.length}")
                
                mapOf(
                    "ok" to (response.status.value in 200..299),
                    "status" to response.status.value,
                    "statusText" to response.status.description,
                    "text" to responseText,
                    "headers" to response.headers.entries().associate { it.key to it.value.firstOrNull() }
                )
            } catch (e: Exception) {
                mapOf(
                    "ok" to false,
                    "status" to 0,
                    "statusText" to "Network Error",
                    "text" to "",
                    "error" to e.message
                )
            }
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
