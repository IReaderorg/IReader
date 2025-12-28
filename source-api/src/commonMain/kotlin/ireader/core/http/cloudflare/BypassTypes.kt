package ireader.core.http.cloudflare

import kotlinx.serialization.Serializable

/**
 * Cookie obtained from Cloudflare bypass.
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

/**
 * Request for Cloudflare bypass.
 */
@Serializable
data class BypassRequest(
    /** URL to fetch */
    val url: String,
    /** Request headers to include */
    val headers: Map<String, String> = emptyMap(),
    /** User agent to use */
    val userAgent: String? = null,
    /** Maximum timeout in milliseconds */
    val timeoutMs: Long = 60000,
    /** Whether to only return cookies (not page content) */
    val cookiesOnly: Boolean = false,
    /** Optional POST data */
    val postData: String? = null
)

/**
 * Response from Cloudflare bypass attempt (for plugin-based bypass).
 */
@Serializable
sealed class PluginBypassResult {
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
    ) : PluginBypassResult()
    
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
    ) : PluginBypassResult()
    
    /**
     * User interaction required (e.g., CAPTCHA).
     */
    @Serializable
    data class UserInteractionRequired(
        /** Description of required action */
        val message: String,
        /** URL to open for manual verification */
        val verificationUrl: String? = null
    ) : PluginBypassResult()
    
    /**
     * Service not available.
     */
    @Serializable
    data class ServiceUnavailable(
        /** Reason service is unavailable */
        val reason: String,
        /** Setup instructions */
        val setupInstructions: String? = null
    ) : PluginBypassResult()
}

/**
 * Interface for Cloudflare bypass strategies (plugin-based).
 * 
 * Implementations can provide different bypass methods:
 * - FlareSolverr (external service)
 * - Browser automation
 * - Cookie replay
 */
interface CloudflareBypassProvider {
    /** Unique identifier */
    val id: String
    
    /** Display name */
    val name: String
    
    /** Priority (higher = tried first) */
    val priority: Int
    
    /**
     * Check if this provider can handle the given challenge.
     */
    suspend fun canHandle(challenge: CloudflareChallenge): Boolean
    
    /**
     * Check if the provider is available and configured.
     */
    suspend fun isAvailable(): Boolean
    
    /**
     * Attempt to bypass Cloudflare protection.
     */
    suspend fun bypass(request: BypassRequest): PluginBypassResult
    
    /**
     * Get current status description.
     */
    fun getStatusDescription(): String = "Not configured"
}
