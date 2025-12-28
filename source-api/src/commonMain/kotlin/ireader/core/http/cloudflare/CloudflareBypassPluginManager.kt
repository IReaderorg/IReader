package ireader.core.http.cloudflare

import ireader.core.log.Log
import ireader.core.util.currentTimeMillis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages Cloudflare bypass providers and coordinates bypass attempts.
 * 
 * This manager:
 * - Maintains a registry of bypass providers
 * - Tries providers in priority order
 * - Caches successful cookies for reuse
 * - Provides status information for UI
 */
class CloudflareBypassPluginManager {
    
    private val _providers = MutableStateFlow<List<CloudflareBypassProvider>>(emptyList())
    val providers: StateFlow<List<CloudflareBypassProvider>> = _providers.asStateFlow()
    
    private val _status = MutableStateFlow<BypassManagerStatus>(BypassManagerStatus.NoProviders)
    val status: StateFlow<BypassManagerStatus> = _status.asStateFlow()
    
    // Cookie cache for reusing successful bypasses
    private val cookieCache = mutableMapOf<String, CachedBypass>()
    
    /**
     * Register a bypass provider.
     */
    fun registerProvider(provider: CloudflareBypassProvider) {
        val current = _providers.value.toMutableList()
        // Remove existing provider with same ID
        current.removeAll { it.id == provider.id }
        current.add(provider)
        // Sort by priority (highest first)
        _providers.value = current.sortedByDescending { it.priority }
        updateStatus()
        Log.info { "[CloudflareBypassPluginManager] Registered provider: ${provider.name} (priority: ${provider.priority})" }
    }
    
    /**
     * Unregister a bypass provider.
     */
    fun unregisterProvider(providerId: String) {
        _providers.value = _providers.value.filter { it.id != providerId }
        updateStatus()
        Log.info { "[CloudflareBypassPluginManager] Unregistered provider: $providerId" }
    }
    
    /**
     * Get all registered providers sorted by priority.
     */
    fun getProviders(): List<CloudflareBypassProvider> = _providers.value
    
    /**
     * Check if any bypass provider is available.
     */
    suspend fun hasAvailableProvider(): Boolean {
        return _providers.value.any { it.isAvailable() }
    }
    
    /**
     * Attempt to bypass Cloudflare protection using registered providers.
     * 
     * @param url URL to fetch
     * @param challenge Detected challenge type
     * @param headers Request headers
     * @param userAgent User agent to use
     * @param timeoutMs Timeout in milliseconds
     * @return Bypass result
     */
    suspend fun bypass(
        url: String,
        challenge: CloudflareChallenge,
        headers: Map<String, String> = emptyMap(),
        userAgent: String? = null,
        timeoutMs: Long = 60000
    ): PluginBypassResult {
        val domain = extractDomain(url)
        
        // Check cache first
        val cached = cookieCache[domain]
        if (cached != null && !cached.isExpired()) {
            Log.debug { "[CloudflareBypassPluginManager] Using cached bypass for $domain" }
            return PluginBypassResult.Success(
                content = "",
                cookies = cached.cookies,
                userAgent = cached.userAgent,
                statusCode = 200
            )
        }
        
        val providers = _providers.value
        if (providers.isEmpty()) {
            return PluginBypassResult.ServiceUnavailable(
                reason = "No Cloudflare bypass providers configured",
                setupInstructions = "Configure FlareSolverr in Settings > Cloudflare Bypass"
            )
        }
        
        val request = BypassRequest(
            url = url,
            headers = headers,
            userAgent = userAgent,
            timeoutMs = timeoutMs
        )
        
        // Try providers in priority order
        for (provider in providers) {
            try {
                if (!provider.isAvailable()) {
                    Log.debug { "[CloudflareBypassPluginManager] Provider ${provider.name} not available, skipping" }
                    continue
                }
                
                if (!provider.canHandle(challenge)) {
                    Log.debug { "[CloudflareBypassPluginManager] Provider ${provider.name} cannot handle challenge type, skipping" }
                    continue
                }
                
                Log.info { "[CloudflareBypassPluginManager] Trying provider: ${provider.name}" }
                val result = provider.bypass(request)
                
                when (result) {
                    is PluginBypassResult.Success -> {
                        // Cache successful bypass
                        cacheCookies(domain, result.cookies, result.userAgent)
                        Log.info { "[CloudflareBypassPluginManager] Bypass successful with ${provider.name}" }
                        return result
                    }
                    is PluginBypassResult.Failed -> {
                        if (!result.canRetry) {
                            Log.warn { "[CloudflareBypassPluginManager] Provider ${provider.name} failed: ${result.reason}" }
                        }
                        // Continue to next provider
                    }
                    is PluginBypassResult.UserInteractionRequired -> {
                        // Return immediately - user needs to act
                        return result
                    }
                    is PluginBypassResult.ServiceUnavailable -> {
                        Log.warn { "[CloudflareBypassPluginManager] Provider ${provider.name} unavailable: ${result.reason}" }
                        // Continue to next provider
                    }
                }
            } catch (e: Exception) {
                Log.error(e, "[CloudflareBypassPluginManager] Provider ${provider.name} threw exception")
                // Continue to next provider
            }
        }
        
        return PluginBypassResult.Failed(
            reason = "All bypass providers failed",
            canRetry = true
        )
    }
    
    /**
     * Get cached cookies for a domain.
     */
    fun getCachedCookies(domain: String): List<BypassCookie>? {
        val cached = cookieCache[domain]
        return if (cached != null && !cached.isExpired()) cached.cookies else null
    }
    
    /**
     * Invalidate cached cookies for a domain.
     */
    fun invalidateCache(domain: String) {
        cookieCache.remove(domain)
        Log.debug { "[CloudflareBypassPluginManager] Invalidated cache for $domain" }
    }
    
    /**
     * Clear all cached cookies.
     */
    fun clearCache() {
        cookieCache.clear()
        Log.debug { "[CloudflareBypassPluginManager] Cleared all cached cookies" }
    }
    
    private fun cacheCookies(
        domain: String,
        cookies: List<BypassCookie>,
        userAgent: String
    ) {
        // Find the earliest expiration among clearance cookies
        val clearanceCookies = cookies.filter { it.isClearanceCookie }
        val expiresAt = clearanceCookies
            .mapNotNull { if (it.expiresAt > 0) it.expiresAt else null }
            .minOrNull()
            ?: (currentTimeMillis() + 30 * 60 * 1000) // Default 30 min
        
        cookieCache[domain] = CachedBypass(
            cookies = cookies,
            userAgent = userAgent,
            expiresAt = expiresAt
        )
        Log.debug { "[CloudflareBypassPluginManager] Cached ${cookies.size} cookies for $domain" }
    }
    
    private fun updateStatus() {
        _status.value = when {
            _providers.value.isEmpty() -> BypassManagerStatus.NoProviders
            else -> BypassManagerStatus.Ready(_providers.value.size)
        }
    }
    
    private fun extractDomain(url: String): String {
        return try {
            url.substringAfter("://").substringBefore("/").substringBefore(":")
        } catch (e: Exception) {
            url
        }
    }
}

/**
 * Status of the bypass manager.
 */
sealed class BypassManagerStatus {
    object NoProviders : BypassManagerStatus()
    data class Ready(val providerCount: Int) : BypassManagerStatus()
}

/**
 * Cached bypass result.
 */
private data class CachedBypass(
    val cookies: List<BypassCookie>,
    val userAgent: String,
    val expiresAt: Long
) {
    fun isExpired(): Boolean = currentTimeMillis() > expiresAt
}
