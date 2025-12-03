package ireader.presentation.imageloader

import io.ktor.client.request.HttpRequestBuilder

/**
 * Desktop implementation - extracts headers from Ktor request and converts to Map.
 */
actual fun HttpRequestBuilder.getHeadersMap(): Map<String, String>? {
    return try {
        val request = this.build().convertToOkHttpRequest()
        request.headers.toMap()
    } catch (e: Exception) {
        null
    }
}

private fun okhttp3.Headers.toMap(): Map<String, String> {
    val map = mutableMapOf<String, String>()
    for (i in 0 until size) {
        map[name(i)] = value(i)
    }
    return map
}
