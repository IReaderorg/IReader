package org.ireader.core_api.http.impl

import android.content.Context
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.BrowserUserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.serialization.gson.gson
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.ireader.core_api.http.CloudflareInterceptor
import org.ireader.core_api.http.UserAgentInterceptor
import org.ireader.core_api.http.WebViewCookieJar
import org.ireader.core_api.http.main.BrowseEngine
import org.ireader.core_api.http.main.HttpClients
import java.io.File
import java.util.concurrent.TimeUnit



class HttpClientsImpl(
    context: Context,
    browseEngine: BrowseEngine,
    cookiesStorage: CookiesStorage,
    webViewCookieJar: WebViewCookieJar
) : HttpClients {

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

    override val browser = browseEngine

    override val default = HttpClient(OkHttp) {
        BrowserUserAgent()
        engine {
            preconfigured = this@HttpClientsImpl.basicClient.cache(cache).build()
        }
        install(ContentNegotiation) {
            gson()
        }
        install(HttpCookies) {
            storage = cookiesStorage
        }
    }
    override val cloudflareClient = HttpClient(OkHttp) {
        BrowserUserAgent()
        engine {
            preconfigured = this@HttpClientsImpl.basicClient.addInterceptor(
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
