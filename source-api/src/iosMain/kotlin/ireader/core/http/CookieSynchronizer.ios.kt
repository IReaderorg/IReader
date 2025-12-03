package ireader.core.http

import platform.Foundation.*
import platform.WebKit.*
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.*

/**
 * iOS implementation of CookieSynchronizer
 * 
 * Synchronizes cookies between:
 * - NSHTTPCookieStorage (used by URLSession/Ktor)
 * - WKHTTPCookieStore (used by WKWebView)
 */
@OptIn(ExperimentalForeignApi::class)
actual class CookieSynchronizer {
    
    private val sharedCookieStorage: NSHTTPCookieStorage
        get() = NSHTTPCookieStorage.sharedHTTPCookieStorage
    
    /**
     * Sync cookies from WKWebView to HTTP client storage (NSHTTPCookieStorage)
     * @param url The URL string to sync cookies for
     */
    actual fun syncFromWebView(url: String) {
        val nsUrl = NSURL.URLWithString(url) ?: return
        
        // Get the default WKWebView configuration's cookie store
        val websiteDataStore = WKWebsiteDataStore.defaultDataStore()
        val cookieStore = websiteDataStore.httpCookieStore
        
        // Get all cookies from WKWebView and add to shared storage
        cookieStore.getAllCookies { cookies ->
            @Suppress("UNCHECKED_CAST")
            val cookieList = cookies as? List<NSHTTPCookie> ?: return@getAllCookies
            
            for (cookie in cookieList) {
                // Check if cookie applies to this URL's domain
                val cookieDomain = cookie.domain
                val urlHost = nsUrl.host ?: continue
                
                if (urlHost.endsWith(cookieDomain) || cookieDomain == urlHost) {
                    sharedCookieStorage.setCookie(cookie)
                }
            }
        }
    }
    
    /**
     * Sync cookies from HTTP client (NSHTTPCookieStorage) to WKWebView
     * @param url The URL string to sync cookies for
     */
    actual fun syncToWebView(url: String) {
        val nsUrl = NSURL.URLWithString(url) ?: return
        
        // Get cookies for this URL from shared storage
        val cookies = sharedCookieStorage.cookiesForURL(nsUrl) ?: return
        
        // Get the default WKWebView configuration's cookie store
        val websiteDataStore = WKWebsiteDataStore.defaultDataStore()
        val cookieStore = websiteDataStore.httpCookieStore
        
        // Add each cookie to WKWebView
        @Suppress("UNCHECKED_CAST")
        for (cookie in cookies as List<NSHTTPCookie>) {
            cookieStore.setCookie(cookie, completionHandler = null)
        }
    }
    
    /**
     * Clear all cookies from both WKWebView and HTTP client storage
     */
    actual fun clearAll() {
        // Clear NSHTTPCookieStorage
        sharedCookieStorage.cookies?.let { cookies ->
            @Suppress("UNCHECKED_CAST")
            for (cookie in cookies as List<NSHTTPCookie>) {
                sharedCookieStorage.deleteCookie(cookie)
            }
        }
        
        // Clear WKWebView cookies
        val websiteDataStore = WKWebsiteDataStore.defaultDataStore()
        val dataTypes = setOf(WKWebsiteDataTypeCookies)
        
        @Suppress("UNCHECKED_CAST")
        websiteDataStore.removeDataOfTypes(
            dataTypes as Set<Any>,
            modifiedSince = NSDate.distantPast,
            completionHandler = {}
        )
    }
}
