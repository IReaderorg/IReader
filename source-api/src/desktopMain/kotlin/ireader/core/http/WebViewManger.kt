package ireader.core.http

import com.fleeksoft.ksoup.nodes.Document

/**
 * Desktop implementation of WebViewManager
 * Currently a stub - could be enhanced with JavaFX WebView or JCEF
 */
actual class WebViewManger {
    actual var isInit: Boolean = false
    actual var userAgent: String = DEFAULT_USER_AGENT
    actual var selector: String? = null
    actual var html: Document? = null
    actual var webUrl: String? = null
    actual var inProgress: Boolean = false

    actual fun init(): Any {
        return 0
    }

    actual fun update() {
        // No-op on desktop
    }

    actual fun destroy() {
        // No-op on desktop
    }
    
    actual fun loadInBackground(url: String, selector: String?, onReady: (String) -> Unit) {
        // Not supported on desktop
        onReady("")
    }
    
    actual fun isProcessingInBackground(): Boolean = false
    
    actual fun isAvailable(): Boolean = false
}
