package ireader.core.http

/**
 * Desktop implementation of cookie synchronization
 * Desktop doesn't have WebView, so this is a no-op implementation
 */
actual class CookieSynchronizer {
    actual fun syncFromWebView(url: String) {
        // No-op: Desktop doesn't have WebView
    }
    
    actual fun syncToWebView(url: String) {
        // No-op: Desktop doesn't have WebView
    }
    
    actual fun clearAll() {
        // No-op: Desktop doesn't have WebView
    }
}
