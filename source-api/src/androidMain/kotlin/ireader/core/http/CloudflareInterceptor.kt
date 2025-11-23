package ireader.core.http

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import ireader.core.log.Log
import kotlinx.coroutines.delay
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * OkHttp interceptor that handles Cloudflare anti-bot challenges.
 * Uses WebView to solve challenges and extract cookies.
 */
class CloudflareInterceptor(
    private val context: Context,
    private val webViewCookieJar: WebViewCookieJar,
    private val webViewManager: WebViewManger? = null // Optional: use existing WebView manager
) : Interceptor {

    private val executor = ContextCompat.getMainExecutor(context)

    /**
     * When this is called, it initializes the WebView if it wasn't already. We use this to avoid
     * blocking the main thread too much. If used too often we could consider moving it to the
     * Application class.
     */
    private val initWebView by lazy {
        // Crashes on some devices. We skip this in some cases since the only impact is slower
        // WebView init in those rare cases.
        // See https://bugs.chromium.org/p/chromium/issues/detail?id=1279562
        if (DeviceUtil.isMiui || Build.VERSION.SDK_INT == Build.VERSION_CODES.S && DeviceUtil.isSamsung) {
            return@lazy
        }

        WebSettings.getDefaultUserAgent(context)
    }

    @Synchronized
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        if (!WebViewUtilLegeacy.supportsWebView(context)) {
            // Throw exception directly instead of launching coroutine
            // The caller should handle this exception appropriately
            throw NeedWebView()
        }

        initWebView

        val response = chain.proceed(originalRequest)

        // Check if Cloudflare anti-bot is on
        if (response.code !in ERROR_CODES || response.header("Server") !in SERVER_CHECK) {
            return response
        }

        try {
            response.close()
            webViewCookieJar.remove(originalRequest.url, COOKIE_NAMES, 0)
            val oldCookie = webViewCookieJar.get(originalRequest.url)
                .firstOrNull { it.name == "cf_clearance" }
            resolveWithWebView(originalRequest, oldCookie)

            return chain.proceed(originalRequest)
        }
        // Because OkHttp's enqueue only handles IOExceptions, wrap the exception so that
        // we don't crash the entire app
        catch (e: CloudflareBypassException) {
            throw CloudflareBypassFailed()
        } catch (e: Exception) {
            throw IOException(e)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun resolveWithWebView(request: Request, oldCookie: Cookie?) {
        // Try to use existing WebView manager if available (for seamless integration)
        if (webViewManager != null && webViewManager.isInit) {
            resolveWithExistingWebView(request, oldCookie)
        } else {
            resolveWithNewWebView(request, oldCookie)
        }
    }
    
    /**
     * Use existing WebView manager for seamless bypass (no visible WebView)
     */
    @SuppressLint("SetJavaScriptEnabled")
    private fun resolveWithExistingWebView(request: Request, oldCookie: Cookie?) {
        val latch = CountDownLatch(1)
        val cloudflareBypassed = AtomicBoolean(false)
        val origRequestUrl = request.url.toString()
        
        webViewManager?.apply {
            isBackgroundMode = true
            inProgress = true
            
            // Set up callback for when cookies are ready
            val checkInterval = 500L
            var elapsedTime = 0L
            
            executor.execute {
                webView?.loadUrl(origRequestUrl, request.headers.toMultimap()
                    .mapValues { it.value.firstOrNull() ?: "" }
                    .toMutableMap())
            }
            
            // Poll for cookie changes using a thread instead of coroutine
            Thread {
                while (elapsedTime < CLOUDFLARE_TIMEOUT_SECONDS * 1000 && !cloudflareBypassed.get()) {
                    if (isCloudFlareBypassed(origRequestUrl, oldCookie)) {
                        cloudflareBypassed.set(true)
                        isBackgroundMode = false
                        inProgress = false
                        latch.countDown()
                        break
                    }
                    Thread.sleep(checkInterval)
                    elapsedTime += checkInterval
                }
                
                if (!cloudflareBypassed.get()) {
                    isBackgroundMode = false
                    inProgress = false
                    latch.countDown()
                }
            }.start()
        }
        
        val success = latch.await(CLOUDFLARE_TIMEOUT_SECONDS + 5, TimeUnit.SECONDS)
        
        if (!cloudflareBypassed.get()) {
            if (!success) {
                Log.error { "Cloudflare bypass timed out using existing WebView" }
            }
            throw CloudflareBypassException()
        }
    }
    
    /**
     * Create new WebView for bypass (fallback method)
     */
    @SuppressLint("SetJavaScriptEnabled")
    private fun resolveWithNewWebView(request: Request, oldCookie: Cookie?) {
        // We need to lock this thread until the WebView finds the challenge solution url, because
        // OkHttp doesn't support asynchronous interceptors.
        val latch = CountDownLatch(1)

        var webView: WebView? = null
        val cloudflareBypassed = AtomicBoolean(false)
        val challengeFound = AtomicBoolean(false)
        val isWebViewOutdated = AtomicBoolean(false)

        val origRequestUrl = request.url.toString()
        val headers = request.headers.toMultimap()
            .mapValues { it.value.firstOrNull() ?: "" }
            .toMutableMap()

        executor.execute {
            try {
                val webview = WebView(context.applicationContext)
                webView = webview
                
                // Configure WebView settings
                webview.settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    userAgentString = request.header("User-Agent") ?: getDefaultUserAgent()
                }

                webview.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        if (isCloudFlareBypassed(origRequestUrl, oldCookie)) {
                            cloudflareBypassed.set(true)
                            latch.countDown()
                        } else if (url == origRequestUrl && !challengeFound.get()) {
                            // The first request didn't return the challenge, abort.
                            latch.countDown()
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onReceivedError(
                        view: WebView,
                        errorCode: Int,
                        description: String?,
                        failingUrl: String
                    ) {
                        if (errorCode in ERROR_CODES) {
                            challengeFound.set(true)
                        } else {
                            latch.countDown()
                        }
                    }
                }

                webView?.loadUrl(origRequestUrl, headers)
            } catch (e: Exception) {
                Log.error { "Error creating WebView for Cloudflare bypass: ${e.message}" }
                latch.countDown()
            }
        }

        // Wait a reasonable amount of time to retrieve the solution. The minimum should be
        // around 4 seconds but it can take more due to slow networks or server issues.
        val success = latch.await(CLOUDFLARE_TIMEOUT_SECONDS, TimeUnit.SECONDS)

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

        // Throw exception if we failed to bypass Cloudflare
        if (!cloudflareBypassed.get()) {
            if (!success) {
                Log.error { "Cloudflare bypass timed out after $CLOUDFLARE_TIMEOUT_SECONDS seconds" }
            }
            
            if (isWebViewOutdated.get()) {
                throw OutOfDateWebView()
            }

            throw CloudflareBypassException()
        }
    }

    private fun isCloudFlareBypassed(url: String, oldCookie: Cookie?): Boolean {
        return try {
            webViewCookieJar.get(url.toHttpUrl())
                .firstOrNull { it.name == CF_CLEARANCE_COOKIE }
                .let { it != null && it != oldCookie }
        } catch (e: Exception) {
            Log.error { "Error checking Cloudflare bypass status: ${e.message}" }
            false
        }
    }

    private fun getDefaultUserAgent(): String {
        return try {
            WebSettings.getDefaultUserAgent(context)
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
        private const val DEFAULT_USER_AGENT = "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
    }
}

// Internal exception class for Cloudflare bypass (used within this file only)
class CloudflareBypassException : IOException("Failed to bypass Cloudflare protection")

// Public exception classes are defined in commonMain Exception.kt:
// - CloudflareBypassFailed
// - NeedWebView  
// - OutOfDateWebView

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
            // May throw android.webkit.WebViewFactory$MissingWebViewPackageException if WebView
            // is not installed
            CookieManager.getInstance()
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_WEBVIEW)
        } catch (e: Throwable) {
            Log.error { "WebView not supported: ${e.message}" }
            false
        }
    }
}

// WebViewUtil object is defined in commonMain/WebViewUtil.kt

fun WebView.isOutdated(): Boolean {
    return getWebViewMajorVersion() < WebViewUtil.MINIMUM_WEBVIEW_VERSION
}
// Based on https://stackoverflow.com/a/29218966
private fun WebView.getDefaultUserAgentString(): String {
    return try {
        val originalUA: String = settings.userAgentString
        
        // Next call to getUserAgentString() will get us the default
        settings.userAgentString = null
        val defaultUserAgentString = settings.userAgentString
        
        // Revert to original UA string
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
