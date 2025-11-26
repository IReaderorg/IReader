package ireader.core.http

import okhttp3.HttpUrl

/**
 * Synchronizes cookies between WebView and HTTP clients
 * This ensures consistent cookie state across different network components
 */
expect class CookieSynchronizer {
    /**
     * Sync cookies from WebView to HTTP client storage
     * @param url The URL to sync cookies for
     */
    fun syncFromWebView(url: HttpUrl)
    
    /**
     * Sync cookies from HTTP client storage to WebView
     * @param url The URL to sync cookies for
     */
    fun syncToWebView(url: HttpUrl)
    
    /**
     * Clear all cookies from both WebView and HTTP client
     */
    fun clearAll()
}
