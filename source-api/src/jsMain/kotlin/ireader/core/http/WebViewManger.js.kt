package ireader.core.http

import com.fleeksoft.ksoup.nodes.Document

/**
 * JavaScript implementation of WebViewManger.
 * 
 * In JS/browser context, we don't have a traditional WebView.
 * Instead, we use the browser's native capabilities.
 */
actual class WebViewManger {
    actual var isInit: Boolean = false
    actual var userAgent: String = DEFAULT_USER_AGENT
    actual var selector: String? = null
    actual var html: Document? = null
    actual var webUrl: String? = null
    actual var inProgress: Boolean = false
    
    /**
     * Initialize the WebView.
     * In JS context, returns null as there's no WebView.
     */
    actual fun init(): Any {
        isInit = true
        return Unit
    }
    
    /**
     * Update WebView state.
     */
    actual fun update() {
        // No-op in JS
    }
    
    /**
     * Destroy and cleanup WebView resources.
     */
    actual fun destroy() {
        isInit = false
        html = null
        webUrl = null
    }
    
    /**
     * Load URL in background mode.
     * In JS context, uses fetch API.
     */
    actual fun loadInBackground(url: String, selector: String?, onReady: (String) -> Unit) {
        inProgress = true
        webUrl = url
        this.selector = selector
        
        // Use JS fetch to load the URL
        js("""
            fetch(url)
                .then(function(response) { return response.text(); })
                .then(function(text) { onReady(text); })
                .catch(function(error) { onReady(''); });
        """)
        
        inProgress = false
    }
    
    /**
     * Check if WebView is currently processing in background.
     */
    actual fun isProcessingInBackground(): Boolean = inProgress
    
    /**
     * Check if WebView is available on this platform.
     * In JS context, we use fetch instead of WebView.
     */
    actual fun isAvailable(): Boolean = true
}
