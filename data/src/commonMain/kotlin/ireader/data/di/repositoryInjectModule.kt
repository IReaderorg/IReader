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
import ireader.domain.usecases.database.RepairDatabaseUseCase
import org.koin.dsl.module


val repositoryInjectModule = module {
    // Include feature-specific repository modules
    includes(bookRepositoryModule)
    includes(chapterRepositoryModule)
    includes(libraryRepositoryModule)
    includes(catalogRepositoryModule)
    includes(userDataRepositoryModule)
    includes(translationRepositoryModule)
    
    // Include remote module for 7-project Supabase setup
    // Supports both default config (from local.properties) and user override
    includes(remoteModule)
    // Include review module for badge functionality
    includes(reviewModule)
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
    single<PluginDatabase> { PluginDatabaseImpl(get(), get()) }
    
    // Plugin monetization repositories
    single<ireader.domain.plugins.PurchaseRepository> { 
        ireader.data.plugin.PurchaseRepositoryImpl(get()) 
    }
    single<ireader.domain.plugins.TrialRepository> { 
        ireader.data.plugin.TrialRepositoryImpl(
            handler = get(),
            getCurrentUserId = { 
                // TODO: Get actual user ID from authentication service
                "default_user"
            }
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
    
    // Character Art Gallery repository
    single<ireader.domain.data.repository.CharacterArtRepository> {
        val provider = get<ireader.domain.data.repository.SupabaseClientProvider>()
        if (provider is ireader.data.remote.NoOpSupabaseClientProvider) {
            // No Supabase configured, use in-memory implementation
            ireader.data.repository.CharacterArtRepositoryImpl()
        } else {
            try {
                // Get library client from multi-project provider for character art
                val supabaseClient = (provider as ireader.data.remote.MultiSupabaseClientProvider).libraryClient
                val getCurrentUserUseCase: ireader.domain.usecases.remote.GetCurrentUserUseCase = get()
                val backendService: ireader.data.backend.BackendService = get()
                
                // Create Supabase metadata storage using BackendService (pure HTTP, no reflection)
                val metadataStorage = ireader.data.characterart.SupabaseCharacterArtMetadata(
                    supabaseClient = supabaseClient,
                    backendService = backendService,
                    getCurrentUserId = { getCurrentUserUseCase().getOrNull()?.id }
                )
                
                // Create image storage using Cloudflare R2
                val r2AccountId = ireader.domain.config.PlatformConfig.getR2AccountId()
                val r2AccessKeyId = ireader.domain.config.PlatformConfig.getR2AccessKeyId()
                val r2SecretAccessKey = ireader.domain.config.PlatformConfig.getR2SecretAccessKey()
                val r2BucketName = ireader.domain.config.PlatformConfig.getR2BucketName()
                val r2PublicUrl = ireader.domain.config.PlatformConfig.getR2PublicUrl()
                
                val imageStorage: ireader.data.characterart.ImageStorageProvider = 
                    if (r2AccountId.isNotEmpty() && r2AccessKeyId.isNotEmpty() && r2SecretAccessKey.isNotEmpty()) {
                        // Use Cloudflare R2 if configured
                        val r2Config = ireader.data.characterart.R2Config(
                            accountId = r2AccountId,
                            accessKeyId = r2AccessKeyId,
                            secretAccessKey = r2SecretAccessKey,
                            bucketName = r2BucketName,
                            publicUrl = r2PublicUrl
                        )
                        val r2DataSource = ireader.data.characterart.CloudflareR2DataSource(
                            httpClient = get<ireader.core.http.HttpClients>().default,
                            config = r2Config
                        )
                        ireader.data.characterart.CloudflareR2ImageStorage(r2DataSource)
                    } else {
                        // Fallback to Supabase Storage
                        ireader.data.characterart.SupabaseImageStorage(
                            httpClient = get<ireader.core.http.HttpClients>().default,
                            supabaseUrl = supabaseClient.supabaseUrl,
                            supabaseKey = supabaseClient.supabaseKey,
                            bucketName = "character-art"
                        )
                    }
                
                // Create the combined data source
                val dataSource = ireader.data.characterart.CharacterArtDataSource(
                    imageStorage = imageStorage,
                    metadataStorage = metadataStorage
                )
                
                // Create repository with remote data source
                ireader.data.repository.CharacterArtRepositoryImpl(
                    remoteDataSource = dataSource
                )
            } catch (e: Exception) {
                // Fallback to in-memory if something goes wrong
                println("CharacterArt: Failed to initialize Supabase, using in-memory: ${e.message}")
                ireader.data.repository.CharacterArtRepositoryImpl()
            }
        }
    }
}
