package org.ireader.core_api.http

import android.app.Application
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.BrowserUserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.serialization.gson.gson
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

class HttpClients(
    context: Application,
    browseEngine: BrowseEngine,
    cookiesStorage: CookiesStorage,
    webViewCookieJar: WebViewCookieJar
) {

    private val cache = run {
        val dir = File(context.cacheDir, "network_cache")
        val size = 15L * 1024 * 1024
        Cache(dir, size)
    }

    private val cookieJar = webViewCookieJar

    private val basicClient = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .callTimeout(2, TimeUnit.MINUTES)
        .addInterceptor(UserAgentInterceptor())

    val browser = browseEngine

    val default = HttpClient(OkHttp) {
        BrowserUserAgent()
        engine {
            preconfigured = this@HttpClients.basicClient.cache(cache).build()
        }
        install(ContentNegotiation) {
            gson()
        }
        install(HttpCookies) {
            storage = cookiesStorage
        }
    }
    val cloudflareClient = HttpClient(OkHttp) {
        BrowserUserAgent()
        engine {
            preconfigured = this@HttpClients.basicClient.addInterceptor(
                CloudflareInterceptor(
                    context,
                    cookieJar
                )
            ).build()
        }
        install(HttpCookies) {
            storage = cookiesStorage
        }
    }
}
