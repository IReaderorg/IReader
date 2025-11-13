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
        
        // Validate primary credentials
        if (usersUrl.isEmpty() || usersKey.isEmpty()) {
            throw IllegalStateException(
                "Supabase credentials not configured. " +
                "Please add your credentials to local.properties (Android) or config.properties (Desktop). " +
                "Alternatively, configure a custom instance in Settings → Supabase Configuration. " +
                "See SECURE_CONFIGURATION_GUIDE.md for instructions."
            )
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
    
    // Supabase client (backward compatibility - returns primary/users client)
    single<SupabaseClient> {
        val provider = get<ireader.domain.data.repository.SupabaseClientProvider>()
        (provider as ireader.data.remote.SupabaseClientProviderImpl)
            .getSupabaseClient(ireader.domain.models.remote.SupabaseEndpoint.USERS)
    }
    
    // Sync queue
    single { SyncQueue() }
    
    // Retry policy
    single { RetryPolicy() }
    
    // Remote cache
    single { RemoteCache() }
    
    // Remote repository
    single<RemoteRepository> {
        SupabaseRemoteRepository(
            supabaseClient = get(),
            syncQueue = get(),
            retryPolicy = get(),
            cache = get()
        )
    }
}
