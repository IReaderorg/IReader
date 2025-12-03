package ireader.core.http

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.api.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import io.ktor.util.date.*
import io.ktor.utils.io.*
import ireader.core.util.currentTimeMillis
import io.ktor.http.Headers as KtorHeaders

/**
 * Configuration for HTTP cache plugin
 */
class HttpCacheConfig {
    /**
     * Default cache duration in milliseconds (5 minutes)
     */
    var cacheDurationMs: Long = 5 * 60 * 1000
    
    /**
     * Whether to cache responses (can be disabled globally)
     */
    var enabled: Boolean = true
    
    /**
     * HTTP methods to cache (default: GET only)
     */
    var cacheableMethods: Set<HttpMethod> = setOf(HttpMethod.Get)
    
    /**
     * Status codes to cache (default: 200 OK only)
     */
    var cacheableStatusCodes: Set<HttpStatusCode> = setOf(HttpStatusCode.OK)
    
    /**
     * Predicate to determine if a request should be cached
     */
    var shouldCache: (HttpRequestBuilder) -> Boolean = { true }
}

/**
 * Ktor plugin for caching HTTP responses
 */
@OptIn(InternalAPI::class)
val HttpCachePlugin = createClientPlugin("HttpCachePlugin", ::HttpCacheConfig) {
    val cache = HttpCache(pluginConfig.cacheDurationMs)
    
    // Intercept requests
    on(Send) { request ->
        // Check for per-request cache control
        val cacheControl = request.attributes.getOrNull(CacheControlAttribute)
        
        // Check if caching is enabled and method is cacheable
        if (!pluginConfig.enabled || 
            request.method !in pluginConfig.cacheableMethods ||
            !pluginConfig.shouldCache(request) ||
            cacheControl?.useCache == false) {
            return@on proceed(request)
        }
        
        // Generate cache key
        val cacheKey = cache.generateKey(request.url.toString(), request.method)
        
        // Try to get from cache (unless force refresh)
        if (cacheControl?.forceRefresh != true) {
            val cachedEntry = cache.get(cacheKey)
            if (cachedEntry != null) {
                // Return cached response as HttpClientCall
                val responseData = HttpResponseData(
                    statusCode = cachedEntry.statusCode,
                    requestTime = GMTDate(),
                    headers = cachedEntry.headers,
                    version = HttpProtocolVersion.HTTP_1_1,
                    body = ByteReadChannel(cachedEntry.response),
                    callContext = request.executionContext
                )
                return@on HttpClientCall(client, request.build(), responseData)
            }
        }
        
        // Proceed with actual request
        val call = proceed(request)
        
        // Cache response if status code is cacheable
        if (call.response.status in pluginConfig.cacheableStatusCodes) {
            try {
                val responseBody = call.response.readBytes()
                // Use custom cache duration if specified
                val cacheDuration = cacheControl?.cacheDurationMs ?: pluginConfig.cacheDurationMs
                val entry = CacheEntry(
                    response = responseBody,
                    contentType = call.response.contentType(),
                    headers = call.response.headers,
                    statusCode = call.response.status,
                    expiresAt = currentTimeMillis() + cacheDuration
                )
                cache.put(cacheKey, entry)
                
                // Return new call with cached body
                val newResponseData = HttpResponseData(
                    statusCode = call.response.status,
                    requestTime = call.response.responseTime,
                    headers = call.response.headers,
                    version = call.response.version,
                    body = ByteReadChannel(responseBody),
                    callContext = call.response.coroutineContext
                )
                HttpClientCall(client, request.build(), newResponseData)
            } catch (e: Exception) {
                // If caching fails, return original call
                call
            }
        } else {
            call
        }
    }
}

/**
 * Extension to install cache plugin with custom configuration
 */
fun HttpClientConfig<*>.installCache(
    cacheDurationMs: Long = 5 * 60 * 1000,
    block: HttpCacheConfig.() -> Unit = {}
) {
    install(HttpCachePlugin) {
        this.cacheDurationMs = cacheDurationMs
        block()
    }
}
