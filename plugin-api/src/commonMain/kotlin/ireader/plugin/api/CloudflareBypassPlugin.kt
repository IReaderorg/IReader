package ireader.plugin.api

import kotlinx.serialization.Serializable

/**
 * Plugin interface for Cloudflare bypass strategies.
 * 
 * These plugins provide different methods to bypass Cloudflare protection:
 * - FlareSolverr (external service)
 * - Browser automation services
 * - Custom bypass implementations
 * 
 * Example:
 * ```kotlin
 * class FlareSolverrPlugin : CloudflareBypassPlugin {
 *     override val manifest = PluginManifest(
 *         id = "com.example.flaresolverr",
 *         name = "FlareSolverr Bypass",
 *         type = PluginType.CLOUDFLARE_BYPASS,
 *         permissions = listOf(PluginPermission.NETWORK),
 *     )
 *     
 *     override val priority = 100
 *     
 *     override suspend fun canHandle(challenge: CloudflareChallenge): Boolean {
 *         return isServiceAvailable() && challenge !is CloudflareChallenge.BlockedIP
 *     }
 *     
 *     override suspend fun bypass(request: BypassRequest): BypassResponse {
 *         // Implement FlareSolverr communication
 *     }
 * }
 * ```
 */
interface CloudflareBypassPlugin : Plugin {
    /**
     * Priority of this bypass strategy.
     * Higher priority strategies are tried first.
     * Recommended ranges:
     * - 100+: Primary strategies (FlareSolverr, browser automation)
     * - 50-99: Secondary strategies (cookie replay)
     * - 1-49: Fallback strategies
     */
    val priority: Int
    
    /**
     * Check if this plugin can handle the given challenge type.
     * 
     * @param challenge The detected Cloudflare challenge
     * @return true if this plugin can attempt to bypass the challenge
     */
    suspend fun canHandle(challenge: CloudflareChallenge): Boolean
    
    /**
     * Check if the bypass service is available and configured.
     * 
     * @return true if the service is ready to use
     */
    suspend fun isAvailable(): Boolean
    
    /**
     * Attempt to bypass Cloudflare protection.
     * 
     * @param request The bypass request containing URL and configuration
     * @return The bypass response with cookies or error
     */
    suspend fun bypass(request: BypassRequest): BypassResponse
    
    /**
     * Get configuration screen for this plugin (optional).
     * 
     * @return Plugin screen for configuration, or null
     */
    fun getConfigurationScreen(): PluginScreen? = null
    
    /**
     * Get current configuration status.
     * 
     * @return Human-readable status string
     */
    fun getStatusDescription(): String = "Not configured"
}

/**
 * Types of Cloudflare challenges that can be detected.
 */
@Serializable
sealed class CloudflareChallenge {
    /** No Cloudflare protection detected */
    @Serializable
    object None : CloudflareChallenge()
    
    /** JavaScript challenge (automatic, usually resolves in ~5 seconds) */
    @Serializable
    data class JSChallenge(val rayId: String? = null) : CloudflareChallenge()
    
    /** CAPTCHA challenge requiring user interaction */
    @Serializable
    data class CaptchaChallenge(
        val siteKey: String? = null,
        val rayId: String? = null
    ) : CloudflareChallenge()
    
    /** Turnstile challenge (Cloudflare's newer CAPTCHA alternative) */
    @Serializable
    data class TurnstileChallenge(
        val siteKey: String? = null,
        val rayId: String? = null
    ) : CloudflareChallenge()
    
    /** Managed challenge (interactive verification) */
    @Serializable
    data class ManagedChallenge(val rayId: String? = null) : CloudflareChallenge()
    
    /** IP has been blocked */
    @Serializable
    data class BlockedIP(val rayId: String? = null) : CloudflareChallenge()
    
    /** Rate limited */
    @Serializable
    data class RateLimited(
        val retryAfterSeconds: Long? = null,
        val rayId: String? = null
    ) : CloudflareChallenge()
    
    /** Unknown challenge type */
    @Serializable
    data class Unknown(
        val statusCode: Int,
        val hints: List<String> = emptyList()
    ) : CloudflareChallenge()
}

/**
 * Request for Cloudflare bypass.
 */
@Serializable
data class BypassRequest(
    /** URL to fetch */
    val url: String,
    /** Detected challenge type */
    val challenge: CloudflareChallenge,
    /** Request headers to include */
    val headers: Map<String, String> = emptyMap(),
    /** User agent to use */
    val userAgent: String? = null,
    /** Maximum timeout in milliseconds */
    val timeoutMs: Long = 60000,
    /** Whether to only return cookies (not page content) */
    val cookiesOnly: Boolean = false,
    /** Optional POST data */
    val postData: String? = null,
    /** Optional proxy configuration */
    val proxy: ProxyConfig? = null
)

/**
 * Proxy configuration for bypass requests.
 */
@Serializable
data class ProxyConfig(
    val host: String,
    val port: Int,
    val username: String? = null,
    val password: String? = null,
    val type: ProxyType = ProxyType.HTTP
)

@Serializable
enum class ProxyType {
    HTTP, HTTPS, SOCKS4, SOCKS5
}

/**
 * Response from Cloudflare bypass attempt.
 */
@Serializable
sealed class BypassResponse {
    /**
     * Bypass succeeded.
     */
    @Serializable
    data class Success(
        /** Page content (if not cookiesOnly) */
        val content: String = "",
        /** Cookies obtained from bypass */
        val cookies: List<BypassCookie>,
        /** User agent used (important for cookie validity) */
        val userAgent: String,
        /** Final URL after redirects */
        val finalUrl: String? = null,
        /** HTTP status code */
        val statusCode: Int = 200
    ) : BypassResponse()
    
    /**
     * Bypass failed.
     */
    @Serializable
    data class Failed(
        /** Error message */
        val reason: String,
        /** Whether retry might succeed */
        val canRetry: Boolean = false,
        /** Suggested wait time before retry (ms) */
        val retryAfterMs: Long? = null
    ) : BypassResponse()
    
    /**
     * User interaction required (e.g., CAPTCHA).
     */
    @Serializable
    data class UserInteractionRequired(
        /** Description of required action */
        val message: String,
        /** URL to open for manual verification */
        val verificationUrl: String? = null
    ) : BypassResponse()
    
    /**
     * Service not available.
     */
    @Serializable
    data class ServiceUnavailable(
        /** Reason service is unavailable */
        val reason: String,
        /** Setup instructions */
        val setupInstructions: String? = null
    ) : BypassResponse()
}

/**
 * Cookie obtained from bypass.
 */
@Serializable
data class BypassCookie(
    val name: String,
    val value: String,
    val domain: String,
    val path: String = "/",
    val expiresAt: Long = 0,
    val secure: Boolean = false,
    val httpOnly: Boolean = false
) {
    /**
     * Check if this is a Cloudflare clearance cookie.
     */
    val isClearanceCookie: Boolean
        get() = name == "cf_clearance" || name == "__cf_bm"
}
