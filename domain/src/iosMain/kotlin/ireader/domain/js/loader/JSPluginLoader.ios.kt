package ireader.domain.js.loader

import ireader.core.http.CookieSynchronizer

/**
 * iOS implementation of JS engine creation
 * 
 * TODO: Implement using JavaScriptCore
 */
actual fun createEngine(bridgeService: JSBridgeService): JSEngine {
    // TODO: Return JavaScriptCore-based engine
    return ireader.domain.js.engine.JSEngine()
}

/**
 * iOS implementation of cookie synchronizer
 */
actual fun createPlatformCookieSynchronizer(): CookieSynchronizer {
    return IosCookieSynchronizer()
}

private class IosCookieSynchronizer : CookieSynchronizer {
    override fun syncCookies(url: String, cookies: List<String>) {
        // TODO: Implement using NSHTTPCookieStorage
    }
    
    override fun getCookies(url: String): List<String> {
        // TODO: Implement using NSHTTPCookieStorage
        return emptyList()
    }
    
    override fun clearCookies() {
        // TODO: Implement
    }
}
