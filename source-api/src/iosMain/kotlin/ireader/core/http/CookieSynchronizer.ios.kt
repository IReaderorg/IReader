package ireader.core.http

/**
 * iOS implementation of CookieSynchronizer
 * 
 * TODO: Implement using NSHTTPCookieStorage and WKHTTPCookieStore
 */
actual class CookieSynchronizer {
    
    actual fun syncFromWebView(url: String) {
        // TODO: Sync cookies from WKWebView to HTTP client
    }
    
    actual fun syncToWebView(url: String) {
        // TODO: Sync cookies from HTTP client to WKWebView
    }
    
    actual fun clearAll() {
        // TODO: Clear all cookies from both WKWebView and HTTP client
    }
}
