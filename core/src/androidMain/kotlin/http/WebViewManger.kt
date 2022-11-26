package ireader.core.http

import android.content.Context
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class WebViewManger(private val context: Context) {

    var isInit = false
    var webView: WebView? = null

    var userAgent = DEFAULT_USER_AGENT

    var selector: String? = null
    var html: org.jsoup.nodes.Document = org.jsoup.nodes.Document("")
    var webUrl: String? = null
    var inProgress: Boolean = false

    val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    fun init() {
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
        }
    }

    fun update() {
    }

    fun destroy() {
        webView?.stopLoading()
        webView?.destroy()
        isInit = false
        webView = null
    }
}
