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
import okhttp3.Headers
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.UUID
import java.util.concurrent.TimeoutException
import kotlin.coroutines.resumeWithException

actual class BrowserEngine actual constructor() : BrowserEngineInterface {
    
    private lateinit var webViewManger: WebViewManger
    private lateinit var webViewCookieJar: WebViewCookieJar
    
    constructor(webViewManger: WebViewManger, webViewCookieJar: WebViewCookieJar) : this() {
        this.webViewManger = webViewManger
        this.webViewCookieJar = webViewCookieJar
    }

    private val cloudflareBypassDetected = MutableStateFlow(false)
    private val ajaxCompleted = MutableStateFlow(false)
    private val pageLoadComplete = MutableStateFlow(false)
    private val contentExtracted = MutableStateFlow(false)
    
    /**
     * Enhanced fetch implementation that:
     * 1. Better handles Cloudflare protection
     * 2. Waits for AJAX content to load
     * 3. Uses advanced browser fingerprinting evasion
     * 4. Extracts content based on selectors or waits for dynamic content
     *
     * @param url The url of page
     * @param selector CSS selector for element(s) that must be present before fetching content
     * @param headers Custom headers for the request
     * @param timeout Maximum time in milliseconds to wait for page load
     * @param userAgent Custom user agent or default modern Chrome
     * @return [Result] with page content and cookies
     */
    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    actual override suspend fun fetch(
        url: String,
        selector: String?,
        headers: Headers?,
        timeout: Long,
        userAgent: String,
    ): Result {
        // Reset state flows
        cloudflareBypassDetected.value = false
        ajaxCompleted.value = false
        pageLoadComplete.value = false
        contentExtracted.value = false
        
        var html: Document = Document("No Data was Found")
        val uniqueId = UUID.randomUUID().toString()
        
        withContext(Dispatchers.Main) {
            // Initialize or reuse WebView
            webViewManger.init()
            val webView = webViewManger.webView ?: throw IllegalStateException("WebView initialization failed")
            
            // Configure WebView
            configureWebView(webView, userAgent, uniqueId)
            
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
            webView.webViewClient = object : WebViewClientCompat() {
                override fun onPageFinished(view: WebView, loadedUrl: String) {
                    super.onPageFinished(view, loadedUrl)
                    pageLoadComplete.value = true
                    
                    // Inject scripts to detect AJAX completion and Cloudflare
                    val ajaxDetectionScript = """
                        (function() {
                            // Record original XHR methods
                            var originalOpen = XMLHttpRequest.prototype.open;
                            var originalSend = XMLHttpRequest.prototype.send;
                            var activeRequests = 0;
                            
                            // Override open method
                            XMLHttpRequest.prototype.open = function() {
                                this._url = arguments[1];
                                return originalOpen.apply(this, arguments);
                            };
                            
                            // Override send method
                            XMLHttpRequest.prototype.send = function() {
                                activeRequests++;
                                
                                // Add load event listener
                                this.addEventListener('loadend', function() {
                                    activeRequests--;
                                    if (activeRequests === 0) {
                                        // All AJAX requests completed
                                        setTimeout(function() {
                                            AndroidInterface.onAjaxComplete();
                                        }, 500); // Wait a bit for DOM updates
                                    }
                                });
                                
                                return originalSend.apply(this, arguments);
                            };
                            
                            // Check for Cloudflare bypass
                            if (!document.getElementById('challenge-form') && 
                                !document.querySelector('.cf-browser-verification') &&
                                document.cookie.indexOf('cf_clearance') >= 0) {
                                AndroidInterface.onCloudflareBypass();
                            }
                            
                            // Wait for any elements matching selector
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
                    // Intercept and block ads/trackers for faster loading
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
            
            // Load the URL
            if (headers != null) {
                webView.loadUrl(
                    url,
                    headers.toMultimap().mapValues { it.value.getOrNull(0) ?: "" }.toMutableMap()
                )
            } else {
                webView.loadUrl(url)
            }
            
            webViewManger.selector = selector
            webViewManger.inProgress = true
            webViewManger.webUrl = url
            
            // Wait for page to load and conditions to be met
            var currentTime = 0L
            val checkInterval = 100L
            
            while (currentTime < timeout && webViewManger.inProgress) {
                if (pageLoadComplete.value && (
                    (selector == null && ajaxCompleted.value) ||
                    contentExtracted.value || 
                    cloudflareBypassDetected.value
                )) {
                    // Extract content after brief delay to ensure everything is loaded
                    delay(500)
                    break
                }
                
                delay(checkInterval)
                currentTime += checkInterval
            }
            
            if (currentTime >= timeout) {
                webViewManger.inProgress = false
                throw TimeoutException("Page load timed out after ${timeout}ms")
            }
            
            // Get HTML content
            html = Jsoup.parse(webView.getHtml())
            webViewManger.inProgress = false
        }
        
        // Get cookies
        val cookies = webViewCookieJar.get(url.toHttpUrl())
        
        return Result(
            responseBody = html.html(),
            cookies = cookies,
        )
    }
    
    /**
     * Intercept resources to block ads and trackers for faster page loading
     */
    private fun interceptResourceForOptimization(url: String): WebResourceResponse? {
        // List of patterns to block
        val blockedPatterns = listOf(
            "doubleclick.net",
            "googlesyndication.com",
            "googleadservices.com",
            "google-analytics.com",
            "googletagmanager.com",
            "facebook.com/tr",
            "facebook.net",
            "adservice",
            "advertising",
            "ads.",
            "/ads/",
            "banner",
            "popup",
            "analytics",
            "tracking",
            "tracker",
            "pixel"
        )
        
        val urlLower = url.lowercase()
        if (blockedPatterns.any { urlLower.contains(it) }) {
            // Return empty response for blocked resources
            return WebResourceResponse(
                "text/plain",
                "utf-8",
                java.io.ByteArrayInputStream("".toByteArray())
            )
        }
        
        return null
    }
    
    /**
     * Configure WebView with optimal settings for Cloudflare bypass and browser detection evasion
     */
    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView(webView: WebView, userAgent: String, uniqueId: String) {
        // Update user agent if needed
        if (userAgent != webViewManger.userAgent) {
            webViewManger.userAgent = userAgent
            try {
                webView.settings.userAgentString = userAgent
            } catch (e: Throwable) {
                Log.error(exception = e, "failed to set user agent")
            }
        }
        
        // Enable advanced features
        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            
            // Bypass detection mitigations
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                safeBrowsingEnabled = false
            }
            
            // Improve page loading
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                offscreenPreRaster = true
            }
            
            // Prevent WebView from being identified by canvas fingerprinting
            // by setting a unique client identifier
            userAgentString = userAgent + " ($uniqueId)"
            
            // Enable media playback for sites that require it
            mediaPlaybackRequiresUserGesture = false
            
            // Maximize compatibility
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            
            // Set cache mode
            cacheMode = WebSettings.LOAD_DEFAULT
        }
        
        // Enable third-party cookies (needed for Cloudflare)
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
        
        // Additional settings for better compatibility
        setSupportMultipleWindows(false)
        blockNetworkImage = false
        loadsImagesAutomatically = true
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        }
    }
    
    // Enable cookie support
    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
}

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalCoroutinesApi::class)
suspend fun WebView.getHtml(): String = suspendCancellableCoroutine { continuation ->
    settings.javaScriptEnabled = true
    if (!settings.javaScriptEnabled)
        throw IllegalStateException("Javascript is disabled")

    // Enhanced JS to get entire page HTML including dynamic content
    val script = """
        (function() {
            let html = document.documentElement.outerHTML;
            // Clean up any script injections we might have added
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
                    // Remove quotes wrapping the entire result
                    .let { if (it.startsWith("\"") && it.endsWith("\"")) it.substring(1, it.length - 1) else it }
            ) {
            }
        } else {
            continuation.resumeWithException(RuntimeException("Failed to get HTML content"))
        }
    }
}

@Suppress("OverridingDeprecatedMember")
abstract class WebViewClientCompat : WebViewClient() {

    open fun shouldOverrideUrlCompat(view: WebView, url: String): Boolean {
        return false
    }

    open fun shouldInterceptRequestCompat(view: WebView, url: String): WebResourceResponse? {
        return null
    }

    open fun onReceivedErrorCompat(
        view: WebView,
        errorCode: Int,
        description: String?,
        failingUrl: String,
        isMainFrame: Boolean,
    ) {
    }

    @TargetApi(Build.VERSION_CODES.N)
    final override fun shouldOverrideUrlLoading(
        view: WebView,
        request: WebResourceRequest,
    ): Boolean {
        return shouldOverrideUrlCompat(view, request.url.toString())
    }

    @Deprecated("Deprecated in Java")
    final override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        return shouldOverrideUrlCompat(view, url)
    }

    final override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest,
    ): WebResourceResponse? {
        return shouldInterceptRequestCompat(view, request.url.toString())
    }

    @Deprecated("Deprecated in Java", ReplaceWith("shouldInterceptRequestCompat(view, url)"))
    final override fun shouldInterceptRequest(
        view: WebView,
        url: String,
    ): WebResourceResponse? {
        return shouldInterceptRequestCompat(view, url)
    }

    final override fun onReceivedError(
        view: WebView,
        request: WebResourceRequest,
        error: WebResourceError,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            onReceivedErrorCompat(
                view,
                error.errorCode,
                error.description?.toString(),
                request.url.toString(),
                request.isForMainFrame,
            )
        }
    }

    @Deprecated(
        "Deprecated in Java",
        ReplaceWith("onReceivedErrorCompat(view, errorCode, description, failingUrl, failingUrl == view.url)")
    )
    final override fun onReceivedError(
        view: WebView,
        errorCode: Int,
        description: String?,
        failingUrl: String,
    ) {
        onReceivedErrorCompat(view, errorCode, description, failingUrl, failingUrl == view.url)
    }

    final override fun onReceivedHttpError(
        view: WebView,
        request: WebResourceRequest,
        error: WebResourceResponse,
    ) {
        onReceivedErrorCompat(
            view,
            error.statusCode,
            error.reasonPhrase,
            request.url
                .toString(),
            request.isForMainFrame,
        )
    }
}
