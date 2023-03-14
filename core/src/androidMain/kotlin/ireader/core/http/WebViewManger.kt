package ireader.core.http

import android.content.Context
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import ireader.core.util.createCoroutineScope
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

    val scope = createCoroutineScope()
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
                                if (view?.title?.contains(
                                        "Cloudflare",
                                        true
                                    ) == false
                                ) {
                                    if (!selector.isNullOrBlank()) {
                                        html = Jsoup.parse(view.getHtml())
                                        val hasText = html.select(selector.toString()).first() != null
                                        if (hasText && webUrl == url) {
                                            webUrl = null
                                            selector = null
                                            html = Document("")
                                            inProgress = false
                                        }
                                    } else {
                                        inProgress = false
                                    }
                                }
                            }
                            delay(1000L)
                        }
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

    actual fun update() {
    }

    actual fun destroy() {
        webView?.stopLoading()
        webView?.destroy()
        isInit = false
        webView = null
    }
}
