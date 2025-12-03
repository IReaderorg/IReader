package ireader.core.http

/**
 * iOS implementation of BrowserEngine
 * 
 * TODO: Implement using WKWebView for JavaScript execution
 */
actual class BrowserEngine : BrowserEngineInterface {
    
    actual override suspend fun fetch(
        url: String,
        headers: Map<String, String>,
        cookies: Map<String, String>,
        timeout: Long,
        userAgent: String?
    ): BrowserResponse {
        // TODO: Implement using WKWebView
        return BrowserResponse(
            url = url,
            statusCode = 0,
            body = "",
            headers = emptyMap(),
            cookies = emptyMap()
        )
    }
    
    actual override suspend fun evaluateJavaScript(
        url: String,
        script: String,
        timeout: Long
    ): String {
        // TODO: Implement using WKWebView
        return ""
    }
    
    actual override fun close() {
        // Clean up WKWebView resources
    }
}
