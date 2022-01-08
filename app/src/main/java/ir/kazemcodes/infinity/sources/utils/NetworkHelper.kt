package ir.kazemcodes.infinity.data.network.utils

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebView
import ir.kazemcodes.infinity.data.network.models.*
import ir.kazemcodes.infinity.data.network.utils.intercepter.CloudflareInterceptor
import ir.kazemcodes.infinity.domain.use_cases.datastore.DataStoreUseCase
import ir.kazemcodes.infinity.util.Resource
import ir.kazemcodes.infinity.util.getHtml
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit


class NetworkHelper(private val context: Context) : DIAware {

    override val di: DI by closestDI(context)

    val datastore: DataStoreUseCase by di.instance<DataStoreUseCase>()
    val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val cacheDir = File(context.cacheDir, "network_cache")

    private val cacheSize = 5L * 1024 * 1024 // 5 MiB

    val cookieManager = AndroidCookieJar()

    val webView by lazy { WebView(context) }

    private val baseClientBuilder: OkHttpClient.Builder
        get() {
            val builder = OkHttpClient.Builder()
                .cookieJar(cookieManager)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(UserAgentInterceptor())

            coroutineScope.launch {
                datastore.readDohPrefUseCase().collectLatest { result ->
                    when (result) {
                        is Resource.Success -> {
                            when (result.data ?: Dns.Disable.prefCode) {
                                PREF_DOH_CLOUDFLARE -> builder.dohCloudflare()
                                PREF_DOH_GOOGLE -> builder.dohGoogle()
                                PREF_DOH_ADGUARD -> builder.dohAdGuard()
                                PREF_DOH_SHECAN -> builder.dohShecan()
                            }
                        }

                        is Resource.Error -> {
                            Timber.e("Timber: ReadDohPref  : ${result.message ?: ""}")
                        }
                        else -> {
                        }
                    }
                }
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
    suspend fun getHtmlFromWebView(url: String, ajaxSelector: String? = null): Document {

        webView.setDefaultSettings()
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(url)


        var docs: Document = Document("No Data was Found")
        var isLoadUp: Boolean = false
        var delayedSec = 0
        val maxDelay = 60000
        webView.webViewClient = object : WebViewClientCompat() {
            override fun onPageFinished(view: WebView, url: String) {
                coroutineScope.launch(Dispatchers.Main) {
                    docs = Jsoup.parse(webView.getHtml())
                    if (ajaxSelector != null) {
                        while (docs.select(ajaxSelector).text().isEmpty()) {
                            docs = Jsoup.parse(webView.getHtml())
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
                webView.destroy()
            }
        }
        docs = Jsoup.parse(webView.getHtml())
        while (!isLoadUp && delayedSec < maxDelay) {
            delay(200)
            delayedSec += 200
        }

        //Timber.e(docs.toString())
        return docs
    }

}

