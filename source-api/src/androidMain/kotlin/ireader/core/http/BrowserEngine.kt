package ireader.core.http

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.os.Build
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import ireader.core.log.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.UUID
import java.util.concurrent.TimeoutException
import kotlin.coroutines.resumeWithException

actual class BrowserEngine actual constructor() : BrowserEngineInterface {
    
    private var webViewManger: WebViewManger? = null
    private var webViewCookieJar: WebViewCookieJar? = null
    
    constructor(webViewManger: WebViewManger, webViewCookieJar: WebViewCookieJar) : this() {
        this.webViewManger = webViewManger
        this.webViewCookieJar = webViewCookieJar
    }
    
    actual override fun isAvailable(): Boolean = webViewManger != null

    private val cloudflareBypassDetected = MutableStateFlow(false)
    private val ajaxCompleted = MutableStateFlow(false)
    private val pageLoadComplete = MutableStateFlow(false)
    private val contentExtracted = MutableStateFlow(false)
    
    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    actual override suspend fun fetch(
        url: String,
        selector: String?,
        headers: Headers,
        timeout: Long,
        userAgent: String,
    ): BrowserResult {
        val manager = webViewManger ?: return BrowserResult(
            responseBody = "",
            cookies = emptyList(),
            statusCode = 501,
            error = "WebViewManager not initialized"
        )
        
        val cookieJar = webViewCookieJar
        
        // Reset state flows
        cloudflareBypassDetected.value = false
        ajaxCompleted.value = false
        pageLoadComplete.value = false
        contentExtracted.value = false
        
        var html = ""
        val uniqueId = UUID.randomUUID().toString()
        
        try {
            withContext(Dispatchers.Main) {
                // Initialize or reuse WebView
                manager.init()
                val webView = manager.webView ?: throw IllegalStateException("WebView initialization failed")
                
                // Configure WebView
                configureWebView(webView, userAgent, uniqueId, manager)
                
                // Set up javascript interface for AJAX detection
                webView.addJavascriptInterface(object : Any() {
                    @JavascriptInterface
                    fun onAjaxComplete() {
                        ajaxCompleted.value = true
                    }
                    
                    @JavascriptInterface
                    fun onCloudflareBypass() {
                        cloudflareBypassDetected.value = true
                    }
                    
                    @JavascriptInterface
                    fun contentReady() {
                        contentExtracted.value = true
                    }
                }, "AndroidInterface")
                
                // Set up WebViewClient
                webView.webViewClient = createWebViewClient(selector)
                
                // Load the URL with headers
                if (headers.isNotEmpty()) {
                    webView.loadUrl(url, headers.toMutableMap())
                } else {
                    webView.loadUrl(url)
                }
                
                manager.selector = selector
                manager.inProgress = true
                manager.webUrl = url
                
                // Wait for page to load and conditions to be met
                var currentTime = 0L
                val checkInterval = 100L
                
                while (currentTime < timeout && manager.inProgress) {
                    if (pageLoadComplete.value && (
                        (selector == null && ajaxCompleted.value) ||
                        contentExtracted.value || 
                        cloudflareBypassDetected.value
                    )) {
                        delay(500)
                        break
                    }
                    
                    delay(checkInterval)
                    currentTime += checkInterval
                }
                
                if (currentTime >= timeout) {
                    manager.inProgress = false
                    throw TimeoutException("Page load timed out after ${timeout}ms")
                }
                
                // Get HTML content
                html = webView.getHtml()
                manager.inProgress = false
            }
            
            // Get cookies and convert to common Cookie type
            val cookies = cookieJar?.let { jar ->
                try {
                    val okHttpUrl = okhttp3.HttpUrl.Builder()
                        .scheme(if (url.startsWith("https")) "https" else "http")
                        .host(url.substringAfter("://").substringBefore("/").substringBefore(":"))
                        .build()
                    jar.loadForRequest(okHttpUrl).map { okCookie ->
                        Cookie(
                            name = okCookie.name,
                            value = okCookie.value,
                            domain = okCookie.domain,
                            path = okCookie.path,
                            expiresAt = okCookie.expiresAt,
                            secure = okCookie.secure,
                            httpOnly = okCookie.httpOnly
                        )
                    }
                } catch (e: Exception) {
                    emptyList()
                }
            } ?: emptyList()
            
            return BrowserResult(
                responseBody = html,
                cookies = cookies,
                statusCode = 200
            )
        } catch (e: Exception) {
            return BrowserResult(
                responseBody = "",
                cookies = emptyList(),
                statusCode = 500,
                error = e.message
            )
        }
    }
    
    private fun createWebViewClient(selector: String?): WebViewClientCompat {
        return object : WebViewClientCompat() {
            override fun onPageFinished(view: WebView, loadedUrl: String) {
                super.onPageFinished(view, loadedUrl)
                pageLoadComplete.value = true
                
                val ajaxDetectionScript = """
                    (function() {
                        var originalOpen = XMLHttpRequest.prototype.open;
                        var originalSend = XMLHttpRequest.prototype.send;
                        var activeRequests = 0;
                        
                        XMLHttpRequest.prototype.open = function() {
                            this._url = arguments[1];
                            return originalOpen.apply(this, arguments);
                        };
                        
                        XMLHttpRequest.prototype.send = function() {
                            activeRequests++;
                            this.addEventListener('loadend', function() {
                                activeRequests--;
                                if (activeRequests === 0) {
                                    setTimeout(function() {
                                        AndroidInterface.onAjaxComplete();
                                    }, 500);
                                }
                            });
                            return originalSend.apply(this, arguments);
                        };
                        
                        if (!document.getElementById('challenge-form') && 
                            !document.querySelector('.cf-browser-verification') &&
                            document.cookie.indexOf('cf_clearance') >= 0) {
                            AndroidInterface.onCloudflareBypass();
                        }
                        
                        function checkForSelector() {
                            if ('${selector ?: ""}') {
                                if (document.querySelector('${selector}')) {
                                    AndroidInterface.contentReady();
                                } else {
                                    setTimeout(checkForSelector, 200);
                                }
                            } else {
                                AndroidInterface.contentReady();
                            }
                        }
                        
                        setTimeout(checkForSelector, 300);
                    })();
                """.trimIndent()
                
                view.evaluateJavascript(ajaxDetectionScript, null)
            }
            
            override fun shouldInterceptRequestCompat(view: WebView, url: String): WebResourceResponse? {
                return interceptResourceForOptimization(url)
            }
            
            override fun onReceivedErrorCompat(
                view: WebView,
                errorCode: Int,
                description: String?,
                failingUrl: String,
                isMainFrame: Boolean,
            ) {
                super.onReceivedErrorCompat(view, errorCode, description, failingUrl, isMainFrame)
                Log.error("WebView error: $errorCode $description")
            }
        }
    }
    
    private fun interceptResourceForOptimization(url: String): WebResourceResponse? {
        val blockedPatterns = listOf(
            "doubleclick.net", "googlesyndication.com", "googleadservices.com",
            "google-analytics.com", "googletagmanager.com", "facebook.com/tr",
            "facebook.net", "adservice", "advertising", "ads.", "/ads/",
            "banner", "popup", "analytics", "tracking", "tracker", "pixel"
        )
        
        val urlLower = url.lowercase()
        if (blockedPatterns.any { urlLower.contains(it) }) {
            return WebResourceResponse(
                "text/plain", "utf-8",
                java.io.ByteArrayInputStream("".toByteArray())
            )
        }
        return null
    }
    
    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView(webView: WebView, userAgent: String, uniqueId: String, manager: WebViewManger) {
        if (userAgent != manager.userAgent) {
            manager.userAgent = userAgent
            try {
                webView.settings.userAgentString = userAgent
            } catch (e: Throwable) {
                Log.error(exception = e, "failed to set user agent")
            }
        }
        
        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                safeBrowsingEnabled = false
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                offscreenPreRaster = true
            }
            
            userAgentString = userAgent + " ($uniqueId)"
            mediaPlaybackRequiresUserGesture = false
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            cacheMode = WebSettings.LOAD_DEFAULT
        }
        
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
    }
}

@SuppressLint("SetJavaScriptEnabled")
fun WebView.setDefaultSettings() {
    with(settings) {
        javaScriptEnabled = true
        domStorageEnabled = true
        databaseEnabled = true
        useWideViewPort = true
        loadWithOverviewMode = true
        cacheMode = WebSettings.LOAD_DEFAULT
        setSupportMultipleWindows(false)
        blockNetworkImage = false
        loadsImagesAutomatically = true
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        }
    }
    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
}

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalCoroutinesApi::class)
suspend fun WebView.getHtml(): String = suspendCancellableCoroutine { continuation ->
    settings.javaScriptEnabled = true
    if (!settings.javaScriptEnabled)
        throw IllegalStateException("Javascript is disabled")

    val script = """
        (function() {
            let html = document.documentElement.outerHTML;
            html = html.replace(/<script data-injected-by-app.*?<\/script>/g, '');
            return html;
        })();
    """.trimIndent()

    evaluateJavascript(script) { result ->
        if (result != null) {
            continuation.resume(
                result.replace("\\u003C", "<")
                    .replace("\\n", "")
                    .replace("\\t", "")
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\")
                    .replace("<hr />", "")
                    .let { if (it.startsWith("\"") && it.endsWith("\"")) it.substring(1, it.length - 1) else it }
            ) {}
        } else {
            continuation.resumeWithException(RuntimeException("Failed to get HTML content"))
        }
    }
}

@Suppress("OverridingDeprecatedMember")
abstract class WebViewClientCompat : WebViewClient() {

    open fun shouldOverrideUrlCompat(view: WebView, url: String): Boolean = false

    open fun shouldInterceptRequestCompat(view: WebView, url: String): WebResourceResponse? = null

    open fun onReceivedErrorCompat(
        view: WebView,
        errorCode: Int,
        description: String?,
        failingUrl: String,
        isMainFrame: Boolean,
    ) {}

    @TargetApi(Build.VERSION_CODES.N)
    final override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        return shouldOverrideUrlCompat(view, request.url.toString())
    }

    @Deprecated("Deprecated in Java")
    final override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        return shouldOverrideUrlCompat(view, url)
    }

    final override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        return shouldInterceptRequestCompat(view, request.url.toString())
    }

    @Deprecated("Deprecated in Java", ReplaceWith("shouldInterceptRequestCompat(view, url)"))
    final override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? {
        return shouldInterceptRequestCompat(view, url)
    }

    final override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            onReceivedErrorCompat(
                view, error.errorCode, error.description?.toString(),
                request.url.toString(), request.isForMainFrame
            )
        }
    }

    @Deprecated("Deprecated in Java")
    final override fun onReceivedError(view: WebView, errorCode: Int, description: String?, failingUrl: String) {
        onReceivedErrorCompat(view, errorCode, description, failingUrl, failingUrl == view.url)
    }

    final override fun onReceivedHttpError(view: WebView, request: WebResourceRequest, error: WebResourceResponse) {
        onReceivedErrorCompat(view, error.statusCode, error.reasonPhrase, request.url.toString(), request.isForMainFrame)
    }
}
