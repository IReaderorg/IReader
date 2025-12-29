package ireader.core.http.cloudflare

import ireader.core.log.Log
import ireader.core.util.currentTimeMillis

/**
 * Bridge strategy that uses CloudflareBypassPluginManager as a CloudflareBypassStrategy.
 * 
 * This allows the plugin manager (with FlareSolverr auto-start capability) to be used
 * by the existing CloudflareBypassManager in EnhancedHttpClients.
 */
class PluginManagerBypassStrategy(
    private val pluginManager: CloudflareBypassPluginManager
) : CloudflareBypassStrategy {
    
    override val priority = 90 // High priority - use plugin manager before other strategies
    override val name = "PluginManager"
    
    override suspend fun canHandle(challenge: CloudflareChallenge): Boolean {
        // Check if any provider in the plugin manager can handle this challenge
        return when (challenge) {
            is CloudflareChallenge.None -> false
            is CloudflareChallenge.BlockedIP -> false
            is CloudflareChallenge.RateLimited -> false
            else -> pluginManager.hasAvailableProvider()
        }
    }
    
    override suspend fun bypass(
        url: String,
        challenge: CloudflareChallenge,
        config: BypassConfig
    ): BypassResult {
        Log.info { "[PluginManagerBypassStrategy] Attempting bypass for $url with challenge: ${challenge::class.simpleName}" }
        
        val result = pluginManager.bypass(
            url = url,
            challenge = challenge,
            headers = emptyMap(),
            userAgent = config.userAgent,
            timeoutMs = config.timeout
        )
        
        return when (result) {
            is PluginBypassResult.Success -> {
                // Convert plugin result to BypassResult
                val cfClearance = result.cookies.find { it.name == "cf_clearance" }?.value
                
                if (cfClearance != null) {
                    Log.info { "[PluginManagerBypassStrategy] Bypass successful, got cf_clearance cookie" }
                    BypassResult.Success(
                        ClearanceCookie(
                            cfClearance = cfClearance,
                            cfBm = result.cookies.find { it.name == "__cf_bm" }?.value,
                            userAgent = result.userAgent,
                            timestamp = currentTimeMillis(),
                            expiresAt = currentTimeMillis() + ClearanceCookie.DEFAULT_VALIDITY_MS,
                            domain = url.extractDomain()
                        )
                    )
                } else {
                    Log.warn { "[PluginManagerBypassStrategy] Bypass succeeded but no cf_clearance cookie found" }
                    BypassResult.Failed(
                        "Plugin bypass succeeded but no cf_clearance cookie found",
                        challenge,
                        canRetry = true
                    )
                }
            }
            
            is PluginBypassResult.UserInteractionRequired -> {
                Log.info { "[PluginManagerBypassStrategy] User interaction required: ${result.message}" }
                BypassResult.UserInteractionRequired(
                    challenge = challenge,
                    message = result.message
                )
            }
            
            is PluginBypassResult.ServiceUnavailable -> {
                Log.warn { "[PluginManagerBypassStrategy] Service unavailable: ${result.reason}" }
                BypassResult.Failed(
                    "Plugin service unavailable: ${result.reason}",
                    challenge,
                    canRetry = false
                )
            }
            
            is PluginBypassResult.Failed -> {
                Log.warn { "[PluginManagerBypassStrategy] Bypass failed: ${result.reason}" }
                BypassResult.Failed(
                    "Plugin bypass failed: ${result.reason}",
                    challenge,
                    canRetry = result.canRetry
                )
            }
        }
    }
}
