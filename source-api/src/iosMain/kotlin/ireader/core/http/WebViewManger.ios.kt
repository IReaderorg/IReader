package ireader.core.http

/**
 * iOS implementation of WebViewManager
 * 
 * TODO: Implement using WKWebView
 */
actual class WebViewManger {
    actual var isInit: Boolean = false
    actual var userAgent: String = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1"
    
    actual fun init() {
        isInit = true
    }
    
    actual fun destroy() {
        isInit = false
    }
    
    actual suspend fun loadUrl(url: String): String {
        // TODO: Implement using WKWebView
        return ""
    }
    
    actual suspend fun evaluateJavaScript(script: String): String {
        // TODO: Implement using WKWebView
        return ""
    }
}
