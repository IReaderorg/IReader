package ireader.domain.utils

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Thread-safe in-memory cookie jar with automatic cleanup to prevent unbounded growth.
 * 
 * Features:
 * - Thread-safe using synchronized map
 * - Automatic removal of expired cookies on each request
 * - Maximum cookie limit to prevent memory issues
 * - Stale cookie cleanup for session cookies without explicit expiration
 * - Domain-specific cookie clearing
 */
class MemoryCookieJar : CookieJar {
    
    // Using synchronized map for thread-safe operations
    // Key is the cookie identifier (name+domain+path), value is the wrapped cookie
    private val cache = mutableMapOf<String, WrappedCookie>()
    
    companion object {
        /**
         * Maximum number of cookies to store.
         * Prevents unbounded memory growth from malicious or misconfigured sites.
         */
        private const val MAX_COOKIES = 500
        
        /**
         * Maximum age for session cookies without explicit expiration (24 hours).
         */
        private const val SESSION_COOKIE_MAX_AGE_MS = 24 * 60 * 60 * 1000L
        
        /**
         * Cleanup threshold - trigger cleanup when cache exceeds this percentage of max
         */
        private const val CLEANUP_THRESHOLD = 0.8
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val now = currentTimeToLong()
        val validCookies = mutableListOf<Cookie>()
        val keysToRemove = mutableListOf<String>()

        cache.forEach { (key, wrappedCookie) ->
            when {
                wrappedCookie.isExpired(now) -> keysToRemove.add(key)
                wrappedCookie.isStale(now) -> keysToRemove.add(key)
                wrappedCookie.matches(url) -> validCookies.add(wrappedCookie.cookie)
            }
        }

        // Remove expired/stale cookies
        keysToRemove.forEach { cache.remove(it) }

        return validCookies
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val now = currentTimeToLong()
        
        cookies.forEach { cookie ->
            val wrapped = WrappedCookie.wrap(cookie, now)
            val key = wrapped.cacheKey
            
            // Only add non-expired cookies
            if (!wrapped.isExpired(now)) {
                cache[key] = wrapped
            }
        }
        
        // Enforce maximum cookie limit if needed
        if (cache.size > (MAX_COOKIES * CLEANUP_THRESHOLD).toInt()) {
            enforceMaxCookies()
        }
    }

    /**
     * Clear all cookies.
     */
    fun clear() {
        cache.clear()
    }
    
    /**
     * Remove all expired and stale cookies.
     * Can be called periodically to clean up memory.
     */
    fun cleanup() {
        val now = currentTimeToLong()
        val keysToRemove = cache.entries
            .filter { it.value.isExpired(now) || it.value.isStale(now) }
            .map { it.key }
        
        keysToRemove.forEach { cache.remove(it) }
    }
    
    /**
     * Get the current number of stored cookies.
     */
    fun size(): Int = cache.size
    
    /**
     * Remove cookies for a specific domain.
     */
    fun clearForDomain(domain: String) {
        val keysToRemove = cache.entries
            .filter { entry ->
                val cookieDomain = entry.value.cookie.domain
                cookieDomain == domain || cookieDomain.endsWith(".$domain")
            }
            .map { it.key }
        
        keysToRemove.forEach { cache.remove(it) }
    }
    
    /**
     * Get all cookies for a specific domain (for debugging/inspection).
     */
    fun getCookiesForDomain(domain: String): List<Cookie> {
        return cache.values
            .filter { wrapped ->
                val cookieDomain = wrapped.cookie.domain
                cookieDomain == domain || cookieDomain.endsWith(".$domain")
            }
            .map { it.cookie }
    }
    
    /**
     * Enforce maximum cookie limit by removing expired, stale, and oldest cookies.
     */
    private fun enforceMaxCookies() {
        val now = currentTimeToLong()
        
        // First pass: remove expired and stale cookies
        val keysToRemove = cache.entries
            .filter { it.value.isExpired(now) || it.value.isStale(now) }
            .map { it.key }
        
        keysToRemove.forEach { cache.remove(it) }
        
        // If still over limit, remove cookies expiring soonest
        if (cache.size > MAX_COOKIES) {
            val sortedEntries = cache.entries
                .sortedBy { it.value.cookie.expiresAt }
            
            val countToRemove = cache.size - MAX_COOKIES
            sortedEntries.take(countToRemove).forEach { entry ->
                cache.remove(entry.key)
            }
        }
    }
}

/**
 * Wrapper for Cookie that tracks creation time and provides utility methods.
 */
class WrappedCookie private constructor(
    val cookie: Cookie,
    private val createdAt: Long
) {
    /**
     * Unique key for this cookie in the cache.
     */
    val cacheKey: String
        get() = "${cookie.name}|${cookie.domain}|${cookie.path}|${cookie.secure}|${cookie.hostOnly}"

    /**
     * Check if cookie has expired based on its expiresAt value.
     */
    fun isExpired(now: Long = currentTimeToLong()): Boolean {
        return cookie.expiresAt < now
    }
    
    /**
     * Check if cookie is stale (session cookie that's been around too long).
     * Session cookies without explicit expiration are considered stale after 24 hours.
     */
    fun isStale(now: Long = currentTimeToLong()): Boolean {
        // Cookies with explicit future expiration are not stale
        if (cookie.expiresAt > now + 1000) return false
        
        // Session cookies (expiresAt at end of session) are stale after max age
        val maxAge = 24 * 60 * 60 * 1000L
        return (now - createdAt) > maxAge
    }

    /**
     * Check if this cookie matches the given URL.
     */
    fun matches(url: HttpUrl): Boolean = cookie.matches(url)

    override fun equals(other: Any?): Boolean {
        if (other !is WrappedCookie) return false

        return other.cookie.name == cookie.name &&
            other.cookie.domain == cookie.domain &&
            other.cookie.path == cookie.path &&
            other.cookie.secure == cookie.secure &&
            other.cookie.hostOnly == cookie.hostOnly
    }

    override fun hashCode(): Int {
        var hash = 17
        hash = 31 * hash + cookie.name.hashCode()
        hash = 31 * hash + cookie.domain.hashCode()
        hash = 31 * hash + cookie.path.hashCode()
        hash = 31 * hash + if (cookie.secure) 0 else 1
        hash = 31 * hash + if (cookie.hostOnly) 0 else 1
        return hash
    }

    companion object {
        /**
         * Create a wrapped cookie with the current timestamp.
         */
        fun wrap(cookie: Cookie, createdAt: Long = currentTimeToLong()) = 
            WrappedCookie(cookie, createdAt)
    }
}
