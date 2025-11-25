package ireader.domain.models.remote

import ireader.domain.config.PlatformConfig

///**
// * Desktop implementation of loadRemoteConfig
// * Loads configuration from PlatformConfig which checks:
// * 1. System properties (JVM args set by build.gradle.kts)
// * 2. Environment variables
// * 3. config.properties file via ConfigLoader
// */
//actual fun loadRemoteConfig(): RemoteConfig? {
//    return try {
//        val url = PlatformConfig.getSupabaseUrl()
//        val key = PlatformConfig.getSupabaseAnonKey()
//
//        // Return null if configuration is not set
//        if (url.isBlank() || key.isBlank()) {
//            println("Supabase not configured: url='$url', key=${if (key.isBlank()) "blank" else "set"}")
//            return null
//        }
//
//        println("Supabase configured: url='$url'")
//
//        RemoteConfig(
//            supabaseUrl = url,
//            supabaseAnonKey = key,
//            enableRealtime = true,
//            syncIntervalMs = 30_000
//        )
//    } catch (e: Exception) {
//        println("Failed to load remote config: ${e.message}")
//        e.printStackTrace()
//        null
//    }
//}
