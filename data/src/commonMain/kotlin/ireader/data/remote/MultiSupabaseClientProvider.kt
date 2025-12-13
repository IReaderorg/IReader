package ireader.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.Realtime
import ireader.domain.data.repository.SupabaseClientProvider
import ireader.domain.models.remote.SupabaseEndpoint
import ireader.core.log.Log
import kotlinx.serialization.Serializable

/**
 * Multi-Supabase Client Provider for 7-project setup
 * 
 * This provider manages connections to 7 separate Supabase projects:
 * 1. Auth - User authentication and profiles
 * 2. Reading - Reading progress tracking
 * 3. Library - Synced books library
 * 4. Book Reviews - Book reviews
 * 5. Chapter Reviews - Chapter reviews
 * 6. Badges - Badge system and NFT integration
 * 7. Analytics - Leaderboard and statistics
 * 
 * Total Storage: 3.5GB (7 × 500MB)
 */
class MultiSupabaseClientProvider(
    // Project 1 - Auth
    private val authUrl: String,
    private val authKey: String,
    // Project 2 - Reading
    private val readingUrl: String,
    private val readingKey: String,
    // Project 3 - Library
    private val libraryUrl: String,
    private val libraryKey: String,
    // Project 4 - Book Reviews
    private val bookReviewsUrl: String,
    private val bookReviewsKey: String,
    // Project 5 - Chapter Reviews
    private val chapterReviewsUrl: String,
    private val chapterReviewsKey: String,
    // Project 6 - Badges
    private val badgesUrl: String,
    private val badgesKey: String,
    // Project 7 - Analytics
    private val analyticsUrl: String,
    private val analyticsKey: String
) : SupabaseClientProvider {
    
    /**
     * Project 1: Auth Database
     * Contains: users
     */
    val authClient: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = authUrl,
            supabaseKey = authKey
        ) {
            install(Auth)
            install(Postgrest)
        }
    }
    
    /**
     * Project 2: Reading Database
     * Contains: reading_progress
     */
    val readingClient: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = readingUrl,
            supabaseKey = readingKey
        ) {
            install(Postgrest)
        }
    }
    
    /**
     * Project 3: Library Database
     * Contains: synced_books
     */
    val libraryClient: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = libraryUrl,
            supabaseKey = libraryKey
        ) {
            install(Postgrest)
        }
    }
    
    /**
     * Project 4: Book Reviews Database
     * Contains: book_reviews
     * Note: Requires Auth plugin for user-specific review operations
     */
    val bookReviewsClient: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = bookReviewsUrl,
            supabaseKey = bookReviewsKey
        ) {
            install(Auth)
            install(Postgrest)
        }
    }
    
    /**
     * Project 5: Chapter Reviews Database
     * Contains: chapter_reviews
     * Note: Requires Auth plugin for user-specific review operations
     */
    val chapterReviewsClient: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = chapterReviewsUrl,
            supabaseKey = chapterReviewsKey
        ) {
            install(Auth)
            install(Postgrest)
        }
    }
    
    /**
     * Project 6: Badges Database
     * Contains: badges, user_badges, payment_proofs, nft_wallets
     * Note: Requires Auth plugin for user-specific badge operations
     */
    val badgesClient: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = badgesUrl,
            supabaseKey = badgesKey
        ) {
            install(Auth)
            install(Postgrest)
        }
    }
    
    /**
     * Project 7: Analytics Database
     * Contains: leaderboard
     */
    val analyticsClient: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = analyticsUrl,
            supabaseKey = analyticsKey
        ) {
            install(Postgrest)
            install(Realtime) // Optional: for live leaderboard updates
        }
    }
    
    override fun getClient(endpoint: SupabaseEndpoint): Any {
        return when (endpoint) {
            SupabaseEndpoint.USERS -> authClient
            SupabaseEndpoint.PROGRESS -> readingClient
            SupabaseEndpoint.BOOKS -> libraryClient
            SupabaseEndpoint.REVIEWS -> bookReviewsClient // Book reviews
            SupabaseEndpoint.COMMUNITY -> analyticsClient // Leaderboard
            else -> authClient // Default fallback
        }
    }
    
    override fun isEndpointAvailable(endpoint: SupabaseEndpoint): Boolean {
        return try {
            when (endpoint) {
                SupabaseEndpoint.USERS -> authUrl.isNotEmpty() && authKey.isNotEmpty()
                SupabaseEndpoint.PROGRESS -> readingUrl.isNotEmpty() && readingKey.isNotEmpty()
                SupabaseEndpoint.BOOKS -> libraryUrl.isNotEmpty() && libraryKey.isNotEmpty()
                SupabaseEndpoint.REVIEWS -> bookReviewsUrl.isNotEmpty() && bookReviewsKey.isNotEmpty()
                SupabaseEndpoint.COMMUNITY -> analyticsUrl.isNotEmpty() && analyticsKey.isNotEmpty()
                else -> false
            }
        } catch (e: Exception) {
            Log.error(e, "Failed to check endpoint availability for $endpoint")
            false
        }
    }
    
    override fun getSupabaseUrl(): String = authUrl
    
    /**
     * Get the current authenticated user ID as a string
     * This is used to sync user_id across all projects
     */
    suspend fun getCurrentUserId(): String? {
        return try {
            authClient.auth.currentUserOrNull()?.id
        } catch (e: Exception) {
            Log.error(e, "Failed to get current user ID")
            null
        }
    }
    
    /**
     * Get the current username
     * Note: This is a simplified version. For production, implement proper user profile fetching.
     */
    suspend fun getCurrentUsername(): String? {
        val userId = getCurrentUserId() ?: return null
        return try {
            // Simplified: Return a default username based on user ID
            // In production, fetch from the users table in authClient
            "User_${userId.take(8)}"
        } catch (e: Exception) {
            Log.error(e, "Failed to get current username")
            null
        }
    }
    
    /**
     * Close all connections (must be called from a coroutine)
     */
    suspend fun closeAll() {
        try {
            authClient.close()
            readingClient.close()
            libraryClient.close()
            bookReviewsClient.close()
            chapterReviewsClient.close()
            badgesClient.close()
            analyticsClient.close()
            Log.info("Closed all Supabase clients")
        } catch (e: Exception) {
            Log.error(e, "Failed to close Supabase clients")
        }
    }
}

/**
 * Simple user profile data class
 */
@Serializable
data class UserProfile(
    val id: String,
    val email: String,
    val username: String?
)
