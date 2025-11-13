package ireader.data.di

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
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
    
    // Supabase client
    single<SupabaseClient> {
        val prefs = get<SupabasePreferences>()
        
        // Load credentials from platform-specific sources
        // Android: BuildConfig (from local.properties or env vars)
        // Desktop: config.properties or env vars
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
        
        // Use custom config if enabled, otherwise use platform config
        val useCustom = prefs.useCustomSupabase().get()
        val url = if (useCustom) {
            prefs.supabaseUrl().get()
        } else {
            platformUrl.ifEmpty { 
                ireader.domain.preferences.prefs.SupabasePreferences.DEFAULT_SUPABASE_URL 
            }
        }
        
        val key = if (useCustom) {
            prefs.supabaseApiKey().get()
        } else {
            platformKey.ifEmpty { 
                ireader.domain.preferences.prefs.SupabasePreferences.DEFAULT_SUPABASE_API_KEY 
            }
        }
        
        // Validate credentials before creating client
        if (url.isEmpty() || key.isEmpty()) {
            throw IllegalStateException(
                "Supabase credentials not configured. " +
                "Please add your credentials to local.properties (Android) or config.properties (Desktop). " +
                "Alternatively, configure a custom instance in Settings → Supabase Configuration. " +
                "See SECURE_CONFIGURATION_GUIDE.md for instructions."
            )
        }
        
        createSupabaseClient(
            supabaseUrl = url,
            supabaseKey = key
        ) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
        }
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
