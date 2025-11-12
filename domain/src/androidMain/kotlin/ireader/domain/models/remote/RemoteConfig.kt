package ireader.domain.models.remote

/**
 * Android implementation of loadRemoteConfig
 * Loads configuration from BuildConfig which is populated from local.properties
 * 
 * Note: Returns null if Supabase is not configured. This allows the app to run
 * without remote backend functionality.
 */
actual fun loadRemoteConfig(): RemoteConfig? {
    return try {
        // Try to load from BuildConfig using reflection to avoid compile-time dependency
        val buildConfigClass = Class.forName("org.ireader.app.BuildConfig")
        val urlField = buildConfigClass.getDeclaredField("SUPABASE_URL")
        val keyField = buildConfigClass.getDeclaredField("SUPABASE_ANON_KEY")
        
        val url = urlField.get(null) as? String ?: ""
        val key = keyField.get(null) as? String ?: ""
        
        // Return null if configuration is not set
        if (url.isBlank() || key.isBlank()) {
            null
        } else {
            RemoteConfig(
                supabaseUrl = url,
                supabaseAnonKey = key,
                enableRealtime = true,
                syncIntervalMs = 30_000
            )
        }
    } catch (e: Exception) {
        // BuildConfig fields don't exist or reflection failed
        // This is expected if Supabase is not configured
        null
    }
}
