package ireader.domain.http

import ireader.core.http.cloudflare.BypassCookie
import ireader.core.http.cloudflare.BypassRequest
import ireader.core.http.cloudflare.CloudflareBypassProvider
import ireader.core.http.cloudflare.CloudflareChallenge
import ireader.core.http.cloudflare.PluginBypassResult
import ireader.plugin.api.CloudflareBypassPlugin
import ireader.plugin.api.BypassResponse as PluginBypassResponse
import ireader.plugin.api.BypassRequest as PluginBypassRequest
import ireader.plugin.api.BypassCookie as PluginBypassCookie
import ireader.plugin.api.CloudflareChallenge as PluginCloudflareChallenge

/**
 * Adapter that wraps a [CloudflareBypassPlugin] (from plugin-api) 
 * to work with the internal [CloudflareBypassProvider] interface.
 * 
 * This ensures backward compatibility with existing external plugins.
 */
class CloudflareBypassPluginAdapter(
    private val plugin: CloudflareBypassPlugin
) : CloudflareBypassProvider {
    
    override val id: String = plugin.manifest.id
    
    override val name: String = plugin.manifest.name
    
    override val priority: Int = plugin.priority
    
    override suspend fun canHandle(challenge: CloudflareChallenge): Boolean {
        return plugin.canHandle(challenge.toPluginChallenge())
    }
    
    override suspend fun isAvailable(): Boolean {
        return plugin.isAvailable()
    }
    
    override suspend fun bypass(request: BypassRequest): PluginBypassResult {
        val pluginRequest = PluginBypassRequest(
            url = request.url,
            challenge = PluginCloudflareChallenge.JSChallenge(), // Default challenge type
            headers = request.headers,
            userAgent = request.userAgent,
            timeoutMs = request.timeoutMs,
            cookiesOnly = request.cookiesOnly,
            postData = request.postData
        )
        
        return when (val response = plugin.bypass(pluginRequest)) {
            is PluginBypassResponse.Success -> PluginBypassResult.Success(
                content = response.content,
                cookies = response.cookies.map { it.toInternalCookie() },
                userAgent = response.userAgent,
                finalUrl = response.finalUrl,
                statusCode = response.statusCode
            )
            is PluginBypassResponse.Failed -> PluginBypassResult.Failed(
                reason = response.reason,
                canRetry = response.canRetry,
                retryAfterMs = response.retryAfterMs
            )
            is PluginBypassResponse.UserInteractionRequired -> PluginBypassResult.UserInteractionRequired(
                message = response.message,
                verificationUrl = response.verificationUrl
            )
            is PluginBypassResponse.ServiceUnavailable -> PluginBypassResult.ServiceUnavailable(
                reason = response.reason,
                setupInstructions = response.setupInstructions
            )
        }
    }
    
    override fun getStatusDescription(): String {
        return plugin.getStatusDescription()
    }
    
    /**
     * Get the underlying plugin for configuration access.
     */
    fun getPlugin(): CloudflareBypassPlugin = plugin
}

/**
 * Convert internal CloudflareChallenge to plugin-api CloudflareChallenge.
 */
private fun CloudflareChallenge.toPluginChallenge(): PluginCloudflareChallenge {
    return when (this) {
        is CloudflareChallenge.None -> PluginCloudflareChallenge.None
        is CloudflareChallenge.JSChallenge -> PluginCloudflareChallenge.JSChallenge(rayId)
        is CloudflareChallenge.CaptchaChallenge -> PluginCloudflareChallenge.CaptchaChallenge(siteKey, rayId)
        is CloudflareChallenge.TurnstileChallenge -> PluginCloudflareChallenge.TurnstileChallenge(siteKey, rayId)
        is CloudflareChallenge.ManagedChallenge -> PluginCloudflareChallenge.ManagedChallenge(rayId)
        is CloudflareChallenge.BlockedIP -> PluginCloudflareChallenge.BlockedIP(rayId)
        is CloudflareChallenge.RateLimited -> PluginCloudflareChallenge.RateLimited(retryAfterSeconds, rayId)
        is CloudflareChallenge.Unknown -> PluginCloudflareChallenge.Unknown(
            statusCode = 503, // Default status code for unknown challenges
            hints = hints
        )
    }
}

/**
 * Convert plugin-api BypassCookie to internal BypassCookie.
 */
private fun PluginBypassCookie.toInternalCookie(): BypassCookie {
    return BypassCookie(
        name = name,
        value = value,
        domain = domain,
        path = path,
        expiresAt = expiresAt,
        secure = secure,
        httpOnly = httpOnly
    )
}

/**
 * Extension function to register external CloudflareBypassPlugin with the manager.
 */
fun ireader.core.http.cloudflare.CloudflareBypassPluginManager.registerPlugin(
    plugin: CloudflareBypassPlugin
) {
    registerProvider(CloudflareBypassPluginAdapter(plugin))
}
