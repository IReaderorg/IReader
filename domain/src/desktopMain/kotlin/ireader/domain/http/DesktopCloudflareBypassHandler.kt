package ireader.domain.http

import ireader.core.http.CloudflareBypassHandler
import ireader.core.http.cloudflare.CloudflareBypassPluginManager
import ireader.core.http.cloudflare.CloudflareChallenge
import ireader.core.http.cloudflare.PluginBypassResult
import ireader.core.log.Log
import ireader.core.util.currentTimeMillis

/**
 * Desktop implementation of CloudflareBypassHandler.
 * Uses CloudflareBypassPluginManager with FlareSolverr auto-start support.
 */
class DesktopCloudflareBypassHandler(
    private val pluginManager: CloudflareBypassPluginManager
) : CloudflareBypassHandler {
    
    // Cookie cache per domain
    private val cookieCache = mutableMapOf<String, CloudflareBypassHandler.CookieData>()
    
    override suspend fun bypass(url: String): CloudflareBypassHandler.BypassResult? {
        try {
            Log.debug { "[DesktopCloudflareBypassHandler] Attempting bypass for $url" }
            Log.debug { "[DesktopCloudflareBypassHandler] Registered providers: ${pluginManager.getProviders().map { it.name }}" }
            
            val result = pluginManager.bypass(
                url = url,
                challenge = CloudflareChallenge.JSChallenge(),
                headers = emptyMap(),
                userAgent = null,
                timeoutMs = 60000
            )
            
            when (result) {
                is PluginBypassResult.Success -> {
                    val cfClearance = result.cookies.find { it.name == "cf_clearance" }?.value
                    if (cfClearance != null) {
                        val domain = extractDomain(url)
                        val expiresAt = currentTimeMillis() + 30 * 60 * 1000 // 30 min
                        
                        // Cache the cookies
                        val cookieData = CloudflareBypassHandler.CookieData(
                            cfClearance = cfClearance,
                            cfBm = result.cookies.find { it.name == "__cf_bm" }?.value,
                            userAgent = result.userAgent,
                            expiresAt = expiresAt
                        )
                        cookieCache[domain] = cookieData
                        
                        Log.debug { "[DesktopCloudflareBypassHandler] Bypass successful, cached cookies for $domain" }
                        
                        return CloudflareBypassHandler.BypassResult(
                            cfClearance = cfClearance,
                            cfBm = result.cookies.find { it.name == "__cf_bm" }?.value,
                            userAgent = result.userAgent,
                            expiresAt = expiresAt
                        )
                    }
                }
                is PluginBypassResult.Failed -> {
                    Log.debug { "[DesktopCloudflareBypassHandler] Bypass failed: ${result.reason}" }
                }
                is PluginBypassResult.UserInteractionRequired -> {
                    Log.debug { "[DesktopCloudflareBypassHandler] User interaction required: ${result.message}" }
                }
                is PluginBypassResult.ServiceUnavailable -> {
                    Log.debug { "[DesktopCloudflareBypassHandler] Service unavailable: ${result.reason}" }
                }
            }
        } catch (e: Exception) {
            Log.debug { "[DesktopCloudflareBypassHandler] Bypass error: ${e.message}" }
        }
        return null
    }
    
    override fun getCachedCookies(domain: String): CloudflareBypassHandler.CookieData? {
        val cached = cookieCache[domain]
        return if (cached != null && !cached.isExpired()) cached else null
    }
    
    private fun extractDomain(url: String): String {
        return url.lowercase()
            .removePrefix("http://")
            .removePrefix("https://")
            .removePrefix("www.")
            .substringBefore("/")
            .substringBefore(":")
    }
}
