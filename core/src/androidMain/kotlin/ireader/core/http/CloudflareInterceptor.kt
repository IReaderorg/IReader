package ireader.core.http

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import ireader.core.R
import ireader.core.log.Log
import ireader.i18n.LocalizeHelper
import ireader.i18n.resources.MR
import kotlinx.coroutines.*
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
class CloudflareInterceptor(private val context: Context, private val webViewCookieJar: WebViewCookieJar,private val localizeHelper: LocalizeHelper) : Interceptor {

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
            launchUI {
                context.toast(R.string.information_webview_required, Toast.LENGTH_LONG)
            }
            return chain.proceed(originalRequest)
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
            throw IOException(localizeHelper.localize(MR.strings.information_cloudflare_bypass_failure))
        } catch (e: Exception) {
            throw IOException(e)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun resolveWithWebView(request: Request, oldCookie: Cookie?) {
        // We need to lock this thread until the WebView finds the challenge solution url, because
        // OkHttp doesn't support asynchronous interceptors.
        val latch = CountDownLatch(5)

        var webView: WebView? = null

        var challengeFound = false
        var cloudflareBypassed = false
        var isWebViewOutdated = false

        val origRequestUrl = request.url.toString()
        val headers = request.headers.toMultimap().mapValues { it.value.getOrNull(0) ?: "" }.toMutableMap()

        executor.execute {
            val webview = WebView(context)
            webView = webview
            webview.setDefaultSettings()

            // Avoid sending empty User-Agent, Chromium WebView will reset to default if empty
            webview.settings.userAgentString = request.header("User-Agent")
                ?: DEFAULT_USER_AGENT

            webview.webViewClient = object : WebViewClientCompat() {
                override fun onPageFinished(view: WebView, url: String) {
                    fun isCloudFlareBypassed(): Boolean {
                        return webViewCookieJar.get(origRequestUrl.toHttpUrl())
                            .firstOrNull { it.name == "cf_clearance" }
                            .let { it != null && it != oldCookie }
                    }

                    if (isCloudFlareBypassed()) {
                        cloudflareBypassed = true
                        latch.countDown()
                    }

                    if (url == origRequestUrl && !challengeFound) {
                        // The first request didn't return the challenge, abort.
                        latch.countDown()
                    }
                }

                override fun onReceivedErrorCompat(
                    view: WebView,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String,
                    isMainFrame: Boolean,
                ) {
                    if (isMainFrame) {
                        if (errorCode in ERROR_CODES) {
                            // Found the Cloudflare challenge page.
                            challengeFound = true
                        } else {
                            // Unlock thread, the challenge wasn't found.
                            latch.countDown()
                        }
                    }
                }
            }

            webView?.loadUrl(origRequestUrl, headers)
        }

        // Wait a reasonable amount of time to retrieve the solution. The minimum should be
        // around 4 seconds but it can take more due to slow networks or server issues.
        latch.await(30, TimeUnit.SECONDS)

        executor.execute {
            if (!cloudflareBypassed) {
                isWebViewOutdated = webView?.isOutdated() == true
            }

            webView?.stopLoading()
            webView?.destroy()
            webView = null
        }

        // Throw exception if we failed to bypass Cloudflare
        if (!cloudflareBypassed) {
            // Prompt user to update WebView if it seems too outdated
            if (isWebViewOutdated) {
                context.toast(R.string.information_webview_outdated, Toast.LENGTH_LONG)
            }

            throw CloudflareBypassException()
        }
    }

    companion object {
        private val ERROR_CODES = listOf(403, 503)
        private val SERVER_CHECK = arrayOf("cloudflare-nginx", "cloudflare")
        private val COOKIE_NAMES = listOf("cf_clearance")
    }
}

private class CloudflareBypassException : Exception()

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
        try {
            // May throw android.webkit.WebViewFactory$MissingWebViewPackageException if WebView
            // is not installed
            CookieManager.getInstance()
        } catch (e: Throwable) {
            Log.error("WebViewUtil : $e")
            return false
        }

        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_WEBVIEW)
    }
}
@OptIn(DelicateCoroutinesApi::class)
fun launchUI(block: suspend CoroutineScope.() -> Unit): Job =
    GlobalScope.launch(Dispatchers.Main, CoroutineStart.DEFAULT, block)

fun WebView.isOutdated(): Boolean {
    return getWebViewMajorVersion() < WebViewUtil.MINIMUM_WEBVIEW_VERSION
}
// Based on https://stackoverflow.com/a/29218966
private fun WebView.getDefaultUserAgentString(): String {
    val originalUA: String = settings.userAgentString

    // Next call to getUserAgentString() will get us the default
    settings.userAgentString = null
    val defaultUserAgentString = settings.userAgentString

    // Revert to original UA string
    settings.userAgentString = originalUA

    return defaultUserAgentString
}

private fun WebView.getWebViewMajorVersion(): Int {
    val uaRegexMatch = """.*Chrome/(\d+)\..*""".toRegex().matchEntire(getDefaultUserAgentString())
    return if (uaRegexMatch != null && uaRegexMatch.groupValues.size > 1) {
        uaRegexMatch.groupValues[1].toInt()
    } else {
        0
    }
}
fun Context.toast(@StringRes resource: Int, duration: Int = Toast.LENGTH_SHORT, block: (Toast) -> Unit = {}): Toast {
    return toast(getString(resource), duration, block)
}
fun Context.toast(text: String?, duration: Int = Toast.LENGTH_SHORT, block: (Toast) -> Unit = {}): Toast {
    return Toast.makeText(this, text.orEmpty(), duration).also {
        block(it)
        it.show()
    }
}
