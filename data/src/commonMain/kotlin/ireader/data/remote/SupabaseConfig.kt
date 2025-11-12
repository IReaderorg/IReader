package ireader.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.functions.Functions

/**
 * Configuration object for creating and managing Supabase client instances
 */
object SupabaseConfig {
    
    /**
     * Creates a configured Supabase client with all required modules
     * 
     * @param url The Supabase project URL
     * @param apiKey The Supabase anonymous API key
     * @return Configured SupabaseClient instance
     */
    fun createClient(url: String, apiKey: String): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = url,
            supabaseKey = apiKey
        ) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
            install(Functions)
        }
    }
}
