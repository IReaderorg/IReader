package ireader.plugin.api.http

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * HTTP client factory for plugin network requests.
 * Provides pre-configured Ktor clients for common use cases.
 */
object PluginHttpClient {
    
    /**
     * Default JSON configuration for HTTP requests.
     */
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        prettyPrint = false
    }
    
    /**
     * Create a new HTTP client with JSON support.
     * Remember to close the client when done or use `use { }` block.
     */
    fun create(): HttpClient {
        return HttpClient {
            install(ContentNegotiation) {
                json(json)
            }
        }
    }
    
    /**
     * Create a new HTTP client with custom configuration.
     */
    fun create(block: io.ktor.client.HttpClientConfig<*>.() -> Unit): HttpClient {
        return HttpClient {
            install(ContentNegotiation) {
                json(json)
            }
            block()
        }
    }
}

/**
 * Extension functions for simplified HTTP requests.
 */
suspend fun HttpClient.getText(
    url: String,
    headers: Map<String, String> = emptyMap()
): String {
    return get(url) {
        headers.forEach { (key, value) ->
            header(key, value)
        }
    }.bodyAsText()
}

suspend fun HttpClient.postText(
    url: String,
    body: String,
    contentType: ContentType = ContentType.Application.Json,
    headers: Map<String, String> = emptyMap()
): String {
    return post(url) {
        headers.forEach { (key, value) ->
            header(key, value)
        }
        contentType(contentType)
        setBody(body)
    }.bodyAsText()
}

suspend fun HttpClient.getBytes(
    url: String,
    headers: Map<String, String> = emptyMap()
): ByteArray {
    return get(url) {
        headers.forEach { (key, value) ->
            header(key, value)
        }
    }.readBytes()
}
