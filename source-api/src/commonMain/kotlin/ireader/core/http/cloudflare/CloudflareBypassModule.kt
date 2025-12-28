package ireader.core.http.cloudflare

import io.ktor.client.HttpClient
import ireader.core.log.Log
import org.koin.dsl.module

/**
 * Koin module for Cloudflare bypass functionality.
 */
val cloudflareBypassModule = module {
    // CloudflareBypassPluginManager as singleton
    single { 
        Log.debug { "[CloudflareBypassModule] Creating CloudflareBypassPluginManager" }
        CloudflareBypassPluginManager() 
    }
    
    // FlareSolverr URL preference
    factory {
        val preferenceStore: ireader.core.prefs.PreferenceStore = get()
        preferenceStore.getString("flaresolverr_url", "http://localhost:8191/v1")
    }
    
    // FlareSolverrProvider - built-in bypass provider (lazy creation)
    single { 
        Log.debug { "[CloudflareBypassModule] Creating FlareSolverrProvider" }
        val httpClient: HttpClient = get()
        val preferenceStore: ireader.core.prefs.PreferenceStore = get()
        val urlPref = preferenceStore.getString("flaresolverr_url", "http://localhost:8191/v1")
        FlareSolverrProvider(
            httpClient = httpClient,
            getServerUrl = { urlPref.get() }
        )
    }
}

/**
 * Initialize Cloudflare bypass by registering the built-in provider.
 * Call this after Koin modules are loaded.
 */
fun initializeCloudflareBypass(
    manager: CloudflareBypassPluginManager,
    provider: FlareSolverrProvider
) {
    Log.info { "[CloudflareBypassModule] Registering FlareSolverrProvider" }
    manager.registerProvider(provider)
}
