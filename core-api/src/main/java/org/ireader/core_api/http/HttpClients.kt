

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
    cookiesStorage: CookiesStorage
) {

    private val cache = run {
        val dir = File(context.cacheDir, "network_cache")
        val size = 15L * 1024 * 1024
        Cache(dir, size)
    }



    private val cookieJar = WebViewCookieJar(cookiesStorage)

    private val okhttpClient = OkHttpClient.Builder()
        .cache(cache)
        .cookieJar(cookieJar)
        .readTimeout(30L, TimeUnit.SECONDS)
        .writeTimeout(30L, TimeUnit.SECONDS)
        .callTimeout(2, TimeUnit.MINUTES)
        .build()

    val browser = browseEngine

    val default = HttpClient(OkHttp) {
        BrowserUserAgent()
        engine {
            preconfigured = okhttpClient
        }
        install(ContentNegotiation) {
            gson()
        }
        install(HttpCookies) {
            storage = cookiesStorage
        }
    }
}
