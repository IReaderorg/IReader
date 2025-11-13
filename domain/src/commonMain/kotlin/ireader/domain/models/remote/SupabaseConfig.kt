package ireader.domain.models.remote

import kotlinx.serialization.Serializable

/**
 * Configuration for a single Supabase endpoint
 */
@Serializable
data class SupabaseEndpointConfig(
    val url: String,
    val anonKey: String,
    val enabled: Boolean = true
)

/**
 * Complete Supabase configuration with multiple endpoints
 */
@Serializable
data class SupabaseConfig(
    val users: SupabaseEndpointConfig,
    val books: SupabaseEndpointConfig? = null,
    val progress: SupabaseEndpointConfig? = null,
    val reviews: SupabaseEndpointConfig? = null,
    val community: SupabaseEndpointConfig? = null
) {
    /**
     * Get configuration for a specific endpoint
     * Falls back to users endpoint if specific endpoint is not configured
     */
    fun getEndpointConfig(endpoint: SupabaseEndpoint): SupabaseEndpointConfig {
        return when (endpoint) {
            SupabaseEndpoint.USERS -> users
            SupabaseEndpoint.BOOKS -> books ?: users
            SupabaseEndpoint.PROGRESS -> progress ?: users
            SupabaseEndpoint.REVIEWS -> reviews ?: users
            SupabaseEndpoint.COMMUNITY -> community ?: users
        }
    }
    
    /**
     * Check if a specific endpoint is configured and enabled
     */
    fun isEndpointEnabled(endpoint: SupabaseEndpoint): Boolean {
        return getEndpointConfig(endpoint).enabled
    }
    
    companion object {
        /**
         * Create a default configuration with single endpoint
         * For backward compatibility
         */
        fun createDefault(url: String, anonKey: String): SupabaseConfig {
            return SupabaseConfig(
                users = SupabaseEndpointConfig(url, anonKey, true)
            )
        }
        
        /**
         * Create a multi-endpoint configuration
         */
        fun createMultiEndpoint(
            usersUrl: String,
            usersKey: String,
            booksUrl: String? = null,
            booksKey: String? = null,
            progressUrl: String? = null,
            progressKey: String? = null
        ): SupabaseConfig {
            return SupabaseConfig(
                users = SupabaseEndpointConfig(usersUrl, usersKey, true),
                books = if (booksUrl != null && booksKey != null) {
                    SupabaseEndpointConfig(booksUrl, booksKey, true)
                } else null,
                progress = if (progressUrl != null && progressKey != null) {
                    SupabaseEndpointConfig(progressUrl, progressKey, true)
                } else null
            )
        }
    }
}
