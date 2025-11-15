package ireader.data.remote

import ireader.domain.data.repository.SupabaseClientProvider
import ireader.domain.models.remote.SupabaseEndpoint

/**
 * No-op implementation of SupabaseClientProvider used when credentials are not configured.
 * This allows the app to run without Supabase sync functionality.
 */
class NoOpSupabaseClientProvider : SupabaseClientProvider {
    
    override fun getClient(endpoint: SupabaseEndpoint): Any {
        throw UnsupportedOperationException(
            "Supabase is not configured. Please configure credentials in Settings → Supabase Configuration."
        )
    }
    
    override fun isEndpointAvailable(endpoint: SupabaseEndpoint): Boolean = false
}
