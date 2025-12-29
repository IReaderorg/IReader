package ireader.domain.plugins

import ireader.core.http.cloudflare.BypassCookie as CoreBypassCookie
import ireader.core.http.cloudflare.BypassRequest as CoreBypassRequest
import ireader.core.http.cloudflare.CloudflareBypassPluginManager
import ireader.core.http.cloudflare.CloudflareBypassProvider
import ireader.core.http.cloudflare.CloudflareChallenge as CoreCloudflareChallenge
import ireader.core.http.cloudflare.PluginBypassResult
import ireader.core.util.createICoroutineScope
import ireader.plugin.api.CloudflareBypassPlugin
import ireader.plugin.api.BypassCookie as PluginBypassCookie
import ireader.plugin.api.BypassRequest as PluginBypassRequest
import ireader.plugin.api.BypassResponse
import ireader.plugin.api.CloudflareChallenge as PluginCloudflareChallenge
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Service that integrates feature plugins with the appropriate system managers.
 * 
 * This service:
 * - Listens to plugin changes from PluginManager
 * - Registers CloudflareBypassPlugin implementations with CloudflareBypassPluginManager
 * - Handles plugin enable/disable lifecycle
 */
class PluginIntegrationService(
    private val pluginManager: PluginManager,
    private val cloudflareBypassManager: CloudflareBypassPluginManager
) {
    private val scope = createICoroutineScope()
    private val registeredProviders = mutableMapOf<String, CloudflareBypassProvider>()
    
    /**
     * Start observing plugin changes and integrating them with system managers.
     */
    fun start() {
        scope.launch {
            pluginManager.pluginsFlow.collectLatest { plugins ->
                syncCloudflareBypassProviders(plugins)
            }
        }
        println("[PluginIntegrationService] Started")
    }
    
    /**
     * Sync CloudflareBypassPlugin implementations with CloudflareBypassPluginManager.
     */
    private fun syncCloudflareBypassProviders(plugins: List<PluginInfo>) {
        val enabledPluginIds = plugins.filter { it.status == PluginStatus.ENABLED }.map { it.id }.toSet()
        
        // Find CloudflareBypassPlugin implementations
        val bypassPlugins = plugins.filter { pluginInfo ->
            pluginInfo.status == PluginStatus.ENABLED && isCloudflareBypassPlugin(pluginInfo)
        }
        
        // Register new providers
        bypassPlugins.forEach { pluginInfo ->
            if (!registeredProviders.containsKey(pluginInfo.id)) {
                val plugin = pluginManager.getPlugin(pluginInfo.id)
                if (plugin is CloudflareBypassPlugin) {
                    val provider = CloudflareBypassPluginAdapter(pluginInfo.id, plugin)
                    cloudflareBypassManager.registerProvider(provider)
                    registeredProviders[pluginInfo.id] = provider
                    println("[PluginIntegrationService] Registered CloudflareBypassPlugin: ${pluginInfo.id}")
                }
            }
        }
        
        // Unregister removed/disabled providers
        val toRemove = registeredProviders.keys.filter { it !in enabledPluginIds }
        toRemove.forEach { pluginId ->
            cloudflareBypassManager.unregisterProvider(pluginId)
            registeredProviders.remove(pluginId)
            println("[PluginIntegrationService] Unregistered CloudflareBypassPlugin: $pluginId")
        }
    }
    
    /**
     * Check if a plugin implements CloudflareBypassPlugin interface.
     */
    private fun isCloudflareBypassPlugin(pluginInfo: PluginInfo): Boolean {
        val plugin = pluginManager.getPlugin(pluginInfo.id)
        return plugin is CloudflareBypassPlugin
    }
}

/**
 * Adapter that wraps a CloudflareBypassPlugin (from plugin-api) to CloudflareBypassProvider (from source-api).
 * This bridges the plugin system with the HTTP client's bypass manager.
 */
private class CloudflareBypassPluginAdapter(
    override val id: String,
    private val plugin: CloudflareBypassPlugin
) : CloudflareBypassProvider {
    
    override val name: String = plugin.manifest.name
    override val priority: Int = plugin.priority
    
    override suspend fun canHandle(challenge: CoreCloudflareChallenge): Boolean {
        val pluginChallenge = challenge.toPluginChallenge()
        return plugin.canHandle(pluginChallenge)
    }
    
    override suspend fun isAvailable(): Boolean {
        return plugin.isAvailable()
    }
    
    override suspend fun bypass(request: CoreBypassRequest): PluginBypassResult {
        val pluginRequest = request.toPluginRequest()
        val response = plugin.bypass(pluginRequest)
        return response.toPluginBypassResult()
    }
    
    override fun getStatusDescription(): String {
        return plugin.getStatusDescription()
    }
}

// ==================== Type Conversion Extensions ====================

/**
 * Convert core CloudflareChallenge to plugin CloudflareChallenge.
 */
private fun CoreCloudflareChallenge.toPluginChallenge(): PluginCloudflareChallenge {
    return when (this) {
        CoreCloudflareChallenge.None -> PluginCloudflareChallenge.None
        is CoreCloudflareChallenge.JSChallenge -> PluginCloudflareChallenge.JSChallenge(rayId)
        is CoreCloudflareChallenge.CaptchaChallenge -> PluginCloudflareChallenge.CaptchaChallenge(siteKey, rayId)
        is CoreCloudflareChallenge.TurnstileChallenge -> PluginCloudflareChallenge.TurnstileChallenge(siteKey, rayId)
        is CoreCloudflareChallenge.ManagedChallenge -> PluginCloudflareChallenge.ManagedChallenge(rayId)
        is CoreCloudflareChallenge.BlockedIP -> PluginCloudflareChallenge.BlockedIP(rayId)
        is CoreCloudflareChallenge.RateLimited -> PluginCloudflareChallenge.RateLimited(retryAfterSeconds, rayId)
        is CoreCloudflareChallenge.Unknown -> PluginCloudflareChallenge.Unknown(0, hints)
    }
}

/**
 * Convert core BypassRequest to plugin BypassRequest.
 */
private fun CoreBypassRequest.toPluginRequest(): PluginBypassRequest {
    return PluginBypassRequest(
        url = url,
        challenge = PluginCloudflareChallenge.Unknown(0), // Challenge already checked in canHandle
        headers = headers,
        userAgent = userAgent,
        timeoutMs = timeoutMs,
        postData = postData
    )
}

/**
 * Convert plugin BypassResponse to core PluginBypassResult.
 */
private fun BypassResponse.toPluginBypassResult(): PluginBypassResult {
    return when (this) {
        is BypassResponse.Success -> PluginBypassResult.Success(
            content = content,
            cookies = cookies.map { it.toCoreBypassCookie() },
            userAgent = userAgent,
            finalUrl = finalUrl,
            statusCode = statusCode
        )
        is BypassResponse.Failed -> PluginBypassResult.Failed(
            reason = reason,
            canRetry = canRetry
        )
        is BypassResponse.UserInteractionRequired -> PluginBypassResult.UserInteractionRequired(
            message = message,
            verificationUrl = verificationUrl
        )
        is BypassResponse.ServiceUnavailable -> PluginBypassResult.ServiceUnavailable(
            reason = reason,
            setupInstructions = setupInstructions
        )
    }
}

/**
 * Convert plugin BypassCookie to core BypassCookie.
 */
private fun PluginBypassCookie.toCoreBypassCookie(): CoreBypassCookie {
    return CoreBypassCookie(
        name = name,
        value = value,
        domain = domain,
        path = path,
        expiresAt = expiresAt,
        secure = secure,
        httpOnly = httpOnly
    )
}
