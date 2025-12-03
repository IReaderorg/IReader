package ireader.core.http

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSError
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSURL
import platform.Foundation.setValue
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.WebKit.javaScriptEnabled
import platform.darwin.NSObject

/**
 * iOS implementation of WebViewManager using WKWebView
 * 
 * Provides WebView functionality for:
 * - Loading web pages with JavaScript support
 * - Cloudflare bypass
 * - Cookie management
 * - HTML content extraction
 */
@OptIn(ExperimentalForeignApi::class)
actual class WebViewManger {
    actual var isInit: Boolean = false
    actual var userAgent: String = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1"
    actual var selector: String? = null
    actual var html: Document? = null
    actual var webUrl: String? = null
    actual var inProgress: Boolean = false
    
    private var webView: WKWebView? = null
    private var navigationDelegate: WebViewManagerDelegate? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    /**
     * Initialize the WebView
     * @return WKWebView instance
     */
    actual fun init(): Any {
        if (isInit && webView != null) {
            return webView!!
        }
        
        val config = WKWebViewConfiguration().apply {
            defaultWebpagePreferences?.allowsContentJavaScript = true
            
            // Enable data detector types
            preferences.javaScriptEnabled = true
            preferences.javaScriptCanOpenWindowsAutomatically = false
        }
        
        // Create WebView with zero frame (invisible)
        webView = WKWebView(
            frame = CGRectMake(0.0, 0.0, 1.0, 1.0),
            configuration = config
        ).apply {
            customUserAgent = userAgent
        }
        
        isInit = true
        return webView!!
    }
    
    /**
     * Update WebView state - refresh current page
     */
    actual fun update() {
        webView?.reload()
    }
    
    /**
     * Destroy and cleanup WebView resources
     */
    actual fun destroy() {
        scope.cancel()
        webView?.stopLoading()
        webView?.navigationDelegate = null
        webView = null
        navigationDelegate = null
        isInit = false
        html = null
        webUrl = null
        selector = null
        inProgress = false
    }
    
    /**
     * Load URL in background mode (invisible to user)
     * @param url The URL to load
     * @param selector CSS selector to wait for
     * @param onReady Callback when content is ready
     */
    actual fun loadInBackground(url: String, selector: String?, onReady: (String) -> Unit) {
        if (inProgress) {
            onReady("")
            return
        }
        
        this.selector = selector
        this.webUrl = url
        inProgress = true
        
        // Ensure WebView is initialized
        if (!isInit) {
            init()
        }
        
        val wv = webView
        if (wv == null) {
            inProgress = false
            onReady("")
            return
        }
        
        // Create navigation delegate
        navigationDelegate = WebViewManagerDelegate(
            selector = selector,
            onComplete = { htmlContent ->
                inProgress = false
                
                // Parse HTML to Document
                if (htmlContent.isNotEmpty()) {
                    try {
                        html = Ksoup.parse(htmlContent)
                    } catch (e: Exception) {
                        html = null
                    }
                }
                
                onReady(htmlContent)
            }
        )
        
        wv.navigationDelegate = navigationDelegate
        
        // Create and load request
        val nsUrl = NSURL.URLWithString(url)
        if (nsUrl == null) {
            inProgress = false
            onReady("")
            return
        }
        
        val request = NSMutableURLRequest.requestWithURL(nsUrl).apply {
            setValue(userAgent, forHTTPHeaderField = "User-Agent")
        }
        
        wv.loadRequest(request)
        
        // Set up timeout (30 seconds)
        scope.launch {
            delay(30000)
            if (inProgress) {
                inProgress = false
                
                // Try to get whatever HTML we have
                wv.evaluateJavaScript("document.documentElement.outerHTML") { result, _ ->
                    val content = result?.toString() ?: ""
                    if (content.isNotEmpty()) {
                        try {
                            html = Ksoup.parse(content)
                        } catch (e: Exception) {
                            html = null
                        }
                    }
                    onReady(content)
                }
            }
        }
    }
    
    /**
     * Check if WebView is currently processing in background
     */
    actual fun isProcessingInBackground(): Boolean = inProgress
    
    /**
     * Check if WebView is available on this platform
     */
    actual fun isAvailable(): Boolean = true
}

/**
 * Navigation delegate for WebViewManager
 */
@OptIn(ExperimentalForeignApi::class)
private class WebViewManagerDelegate(
    private val selector: String?,
    private val onComplete: (String) -> Unit
) : NSObject(), WKNavigationDelegateProtocol {
    
    private var isComplete = false
    private val scope = CoroutineScope(Dispatchers.Main)
    
    override fun webView(webView: WKWebView, didFinishNavigation: WKNavigation?) {
        if (isComplete) return
        
        if (selector != null) {
            waitForSelector(webView, selector)
        } else {
            // Small delay to let JavaScript execute
            scope.launch {
                delay(500)
                getHtmlAndComplete(webView)
            }
        }
    }
    
    @ObjCSignatureOverride
    override fun webView(
        webView: WKWebView,
        didFailNavigation: WKNavigation?,
        withError: NSError
    ) {
        if (isComplete) return
        isComplete = true
        onComplete("")
    }

    private fun waitForSelector(webView: WKWebView, selector: String) {
        val checkScript = """
            (function() {
                var element = document.querySelector('$selector');
                return element !== null;
            })()
        """.trimIndent()
        
        var attempts = 0
        val maxAttempts = 100 // 10 seconds max
        
        fun check() {
            if (isComplete) return
            
            webView.evaluateJavaScript(checkScript) { result, _ ->
                if (isComplete) return@evaluateJavaScript
                
                val found = (result as? Boolean) ?: false
                
                if (found || attempts >= maxAttempts) {
                    getHtmlAndComplete(webView)
                } else {
                    attempts++
                    scope.launch {
                        delay(100)
                        check()
                    }
                }
            }
        }
        
        check()
    }
    
    private fun getHtmlAndComplete(webView: WKWebView) {
        if (isComplete) return
        isComplete = true
        
        webView.evaluateJavaScript("document.documentElement.outerHTML") { result, _ ->
            val html = result?.toString() ?: ""
            onComplete(html)
        }
    }
}
