package ireader.core.http.cloudflare

import ireader.core.http.BrowserEngine
import ireader.core.http.BrowserEngineInterface
import ireader.core.http.fingerprint.BrowserFingerprint
import ireader.core.http.fingerprint.FingerprintEvasionScripts
import ireader.core.http.fingerprint.FingerprintManager
import ireader.core.util.currentTimeMillis

/**
 * WebView-based Cloudflare bypass strategy
 * Uses BrowserEngine to solve JS challenges
 */
class WebViewBypassStrategy(
    private val browserEngine: BrowserEngineInterface,
    private val fingerprintManager: FingerprintManager? = null
) : CloudflareBypassStrategy {
    
    override val priority = 100 // High priority - native solution
    override val name = "WebView"
    
    override suspend fun canHandle(challenge: CloudflareChallenge): Boolean {
        if (!browserEngine.isAvailable()) return false
        
        return when (challenge) {
            is CloudflareChallenge.JSChallenge -> true
            is CloudflareChallenge.ManagedChallenge -> true
            is CloudflareChallenge.RateLimited -> false // Just need to wait
            is CloudflareChallenge.BlockedIP -> false // Can't bypass
            is CloudflareChallenge.CaptchaChallenge -> false // Needs solver
            is CloudflareChallenge.TurnstileChallenge -> false // Needs solver
            is CloudflareChallenge.None -> false
            is CloudflareChallenge.Unknown -> true // Try anyway
        }
    }
    
    override suspend fun bypass(
        url: String,
        challenge: CloudflareChallenge,
        config: BypassConfig
    ): BypassResult {
        if (!browserEngine.isAvailable()) {
            return BypassResult.Failed(
                "WebView/BrowserEngine not available",
                challenge,
                canRetry = false
            )
        }
        
        val domain = url.extractDomain()
        val fingerprint = fingerprintManager?.getOrCreateProfile(domain) 
            ?: BrowserFingerprint.DEFAULT
        
        val userAgent = config.userAgent ?: fingerprint.userAgent

        // Build headers with fingerprint consistency
        val headers = buildMap {
            put("User-Agent", userAgent)
            put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
            put("Accept-Language", fingerprint.languages.joinToString(","))
            put("Accept-Encoding", "gzip, deflate, br")
            put("Connection", "keep-alive")
            put("Upgrade-Insecure-Requests", "1")
            put("Sec-Fetch-Dest", "document")
            put("Sec-Fetch-Mode", "navigate")
            put("Sec-Fetch-Site", "none")
            put("Sec-Fetch-User", "?1")
        }
        
        return try {
            // Fetch with browser engine
            val result = browserEngine.fetch(
                url = url,
                selector = null, // Wait for Cloudflare to resolve
                headers = headers,
                timeout = config.timeout,
                userAgent = userAgent
            )
            
            if (!result.isSuccess) {
                return BypassResult.Failed(
                    result.error ?: "Browser fetch failed with status ${result.statusCode}",
                    challenge,
                    canRetry = true
                )
            }
            
            // Check if we got cf_clearance cookie
            val cfClearance = result.cookies.find { it.name == "cf_clearance" }
            val cfBm = result.cookies.find { it.name == "__cf_bm" }
            
            if (cfClearance != null) {
                BypassResult.Success(
                    ClearanceCookie(
                        cfClearance = cfClearance.value,
                        cfBm = cfBm?.value,
                        userAgent = userAgent,
                        timestamp = currentTimeMillis(),
                        expiresAt = cfClearance.expiresAt.takeIf { it > 0 }
                            ?: (currentTimeMillis() + ClearanceCookie.DEFAULT_VALIDITY_MS),
                        domain = domain
                    )
                )
            } else {
                // Check if the page content indicates success (no more challenge)
                val body = result.responseBody
                if (!isCloudflareChallengePage(body)) {
                    // Challenge might be solved but cookie not captured
                    // This can happen with some Cloudflare configurations
                    BypassResult.Failed(
                        "Challenge appears solved but cf_clearance cookie not found",
                        challenge,
                        canRetry = true
                    )
                } else {
                    BypassResult.Failed(
                        "Challenge not solved - still on Cloudflare page",
                        challenge,
                        canRetry = true
                    )
                }
            }
        } catch (e: Exception) {
            BypassResult.Failed(
                "WebView bypass error: ${e.message}",
                challenge,
                canRetry = true
            )
        }
    }

    /**
     * Check if the page content is still a Cloudflare challenge page
     */
    private fun isCloudflareChallengePage(body: String): Boolean {
        val challengeIndicators = listOf(
            "Just a moment...",
            "Checking your browser",
            "cf-browser-verification",
            "_cf_chl_opt",
            "challenge-platform",
            "cf-please-wait"
        )
        
        return challengeIndicators.any { body.contains(it, ignoreCase = true) }
    }
    
    /**
     * Get the fingerprint evasion script to inject
     */
    fun getEvasionScript(): String = FingerprintEvasionScripts.fullEvasion
    
    /**
     * Get minimal evasion script for faster loading
     */
    fun getMinimalEvasionScript(): String = FingerprintEvasionScripts.minimalEvasion
}

/**
 * Factory for creating WebView bypass strategy
 * Platform implementations should provide the actual BrowserEngine
 */
object WebViewBypassStrategyFactory {
    
    /**
     * Create a WebView bypass strategy if browser engine is available
     */
    fun createIfAvailable(
        browserEngine: BrowserEngineInterface,
        fingerprintManager: FingerprintManager? = null
    ): WebViewBypassStrategy? {
        return if (browserEngine.isAvailable()) {
            WebViewBypassStrategy(browserEngine, fingerprintManager)
        } else {
            null
        }
    }
}
