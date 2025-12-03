package ireader.core.http

import android.content.Context
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import ireader.core.util.DefaultDispatcher
import ireader.core.util.createICoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

actual class WebViewManger(private val context: Context) {

    actual var isInit = false
    var webView: WebView? = null

    actual var userAgent = DEFAULT_USER_AGENT

    actual var selector: String? = null
    actual var html: Document? = null
    actual var webUrl: String? = null
    actual var inProgress: Boolean = false
    
    var isBackgroundMode: Boolean = false
    var onContentReady: ((String) -> Unit)? = null
    var lastError: String? = null

    val scope = createICoroutineScope(DefaultDispatcher)
    
    actual fun init(): Any {
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
                                        val htmlContent = view?.getHtml() ?: ""
                                        html = Ksoup.parse(htmlContent)
                                        val hasText = html?.selectFirst(selector.toString()) != null
                                        if (hasText && webUrl == url) {
                                            onContentReady?.invoke(htmlContent)
                                            webUrl = null
                                            selector = null
                                            html = null
                                            inProgress = false
                                            lastError = null
                                        }
                                    } else {
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
    
    actual fun isAvailable(): Boolean = true
    
    actual fun loadInBackground(url: String, selector: String?, onReady: (String) -> Unit) {
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
    
    actual fun isProcessingInBackground(): Boolean {
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
