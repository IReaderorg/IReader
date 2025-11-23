package ireader.data.di

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import ireader.data.remote.RemoteCache
import ireader.data.remote.RetryPolicy
import ireader.data.remote.SupabaseRemoteRepository
import ireader.data.remote.SyncQueue
import ireader.domain.data.repository.RemoteRepository
import ireader.domain.preferences.prefs.SupabasePreferences
import org.koin.dsl.module

/**
 * Dependency injection module for remote/sync components
 */
val remoteModule = module {
    
    // Supabase Client Provider (Multi-endpoint support)
    single<ireader.domain.data.repository.SupabaseClientProvider> {
        val prefs = get<SupabasePreferences>()
        
        // Load credentials from platform-specific sources
        val platformUrl = try {
            ireader.domain.config.PlatformConfig.getSupabaseUrl()
        } catch (e: Exception) {
            ""
        }
        
        val platformKey = try {
            ireader.domain.config.PlatformConfig.getSupabaseAnonKey()
        } catch (e: Exception) {
            ""
        }
        
        // Load multi-endpoint platform configs
        val platformBooksUrl = try {
            ireader.domain.config.PlatformConfig.getSupabaseBooksUrl()
        } catch (e: Exception) {
            ""
        }
        
        val platformBooksKey = try {
            ireader.domain.config.PlatformConfig.getSupabaseBooksKey()
        } catch (e: Exception) {
            ""
        }
        
        val platformProgressUrl = try {
            ireader.domain.config.PlatformConfig.getSupabaseProgressUrl()
        } catch (e: Exception) {
            ""
        }
        
        val platformProgressKey = try {
            ireader.domain.config.PlatformConfig.getSupabaseProgressKey()
        } catch (e: Exception) {
            ""
        }
        
        // Use custom config if enabled, otherwise use platform config
        val useCustom = prefs.useCustomSupabase().get()
        val useMulti = prefs.useMultiEndpoint().get()
        
        // Primary endpoint (Users)
        val usersUrl = if (useCustom) {
            prefs.supabaseUrl().get()
        } else {
            platformUrl.ifEmpty { 
                ireader.domain.preferences.prefs.SupabasePreferences.DEFAULT_SUPABASE_URL 
            }
        }
        
        val usersKey = if (useCustom) {
            prefs.supabaseApiKey().get()
        } else {
            platformKey.ifEmpty { 
                ireader.domain.preferences.prefs.SupabasePreferences.DEFAULT_SUPABASE_API_KEY 
            }
        }
        
        // Validate primary credentials - if not configured, return null provider
        if (usersUrl.isEmpty() || usersKey.isEmpty() || 
            usersUrl == ireader.domain.preferences.prefs.SupabasePreferences.DEFAULT_SUPABASE_URL ||
            usersKey == ireader.domain.preferences.prefs.SupabasePreferences.DEFAULT_SUPABASE_API_KEY) {
            // Return a no-op provider when credentials are not configured
            return@single ireader.data.remote.NoOpSupabaseClientProvider()
        }
        
        // Create configuration
        val config = if (useMulti && useCustom) {
            // Multi-endpoint from user preferences
            val booksUrl = prefs.booksUrl().get()
            val booksKey = prefs.booksApiKey().get()
            val progressUrl = prefs.progressUrl().get()
            val progressKey = prefs.progressApiKey().get()
            
            ireader.domain.models.remote.SupabaseConfig(
                users = ireader.domain.models.remote.SupabaseEndpointConfig(usersUrl, usersKey, true),
                books = if (booksUrl.isNotEmpty() && booksKey.isNotEmpty()) {
                    ireader.domain.models.remote.SupabaseEndpointConfig(booksUrl, booksKey, true)
                } else null,
                progress = if (progressUrl.isNotEmpty() && progressKey.isNotEmpty()) {
                    ireader.domain.models.remote.SupabaseEndpointConfig(progressUrl, progressKey, true)
                } else null
            )
        } else if (!useCustom && platformBooksUrl.isNotEmpty() && platformBooksKey.isNotEmpty()) {
            // Multi-endpoint from platform config (BuildConfig/System properties)
            ireader.domain.models.remote.SupabaseConfig(
                users = ireader.domain.models.remote.SupabaseEndpointConfig(usersUrl, usersKey, true),
                books = ireader.domain.models.remote.SupabaseEndpointConfig(platformBooksUrl, platformBooksKey, true),
                progress = if (platformProgressUrl.isNotEmpty() && platformProgressKey.isNotEmpty()) {
                    ireader.domain.models.remote.SupabaseEndpointConfig(platformProgressUrl, platformProgressKey, true)
                } else null
            )
        } else {
            // Single endpoint (backward compatible)
            ireader.domain.models.remote.SupabaseConfig.createDefault(usersUrl, usersKey)
        }
        
        ireader.data.remote.SupabaseClientProviderImpl(config)
    }
    
    // Sync queue
    single { SyncQueue() }
    
    // Retry policy
    single { RetryPolicy() }
    
    // Remote cache
    single { RemoteCache() }
    
    // Backend Service (abstraction layer)
    single<ireader.data.backend.BackendService> {
        val provider = get<ireader.domain.data.repository.SupabaseClientProvider>()
        if (provider is ireader.data.remote.NoOpSupabaseClientProvider) {
            ireader.data.backend.NoOpBackendService()
        } else {
            val supabaseClient = (provider as ireader.data.remote.SupabaseClientProviderImpl)
                .getSupabaseClient(ireader.domain.models.remote.SupabaseEndpoint.USERS) as SupabaseClient
            ireader.data.backend.SupabaseBackendService(supabaseClient)
        }
    }
    
    // Auth Service (authentication abstraction)
    single<ireader.data.backend.AuthService> {
        val provider = get<ireader.domain.data.repository.SupabaseClientProvider>()
        if (provider is ireader.data.remote.NoOpSupabaseClientProvider) {
            ireader.data.backend.NoOpAuthService()
        } else {
            val supabaseClient = (provider as ireader.data.remote.SupabaseClientProviderImpl)
                .getSupabaseClient(ireader.domain.models.remote.SupabaseEndpoint.USERS) as SupabaseClient
            ireader.data.backend.SupabaseAuthService(supabaseClient)
        }
    }
    
    // Remote repository
    single<RemoteRepository> {
        val provider = get<ireader.domain.data.repository.SupabaseClientProvider>()
        if (provider is ireader.data.remote.NoOpSupabaseClientProvider) {
            ireader.data.remote.NoOpRemoteRepository()
        } else {
            val supabaseClient = (provider as ireader.data.remote.SupabaseClientProviderImpl)
                .getSupabaseClient(ireader.domain.models.remote.SupabaseEndpoint.USERS)
            SupabaseRemoteRepository(
                supabaseClient = supabaseClient,
                backendService = get(),
                syncQueue = get(),
                retryPolicy = get(),
                cache = get()
            )
        }
    }
}
