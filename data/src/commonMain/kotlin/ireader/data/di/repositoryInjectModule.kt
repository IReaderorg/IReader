package ireader.data.di

import ireader.data.book.BookRepositoryImpl
import ireader.data.core.DatabaseOptimizations
import ireader.data.catalog.CatalogRemoteRepositoryImpl
import ireader.data.category.BookCategoryRepositoryImpl
import ireader.data.category.CategoryRepositoryImpl
import ireader.data.chapter.ChapterRepositoryImpl
import ireader.data.chapterhealth.ChapterHealthRepositoryImpl
import ireader.data.chapterreport.ChapterReportRepositoryImpl
import ireader.data.database.RepairDatabaseUseCaseImpl
import ireader.data.downloads.DownloadRepositoryImpl
import ireader.data.font.FontRepositoryImpl
import ireader.data.history.HistoryRepositoryImpl
import ireader.data.plugin.PluginDatabaseImpl
import ireader.data.plugin.PluginRepositoryImpl
import ireader.data.repository.FundingGoalRepositoryImpl
import ireader.data.repository.LibraryRepositoryImpl
import ireader.data.repository.ReaderThemeRepositoryImpl
import ireader.data.repository.SourceCredentialsRepositoryImpl
import ireader.data.repository.ThemeRepositoryImpl
import ireader.data.repository.UpdatesRepositoryImpl
import ireader.data.security.SecurityRepositoryImpl
import ireader.data.services.SourceHealthCheckerImpl
import ireader.data.sourcecomparison.SourceComparisonRepositoryImpl
import ireader.data.sourcereport.SourceReportRepositoryImpl
import ireader.data.statistics.ReadingStatisticsRepositoryImpl
import ireader.data.translation.GlossaryRepositoryImpl
import ireader.data.translation.TranslatedChapterRepositoryImpl
import ireader.data.tts.VoiceModelRepositoryImpl
import ireader.domain.catalogs.service.CatalogRemoteRepository
import ireader.domain.data.repository.BookCategoryRepository
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.CategoryRepository
import ireader.domain.data.repository.ChapterHealthRepository
import ireader.domain.data.repository.ChapterReportRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.data.repository.DownloadRepository
import ireader.domain.data.repository.FontRepository
import ireader.domain.data.repository.GlossaryRepository
import ireader.domain.data.repository.HistoryRepository
import ireader.domain.data.repository.LibraryRepository
import ireader.domain.data.repository.PluginRepository
import ireader.domain.data.repository.ReaderThemeRepository
import ireader.domain.data.repository.ReadingStatisticsRepository
import ireader.domain.data.repository.SecurityRepository
import ireader.domain.data.repository.SourceComparisonRepository
import ireader.domain.data.repository.SourceCredentialsRepository
import ireader.domain.data.repository.SourceReportRepository
import ireader.domain.data.repository.ThemeRepository
import ireader.domain.data.repository.TranslatedChapterRepository
import ireader.domain.data.repository.UpdatesRepository
import ireader.domain.data.repository.VoiceModelRepository
import ireader.domain.plugins.PluginDatabase
import ireader.domain.services.SourceHealthChecker
import ireader.domain.usecases.database.RepairDatabaseUseCase
import org.koin.dsl.module
import java.io.File


val repositoryInjectModule = module {
    // Include remote module for 7-project Supabase setup
    // Supports both default config (from local.properties) and user override
    includes(remoteModule)
    // Include review module for badge functionality
    includes(reviewModule)
    // Include backup module for Google Drive backup functionality
    includes(backupModule)
    // Include platform-specific module for platform implementations
    includes(dataPlatformModule)
    
    single<DownloadRepository> { DownloadRepositoryImpl(get()) }
    single<UpdatesRepository> { UpdatesRepositoryImpl(get()) }
    single<LibraryRepository> { LibraryRepositoryImpl(get()) }
    single<CategoryRepository> { CategoryRepositoryImpl(get()) }
    single<CatalogRemoteRepository> { CatalogRemoteRepositoryImpl(get()) }
    single<ChapterRepository> { ChapterRepositoryImpl(get(), getOrNull()) }
    single<ChapterHealthRepository> { ChapterHealthRepositoryImpl(get()) }
    single<SourceComparisonRepository> { SourceComparisonRepositoryImpl(get()) }
    single<BookCategoryRepository> { BookCategoryRepositoryImpl(get()) }
    single<BookRepository> { BookRepositoryImpl(get(), get<BookCategoryRepository>(), getOrNull()) }
    single<ireader.domain.data.repository.consolidated.BookRepository> { 
        ireader.data.repository.ConsolidatedBookRepositoryImpl(get<BookRepository>(), get<BookCategoryRepository>()) 
    }
    single<HistoryRepository> { HistoryRepositoryImpl(get(), getOrNull()) }
    single<ThemeRepository> { ThemeRepositoryImpl(get()) }
    single<ReaderThemeRepository> { ReaderThemeRepositoryImpl(get()) }
    
    // Translation repositories
    single<TranslatedChapterRepository> { TranslatedChapterRepositoryImpl(get()) }
    single<GlossaryRepository> { GlossaryRepositoryImpl(get()) }
    
    // Source health checker
    single<SourceHealthChecker> { SourceHealthCheckerImpl(get()) }
    
    // Source credentials repository
    single<SourceCredentialsRepository> { SourceCredentialsRepositoryImpl(get()) }
    
    // Reading statistics repository
    single<ReadingStatisticsRepository> { ReadingStatisticsRepositoryImpl(get()) }
    
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
    
    // Security repository
    single<SecurityRepository> { SecurityRepositoryImpl(get(), get()) }
    
    // Chapter report repository
    single<ChapterReportRepository> { ChapterReportRepositoryImpl(get()) }
    
    // Source report repository
    single<SourceReportRepository> { SourceReportRepositoryImpl(get()) }
    
    // Migration repository
    single<ireader.domain.data.repository.MigrationRepository> { 
        ireader.data.repository.MigrationRepositoryImpl(get(), get()) 
    }
    
    // Notification repository is provided by platform-specific modules
    
    // Font repository
    single<FontRepository> { FontRepositoryImpl(get(), get()) }

    single<ireader.domain.data.repository.FundingGoalRepository> {
        FundingGoalRepositoryImpl()
    }
    
    // Voice model repository
    single<VoiceModelRepository> { VoiceModelRepositoryImpl(get(), get()) }
    
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
        val appDataDir = File(System.getProperty("user.home"), ".ireader")
        ireader.data.storage.VoiceStorageImpl(appDataDir)
    }
    
    // Leaderboard repository
    single<ireader.domain.data.repository.LeaderboardRepository> {
        val provider = get<ireader.domain.data.repository.SupabaseClientProvider>()
        if (provider is ireader.data.remote.NoOpSupabaseClientProvider) {
            // No Supabase configured, use NoOp
            ireader.data.repository.NoOpLeaderboardRepository()
        } else {
            try {
                // Get analytics client from multi-project provider
                val supabaseClient = (provider as ireader.data.remote.MultiSupabaseClientProvider).analyticsClient
                ireader.data.leaderboard.LeaderboardRepositoryImpl(
                    supabaseClient = supabaseClient,
                    backendService = get()
                )
            } catch (e: Exception) {
                // Fallback to NoOp if something goes wrong
                ireader.data.repository.NoOpLeaderboardRepository()
            }
        }
    }
    
    // Popular books repository
    single<ireader.domain.data.repository.PopularBooksRepository> {
        val provider = get<ireader.domain.data.repository.SupabaseClientProvider>()
        if (provider is ireader.data.remote.NoOpSupabaseClientProvider) {
            // No Supabase configured, use NoOp
            ireader.data.repository.NoOpPopularBooksRepository()
        } else {
            try {
                val supabaseClient = (provider as ireader.data.remote.MultiSupabaseClientProvider).libraryClient
                ireader.data.popular.PopularBooksRepositoryImpl(
                    supabaseClient = supabaseClient,
                    backendService = get()
                )
            } catch (e: Exception) {
                ireader.data.repository.NoOpPopularBooksRepository()
            }
        }
    }
    
    // All reviews repository
    single<ireader.domain.data.repository.AllReviewsRepository> {
        val provider = get<ireader.domain.data.repository.SupabaseClientProvider>()
        if (provider is ireader.data.remote.NoOpSupabaseClientProvider) {
            // No Supabase configured, use NoOp
            ireader.data.repository.NoOpAllReviewsRepository()
        } else {
            try {
                val supabaseClient = (provider as ireader.data.remote.MultiSupabaseClientProvider).bookReviewsClient
                ireader.data.review.AllReviewsRepositoryImpl(
                    supabaseClient = supabaseClient,
                    backendService = get()
                )
            } catch (e: Exception) {
                ireader.data.repository.NoOpAllReviewsRepository()
            }
        }
    }
    
    // Donation leaderboard repository
    single<ireader.domain.data.repository.DonationLeaderboardRepository> {
        val provider = get<ireader.domain.data.repository.SupabaseClientProvider>()
        if (provider is ireader.data.remote.NoOpSupabaseClientProvider) {
            // No Supabase configured, use NoOp
            ireader.data.repository.NoOpDonationLeaderboardRepository()
        } else {
            try {
                // Get analytics client from multi-project provider
                val supabaseClient = (provider as ireader.data.remote.MultiSupabaseClientProvider).analyticsClient
                ireader.data.donationleaderboard.DonationLeaderboardRepositoryImpl(
                    supabaseClient = supabaseClient,
                    backendService = get()
                )
            } catch (e: Exception) {
                // Fallback to NoOp if something goes wrong
                ireader.data.repository.NoOpDonationLeaderboardRepository()
            }
        }
    }
}