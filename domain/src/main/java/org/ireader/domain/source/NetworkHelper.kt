package org.ireader.domain.source

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import kotlinx.coroutines.*
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.ireader.core.utils.getHtml
import org.ireader.domain.use_cases.preferences.reader_preferences.PreferencesUseCase
import org.ireader.domain.utils.AndroidCookieJar
import org.ireader.domain.utils.intercepter.CloudflareInterceptor
import org.ireader.infinity.core.data.network.models.*
import org.ireader.infinity.core.data.network.utils.UserAgentInterceptor
import org.ireader.infinity.core.data.network.utils.setDefaultSettings
import org.ireader.infinity.feature_sources.sources.utils.WebViewClientCompat
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit


class NetworkHelper(private val context: Context) : KoinComponent {


    val preferencesUseCase: PreferencesUseCase by inject()

    val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val cacheDir = File(context.cacheDir, "network_cache")

    private val cacheSize = 5L * 1024 * 1024 // 5 MiB

    val cookieManager = AndroidCookieJar()

    val webView: WebView by inject()

    private val baseClientBuilder: OkHttpClient.Builder
        get() {
            val builder = OkHttpClient.Builder()
                .cookieJar(cookieManager)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(UserAgentInterceptor())

            when (preferencesUseCase.readDohPrefUseCase()) {
                PREF_DOH_CLOUDFLARE -> builder.dohCloudflare()
                PREF_DOH_GOOGLE -> builder.dohGoogle()
                PREF_DOH_ADGUARD -> builder.dohAdGuard()
                PREF_DOH_SHECAN -> builder.dohShecan()
            }

            return builder
        }

    val client by lazy { baseClientBuilder.cache(Cache(cacheDir, cacheSize)).build() }

    val cloudflareClient by lazy {
        client.newBuilder()
            .addInterceptor(CloudflareInterceptor(context))
            .build()
    }

    @SuppressLint("SetJavaScriptEnabled")
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun getHtmlFromWebView(
        url: String,
        ajaxSelector: String? = null,
        ua: String,
    ): Document {

        webView.settings.userAgentString = ua
        webView.setDefaultSettings()
        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            setAppCacheEnabled(true)
            useWideViewPort = true
            loadWithOverviewMode = true
            cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
        }
        webView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        val jInterface = MyJavaScriptInterface()
        webView.addJavascriptInterface(jInterface, "HtmlViewer")
        if (webView.url != url) {
            webView.loadUrl(url)
        }


        var docs: Document = Document("No Data was Found")
        var isLoadUp: Boolean = false
        val maxDelay = 120000
        webView.webViewClient = object : WebViewClientCompat() {

            override fun shouldOverrideUrlCompat(view: WebView, url: String): Boolean {
                return false
            }

            override fun onPageFinished(view: WebView, url: String) {
                coroutineScope.launch {
                    docs = Jsoup.parse(webView.getHtml())
                    if (ajaxSelector != null) {
                        while (docs.select(ajaxSelector).text().isEmpty()) {
                            docs = Jsoup.parse(webView.getHtml())
                            delay(200)
                        }
                        isLoadUp = true
                    } else {
                        isLoadUp = true
                    }
                }
            }

            override fun onReceivedErrorCompat(
                view: WebView,
                errorCode: Int,
                description: String?,
                failingUrl: String,
                isMainFrame: Boolean,
            ) {
                isLoadUp = true
                Timber.e("WebView: Not shown")
            }
        }

        docs = Jsoup.parse(webView.getHtml())
        while (!isLoadUp) {
            delay(200)
        }
        return docs
    }

    class MyJavaScriptInterface {
        var html: String? = null

        @JavascriptInterface
        fun showHTML(_html: String?) {
            html = _html
        }
    }
}

