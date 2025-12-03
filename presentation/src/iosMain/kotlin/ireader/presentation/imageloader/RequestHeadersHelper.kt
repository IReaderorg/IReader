package ireader.presentation.imageloader

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpHeaders

/**
 * iOS implementation - extracts headers directly from Ktor request.
 */
actual fun HttpRequestBuilder.getHeadersMap(): Map<String, String>? {
    return try {
        val headersMap = mutableMapOf<String, String>()
        this.headers.entries().forEach { (key, values) ->
            headersMap[key] = values.joinToString(", ")
        }
        headersMap.ifEmpty { null }
    } catch (e: Exception) {
        null
    }
}
