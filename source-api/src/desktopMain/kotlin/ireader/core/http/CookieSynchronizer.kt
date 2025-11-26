package ireader.core.http

import okhttp3.HttpUrl

/**
 * Desktop implementation of cookie synchronization
 * Desktop doesn't have WebView, so this is a no-op implementation
 */
actual class CookieSynchronizer {
    actual fun syncFromWebView(url: HttpUrl) {
        // No-op: Desktop doesn't have WebView
    }
    
    actual fun syncToWebView(url: HttpUrl) {
        // No-op: Desktop doesn't have WebView
    }
    
    actual fun clearAll() {
        // No-op: Desktop doesn't have WebView
    }
}
