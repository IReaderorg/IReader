package ireader.data.di

import ireader.data.util.AppDataDirectory
import ireader.data.database.RepairDatabaseUseCaseImpl
import ireader.data.plugin.PluginDatabaseImpl
import ireader.data.plugin.PluginRepositoryImpl
import ireader.data.repository.FundingGoalRepositoryImpl
import ireader.data.tts.VoiceModelRepositoryImpl
import ireader.data.repository.PiperVoiceRepositoryImpl
import ireader.domain.data.repository.PiperVoiceRepository
import ireader.domain.data.repository.PluginRepository
import ireader.domain.data.repository.VoiceModelRepository
import ireader.domain.plugins.PluginDatabase
import ireader.domain.services.library.LibraryChangeNotifier
import ireader.domain.usecases.database.RepairDatabaseUseCase
import org.koin.dsl.module


val repositoryInjectModule = module {
    // LibraryChangeNotifier MUST be registered FIRST before any repositories
    // because repositories use getOrNull<LibraryChangeNotifier>() to notify changes.
    // If this is registered after repositories, they will get null and won't notify.
    single { LibraryChangeNotifier() }
    
    // Include feature-specific repository modules
    includes(bookRepositoryModule)
    includes(chapterRepositoryModule)
    includes(libraryRepositoryModule)
    includes(catalogRepositoryModule)
    includes(userDataRepositoryModule)
    includes(translationRepositoryModule)
    includes(trackingModule)
    
    // Include sync module for Local WiFi Book Sync feature
    includes(syncDataModule)
    
    // Include remote module for 7-project Supabase setup
    // Supports both default config (from local.properties) and user override
    includes(remoteModule)
    // Include review module for badge functionality
    includes(reviewModule)
    // Include plugin review module for plugin marketplace reviews
    includes(pluginReviewModule)
    // Include backup module for Google Drive backup functionality
    includes(backupModule)
    // Include platform-specific module for platform implementations
    includes(dataPlatformModule)
    
    // Library insights repository
    single<ireader.domain.data.repository.LibraryInsightsRepository> { 
        ireader.data.repository.LibraryInsightsRepositoryImpl(get(), get(), get(), get()) 
    }
    
    // Global search repository
    single<ireader.domain.data.repository.GlobalSearchRepository> { 
        ireader.data.repository.GlobalSearchRepositoryImpl(get(), get(), get()) 
    }
    
    // Advanced filter repository
    single<ireader.domain.data.repository.AdvancedFilterRepository> { 
        ireader.data.repository.AdvancedFilterRepositoryImpl(get(), get(), get()) 
    }
    
    // Migration repository
    single<ireader.domain.data.repository.MigrationRepository> { 
        ireader.data.repository.MigrationRepositoryImpl(get(), get()) 
    }
    
    // Notification repository is provided by platform-specific modules

    single<ireader.domain.data.repository.FundingGoalRepository> {
        FundingGoalRepositoryImpl()
    }
    
    // Voice model repository
    single<VoiceModelRepository> { VoiceModelRepositoryImpl(get(), get()) }
    
    // Piper voice repository (for unified voice catalog)
    single<PiperVoiceRepository> { PiperVoiceRepositoryImpl(get()) }
    
    // NFT repository is defined in reviewModule
    
    // Plugin repository and database
    single<PluginRepository> { PluginRepositoryImpl(get()) }
    single<PluginDatabase> { PluginDatabaseImpl(get()) }
    
    // Plugin Repository Repository - for managing plugin sources
    single<ireader.domain.plugins.PluginRepositoryRepository> {
        ireader.data.plugins.PluginRepositoryRepositoryImpl()
    }
    
    // Plugin Repository Index Fetcher - for fetching repository index.json
    single<ireader.domain.plugins.PluginRepositoryIndexFetcher> {
        ireader.data.plugins.PluginRepositoryIndexFetcherImpl(
            httpClient = get<ireader.core.http.HttpClients>().default
        )
    }
    
    // Plugin Security Validator - ensures paid plugins only work on official app
    single<ireader.domain.plugins.PluginSecurityValidator> {
        val supabaseProvider = get<ireader.domain.data.repository.SupabaseClientProvider>()
        
        ireader.data.plugins.PluginSecurityValidatorImpl(
            supabaseUrl = supabaseProvider.getSupabaseUrl(),
            getCurrentUserId = { null }, // Will be set lazily when needed
            getDeviceId = { 
                // Get unique device ID - platform specific
                ireader.domain.config.PlatformConfig.getDeviceId()
            }
        )
    }
    
    // Developer Portal Repository - for plugin developers
    single<ireader.domain.plugins.DeveloperPortalRepository> {
        ireader.data.plugins.DeveloperPortalRepositoryImpl(
            checkDeveloperBadge = { false }, // Simplified - no backend check
            getCurrentUserId = { null } // Will be set lazily when needed
        )
    }
    
    // Plugin monetization repositories
    single<ireader.domain.plugins.PurchaseRepository> { 
        ireader.data.plugin.PurchaseRepositoryImpl(get()) 
    }
    single<ireader.domain.plugins.TrialRepository> { 
        ireader.data.plugin.TrialRepositoryImpl(
            handler = get(),
            getCurrentUserId = { "anonymous" } // Simplified
        ) 
    }
    
    // Database use cases
    single<RepairDatabaseUseCase> { RepairDatabaseUseCaseImpl(get()) }
    single<ireader.domain.preferences.VoicePreferences> {
        ireader.data.preferences.VoicePreferencesImpl(
            preferenceStore = get()
        )
    }
    // Voice Storage for TTS voice models
    single<ireader.domain.storage.VoiceStorage> {
        ireader.data.storage.VoiceStorageImpl(AppDataDirectory.getPath())
    }
    
    // Leaderboard repository
    single<ireader.domain.data.repository.LeaderboardRepository> {
        val provider = get<ireader.domain.data.repository.SupabaseClientProvider>()
        if (provider is ireader.data.remote.NoOpSupabaseClientProvider) {
            // No Supabase configured, use NoOp singleton
            ireader.data.repository.NoOpLeaderboardRepository
        } else {
            try {
                // Get analytics client from multi-project provider
                val supabaseClient = (provider as ireader.data.remote.MultiSupabaseClientProvider).analyticsClient
                ireader.data.leaderboard.LeaderboardRepositoryImpl(
                    supabaseClient = supabaseClient,
                    backendService = get()
                )
            } catch (e: Exception) {
                // Fallback to NoOp singleton if something goes wrong
                ireader.data.repository.NoOpLeaderboardRepository
            }
        }
    }
    
    // Popular books repository
    single<ireader.domain.data.repository.PopularBooksRepository> {
        val provider = get<ireader.domain.data.repository.SupabaseClientProvider>()
        if (provider is ireader.data.remote.NoOpSupabaseClientProvider) {
            // No Supabase configured, use NoOp singleton
            ireader.data.repository.NoOpPopularBooksRepository
        } else {
            try {
                val supabaseClient = (provider as ireader.data.remote.MultiSupabaseClientProvider).libraryClient
                ireader.data.popular.PopularBooksRepositoryImpl(
                    supabaseClient = supabaseClient,
                    backendService = get()
                )
            } catch (e: Exception) {
                // Fallback to NoOp singleton if something goes wrong
                ireader.data.repository.NoOpPopularBooksRepository
            }
        }
    }
    
    // All reviews repository
    single<ireader.domain.data.repository.AllReviewsRepository> {
        val provider = get<ireader.domain.data.repository.SupabaseClientProvider>()
        if (provider is ireader.data.remote.NoOpSupabaseClientProvider) {
            // No Supabase configured, use NoOp singleton
            ireader.data.repository.NoOpAllReviewsRepository
        } else {
            try {
                val supabaseClient = (provider as ireader.data.remote.MultiSupabaseClientProvider).bookReviewsClient
                ireader.data.review.AllReviewsRepositoryImpl(
                    supabaseClient = supabaseClient,
                    backendService = get()
                )
            } catch (e: Exception) {
                // Fallback to NoOp singleton if something goes wrong
                ireader.data.repository.NoOpAllReviewsRepository
            }
        }
    }
    
    // Donation leaderboard repository
    single<ireader.domain.data.repository.DonationLeaderboardRepository> {
        val provider = get<ireader.domain.data.repository.SupabaseClientProvider>()
        if (provider is ireader.data.remote.NoOpSupabaseClientProvider) {
            // No Supabase configured, use NoOp singleton
            ireader.data.repository.NoOpDonationLeaderboardRepository
        } else {
            try {
                // Get analytics client from multi-project provider
                val supabaseClient = (provider as ireader.data.remote.MultiSupabaseClientProvider).analyticsClient
                ireader.data.donationleaderboard.DonationLeaderboardRepositoryImpl(
                    supabaseClient = supabaseClient,
                    backendService = get()
                )
            } catch (e: Exception) {
                // Fallback to NoOp singleton if something goes wrong
                ireader.data.repository.NoOpDonationLeaderboardRepository
            }
        }
    }
    
    // Glossary repository - for local book glossaries
    single<ireader.domain.data.repository.GlossaryRepository> {
        ireader.data.glossary.GlossaryRepositoryImpl(get())
    }
    
    // Content filter repository - for regex-based content filtering
    single<ireader.domain.data.repository.ContentFilterRepository> {
        ireader.data.contentfilter.ContentFilterRepositoryImpl(get())
    }
    
    // Text replacement repository - for find-and-replace rules
    single<ireader.domain.data.repository.TextReplacementRepository> {
        ireader.data.textreplacement.TextReplacementRepositoryImpl(get())
    }
    
    // User Source repository - for user-defined sources
    single<ireader.domain.usersource.repository.UserSourceRepository> {
        ireader.data.usersource.UserSourceRepositoryImpl(get())
    }
    
    // Global Glossary repository - for glossaries independent of library with Supabase sync
    single<ireader.domain.data.repository.GlobalGlossaryRepository> {
        val provider = get<ireader.domain.data.repository.SupabaseClientProvider>()
        val getCurrentUserUseCase: ireader.domain.usecases.remote.GetCurrentUserUseCase = get()
        
        if (provider is ireader.data.remote.NoOpSupabaseClientProvider) {
            // No Supabase configured, use local-only implementation
            ireader.data.glossary.GlobalGlossaryRepositoryImpl(
                handler = get(),
                backendService = null,
                getCurrentUserId = { null }
            )
        } else {
            try {
                // Get backend service for Supabase operations
                val backendService: ireader.data.backend.BackendService = get()
                
                ireader.data.glossary.GlobalGlossaryRepositoryImpl(
                    handler = get(),
                    backendService = backendService,
                    getCurrentUserId = { getCurrentUserUseCase().getOrNull()?.id }
                )
            } catch (e: Exception) {
                // Fallback to local-only if something goes wrong
                ireader.data.glossary.GlobalGlossaryRepositoryImpl(
                    handler = get(),
                    backendService = null,
                    getCurrentUserId = { null }
                )
            }
        }
    }
    
    // Local Quote repository - for personal quote collection
    single<ireader.domain.data.repository.LocalQuoteRepository> {
        ireader.data.quote.LocalQuoteRepositoryImpl(get())
    }
    
    // Discord Quote repository - for community quote sharing
    single<ireader.domain.data.repository.DiscordQuoteRepository> {
        val discordWebhookUrl = ireader.domain.config.PlatformConfig.getDiscordQuoteWebhookUrl()
        ireader.data.quote.DiscordQuoteRepositoryImpl(
            webhookUrl = discordWebhookUrl,
            httpClient = get<ireader.core.http.HttpClients>().default,
            quoteCardGenerator = ireader.data.quote.createQuoteCardGenerator()
        )
    }
    
    // Character Art Gallery repository - Discord only
    single<ireader.domain.data.repository.CharacterArtRepository> {
        val discordWebhookUrl = ireader.domain.config.PlatformConfig.getDiscordCharacterArtWebhookUrl()
        
        if (discordWebhookUrl.isNotBlank()) {
            // Use Discord webhook integration
            val discordService = ireader.domain.services.discord.DiscordWebhookService(
                httpClient = get<ireader.core.http.HttpClients>().default,
                webhookUrl = discordWebhookUrl
            )
            ireader.data.repository.DiscordCharacterArtRepository(discordService)
        } else {
            // No Discord configured, use no-op implementation
            ireader.data.repository.NoOpCharacterArtRepository
        }
    }
}
