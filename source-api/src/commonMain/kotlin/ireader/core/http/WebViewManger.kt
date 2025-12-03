package ireader.core.http

import com.fleeksoft.ksoup.nodes.Document

/**
 * WebView manager for handling web content loading and Cloudflare bypass
 * Provides platform-specific WebView implementations
 */
expect class WebViewManger {
    var isInit: Boolean
    var userAgent: String
    var selector: String?
    var html: Document?
    var webUrl: String?
    var inProgress: Boolean

    /**
     * Initialize the WebView
     * @return Platform-specific WebView instance
     */
    fun init(): Any

    /**
     * Update WebView state
     */
    fun update()

    /**
     * Destroy and cleanup WebView resources
     */
    fun destroy()
    
    /**
     * Load URL in background mode (invisible to user)
     * @param url The URL to load
     * @param selector CSS selector to wait for
     * @param onReady Callback when content is ready
     */
    fun loadInBackground(url: String, selector: String?, onReady: (String) -> Unit)
    
    /**
     * Check if WebView is currently processing in background
     */
    fun isProcessingInBackground(): Boolean
    
    /**
     * Check if WebView is available on this platform
     */
    fun isAvailable(): Boolean
}
