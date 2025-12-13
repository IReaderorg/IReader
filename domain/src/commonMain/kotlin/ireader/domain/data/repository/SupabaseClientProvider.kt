package ireader.domain.data.repository

import ireader.domain.models.remote.SupabaseEndpoint

/**
 * Interface for providing Supabase clients for different endpoints
 * Allows multiple Supabase instances for data distribution
 */
interface SupabaseClientProvider {
    
    /**
     * Get a Supabase client for a specific endpoint
     * @param endpoint The endpoint type (USERS, BOOKS, PROGRESS, etc.)
     * @return The Supabase client for that endpoint
     */
    fun getClient(endpoint: SupabaseEndpoint): Any
    
    /**
     * Check if an endpoint is available and configured
     * @param endpoint The endpoint type to check
     * @return true if the endpoint is configured and enabled
     */
    fun isEndpointAvailable(endpoint: SupabaseEndpoint): Boolean
    
    /**
     * Get the default client (for backward compatibility)
     * @return The default Supabase client (usually USERS endpoint)
     */
    fun getDefaultClient(): Any {
        return getClient(SupabaseEndpoint.USERS)
    }
    
    /**
     * Get the Supabase URL for security validation
     * Used to verify app is using official backend
     */
    fun getSupabaseUrl(): String
}
