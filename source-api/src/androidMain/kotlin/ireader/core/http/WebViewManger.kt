package ireader.core.http

import android.content.Context
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import ireader.core.http.fingerprint.FingerprintEvasionScripts
import ireader.core.log.Log
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
    
    // Cloudflare handling
    private var isShowingChallenge = false
    private var challengeWebView: WebView? = null
    private var onChallengeComplete: ((String) -> Unit)? = null

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
                                
                                if (isCloudflareChallenge && !isShowingChallenge) {
                                    // Show WebView to user for manual Cloudflare completion
                                    showChallengeWebView(url)
                                } else if (!isCloudflareChallenge) {
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
                                            hideChallengeWebView()
                                        }
                                    } else {
                                        val content = view?.getHtml() ?: ""
                                        onContentReady?.invoke(content)
                                        inProgress = false
                                        lastError = null
                                        hideChallengeWebView()
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
    
    /**
     * Show a visible WebView for the user to complete Cloudflare challenge manually
     */
    private fun showChallengeWebView(url: String?) {
        isShowingChallenge = true
        
        // Create a visible WebView for the user to interact with
        challengeWebView = WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.userAgentString = DEFAULT_USER_AGENT
            
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, startedUrl: String?, favicon: android.graphics.Bitmap?) {
                    super.onPageStarted(view, startedUrl, favicon)
                    // Inject evasion before page JS runs
                    view?.evaluateJavascript(FingerprintEvasionScripts.fullEvasion, null)
                }
                
                override fun onPageFinished(view: WebView?, finishedUrl: String?) {
                    super.onPageFinished(view, finishedUrl)
                    // Re-inject webdriver evasion
                    view?.evaluateJavascript(FingerprintEvasionScripts.webdriverEvasion, null)
                    
                    // Check if challenge is completed
                    val title = view?.title ?: ""
                    val isStillChallenge = title.contains("Cloudflare", true) || 
                                          title.contains("Just a moment", true) ||
                                          title.contains("Checking your browser", true)
                    
                    if (!isStillChallenge && isShowingChallenge) {
                        // Challenge completed! Copy cookies and reload in main WebView
                        scope.launch {
                            delay(500) // Wait for cookies to settle
                            val cookies = android.webkit.CookieManager.getInstance().getCookie(finishedUrl)
                            Log.info("Cloudflare challenge completed, cookies: $cookies")
                            
                            // Reload the original URL in the main WebView
                            webView?.loadUrl(url ?: finishedUrl ?: "")
                            hideChallengeWebView()
                        }
                    }
                }
            }
            
            webChromeClient = WebChromeClient()
            
            // Load the URL
            loadUrl(url ?: "")
        }
        
        Log.info("Showing Cloudflare challenge WebView for user to complete")
    }
    
    /**
     * Hide the challenge WebView
     */
    private fun hideChallengeWebView() {
        isShowingChallenge = false
        challengeWebView?.let { webView ->
            webView.stopLoading()
            webView.destroy()
        }
        challengeWebView = null
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
