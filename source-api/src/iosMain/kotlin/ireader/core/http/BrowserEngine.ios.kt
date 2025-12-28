package ireader.core.http

import ireader.core.http.cloudflare.CloudflareBypassPluginManager
import ireader.core.http.cloudflare.CloudflareChallenge
import ireader.core.http.cloudflare.PluginBypassResult
import ireader.core.log.Log
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSError
import platform.Foundation.NSHTTPCookie
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSURL
import platform.Foundation.setValue
import platform.Foundation.timeIntervalSince1970
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * iOS implementation of BrowserEngine using WKWebView
 * 
 * WKWebView provides full browser capabilities including:
 * - JavaScript execution
 * - Cookie handling
 * - Cloudflare bypass (via real browser fingerprint)
 * 
 * Also supports plugin-based bypass (FlareSolverr) for additional flexibility.
 */
@OptIn(ExperimentalForeignApi::class)
actual class BrowserEngine : BrowserEngineInterface {
    
    // Plugin manager for Cloudflare bypass (injected via setter for DI compatibility)
    private var bypassManager: CloudflareBypassPluginManager? = null
    
    /**
     * Set the CloudflareBypassPluginManager for plugin-based bypass.
     * Called by DI after construction.
     */
    fun setBypassManager(manager: CloudflareBypassPluginManager) {
        bypassManager = manager
        Log.info { "[iOSBrowserEngine] Bypass manager configured" }
    }
    
    actual override suspend fun fetch(
        url: String,
        selector: String?,
        headers: Headers,
        timeout: Long,
        userAgent: String
    ): BrowserResult {
        // First try WKWebView (native browser)
        val webViewResult = fetchWithWebView(url, selector, headers, timeout, userAgent)
        
        // Check if we hit Cloudflare protection that WKWebView couldn't handle
        if (webViewResult.statusCode in listOf(403, 503) && 
            webViewResult.responseBody.contains("cloudflare", ignoreCase = true)) {
            
            // Try plugin-based bypass as fallback
            val manager = bypassManager
            if (manager != null) {
                val hasProvider = manager.hasAvailableProvider()
                if (hasProvider) {
                    Log.info { "[iOSBrowserEngine] WKWebView hit Cloudflare, trying plugin bypass" }
                    return fetchWithBypassManager(url, headers, timeout, userAgent)
                }
            }
        }
        
        return webViewResult
    }
    
    /**
     * Fetch using CloudflareBypassPluginManager for plugin-based bypass
     */
    private suspend fun fetchWithBypassManager(
        url: String,
        headers: Headers,
        timeout: Long,
        userAgent: String
    ): BrowserResult {
        val manager = bypassManager ?: return BrowserResult(
            responseBody = "",
            cookies = emptyList(),
            statusCode = 500,
            error = "Bypass manager not configured"
        )
        
        try {
            val challenge = CloudflareChallenge.JSChallenge()
            
            Log.info { "[iOSBrowserEngine] Attempting plugin-based bypass for: $url" }
            
            val result = manager.bypass(
                url = url,
                challenge = challenge,
                headers = headers,
                userAgent = userAgent,
                timeoutMs = timeout
            )
            
            return when (result) {
                is PluginBypassResult.Success -> {
                    Log.info { "[iOSBrowserEngine] Plugin bypass successful for: $url" }
                    
                    val cookies = result.cookies.map { cookie ->
                        Cookie(
                            name = cookie.name,
                            value = cookie.value,
                            domain = cookie.domain,
                            path = cookie.path,
                            expiresAt = cookie.expiresAt,
                            secure = cookie.secure,
                            httpOnly = cookie.httpOnly
                        )
                    }
                    
                    BrowserResult(
                        responseBody = result.content,
                        cookies = cookies,
                        statusCode = result.statusCode
                    )
                }
                is PluginBypassResult.Failed -> {
                    BrowserResult(
                        responseBody = "",
                        cookies = emptyList(),
                        statusCode = 500,
                        error = "Bypass failed: ${result.reason}"
                    )
                }
                is PluginBypassResult.UserInteractionRequired -> {
                    BrowserResult(
                        responseBody = "",
                        cookies = emptyList(),
                        statusCode = 403,
                        error = "User interaction required: ${result.message}"
                    )
                }
                is PluginBypassResult.ServiceUnavailable -> {
                    BrowserResult(
                        responseBody = "",
                        cookies = emptyList(),
                        statusCode = 503,
                        error = "Bypass service unavailable: ${result.reason}"
                    )
                }
            }
        } catch (e: Exception) {
            Log.error(e, "[iOSBrowserEngine] Plugin bypass error")
            return BrowserResult(
                responseBody = "",
                cookies = emptyList(),
                statusCode = 500,
                error = "Plugin bypass error: ${e.message}"
            )
        }
    }
    
    private suspend fun fetchWithWebView(
        url: String,
        selector: String?,
        headers: Headers,
        timeout: Long,
        userAgent: String
    ): BrowserResult = withContext(Dispatchers.Main) {
        suspendCoroutine { continuation ->
            val config = WKWebViewConfiguration().apply {
                // Enable JavaScript
                defaultWebpagePreferences?.allowsContentJavaScript = true
                
                // Set user agent
                applicationNameForUserAgent = userAgent
            }
            
            // Create WebView with zero frame
            val webView = WKWebView(frame = CGRectMake(0.0, 0.0, 1.0, 1.0), configuration = config)
            webView.customUserAgent = userAgent
            
            // Create navigation delegate
            val delegate = WebViewNavigationDelegate(
                selector = selector,
                timeout = timeout,
                onComplete = { html, cookies, error ->
                    // Clean up
                    webView.stopLoading()
                    webView.navigationDelegate = null
                    
                    if (error != null) {
                        continuation.resume(BrowserResult(
                            responseBody = "",
                            cookies = cookies,
                            statusCode = 0,
                            error = error
                        ))
                    } else {
                        continuation.resume(BrowserResult(
                            responseBody = html,
                            cookies = cookies,
                            statusCode = 200
                        ))
                    }
                }
            )
            
            webView.navigationDelegate = delegate
            
            // Create request with custom headers
            val nsUrl = NSURL.URLWithString(url)
            if (nsUrl == null) {
                continuation.resume(BrowserResult(
                    responseBody = "",
                    statusCode = 0,
                    error = "Invalid URL: $url"
                ))
                return@suspendCoroutine
            }
            
            val request = NSMutableURLRequest.requestWithURL(nsUrl).apply {
                headers.forEach { (key, value) ->
                    setValue(value, forHTTPHeaderField = key)
                }
                setValue(userAgent, forHTTPHeaderField = "User-Agent")
            }
            
            // Start loading
            webView.loadRequest(request)
            
            // Set up timeout
            MainScope().launch {
                delay(timeout)
                if (!delegate.isComplete) {
                    delegate.isComplete = true
                    webView.stopLoading()
                    
                    // Try to get whatever HTML we have
                    webView.evaluateJavaScript("document.documentElement.outerHTML") { result, _ ->
                        val html = result?.toString() ?: ""
                        continuation.resume(BrowserResult(
                            responseBody = html,
                            cookies = delegate.collectedCookies,
                            statusCode = 408,
                            error = "Request timed out after ${timeout}ms"
                        ))
                    }
                }
            }
        }
    }
    
    actual override fun isAvailable(): Boolean {
        // WKWebView is available on iOS 8.0+
        return true
    }
}

/**
 * WKNavigationDelegate implementation for handling page load events
 */
@OptIn(ExperimentalForeignApi::class)
private class WebViewNavigationDelegate(
    private val selector: String?,
    private val timeout: Long,
    private val onComplete: (html: String, cookies: List<Cookie>, error: String?) -> Unit
) : NSObject(), WKNavigationDelegateProtocol {
    
    var isComplete = false
    val collectedCookies = mutableListOf<Cookie>()
    private var webView: WKWebView? = null
    
    override fun webView(webView: WKWebView, didFinishNavigation: WKNavigation?) {
        if (isComplete) return
        this.webView = webView
        
        // Collect cookies
        collectCookies(webView)
        
        if (selector != null) {
            // Wait for selector to appear
            waitForSelector(webView, selector)
        } else {
            // No selector, get HTML immediately
            getHtmlAndComplete(webView)
        }
    }


    
    private fun collectCookies(webView: WKWebView) {
        val cookieStore = webView.configuration.websiteDataStore.httpCookieStore
        cookieStore.getAllCookies { cookies ->
            @Suppress("UNCHECKED_CAST")
            val cookieList = cookies as? List<NSHTTPCookie> ?: return@getAllCookies
            
            collectedCookies.clear()
            for (nsCookie in cookieList) {
                collectedCookies.add(Cookie(
                    name = nsCookie.name,
                    value = nsCookie.value,
                    domain = nsCookie.domain,
                    path = nsCookie.path,
                    expiresAt = nsCookie.expiresDate?.timeIntervalSince1970?.toLong()?.times(1000) ?: 0L,
                    secure = nsCookie.isSecure(),
                    httpOnly = nsCookie.isHTTPOnly()
                ))
            }
        }
    }
    
    private fun waitForSelector(webView: WKWebView, selector: String) {
        val checkScript = """
            (function() {
                var element = document.querySelector('$selector');
                return element !== null;
            })()
        """.trimIndent()
        
        var attempts = 0
        val maxAttempts = (timeout / 100).toInt()
        
        fun check() {
            if (isComplete) return
            
            webView.evaluateJavaScript(checkScript) { result, _ ->
                if (isComplete) return@evaluateJavaScript
                
                val found = (result as? Boolean) ?: false
                
                if (found || attempts >= maxAttempts) {
                    getHtmlAndComplete(webView)
                } else {
                    attempts++
                    MainScope().launch {
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
        
        webView.evaluateJavaScript("document.documentElement.outerHTML") { result, error ->
            if (isComplete) return@evaluateJavaScript
            isComplete = true
            
            val html = result?.toString() ?: ""
            val errorMsg = error?.let { (it as? NSError)?.localizedDescription }
            
            onComplete(html, collectedCookies, errorMsg)
        }
    }
}
