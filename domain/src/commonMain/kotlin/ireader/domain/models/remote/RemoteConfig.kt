package ireader.domain.models.remote

/**
 * Configuration for remote backend connection
 */
data class RemoteConfig(
    val supabaseUrl: String,
    val supabaseAnonKey: String,
    val enableRealtime: Boolean = true,
    val syncIntervalMs: Long = 30_000
)

///**
// * Platform-specific function to load remote configuration
// * Implementations should load from BuildConfig (Android) or properties file (Desktop)
// */
//expect fun loadRemoteConfig(): RemoteConfig?
