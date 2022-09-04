package org.ireader.core_api.http

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.os.Build
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Headers
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.ireader.core_api.log.Log
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.concurrent.TimeoutException

actual class BrowserEngine(private val webViewManger: WebViewManger, private val webViewCookieJar: WebViewCookieJar) : BrowserEngineInterface {
    /**
     * this function
     * @param url  the url of page
     * @param selector  the selector of first element should appear before fetching the html content
     * @param headers  the header of request
     * @param timeout  the timeout of request
     * @param userAgent  the userAgent of request
     * @return [Result]
     */
    actual override suspend fun fetch(
        url: String,
        selector: String?,
        headers: Headers?,
        timeout: Long,
        userAgent: String,
    ): Result {
        var currentTime = 0L
        var html: Document = Document("No Data was Found")
        withContext(Dispatchers.Main) {
            webViewManger.init()
            val client = webViewManger.webView!!
            if (userAgent != webViewManger.userAgent) {
                webViewManger.userAgent = userAgent
                try {
                    client.settings.userAgentString = userAgent
                } catch (e: Throwable) {
                    Log.error(exception = e, "failed to set user agent")
                }
            }
            if (headers != null) {
                client.loadUrl(
                    url,
                    headers.toMultimap().mapValues { it.value.getOrNull(0) ?: "" }.toMutableMap()
                )
            } else {
                client.loadUrl(url)
            }

            webViewManger.selector = selector
            webViewManger.inProgress = true
            webViewManger.webUrl = url

            while (currentTime < timeout && webViewManger.inProgress) {
                delay(1000)
                currentTime += 1000
            }
            if (currentTime >= timeout) {
                webViewManger.inProgress = false
                throw TimeoutException()
            }
            webViewManger.inProgress = false
            html = Jsoup.parse(client.getHtml())
        }
        val cookies = webViewCookieJar.get(url.toHttpUrl())
        webViewManger.webView
        return org.ireader.core_api.http.Result(
            responseBody = html.html(),
            cookies = cookies,
        )
    }


}

@SuppressLint("SetJavaScriptEnabled")
fun WebView.setDefaultSettings() {
    with(settings) {
        javaScriptEnabled = true
        domStorageEnabled = true
        databaseEnabled = true
        // https://stackoverflow.com/questions/9128952/caching-in-android-webview
        // setAppCacheEnabled(true)
        useWideViewPort = true
        loadWithOverviewMode = true
        cacheMode = WebSettings.LOAD_DEFAULT
    }
}

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalCoroutinesApi::class)
suspend fun WebView.getHtml(): String = suspendCancellableCoroutine { continuation ->
    settings.javaScriptEnabled = true
    if (!settings.javaScriptEnabled)
        throw IllegalStateException("Javascript is disabled")

    evaluateJavascript(
        "(function() { return ('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>'); })();"
    ) {
        continuation.resume(
            it!!.replace("\\u003C", "<")
                .replace("\\n", "")
                .replace("\\t", "")
                .replace("\\\"", "\"")
                .replace("<hr />", "")
        ) {
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
