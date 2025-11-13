package ireader.domain.models.remote

/**
 * Enum representing different Supabase endpoints for data distribution
 * Each endpoint can point to a different Supabase project for scalability
 */
enum class SupabaseEndpoint {
    /**
     * Primary endpoint for user authentication and profile data
     */
    USERS,
    
    /**
     * Endpoint for book metadata and sync data
     */
    BOOKS,
    
    /**
     * Endpoint for reading progress and statistics
     */
    PROGRESS,
    
    /**
     * Endpoint for reviews and ratings (future)
     */
    REVIEWS,
    
    /**
     * Endpoint for community features (future)
     */
    COMMUNITY;
    
    companion object {
        /**
         * Get the default endpoint for backward compatibility
         */
        fun getDefault(): SupabaseEndpoint = USERS
    }
}
