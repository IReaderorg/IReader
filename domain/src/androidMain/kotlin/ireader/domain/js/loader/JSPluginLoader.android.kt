package ireader.domain.js.loader

import ireader.core.http.AcceptAllCookiesStorage
import ireader.core.http.CookieSynchronizer
import ireader.core.http.WebViewCookieJar

/**
 * Android implementation - CookieSynchronizer needs webViewCookieJar
 * For now, create a simple WebViewCookieJar instance
 */
actual fun createPlatformCookieSynchronizer(): CookieSynchronizer {
    // Create a WebViewCookieJar instance with cookie storage
    // In a real implementation, this should be injected from DI
    val cookiesStorage = AcceptAllCookiesStorage()
    val webViewCookieJar = WebViewCookieJar(cookiesStorage)
    return CookieSynchronizer(webViewCookieJar)
}
