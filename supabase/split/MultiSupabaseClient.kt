/**
 * Multi-Supabase Client Helper (7 Projects Version)
 * 
 * This file provides a helper class to manage connections to 7 Supabase projects.
 * Use this when you've split your database across multiple free-tier projects.
 * 
 * Total Storage: 3.5GB (7 × 500MB)
 * 
 * Setup:
 * 1. Add environment variables for each project
 * 2. Initialize the MultiSupabaseClient in your app
 * 3. Use the appropriate client for each operation
 */

package ireader.data.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

/**
 * Manages connections to 7 Supabase projects
 */
class MultiSupabaseClient(
    private val authUrl: String,
    private val authKey: String,
    private val readingUrl: String,
    private val readingKey: String,
    private val libraryUrl: String,
    private val libraryKey: String,
    private val bookReviewsUrl: String,
    private val bookReviewsKey: String,
    private val chapterReviewsUrl: String,
    private val chapterReviewsKey: String,
    private val badgesUrl: String,
    private val badgesKey: String,
    private val analyticsUrl: String,
    private val analyticsKey: String
) {
    
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
     */
    val bookReviewsClient: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = bookReviewsUrl,
            supabaseKey = bookReviewsKey
        ) {
            install(Postgrest)
        }
    }
    
    /**
     * Project 5: Chapter Reviews Database
     * Contains: chapter_reviews
     */
    val chapterReviewsClient: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = chapterReviewsUrl,
            supabaseKey = chapterReviewsKey
        ) {
            install(Postgrest)
        }
    }
    
    /**
     * Project 6: Badges Database
     * Contains: badges, user_badges, payment_proofs, nft_wallets
     */
    val badgesClient: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = badgesUrl,
            supabaseKey = badgesKey
        ) {
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
    
    /**
     * Get the current authenticated user ID as a string
     * This is used to sync user_id across all projects
     */
    suspend fun getCurrentUserId(): String? {
        return authClient.auth.currentUserOrNull()?.id
    }
    
    /**
     * Get the current username
     */
    suspend fun getCurrentUsername(): String? {
        val userId = getCurrentUserId() ?: return null
        return try {
            authClient.postgrest["users"]
                .select {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingle<UserProfile>()
                .username
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Sign in user (only on auth project)
     */
    suspend fun signIn(email: String, password: String) {
        authClient.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }
    
    /**
     * Sign out user (only on auth project)
     */
    suspend fun signOut() {
        authClient.auth.signOut()
    }
    
    /**
     * Close all connections
     */
    fun close() {
        authClient.close()
        readingClient.close()
        libraryClient.close()
        bookReviewsClient.close()
        chapterReviewsClient.close()
        badgesClient.close()
        analyticsClient.close()
    }
}

/**
 * Simple user profile data class
 */
data class UserProfile(
    val id: String,
    val email: String,
    val username: String?
)

/**
 * Example usage in your app:
 * 
 * ```kotlin
 * // Initialize
 * val multiClient = MultiSupabaseClient(
 *     authUrl = BuildConfig.SUPABASE_AUTH_URL,
 *     authKey = BuildConfig.SUPABASE_AUTH_KEY,
 *     readingUrl = BuildConfig.SUPABASE_READING_URL,
 *     readingKey = BuildConfig.SUPABASE_READING_KEY,
 *     libraryUrl = BuildConfig.SUPABASE_LIBRARY_URL,
 *     libraryKey = BuildConfig.SUPABASE_LIBRARY_KEY,
 *     bookReviewsUrl = BuildConfig.SUPABASE_BOOK_REVIEWS_URL,
 *     bookReviewsKey = BuildConfig.SUPABASE_BOOK_REVIEWS_KEY,
 *     chapterReviewsUrl = BuildConfig.SUPABASE_CHAPTER_REVIEWS_URL,
 *     chapterReviewsKey = BuildConfig.SUPABASE_CHAPTER_REVIEWS_KEY,
 *     badgesUrl = BuildConfig.SUPABASE_BADGES_URL,
 *     badgesKey = BuildConfig.SUPABASE_BADGES_KEY,
 *     analyticsUrl = BuildConfig.SUPABASE_ANALYTICS_URL,
 *     analyticsKey = BuildConfig.SUPABASE_ANALYTICS_KEY
 * )
 * 
 * // Use auth client for user data
 * val user = multiClient.authClient.auth.currentUserOrNull()
 * 
 * // Use reading client for reading progress
 * val progress = multiClient.readingClient.postgrest["reading_progress"]
 *     .select()
 *     .decodeList<ReadingProgress>()
 * 
 * // Use library client for synced books
 * val books = multiClient.libraryClient.postgrest["synced_books"]
 *     .select()
 *     .decodeList<SyncedBook>()
 * 
 * // Use book reviews client
 * val bookReviews = multiClient.bookReviewsClient.postgrest["book_reviews"]
 *     .select()
 *     .decodeList<BookReview>()
 * 
 * // Use chapter reviews client
 * val chapterReviews = multiClient.chapterReviewsClient.postgrest["chapter_reviews"]
 *     .select()
 *     .decodeList<ChapterReview>()
 * 
 * // Use badges client
 * val badges = multiClient.badgesClient.postgrest["badges"]
 *     .select()
 *     .decodeList<Badge>()
 * 
 * // Use analytics client for leaderboard
 * val leaderboard = multiClient.analyticsClient.postgrest["leaderboard"]
 *     .select()
 *     .decodeList<LeaderboardEntry>()
 * ```
 */

/**
 * Repository pattern example for cross-project operations
 */
class CrossProjectRepository(private val multiClient: MultiSupabaseClient) {
    
    /**
     * Create a book review (requires data from auth and book reviews projects)
     */
    suspend fun createBookReview(
        bookTitle: String,
        rating: Int,
        reviewText: String
    ): Result<Unit> {
        return try {
            // Get user info from auth project
            val userId = multiClient.getCurrentUserId() 
                ?: return Result.failure(Exception("User not authenticated. Please sign in to continue."))
            val username = multiClient.getCurrentUsername() 
                ?: return Result.failure(Exception("User not found. Please update your profile with a username."))
            
            // Insert review in book reviews project
            multiClient.bookReviewsClient.postgrest["book_reviews"].insert(
                mapOf(
                    "user_id" to userId,
                    "username" to username,
                    "book_title" to bookTitle.lowercase().trim(),
                    "rating" to rating,
                    "review_text" to reviewText
                )
            )
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Create a chapter review (requires data from auth and chapter reviews projects)
     */
    suspend fun createChapterReview(
        bookTitle: String,
        chapterName: String,
        rating: Int,
        reviewText: String
    ): Result<Unit> {
        return try {
            // Get user info from auth project
            val userId = multiClient.getCurrentUserId() 
                ?: return Result.failure(Exception("User not authenticated. Please sign in to continue."))
            val username = multiClient.getCurrentUsername() 
                ?: return Result.failure(Exception("User not found. Please update your profile with a username."))
            
            // Insert review in chapter reviews project
            multiClient.chapterReviewsClient.postgrest["chapter_reviews"].insert(
                mapOf(
                    "user_id" to userId,
                    "username" to username,
                    "book_title" to bookTitle.lowercase().trim(),
                    "chapter_name" to chapterName.lowercase().trim(),
                    "rating" to rating,
                    "review_text" to reviewText
                )
            )
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update leaderboard (requires data from auth and analytics projects)
     */
    suspend fun updateLeaderboard(
        totalReadingTimeMinutes: Long,
        totalChaptersRead: Int,
        booksCompleted: Int,
        readingStreak: Int
    ): Result<Unit> {
        return try {
            // Get user info from auth project
            val userId = multiClient.getCurrentUserId() 
                ?: return Result.failure(Exception("User not authenticated. Please sign in to continue."))
            val username = multiClient.getCurrentUsername() 
                ?: return Result.failure(Exception("User not found. Please update your profile with a username."))
            
            // Upsert leaderboard entry in analytics project
            multiClient.analyticsClient.postgrest["leaderboard"].upsert(
                mapOf(
                    "user_id" to userId,
                    "username" to username,
                    "total_reading_time_minutes" to totalReadingTimeMinutes,
                    "total_chapters_read" to totalChaptersRead,
                    "books_completed" to booksCompleted,
                    "reading_streak" to readingStreak
                )
            )
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get user's complete profile (data from all 7 projects)
     */
    suspend fun getUserCompleteProfile(userId: String): Result<CompleteUserProfile> {
        return try {
            // Get user from auth project
            val user = multiClient.authClient.postgrest["users"]
                .select { filter { eq("id", userId) } }
                .decodeSingle<UserProfile>()
            
            // Get reading progress from reading project
            val readingProgress = multiClient.readingClient.postgrest["reading_progress"]
                .select { filter { eq("user_id", userId) } }
                .decodeList<ReadingProgress>()
            
            // Get synced books from library project
            val syncedBooks = multiClient.libraryClient.postgrest["synced_books"]
                .select { filter { eq("user_id", userId) } }
                .decodeList<SyncedBook>()
            
            // Get badges from badges project
            val badges = multiClient.badgesClient.postgrest["user_badges"]
                .select { filter { eq("user_id", userId) } }
                .decodeList<UserBadge>()
            
            // Get leaderboard stats from analytics project
            val stats = multiClient.analyticsClient.postgrest["leaderboard"]
                .select { filter { eq("user_id", userId) } }
                .decodeSingleOrNull<LeaderboardStats>()
            
            Result.success(
                CompleteUserProfile(
                    user = user,
                    readingProgress = readingProgress,
                    syncedBooks = syncedBooks,
                    badges = badges,
                    stats = stats
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class UserBadge(
    val badge_id: String,
    val earned_at: String
)

data class LeaderboardStats(
    val total_reading_time_minutes: Long,
    val total_chapters_read: Int,
    val books_completed: Int,
    val reading_streak: Int
)

data class ReadingProgress(
    val id: String,
    val book_id: String,
    val last_chapter_slug: String,
    val last_scroll_position: Float
)

data class SyncedBook(
    val book_id: String,
    val title: String,
    val book_url: String,
    val last_read: Long
)

data class CompleteUserProfile(
    val user: UserProfile,
    val readingProgress: List<ReadingProgress>,
    val syncedBooks: List<SyncedBook>,
    val badges: List<UserBadge>,
    val stats: LeaderboardStats?
)
