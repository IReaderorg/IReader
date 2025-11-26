package ireader.core.http

import org.jsoup.nodes.Document

/**
 * Desktop implementation of WebViewManager
 * Currently a stub - could be enhanced with JavaFX WebView or JCEF
 */
actual class WebViewManger {
    actual var isInit: Boolean
        get() = false
        set(value) {}
    actual var userAgent: String = DEFAULT_USER_AGENT
    actual var selector: String?
        get() = null
        set(value) {}
    actual var html: Document
        get() = Document("")
        set(value) {}
    actual var webUrl: String?
        get() = null
        set(value) {}
    actual var inProgress: Boolean
        get() = false
        set(value) {}

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