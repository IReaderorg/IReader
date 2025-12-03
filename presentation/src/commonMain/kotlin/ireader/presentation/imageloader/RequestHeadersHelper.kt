package ireader.presentation.imageloader

import io.ktor.client.request.HttpRequestBuilder

/**
 * Platform-specific helper to extract headers from a Ktor HttpRequestBuilder.
 * Returns headers as a Map for cross-platform compatibility.
 */
expect fun HttpRequestBuilder.getHeadersMap(): Map<String, String>?
