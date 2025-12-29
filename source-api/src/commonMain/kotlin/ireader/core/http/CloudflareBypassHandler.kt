package ireader.core.http

import ireader.core.util.currentTimeMillis

/**
 * Interface for handling Cloudflare bypass.
 * Implemented in domain module where CloudflareBypassPluginManager is available.
 */
interface CloudflareBypassHandler {
    /**
     * Attempt to bypass Cloudflare protection for the given URL.
     * @param url The URL that triggered the Cloudflare challenge
     * @return BypassResult with cookies if successful, null if bypass not available or failed
     */
    suspend fun bypass(url: String): BypassResult?
    
    /**
     * Get cached cookies for a domain if available.
     */
    fun getCachedCookies(domain: String): CookieData?
    
    /**
     * Result of a bypass attempt.
     */
    data class BypassResult(
        val cfClearance: String,
        val cfBm: String?,
        val userAgent: String,
        val expiresAt: Long
    )
    
    /**
     * Cached cookie data.
     */
    data class CookieData(
        val cfClearance: String,
        val cfBm: String?,
        val userAgent: String,
        val expiresAt: Long
    ) {
        fun isExpired(): Boolean = currentTimeMillis() > expiresAt
    }
}

/**
 * No-op implementation for platforms without bypass support.
 */
object NoOpCloudflareBypassHandler : CloudflareBypassHandler {
    override suspend fun bypass(url: String): CloudflareBypassHandler.BypassResult? = null
    override fun getCachedCookies(domain: String): CloudflareBypassHandler.CookieData? = null
}
