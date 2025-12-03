package ireader.core.http

/**
 * iOS implementation of CookieSynchronizer
 * 
 * TODO: Implement using NSHTTPCookieStorage and WKHTTPCookieStore
 */
actual class CookieSynchronizer {
    actual fun syncCookies(url: String, cookies: List<String>) {
        // TODO: Sync cookies to NSHTTPCookieStorage
    }
    
    actual fun getCookies(url: String): List<String> {
        // TODO: Get cookies from NSHTTPCookieStorage
        return emptyList()
    }
    
    actual fun clearCookies() {
        // TODO: Clear all cookies
    }
}
