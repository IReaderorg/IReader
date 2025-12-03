package ireader.core.http

/**
 * Desktop implementation of BrowserEngine
 * Currently a stub - could be enhanced with JavaFX WebView or JCEF
 */
actual class BrowserEngine actual constructor() : BrowserEngineInterface {
    actual override suspend fun fetch(
        url: String,
        selector: String?,
        headers: Headers,
        timeout: Long,
        userAgent: String
    ): BrowserResult {
       return BrowserResult(
           responseBody = "",
           cookies = emptyList(),
           statusCode = 501,
           error = "BrowserEngine not available on desktop platform. Consider using JavaFX WebView or JCEF for full browser capabilities."
       )
    }
    
    actual override fun isAvailable(): Boolean = false
}
