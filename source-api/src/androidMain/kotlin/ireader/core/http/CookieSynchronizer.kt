package ireader.core.http

import android.webkit.CookieManager
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.http.Url
import kotlinx.coroutines.runBlocking
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/**
 * Android implementation of cookie synchronization between WebView and OkHttp
 */
actual class CookieSynchronizer(
    private val webViewCookieJar: WebViewCookieJar,
    private val cookiesStorage: CookiesStorage
) {
    private val cookieManager = CookieManager.getInstance()

    actual fun syncFromWebView(url: String) {
        val httpUrl = url.toHttpUrlOrNull() ?: return
        val webViewCookies = cookieManager.getCookie(url)
        if (!webViewCookies.isNullOrEmpty()) {
            val cookies = webViewCookies.split(";")
                .mapNotNull { Cookie.parse(httpUrl, it.trim()) }
            webViewCookieJar.saveFromResponse(httpUrl, cookies)
        }
    }

    actual fun syncToWebView(url: String) {
        val httpUrl = url.toHttpUrlOrNull() ?: return
        val ktorCookies = runBlocking {
            try {
                cookiesStorage.get(Url(url))
            } catch (_: Exception) {
                emptyList()
            }
        }
        ktorCookies.forEach { ktorCookie ->
            val cookieBuilder = Cookie.Builder()
                .domain(ktorCookie.domain ?: httpUrl.host)
                .path(ktorCookie.path ?: "/")
                .name(ktorCookie.name)
                .value(ktorCookie.value)
            if (ktorCookie.secure) cookieBuilder.secure()
            if (ktorCookie.httpOnly) cookieBuilder.httpOnly()
            cookieManager.setCookie(url, cookieBuilder.build().toString())
        }
        cookieManager.flush()
    }

    actual fun clearAll() {
        cookieManager.removeAllCookies(null)
        cookieManager.flush()
        webViewCookieJar.removeAll()
    }
}
