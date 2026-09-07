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

import ireader.domain.preferences.prefs.SupabasePreferences

/**
 * Multi-Supabase Client Provider for 7-project setup
 * 
 * This provider manages connections to 7 separate Supabase projects:
 * 1. Auth - User authentication and profiles
 * 2. Reading - Reading progress tracking
 * 3. Library - Synced books library
 * 4. Book Reviews - Book reviews
 * 5. Chapter Reviews - Chapter reviews
 * 6. Badges - Badge system and payment proofs
 * 7. Analytics - Leaderboard and statistics
 * 
 * Total Storage: 3.5GB (7 × 500MB)
 * Supports dynamic configuration reload at runtime via invalidateClients().
 */
class MultiSupabaseClientProvider(
    private val preferences: SupabasePreferences? = null,
    // Project 1 - Auth
    private var initialAuthUrl: String = "",
    private var initialAuthKey: String = "",
    // Project 2 - Reading
    private var initialReadingUrl: String = "",
    private var initialReadingKey: String = "",
    // Project 3 - Library
    private var initialLibraryUrl: String = "",
    private var initialLibraryKey: String = "",
    // Project 4 - Book Reviews
    private var initialBookReviewsUrl: String = "",
    private var initialBookReviewsKey: String = "",
    // Project 5 - Chapter Reviews
    private var initialChapterReviewsUrl: String = "",
    private var initialChapterReviewsKey: String = "",
    // Project 6 - Badges
    private var initialBadgesUrl: String = "",
    private var initialBadgesKey: String = "",
    // Project 7 - Analytics
    private var initialAnalyticsUrl: String = "",
    private var initialAnalyticsKey: String = ""
) : SupabaseClientProvider {

    constructor(
        authUrl: String,
        authKey: String,
        readingUrl: String,
        readingKey: String,
        libraryUrl: String,
        libraryKey: String,
        bookReviewsUrl: String,
        bookReviewsKey: String,
        chapterReviewsUrl: String,
        chapterReviewsKey: String,
        badgesUrl: String,
        badgesKey: String,
        analyticsUrl: String,
        analyticsKey: String
    ) : this(
        preferences = null,
        initialAuthUrl = authUrl,
        initialAuthKey = authKey,
        initialReadingUrl = readingUrl,
        initialReadingKey = readingKey,
        initialLibraryUrl = libraryUrl,
        initialLibraryKey = libraryKey,
        initialBookReviewsUrl = bookReviewsUrl,
        initialBookReviewsKey = bookReviewsKey,
        initialChapterReviewsUrl = chapterReviewsUrl,
        initialChapterReviewsKey = chapterReviewsKey,
        initialBadgesUrl = badgesUrl,
        initialBadgesKey = badgesKey,
        initialAnalyticsUrl = analyticsUrl,
        initialAnalyticsKey = analyticsKey
    )

    companion object {
        private const val PLACEHOLDER_URL = "https://unconfigured.ireader.internal"
        private const val PLACEHOLDER_KEY = "unconfigured_key"
    }

    private val clientLock = Any()
    private val clientCache = mutableMapOf<String, SupabaseClient>()

    private fun resolveConfig(
        userPrefUrl: () -> String,
        userPrefKey: () -> String,
        platformConfigUrl: () -> String,
        platformConfigKey: () -> String,
        initialUrl: String,
        initialKey: String
    ): Pair<String, String> {
        val prefs = preferences ?: return Pair(initialUrl, initialKey)
        val useCustom = prefs.useCustomSupabase().get()
        val url = if (useCustom) {
            userPrefUrl().trim().ifBlank { prefs.userSupabaseUrl().get().trim() }
        } else {
            initialUrl.trim().ifBlank {
                try { platformConfigUrl() } catch (_: Exception) { "" }
            }
        }

        val key = if (useCustom) {
            userPrefKey().trim().ifBlank { prefs.userSupabaseAnonKey().get().trim() }
        } else {
            initialKey.trim().ifBlank {
                try { platformConfigKey() } catch (_: Exception) { "" }
            }
        }

        return Pair(url.trim(), key.trim())
    }

    fun getAuthConfig(): Pair<String, String> {
        val prefs = preferences ?: return Pair(initialAuthUrl, initialAuthKey)
        return resolveConfig(
            userPrefUrl = { prefs.supabaseAuthUrl().get() },
            userPrefKey = { prefs.supabaseAuthKey().get() },
            platformConfigUrl = { ireader.domain.config.PlatformConfig.getSupabaseAuthUrl() },
            platformConfigKey = { ireader.domain.config.PlatformConfig.getSupabaseAuthKey() },
            initialUrl = initialAuthUrl,
            initialKey = initialAuthKey
        )
    }

    fun getReadingConfig(): Pair<String, String> {
        val prefs = preferences ?: return Pair(initialReadingUrl, initialReadingKey)
        val singleUrl = prefs.userSupabaseUrl().get().trim().ifEmpty {
            if (prefs.useCustomSupabase().get()) prefs.supabaseReadingUrl().get().trim() else ""
        }.ifBlank { initialReadingUrl }
        val singleKey = prefs.userSupabaseAnonKey().get().trim().ifEmpty {
            if (prefs.useCustomSupabase().get()) prefs.supabaseReadingKey().get().trim() else ""
        }.ifBlank { initialReadingKey }
        return Pair(singleUrl, singleKey)
    }

    fun getLibraryConfig(): Pair<String, String> {
        val prefs = preferences ?: return Pair(initialLibraryUrl, initialLibraryKey)
        val singleUrl = prefs.userSupabaseUrl().get().trim().ifEmpty {
            if (prefs.useCustomSupabase().get()) prefs.supabaseLibraryUrl().get().trim() else ""
        }.ifBlank { initialLibraryUrl }
        val singleKey = prefs.userSupabaseAnonKey().get().trim().ifEmpty {
            if (prefs.useCustomSupabase().get()) prefs.supabaseLibraryKey().get().trim() else ""
        }.ifBlank { initialLibraryKey }
        return Pair(singleUrl, singleKey)
    }

    fun getBookReviewsConfig(): Pair<String, String> {
        val prefs = preferences ?: return Pair(initialBookReviewsUrl, initialBookReviewsKey)
        return resolveConfig(
            userPrefUrl = { prefs.supabaseBookReviewsUrl().get() },
            userPrefKey = { prefs.supabaseBookReviewsKey().get() },
            platformConfigUrl = { ireader.domain.config.PlatformConfig.getSupabaseBookReviewsUrl() },
            platformConfigKey = { ireader.domain.config.PlatformConfig.getSupabaseBookReviewsKey() },
            initialUrl = initialBookReviewsUrl,
            initialKey = initialBookReviewsKey
        )
    }

    fun getChapterReviewsConfig(): Pair<String, String> {
        val prefs = preferences ?: return Pair(initialChapterReviewsUrl, initialChapterReviewsKey)
        return resolveConfig(
            userPrefUrl = { prefs.supabaseChapterReviewsUrl().get() },
            userPrefKey = { prefs.supabaseChapterReviewsKey().get() },
            platformConfigUrl = { ireader.domain.config.PlatformConfig.getSupabaseChapterReviewsUrl() },
            platformConfigKey = { ireader.domain.config.PlatformConfig.getSupabaseChapterReviewsKey() },
            initialUrl = initialChapterReviewsUrl,
            initialKey = initialChapterReviewsKey
        )
    }

    fun getBadgesConfig(): Pair<String, String> {
        val prefs = preferences ?: return Pair(initialBadgesUrl, initialBadgesKey)
        return resolveConfig(
            userPrefUrl = { prefs.supabaseBadgesUrl().get() },
            userPrefKey = { prefs.supabaseBadgesKey().get() },
            platformConfigUrl = { ireader.domain.config.PlatformConfig.getSupabaseBadgesUrl() },
            platformConfigKey = { ireader.domain.config.PlatformConfig.getSupabaseBadgesKey() },
            initialUrl = initialBadgesUrl,
            initialKey = initialBadgesKey
        )
    }

    fun getAnalyticsConfig(): Pair<String, String> {
        val prefs = preferences ?: return Pair(initialAnalyticsUrl, initialAnalyticsKey)
        
        // 1. If custom community server is explicitly configured and enabled
        if (prefs.useCustomCommunityServer().get()) {
            val customUrl = prefs.customCommunityUrl().get().trim()
            val customKey = prefs.customCommunityApiKey().get().trim()
            if (customUrl.isNotBlank() && customKey.isNotBlank()) {
                return Pair(customUrl, customKey)
            }
        }
        
        // 2. If user enabled custom Supabase in settings
        if (prefs.useCustomSupabase().get()) {
            val customAnalyticsUrl = prefs.supabaseAnalyticsUrl().get().trim()
            val customAnalyticsKey = prefs.supabaseAnalyticsKey().get().trim()
            if (customAnalyticsUrl.isNotBlank() && customAnalyticsKey.isNotBlank()) {
                return Pair(customAnalyticsUrl, customAnalyticsKey)
            }
            // Fall back to user single-project Supabase config
            val singleUrl = prefs.userSupabaseUrl().get().trim()
            val singleKey = prefs.userSupabaseAnonKey().get().trim()
            if (singleUrl.isNotBlank() && singleKey.isNotBlank()) {
                return Pair(singleUrl, singleKey)
            }
        }
        
        // 3. Environment / PlatformConfig default
        val platformUrl = initialAnalyticsUrl.trim().ifBlank {
            try { ireader.domain.config.PlatformConfig.getSupabaseAnalyticsUrl() } catch (_: Exception) { "" }.trim()
        }.ifBlank {
            initialAuthUrl.trim().ifBlank {
                try { ireader.domain.config.PlatformConfig.getSupabaseAuthUrl() } catch (_: Exception) { "" }.trim()
            }
        }
        val platformKey = initialAnalyticsKey.trim().ifBlank {
            try { ireader.domain.config.PlatformConfig.getSupabaseAnalyticsKey() } catch (_: Exception) { "" }.trim()
        }.ifBlank {
            initialAuthKey.trim().ifBlank {
                try { ireader.domain.config.PlatformConfig.getSupabaseAuthKey() } catch (_: Exception) { "" }.trim()
            }
        }
        return Pair(platformUrl, platformKey)
    }

    private fun getOrCreateClient(
        key: String,
        config: Pair<String, String>,
        configure: io.github.jan.supabase.SupabaseClientBuilder.() -> Unit
    ): SupabaseClient {
        synchronized(clientLock) {
            val cached = clientCache[key]
            if (cached != null) return cached

            val (url, apiKey) = config
            val effectiveUrl = if (url.isNotBlank()) url else PLACEHOLDER_URL
            val effectiveKey = if (apiKey.isNotBlank()) apiKey else PLACEHOLDER_KEY

            val client = createSupabaseClient(
                supabaseUrl = effectiveUrl,
                supabaseKey = effectiveKey,
                builder = configure
            )
            clientCache[key] = client
            return client
        }
    }

    /**
     * Project 1: Auth Database
     * Contains: users
     */
    val authClient: SupabaseClient
        get() = getOrCreateClient("auth", getAuthConfig()) {
            install(Auth)
            install(Postgrest)
        }
    
    /**
     * Project 2: Reading Database
     * Contains: reading_progress
     */
    val readingClient: SupabaseClient
        get() = getOrCreateClient("reading", getReadingConfig()) {
            install(Auth)
            install(Postgrest)
        }
    
    /**
     * Project 3: Library Database
     * Contains: synced_books
     */
    val libraryClient: SupabaseClient
        get() = getOrCreateClient("library", getLibraryConfig()) {
            install(Auth)
            install(Postgrest)
        }
    
    /**
     * Project 4: Book Reviews Database
     * Contains: book_reviews
     */
    val bookReviewsClient: SupabaseClient
        get() = getOrCreateClient("book_reviews", getBookReviewsConfig()) {
            install(Auth)
            install(Postgrest)
        }
    
    /**
     * Project 5: Chapter Reviews Database
     * Contains: chapter_reviews
     */
    val chapterReviewsClient: SupabaseClient
        get() = getOrCreateClient("chapter_reviews", getChapterReviewsConfig()) {
            install(Auth)
            install(Postgrest)
        }
    
    /**
     * Project 6: Badges Database
     * Contains: badges, user_badges, payment_proofs
     */
    val badgesClient: SupabaseClient
        get() = getOrCreateClient("badges", getBadgesConfig()) {
            install(Auth)
            install(Postgrest)
        }
    
    /**
     * Project 7: Analytics Database
     * Contains: leaderboard
     */
    val analyticsClient: SupabaseClient
        get() = getOrCreateClient("analytics", getAnalyticsConfig()) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
        }
    
    override fun invalidateClients() {
        synchronized(clientLock) {
            clientCache.clear()
            Log.info("MultiSupabaseClientProvider: invalidated and cleared all cached clients")
        }
    }

    override fun getClient(endpoint: SupabaseEndpoint): Any {
        return when (endpoint) {
            SupabaseEndpoint.USERS -> authClient
            SupabaseEndpoint.PROGRESS -> readingClient
            SupabaseEndpoint.BOOKS -> libraryClient
            SupabaseEndpoint.REVIEWS -> bookReviewsClient
            SupabaseEndpoint.COMMUNITY -> analyticsClient
        }
    }
    
    override fun isEndpointAvailable(endpoint: SupabaseEndpoint): Boolean {
        return try {
            val (url, key) = when (endpoint) {
                SupabaseEndpoint.USERS -> getAuthConfig()
                SupabaseEndpoint.PROGRESS -> getReadingConfig()
                SupabaseEndpoint.BOOKS -> getLibraryConfig()
                SupabaseEndpoint.REVIEWS -> getBookReviewsConfig()
                SupabaseEndpoint.COMMUNITY -> getAnalyticsConfig()
            }
            url.isNotBlank() && key.isNotBlank() && url != PLACEHOLDER_URL
        } catch (e: Exception) {
            Log.error(e, "Failed to check endpoint availability for $endpoint")
            false
        }
    }

    override fun getSupabaseUrl(): String {
        val (u, _) = getAuthConfig()
        return if (u.isNotBlank() && u != PLACEHOLDER_URL) u else ""
    }
    
    /**
     * Get the current authenticated user ID as a string
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
     */
    suspend fun getCurrentUsername(): String? {
        val userId = getCurrentUserId() ?: return null
        return try {
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
        val clientsToClose = synchronized(clientLock) {
            val list = clientCache.values.toList()
            clientCache.clear()
            list
        }
        clientsToClose.forEach { client ->
            try {
                client.close()
            } catch (e: Exception) {
                Log.error(e, "Failed to close Supabase client")
            }
        }
        Log.info("Closed all Supabase clients")
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
