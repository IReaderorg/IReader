package ireader.core.http

/**
 * JavaScript implementation of CookieSynchronizer.
 * 
 * In browser/JS context, cookies are handled automatically by the browser.
 * For iOS JavaScriptCore, cookies are managed by the native layer.
 */
actual class CookieSynchronizer : CookieSynchronizerInterface {
    
    /**
     * Sync cookies from WebView to HTTP client storage.
     * In JS context, this is a no-op as cookies are shared.
     */
    actual override fun syncFromWebView(url: String) {
        // In browser context, cookies are automatically shared
        // For iOS JavaScriptCore, the native layer handles this
    }
    
    /**
     * Sync cookies from HTTP client to WebView.
     * In JS context, this is a no-op as cookies are shared.
     */
    actual override fun syncToWebView(url: String) {
        // In browser context, cookies are automatically shared
        // For iOS JavaScriptCore, the native layer handles this
    }
    
    /**
     * Clear all cookies.
     */
    actual override fun clearAll() {
        try {
            js("""
                document.cookie.split(';').forEach(function(c) {
                    document.cookie = c.replace(/^ +/, '').replace(/=.*/, '=;expires=' + new Date().toUTCString() + ';path=/');
                });
            """)
        } catch (e: Exception) {
            // Ignore errors in non-browser context (e.g., JavaScriptCore)
        }
    }
}
