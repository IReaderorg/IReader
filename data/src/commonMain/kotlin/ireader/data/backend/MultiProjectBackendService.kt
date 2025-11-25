package ireader.data.backend

import ireader.data.remote.MultiSupabaseClientProvider
import kotlinx.serialization.json.JsonElement

/**
 * Backend service implementation for multi-project Supabase setup
 * 
 * This service routes requests to the appropriate Supabase client based on the table name:
 * - users → authClient (Project 1)
 * - reading_progress → readingClient (Project 2)
 * - synced_books → libraryClient (Project 3)
 * - book_reviews → bookReviewsClient (Project 4)
 * - chapter_reviews → chapterReviewsClient (Project 5)
 * - badges, user_badges, payment_proofs, nft_wallets → badgesClient (Project 6)
 * - leaderboard → analyticsClient (Project 7)
 */
class MultiProjectBackendService(
    private val multiProvider: MultiSupabaseClientProvider
) : BackendService {
    
    // Delegate services for each project
    private val authService by lazy { SupabaseBackendService(multiProvider.authClient) }
    private val readingService by lazy { SupabaseBackendService(multiProvider.readingClient) }
    private val libraryService by lazy { SupabaseBackendService(multiProvider.libraryClient) }
    private val bookReviewsService by lazy { SupabaseBackendService(multiProvider.bookReviewsClient) }
    private val chapterReviewsService by lazy { SupabaseBackendService(multiProvider.chapterReviewsClient) }
    private val badgesService by lazy { SupabaseBackendService(multiProvider.badgesClient) }
    private val analyticsService by lazy { SupabaseBackendService(multiProvider.analyticsClient) }
    
    /**
     * Route to the appropriate service based on table name
     */
    private fun getServiceForTable(table: String): BackendService {
        return when (table) {
            // Project 1 - Auth
            "users" -> authService
            
            // Project 2 - Reading
            "reading_progress" -> readingService
            
            // Project 3 - Library
            "synced_books" -> libraryService
            
            // Project 4 - Book Reviews
            "book_reviews" -> bookReviewsService
            
            // Project 5 - Chapter Reviews
            "chapter_reviews" -> chapterReviewsService
            
            // Project 6 - Badges
            "badges", "user_badges", "payment_proofs", "nft_wallets" -> badgesService
            
            // Project 7 - Analytics
            "leaderboard" -> analyticsService
            
            // Default to auth service for unknown tables
            else -> authService
        }
    }
    
    /**
     * Route RPC functions to the appropriate service
     * Most RPC functions are in the badges project
     */
    private fun getServiceForRpc(function: String): BackendService {
        return when {
            // Badge-related functions → Project 6
            function.contains("badge", ignoreCase = true) -> badgesService
            
            // Leaderboard functions → Project 7
            function.contains("leaderboard", ignoreCase = true) -> analyticsService
            
            // User functions → Project 1
            function.contains("user", ignoreCase = true) -> authService
            
            // Default to auth service
            else -> authService
        }
    }
    
    override suspend fun query(
        table: String,
        filters: Map<String, Any>,
        columns: String,
        orderBy: String?,
        ascending: Boolean,
        limit: Int?,
        offset: Int?
    ): Result<List<JsonElement>> {
        return getServiceForTable(table).query(table, filters, columns, orderBy, ascending, limit, offset)
    }
    
    override suspend fun insert(
        table: String,
        data: JsonElement,
        returning: Boolean
    ): Result<JsonElement?> {
        return getServiceForTable(table).insert(table, data, returning)
    }
    
    override suspend fun update(
        table: String,
        filters: Map<String, Any>,
        data: JsonElement,
        returning: Boolean
    ): Result<JsonElement?> {
        return getServiceForTable(table).update(table, filters, data, returning)
    }
    
    override suspend fun delete(
        table: String,
        filters: Map<String, Any>
    ): Result<Unit> {
        return getServiceForTable(table).delete(table, filters)
    }
    
    override suspend fun rpc(
        function: String,
        parameters: Map<String, Any>
    ): Result<JsonElement> {
        return getServiceForRpc(function).rpc(function, parameters)
    }
    
    override suspend fun upsert(
        table: String,
        data: JsonElement,
        onConflict: String?,
        returning: Boolean
    ): Result<JsonElement?> {
        return getServiceForTable(table).upsert(table, data, onConflict, returning)
    }
}
