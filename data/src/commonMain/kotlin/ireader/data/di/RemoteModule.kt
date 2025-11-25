package ireader.data.di

import io.github.jan.supabase.SupabaseClient
import ireader.data.remote.MultiSupabaseClientProvider
import ireader.data.remote.RemoteCache
import ireader.data.remote.RetryPolicy
import ireader.data.remote.SupabaseRemoteRepository
import ireader.data.remote.SyncQueue
import ireader.domain.data.repository.RemoteRepository
import ireader.domain.data.repository.SupabaseClientProvider
import ireader.domain.models.remote.SupabaseEndpoint
import ireader.domain.preferences.prefs.SupabasePreferences
import org.koin.dsl.module

/**
 * Remote module for 7-project Supabase setup
 * 
 * This module provides the MultiSupabaseClientProvider that always uses
 * the 7-project database architecture (3.5GB total storage).
 * 
 * Users can configure all 7 projects individually or use the same URL for all.
 */
val remoteModule = module {
    
    // Multi-Project Supabase Client Provider (always enabled)
    single<SupabaseClientProvider> {
        val prefs = get<SupabasePreferences>()
        
        // Check if user wants to use custom configuration
        val useCustom = prefs.useCustomSupabase().get()
        
        // Helper function to get config with fallback: user preference -> platform config
        fun getUrl(userPref: String, platformConfig: () -> String): String {
            return if (useCustom && userPref.isNotEmpty()) {
                userPref
            } else {
                try {
                    platformConfig()
                } catch (e: Exception) {
                    ""
                }
            }
        }
        
        fun getKey(userPref: String, platformConfig: () -> String): String {
            return if (useCustom && userPref.isNotEmpty()) {
                userPref
            } else {
                try {
                    platformConfig()
                } catch (e: Exception) {
                    ""
                }
            }
        }
        
        // Load credentials with fallback chain: user preferences -> platform config (local.properties/config.properties)
        // Project 1 - Auth
        val authUrl = getUrl(
            prefs.supabaseAuthUrl().get(),
            { ireader.domain.config.PlatformConfig.getSupabaseAuthUrl() }
        )
        val authKey = getKey(
            prefs.supabaseAuthKey().get(),
            { ireader.domain.config.PlatformConfig.getSupabaseAuthKey() }
        )
        
        // Project 2 - Reading
        val readingUrl = getUrl(
            prefs.supabaseReadingUrl().get(),
            { ireader.domain.config.PlatformConfig.getSupabaseReadingUrl() }
        )
        val readingKey = getKey(
            prefs.supabaseReadingKey().get(),
            { ireader.domain.config.PlatformConfig.getSupabaseReadingKey() }
        )
        
        // Project 3 - Library
        val libraryUrl = getUrl(
            prefs.supabaseLibraryUrl().get(),
            { ireader.domain.config.PlatformConfig.getSupabaseLibraryUrl() }
        )
        val libraryKey = getKey(
            prefs.supabaseLibraryKey().get(),
            { ireader.domain.config.PlatformConfig.getSupabaseLibraryKey() }
        )
        
        // Project 4 - Book Reviews
        val bookReviewsUrl = getUrl(
            prefs.supabaseBookReviewsUrl().get(),
            { ireader.domain.config.PlatformConfig.getSupabaseBookReviewsUrl() }
        )
        val bookReviewsKey = getKey(
            prefs.supabaseBookReviewsKey().get(),
            { ireader.domain.config.PlatformConfig.getSupabaseBookReviewsKey() }
        )
        
        // Project 5 - Chapter Reviews
        val chapterReviewsUrl = getUrl(
            prefs.supabaseChapterReviewsUrl().get(),
            { ireader.domain.config.PlatformConfig.getSupabaseChapterReviewsUrl() }
        )
        val chapterReviewsKey = getKey(
            prefs.supabaseChapterReviewsKey().get(),
            { ireader.domain.config.PlatformConfig.getSupabaseChapterReviewsKey() }
        )
        
        // Project 6 - Badges
        val badgesUrl = getUrl(
            prefs.supabaseBadgesUrl().get(),
            { ireader.domain.config.PlatformConfig.getSupabaseBadgesUrl() }
        )
        val badgesKey = getKey(
            prefs.supabaseBadgesKey().get(),
            { ireader.domain.config.PlatformConfig.getSupabaseBadgesKey() }
        )
        
        // Project 7 - Analytics
        val analyticsUrl = getUrl(
            prefs.supabaseAnalyticsUrl().get(),
            { ireader.domain.config.PlatformConfig.getSupabaseAnalyticsUrl() }
        )
        val analyticsKey = getKey(
            prefs.supabaseAnalyticsKey().get(),
            { ireader.domain.config.PlatformConfig.getSupabaseAnalyticsKey() }
        )
        
        // If no configuration available (neither user nor platform), use NoOp provider
        if (authUrl.isEmpty() || authKey.isEmpty()) {
            return@single ireader.data.remote.NoOpSupabaseClientProvider()
        }
        
        // Create Multi-Project provider
        MultiSupabaseClientProvider(
            authUrl = authUrl,
            authKey = authKey,
            readingUrl = readingUrl,
            readingKey = readingKey,
            libraryUrl = libraryUrl,
            libraryKey = libraryKey,
            bookReviewsUrl = bookReviewsUrl,
            bookReviewsKey = bookReviewsKey,
            chapterReviewsUrl = chapterReviewsUrl,
            chapterReviewsKey = chapterReviewsKey,
            badgesUrl = badgesUrl,
            badgesKey = badgesKey,
            analyticsUrl = analyticsUrl,
            analyticsKey = analyticsKey
        )
    }
    
    // Sync queue
    single { SyncQueue() }
    
    // Retry policy
    single { RetryPolicy() }
    
    // Remote cache
    single { RemoteCache() }
    
    // Backend Service (abstraction layer)
    single<ireader.data.backend.BackendService> {
        val provider = get<SupabaseClientProvider>()
        if (provider is ireader.data.remote.NoOpSupabaseClientProvider) {
            ireader.data.backend.NoOpBackendService()
        } else {
            val supabaseClient = (provider as MultiSupabaseClientProvider).authClient
            ireader.data.backend.SupabaseBackendService(supabaseClient)
        }
    }
    
    // Auth Service (authentication abstraction)
    single<ireader.data.backend.AuthService> {
        val provider = get<SupabaseClientProvider>()
        if (provider is ireader.data.remote.NoOpSupabaseClientProvider) {
            ireader.data.backend.NoOpAuthService()
        } else {
            val supabaseClient = (provider as MultiSupabaseClientProvider).authClient
            ireader.data.backend.SupabaseAuthService(supabaseClient)
        }
    }
    
    // Remote repository
    single<RemoteRepository> {
        val provider = get<SupabaseClientProvider>()
        if (provider is ireader.data.remote.NoOpSupabaseClientProvider) {
            ireader.data.remote.NoOpRemoteRepository()
        } else {
            val supabaseClient = (provider as MultiSupabaseClientProvider).authClient
            SupabaseRemoteRepository(
                supabaseClient = supabaseClient,
                backendService = get(),
                syncQueue = get(),
                retryPolicy = get(),
                cache = get()
            )
        }
    }
}
