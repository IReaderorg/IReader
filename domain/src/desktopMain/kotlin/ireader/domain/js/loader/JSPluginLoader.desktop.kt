package ireader.domain.js.loader

import ireader.core.http.CookieSynchronizer

/**
 * Desktop implementation - CookieSynchronizer doesn't need webViewCookieJar
 */
actual fun createPlatformCookieSynchronizer(): CookieSynchronizer {
    return CookieSynchronizer()
}
