package ireader.core.http

/**
 * iOS implementation of BrowserEngine using WKWebView
 * 
 * TODO: Full implementation using WKWebView for JavaScript execution
 */
actual class BrowserEngine : BrowserEngineInterface {
    
    actual override suspend fun fetch(
        url: String,
        selector: String?,
        headers: Headers,
        timeout: Long,
        userAgent: String
    ): BrowserResult {
        // TODO: Implement using WKWebView
        return BrowserResult(
            responseBody = "",
            cookies = emptyList(),
            statusCode = 200,
            error = "WKWebView implementation pending"
        )
    }
    
    actual override fun isAvailable(): Boolean {
        // WKWebView is available on iOS 8.0+
        return true
    }
}
