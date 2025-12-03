package ireader.core.http

/**
 * Synchronizes cookies between WebView and HTTP client.
 * Platform-specific implementations handle the actual synchronization.
 */
expect class CookieSynchronizer {
    /**
     * Sync cookies from WebView to HTTP client storage
     * @param url The URL string to sync cookies for
     */
    fun syncFromWebView(url: String)
    
    /**
     * Sync cookies from HTTP client to WebView
     * @param url The URL string to sync cookies for
     */
    fun syncToWebView(url: String)
    
    /**
     * Clear all cookies
     */
    fun clearAll()
}

/**
 * Common cookie synchronizer interface for platform implementations
 */
interface CookieSynchronizerInterface {
    fun syncFromWebView(url: String)
    fun syncToWebView(url: String)
    fun clearAll()
}
