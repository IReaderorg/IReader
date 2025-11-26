package ireader.core.http

import android.webkit.CookieManager
import okhttp3.Cookie
import okhttp3.HttpUrl

/**
 * Android implementation of cookie synchronization between WebView and OkHttp
 */
actual class CookieSynchronizer(
    private val webViewCookieJar: WebViewCookieJar
) {
    private val cookieManager = CookieManager.getInstance()
    
    actual fun syncFromWebView(url: HttpUrl) {
        val webViewCookies = cookieManager.getCookie(url.toString())
        if (!webViewCookies.isNullOrEmpty()) {
            val cookies = webViewCookies.split(";")
                .mapNotNull { Cookie.parse(url, it.trim()) }
            webViewCookieJar.saveFromResponse(url, cookies)
        }
    }
    
    actual fun syncToWebView(url: HttpUrl) {
        val cookies = webViewCookieJar.loadForRequest(url)
        cookies.forEach { cookie ->
            cookieManager.setCookie(url.toString(), cookie.toString())
        }
        cookieManager.flush()
    }
    
    actual fun clearAll() {
        cookieManager.removeAllCookies(null)
        cookieManager.flush()
        webViewCookieJar.removeAll()
    }
}
