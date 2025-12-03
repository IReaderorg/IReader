package ireader.core.http

import android.webkit.CookieManager
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/**
 * Android implementation of cookie synchronization between WebView and OkHttp
 */
actual class CookieSynchronizer(
    private val webViewCookieJar: WebViewCookieJar
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
        val cookies = webViewCookieJar.loadForRequest(httpUrl)
        cookies.forEach { cookie ->
            cookieManager.setCookie(url, cookie.toString())
        }
        cookieManager.flush()
    }
    
    actual fun clearAll() {
        cookieManager.removeAllCookies(null)
        cookieManager.flush()
        webViewCookieJar.removeAll()
    }
}
