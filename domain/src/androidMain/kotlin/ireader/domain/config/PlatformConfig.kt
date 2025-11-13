package ireader.domain.config

import org.ireader.app.BuildConfig

/**
 * Android platform configuration
 * Loads credentials from BuildConfig (which comes from local.properties or environment variables)
 */
actual object PlatformConfig {
    actual fun getSupabaseUrl(): String = BuildConfig.SUPABASE_URL
    actual fun getSupabaseAnonKey(): String = BuildConfig.SUPABASE_ANON_KEY
}
