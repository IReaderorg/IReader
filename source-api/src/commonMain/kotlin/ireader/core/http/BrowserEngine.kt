package ireader.core.http

/**
 * Browser engine interface for web scraping and dynamic content loading
 * Provides platform-specific implementations for rendering JavaScript and handling Cloudflare
 */
interface BrowserEngineInterface {
    /**
     * Fetch web content with optional JavaScript rendering
     * @param url The URL to fetch
     * @param selector CSS selector to wait for before returning (null = return immediately after page load)
     * @param headers Custom HTTP headers as Map
     * @param timeout Maximum time to wait in milliseconds
     * @param userAgent Custom user agent string
     * @return BrowserResult containing HTML content and cookies
     */
    suspend fun fetch(
        url: String,
        selector: String? = null,
        headers: Headers = emptyMap(),
        timeout: Long = 50000L,
        userAgent: String = DEFAULT_USER_AGENT
    ): BrowserResult
    
    /**
     * Check if browser engine is available on this platform
     */
    fun isAvailable(): Boolean
}

expect class BrowserEngine() : BrowserEngineInterface {
    override suspend fun fetch(
        url: String,
        selector: String?,
        headers: Headers,
        timeout: Long,
        userAgent: String,
    ): BrowserResult
    
    override fun isAvailable(): Boolean
}

/**
 * Result of a browser engine fetch operation
 * @param responseBody The HTML content
 * @param cookies Cookies received from the response
 * @param statusCode HTTP status code (if available)
 * @param error Error message if fetch failed
 */
data class BrowserResult(
    val responseBody: String,
    val cookies: List<Cookie> = emptyList(),
    val statusCode: Int = 200,
    val error: String? = null
) {
    val isSuccess: Boolean get() = error == null && statusCode in 200..299
}
