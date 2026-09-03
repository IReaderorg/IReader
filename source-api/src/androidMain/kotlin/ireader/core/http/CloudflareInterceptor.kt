package ireader.core.http

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import ireader.core.log.Log
import okhttp3.Cookie
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * OkHttp interceptor that handles Cloudflare anti-bot and Turnstile challenges.
 * Uses Android WebView to solve challenges and extract clearance cookies.
 */
class CloudflareInterceptor(
    private val context: Context,
    private val webViewCookieJar: WebViewCookieJar,
    private val webViewManager: WebViewManger? = null,
    private val defaultUserAgentProvider: () -> String = { DEFAULT_USER_AGENT }
) : Interceptor {

    private val executor = ContextCompat.getMainExecutor(context)

    /**
     * Initializes WebView on startup to avoid blocking subsequent network requests.
     */
    private val initWebView by lazy {
        if (DeviceUtil.isMiui || (Build.VERSION.SDK_INT == Build.VERSION_CODES.S && DeviceUtil.isSamsung)) {
            return@lazy
        }
        try {
            WebSettings.getDefaultUserAgent(context)
        } catch (_: Exception) {
            // Avoid crashes when Chrome/WebView is actively updating
        }
    }

    @Synchronized
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        if (!WebViewUtilLegeacy.supportsWebView(context)) {
            throw NeedWebView()
        }

        initWebView

        val response = chain.proceed(originalRequest)

        // Check if Cloudflare anti-bot challenge is triggered
        if (!isCloudflareChallenge(response)) {
            return response
        }

        try {
            response.close()

            // 1. Capture existing cf_clearance before wiping
            val oldCookie = webViewCookieJar.get(originalRequest.url)
                .firstOrNull { it.name == CF_CLEARANCE_COOKIE }

            // 2. Remove stale clearance cookie
            webViewCookieJar.remove(originalRequest.url, COOKIE_NAMES, 0)
            try {
                CookieManager.getInstance().flush()
            } catch (_: Exception) {}

            // 3. Resolve challenge via WebView
            val bypassed = resolveWithWebView(originalRequest, oldCookie)
            if (!bypassed) {
                throw CloudflareBypassException("Failed to bypass Cloudflare challenge")
            }

            try {
                CookieManager.getInstance().flush()
            } catch (_: Exception) {}

            // 4. Build retry request with new clearance cookies and matching User-Agent
            val freshCookies = webViewCookieJar.get(originalRequest.url)
            val requestBuilder = originalRequest.newBuilder()

            val userAgent = originalRequest.header("User-Agent") ?: getDefaultUserAgent()
            requestBuilder.header("User-Agent", userAgent)

            if (freshCookies.isNotEmpty()) {
                requestBuilder.header("Cookie", freshCookies.joinToString("; ") { "${it.name}=${it.value}" })
            }

            return chain.proceed(requestBuilder.build())
        } catch (e: CloudflareBypassException) {
            // Wrap in IOException because OkHttp's RealCall$AsyncCall only catches IOExceptions.
            // Throwing any non-IOException on a background thread terminates ThreadPoolExecutor and crashes the app!
            val cfFailed = CloudflareBypassFailed(e.message, e)
            throw IOException(e.message ?: "Cloudflare bypass failed", cfFailed)
        } catch (e: CloudflareBypassFailed) {
            throw IOException(e.message ?: "Cloudflare bypass failed", e)
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw IOException(e)
        }
    }

    private fun isCloudflareChallenge(response: Response): Boolean {
        // Official Cloudflare challenge header
        if (response.header("cf-mitigated")?.equals("challenge", ignoreCase = true) == true) {
            return true
        }

        val code = response.code
        if (code in ERROR_CODES) {
            val server = response.header("Server") ?: ""
            if (SERVER_CHECK.any { server.contains(it, ignoreCase = true) }) {
                return true
            }
            if (response.header("cf-ray") != null || response.header("cf-cache-status") != null) {
                return true
            }
        }
        return false
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun resolveWithWebView(request: Request, oldCookie: Cookie?): Boolean {
        return if (webViewManager != null && webViewManager.isInit) {
            resolveWithExistingWebView(request, oldCookie)
        } else {
            resolveWithNewWebView(request, oldCookie)
        }
    }

    /**
     * Use existing WebView manager for bypass when available
     */
    @SuppressLint("SetJavaScriptEnabled")
    private fun resolveWithExistingWebView(request: Request, oldCookie: Cookie?): Boolean {
        val latch = CountDownLatch(1)
        val cloudflareBypassed = AtomicBoolean(false)
        val origRequestUrl = request.url.toString()
        val safeHeaders = parseHeaders(request.headers)

        webViewManager?.apply {
            isBackgroundMode = true
            inProgress = true

            executor.execute {
                webView?.loadUrl(origRequestUrl, safeHeaders.toMutableMap())
            }

            val checkInterval = 400L
            val maxWaitTime = CLOUDFLARE_TIMEOUT_SECONDS * 1000
            val startTime = System.currentTimeMillis()

            Thread {
                while (System.currentTimeMillis() - startTime < maxWaitTime &&
                    !cloudflareBypassed.get() &&
                    latch.count > 0
                ) {
                    try {
                        Thread.sleep(checkInterval)
                    } catch (_: InterruptedException) {
                        break
                    }
                    try {
                        CookieManager.getInstance().flush()
                    } catch (_: Exception) {}

                    if (isCloudFlareBypassed(origRequestUrl, oldCookie)) {
                        cloudflareBypassed.set(true)
                        isBackgroundMode = false
                        inProgress = false
                        latch.countDown()
                        break
                    }
                }

                if (!cloudflareBypassed.get()) {
                    isBackgroundMode = false
                    inProgress = false
                    latch.countDown()
                }
            }.start()
        }

        latch.await(CLOUDFLARE_TIMEOUT_SECONDS + 5, TimeUnit.SECONDS)
        return cloudflareBypassed.get()
    }

    /**
     * Create isolated WebView to solve Cloudflare challenge and extract clearance cookies
     */
    @SuppressLint("SetJavaScriptEnabled")
    private fun resolveWithNewWebView(request: Request, oldCookie: Cookie?): Boolean {
        val latch = CountDownLatch(1)

        var webView: WebView? = null
        val cloudflareBypassed = AtomicBoolean(false)
        val challengeFound = AtomicBoolean(false)
        val interactiveDetected = AtomicBoolean(false)
        val isWebViewOutdated = AtomicBoolean(false)

        val origRequestUrl = request.url.toString()
        val userAgent = request.header("User-Agent") ?: getDefaultUserAgent()
        val safeHeaders = parseHeaders(request.headers)

        executor.execute {
            try {
                val webview = WebView(context.applicationContext)
                webView = webview

                // Configure WebView settings for modern Cloudflare & Turnstile
                webview.settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    javaScriptCanOpenWindowsAutomatically = true
                    userAgentString = userAgent
                }

                // Layout with real dimensions to prevent Chromium throttling hidden/0x0 views
                webview.layout(0, 0, 1080, 1920)
                webview.resumeTimers()
                webview.onResume()

                // Bridge to capture interactive challenge signal from Turnstile
                webview.addJavascriptInterface(
                    object {
                        @Suppress("unused")
                        @JavascriptInterface
                        fun interactiveDetected() {
                            Log.warn { "Cloudflare interactive challenge detected (user interaction required)" }
                            interactiveDetected.set(true)
                            latch.countDown()
                        }
                    },
                    "ireaderBridge"
                )

                webview.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        super.onPageFinished(view, url)
                        try {
                            CookieManager.getInstance().flush()
                        } catch (_: Exception) {}

                        if (isCloudFlareBypassed(origRequestUrl, oldCookie)) {
                            cloudflareBypassed.set(true)
                            latch.countDown()
                            return
                        }

                        if (url == origRequestUrl) {
                            if (!challengeFound.get()) {
                                // First request completed with 200 without reporting challenge error
                                if (isCloudFlareBypassed(origRequestUrl, oldCookie)) {
                                    cloudflareBypassed.set(true)
                                }
                                latch.countDown()
                            } else {
                                // Listen for interactive Turnstile challenge
                                view.evaluateJavascript(
                                    """
                                    (function() {
                                        window.addEventListener("message", function(e) {
                                            if (e && e.data && e.data.source === "cloudflare-challenge" && e.data.event === "interactiveBegin") {
                                                if (window.ireaderBridge) {
                                                    window.ireaderBridge.interactiveDetected();
                                                }
                                            }
                                        });
                                    })();
                                    """.trimIndent(),
                                    null
                                )
                            }
                        }
                    }

                    override fun onReceivedHttpError(
                        view: WebView,
                        request: WebResourceRequest,
                        errorResponse: WebResourceResponse
                    ) {
                        super.onReceivedHttpError(view, request, errorResponse)
                        if (request.isForMainFrame) {
                            val code = errorResponse.statusCode
                            val cfMitigated = errorResponse.responseHeaders?.get("cf-mitigated")
                            val server = errorResponse.responseHeaders?.get("Server") ?: ""

                            if (cfMitigated?.equals("challenge", ignoreCase = true) == true ||
                                code in ERROR_CODES ||
                                SERVER_CHECK.any { server.contains(it, ignoreCase = true) }
                            ) {
                                Log.info { "Cloudflare challenge detected on main frame in WebView (HTTP $code)" }
                                challengeFound.set(true)
                            } else {
                                latch.countDown()
                            }
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onReceivedError(
                        view: WebView,
                        errorCode: Int,
                        description: String?,
                        failingUrl: String
                    ) {
                        Log.warn { "WebView connection error $errorCode: $description on $failingUrl" }
                        latch.countDown()
                    }
                }

                webview.loadUrl(origRequestUrl, safeHeaders)
            } catch (e: Exception) {
                Log.error { "Error creating WebView for Cloudflare bypass: ${e.message}" }
                latch.countDown()
            }
        }

        // Active background poller checking CookieManager while challenge resolves
        val checkInterval = 400L
        val maxWaitTime = CLOUDFLARE_TIMEOUT_SECONDS * 1000
        val startTime = System.currentTimeMillis()

        Thread {
            while (System.currentTimeMillis() - startTime < maxWaitTime &&
                !cloudflareBypassed.get() &&
                !interactiveDetected.get() &&
                latch.count > 0
            ) {
                try {
                    Thread.sleep(checkInterval)
                } catch (_: InterruptedException) {
                    break
                }
                try {
                    CookieManager.getInstance().flush()
                } catch (_: Exception) {}

                if (isCloudFlareBypassed(origRequestUrl, oldCookie)) {
                    Log.info { "Cloudflare clearance cookie detected via background poller!" }
                    cloudflareBypassed.set(true)
                    latch.countDown()
                    break
                }
            }
        }.start()

        val completedInTime = latch.await(CLOUDFLARE_TIMEOUT_SECONDS, TimeUnit.SECONDS)

        executor.execute {
            try {
                if (!cloudflareBypassed.get()) {
                    isWebViewOutdated.set(webView?.isOutdated() == true)
                }
                webView?.apply {
                    stopLoading()
                    destroy()
                }
            } catch (e: Exception) {
                Log.error { "Error cleaning up WebView: ${e.message}" }
            } finally {
                webView = null
            }
        }

        if (!cloudflareBypassed.get()) {
            if (isWebViewOutdated.get()) {
                executor.execute {
                    context.toast("Android System WebView is outdated. Please update it.", Toast.LENGTH_LONG)
                }
                throw OutOfDateWebView("Android System WebView is outdated.")
            }

            if (interactiveDetected.get()) {
                executor.execute {
                    context.toast("Cloudflare verification required. Please open this source in WebView to solve.", Toast.LENGTH_LONG)
                }
                throw CloudflareBypassException("Cloudflare challenge requires manual verification. Please open the source in WebView.")
            }

            if (!completedInTime) {
                Log.error { "Cloudflare bypass timed out after $CLOUDFLARE_TIMEOUT_SECONDS seconds" }
                executor.execute {
                    context.toast("Cloudflare bypass timed out. Please open this source in WebView to solve.", Toast.LENGTH_LONG)
                }
                throw CloudflareBypassException("Cloudflare bypass timed out after $CLOUDFLARE_TIMEOUT_SECONDS seconds. Please open the source in WebView.")
            }

            throw CloudflareBypassException("Failed to bypass Cloudflare protection. Please open the source in WebView to solve.")
        }

        return true
    }

    private fun isCloudFlareBypassed(url: String, oldCookie: Cookie?): Boolean {
        return try {
            val cookieStr = CookieManager.getInstance().getCookie(url) ?: return false
            if (!cookieStr.contains(CF_CLEARANCE_COOKIE)) return false

            val cookies = cookieStr.split(";").map { it.trim() }
            val cfCookie = cookies.firstOrNull { it.startsWith("$CF_CLEARANCE_COOKIE=") } ?: return false
            val value = cfCookie.substringAfter("=")

            if (value.isBlank()) return false
            if (oldCookie != null && oldCookie.value == value) return false

            true
        } catch (e: Exception) {
            Log.error { "Error checking Cloudflare bypass status: ${e.message}" }
            false
        }
    }

    private fun parseHeaders(headers: Headers): Map<String, String> {
        return headers
            .filter { (name, value) -> isRequestHeaderSafe(name, value) }
            .groupBy(keySelector = { (name, _) -> name }) { (_, value) -> value }
            .mapValues { it.value.firstOrNull().orEmpty() }
    }

    private fun isRequestHeaderSafe(_name: String, _value: String): Boolean {
        val name = _name.lowercase(Locale.ENGLISH)
        val value = _value.lowercase(Locale.ENGLISH)
        if (name in UNSAFE_HEADER_NAMES || name.startsWith("proxy-")) return false
        if (name == "connection" && value == "upgrade") return false
        return true
    }

    private fun getDefaultUserAgent(): String {
        return try {
            defaultUserAgentProvider()
        } catch (e: Exception) {
            DEFAULT_USER_AGENT
        }
    }

    companion object {
        private val ERROR_CODES = listOf(403, 503)
        private val SERVER_CHECK = arrayOf("cloudflare-nginx", "cloudflare")
        private val COOKIE_NAMES = listOf("cf_clearance")
        private const val CF_CLEARANCE_COOKIE = "cf_clearance"
        private const val CLOUDFLARE_TIMEOUT_SECONDS = 30L
        private const val DEFAULT_USER_AGENT = "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.6478.71 Mobile Safari/537.36"
        private val UNSAFE_HEADER_NAMES = listOf(
            "content-length", "host", "trailer", "te", "upgrade", "cookie2", "keep-alive", "transfer-encoding", "set-cookie", "cookie"
        )
    }
}

// Internal exception class for Cloudflare bypass
class CloudflareBypassException(message: String = "Failed to bypass Cloudflare protection") : IOException(message)

object DeviceUtil {

    val isMiui by lazy {
        getSystemProperty("ro.miui.ui.version.name")?.isNotEmpty() ?: false
    }

    @SuppressLint("PrivateApi")
    fun isMiuiOptimizationDisabled(): Boolean {
        val sysProp = getSystemProperty("persist.sys.miui_optimization")
        if (sysProp == "0" || sysProp == "false") {
            return true
        }

        return try {
            Class.forName("android.miui.AppOpsUtils")
                .getDeclaredMethod("isXOptMode")
                .invoke(null) as Boolean
        } catch (e: Exception) {
            false
        }
    }

    val isSamsung by lazy {
        Build.MANUFACTURER.equals("samsung", ignoreCase = true)
    }

    @SuppressLint("PrivateApi")
    private fun getSystemProperty(key: String?): String? {
        return try {
            Class.forName("android.os.SystemProperties")
                .getDeclaredMethod("get", String::class.java)
                .invoke(null, key) as String
        } catch (e: Exception) {
            Log.error { "Unable to use SystemProperties.get()" }
            null
        }
    }
}

object WebViewUtilLegeacy {
    fun supportsWebView(context: Context): Boolean {
        return try {
            CookieManager.getInstance()
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_WEBVIEW)
        } catch (e: Throwable) {
            Log.error { "WebView not supported: ${e.message}" }
            false
        }
    }
}

fun WebView.isOutdated(): Boolean {
    return getWebViewMajorVersion() < WebViewUtil.MINIMUM_WEBVIEW_VERSION
}

private fun WebView.getDefaultUserAgentString(): String {
    return try {
        val originalUA: String = settings.userAgentString
        settings.userAgentString = null
        val defaultUserAgentString = settings.userAgentString
        settings.userAgentString = originalUA
        defaultUserAgentString
    } catch (e: Exception) {
        Log.error { "Error getting default user agent: ${e.message}" }
        ""
    }
}

private fun WebView.getWebViewMajorVersion(): Int {
    return try {
        val uaRegexMatch = """.*Chrome/(\d+)\..*""".toRegex()
            .matchEntire(getDefaultUserAgentString())

        uaRegexMatch?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 0
    } catch (e: Exception) {
        Log.error { "Error getting WebView version: ${e.message}" }
        0
    }
}

// Extension functions for Toast
fun Context.toast(
    @StringRes resource: Int,
    duration: Int = Toast.LENGTH_SHORT,
    block: (Toast) -> Unit = {}
): Toast {
    return toast(getString(resource), duration, block)
}

fun Context.toast(
    text: String?,
    duration: Int = Toast.LENGTH_SHORT,
    block: (Toast) -> Unit = {}
): Toast {
    return Toast.makeText(this, text.orEmpty(), duration).also {
        block(it)
        it.show()
    }
}
