package ireader.core.http

import android.content.Context
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import ireader.core.util.DefaultDispatcher
import ireader.core.util.createICoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

actual class WebViewManger(private val context: Context) {

    actual var isInit = false
    var webView: WebView? = null

    actual var userAgent = DEFAULT_USER_AGENT

    actual  var selector: String? = null
    actual var html: org.jsoup.nodes.Document = org.jsoup.nodes.Document("")
    actual var webUrl: String? = null
    actual var inProgress: Boolean = false
    
    // New properties for improved integration
    var isBackgroundMode: Boolean = false // When true, WebView works invisibly
    var onContentReady: ((String) -> Unit)? = null // Callback when content is ready
    var lastError: String? = null

    val scope = createICoroutineScope(DefaultDispatcher)
    actual fun init() : Any {
        if (webView == null) {
            webView = WebView(context)
            webView?.setDefaultSettings()
            val webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    scope.launch {
                        while (true) {
                            if (inProgress) {
                                val title = view?.title ?: ""
                                val isCloudflareChallenge = title.contains("Cloudflare", true) || 
                                                           title.contains("Just a moment", true) ||
                                                           title.contains("Checking your browser", true)
                                
                                if (!isCloudflareChallenge) {
                                    if (!selector.isNullOrBlank()) {
                                        html = Jsoup.parse(view?.getHtml() ?: "")
                                        val hasText = html.select(selector.toString()).first() != null
                                        if (hasText && webUrl == url) {
                                            // Content is ready
                                            val content = view?.getHtml() ?: ""
                                            onContentReady?.invoke(content)
                                            
                                            webUrl = null
                                            selector = null
                                            html = Document("")
                                            inProgress = false
                                            lastError = null
                                        }
                                    } else {
                                        // No selector specified, just wait for page load
                                        val content = view?.getHtml() ?: ""
                                        onContentReady?.invoke(content)
                                        inProgress = false
                                        lastError = null
                                    }
                                }
                            }
                            delay(1000L)
                        }
                    }
                }
                
                override fun onReceivedError(
                    view: WebView?,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String?
                ) {
                    super.onReceivedError(view, errorCode, description, failingUrl)
                    lastError = description
                    if (errorCode !in listOf(403, 503)) {
                        // Not a Cloudflare challenge, actual error
                        inProgress = false
                    }
                }
            }

            webView?.webViewClient = webViewClient
            webView?.webChromeClient = WebChromeClient()
            webView?.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            isInit = true
            return webView as WebView
        } else {
            return webView as WebView
        }
    }
    
    /**
     * Load URL in background mode (invisible to user)
     * Useful for bypassing Cloudflare without disrupting reading
     */
    fun loadInBackground(url: String, selector: String? = null, onReady: (String) -> Unit) {
        isBackgroundMode = true
        this.selector = selector
        this.webUrl = url
        this.onContentReady = onReady
        inProgress = true
        
        if (!isInit) {
            init()
        }
        
        webView?.loadUrl(url)
    }
    
    /**
     * Check if WebView is currently processing in background
     */
    fun isProcessingInBackground(): Boolean {
        return isBackgroundMode && inProgress
    }

    actual fun update() {
    }

    actual fun destroy() {
        webView?.stopLoading()
        webView?.destroy()
        isInit = false
        webView = null
    }
}
