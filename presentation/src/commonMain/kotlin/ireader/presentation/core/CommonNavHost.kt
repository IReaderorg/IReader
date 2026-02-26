package ireader.presentation.core

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.savedstate.read
import ireader.domain.catalogs.CatalogStore
import ireader.presentation.core.ui.AboutSettingSpec
import ireader.presentation.core.ui.AdminBadgeVerificationScreenSpec
import ireader.presentation.core.ui.AdvanceSettingSpec
import ireader.presentation.core.ui.AppearanceScreenSpec
import ireader.presentation.core.ui.BackupAndRestoreScreenSpec
import ireader.presentation.core.ui.BadgeManagementScreenSpec
import ireader.presentation.core.ui.BadgeStoreScreenSpec
import ireader.presentation.core.ui.BookDetailScreenSpec
import ireader.presentation.core.ui.BrowseSettingsScreenSpec
import ireader.presentation.core.ui.CategoryScreenSpec
import ireader.presentation.core.ui.ChangelogScreenSpec
import ireader.presentation.core.ui.ChatGptLoginScreenSpec
import ireader.presentation.core.ui.CloudBackupScreenSpec
import ireader.presentation.core.ui.DeepSeekLoginScreenSpec
import ireader.presentation.core.ui.DonationScreenSpec
import ireader.presentation.core.ui.DownloaderScreenSpec
import ireader.presentation.core.ui.ExploreScreenSpec
import ireader.presentation.core.ui.FontScreenSpec
import ireader.presentation.core.ui.GeneralScreenSpec
import ireader.presentation.core.ui.GlobalSearchScreenSpec
import ireader.presentation.core.ui.GoogleDriveBackupScreenSpec
import ireader.presentation.core.ui.LocalNavigationViewModelStore
import ireader.presentation.core.ui.NFTBadgeScreenSpec
import ireader.presentation.core.ui.NavigationViewModelStore
import ireader.presentation.core.ui.ReaderScreenSpec
import ireader.presentation.core.ui.ReaderSettingSpec
import ireader.presentation.core.ui.RepositoryAddScreenSpec
import ireader.presentation.core.ui.RepositoryScreenSpec
import ireader.presentation.core.ui.SecuritySettingSpec
import ireader.presentation.core.ui.SettingScreenSpec
import ireader.presentation.core.ui.SourceMigrationScreenSpec
import ireader.presentation.core.ui.SyncScreenSpec
import ireader.presentation.core.ui.TTSEngineManagerScreenSpec
import ireader.presentation.core.ui.TTSV2ScreenSpec
import ireader.presentation.core.ui.TrackingSettingsScreenSpec
import ireader.presentation.core.ui.TranslationScreenSpec
import ireader.presentation.core.ui.WebViewScreenSpec
import ireader.presentation.core.ui.getIViewModel
import ireader.presentation.ui.home.sources.extension.SourceDetailScreen
import ireader.presentation.ui.plugins.integration.FeaturePluginIntegration
import ireader.presentation.ui.plugins.integration.PluginNavigationExtensions
import org.koin.compose.koinInject

/**
 * Helper function to safely get a String argument from NavBackStackEntry.
 * Uses arguments.read {} which is the correct KMP way to access navigation arguments.
 * Falls back to savedStateHandle if arguments is null (for restored state).
 */
private fun NavBackStackEntry.getStringArg(key: String): String? {
    return arguments?.read { getStringOrNull(key) }
        ?: savedStateHandle.get<String>(key)
}

/**
 * Helper function to safely get a Boolean argument from NavBackStackEntry.
 */
private fun NavBackStackEntry.getBooleanArg(key: String, default: Boolean = false): Boolean {
    return arguments?.read { getBooleanOrNull(key) }
        ?: savedStateHandle.get<Boolean>(key)
        ?: default
}

@ExperimentalMaterial3Api
@Composable
fun CommonNavHost(
    navController: NavHostController,
    startDestination: String = "main",
    additionalRoutes: (NavGraphBuilder.() -> Unit)? = null
) {
    val viewModelStore = remember { NavigationViewModelStore() }
    
    // Get FeaturePluginIntegration at composable level for plugin screen registration
    // Use getKoin().getOrNull() instead of try-catch around koinInject (composable)
    val koin = org.koin.compose.getKoin()
    val featurePluginIntegration: FeaturePluginIntegration? = remember {
        koin.getOrNull<FeaturePluginIntegration>()
    }
    
    androidx.compose.runtime.CompositionLocalProvider(
        LocalNavigationViewModelStore provides viewModelStore
    ) {
        AnimatedNavHost(
            navController = navController,
            startDestination = startDestination,
            backgroundColor = androidx.compose.material3.MaterialTheme.colorScheme.background
        ) {
        // Main screen
        composable("main") {
            MainStarterScreen()
        }
        
        // Settings routes
        composable(NavigationRoutes.profile) {
            ireader.presentation.ui.settings.auth.ProfileScreen().Content()
        }
        composable(NavigationRoutes.auth) {
            ireader.presentation.ui.settings.auth.AuthScreen().Content()
        }
        composable(NavigationRoutes.settings) {
            SettingScreenSpec().Content()
        }
        composable(NavigationRoutes.appearance) {
            AppearanceScreenSpec().Content()
        }
        composable(NavigationRoutes.donation) {
            DonationScreenSpec().Content()
        }
        composable(NavigationRoutes.cloudBackup) {
            CloudBackupScreenSpec().Content()
        }
        composable(NavigationRoutes.googleDriveBackup) {
            GoogleDriveBackupScreenSpec().Content()
        }
        composable(NavigationRoutes.supabaseConfig) {
            ireader.presentation.ui.settings.sync.SupabaseConfigScreen().Content()
        }
        
        // Badge routes
        composable(NavigationRoutes.badgeStore) {
            BadgeStoreScreenSpec().Content()
        }
        composable(NavigationRoutes.nftBadge) {
            NFTBadgeScreenSpec().Content()
        }
        composable(NavigationRoutes.badgeManagement) {
            BadgeManagementScreenSpec().Content()
        }
        composable(NavigationRoutes.adminBadgeVerification) {
            AdminBadgeVerificationScreenSpec().Content()
        }
        
        composable(NavigationRoutes.leaderboard) {
            ireader.presentation.core.ui.LeaderboardScreenSpec().Content()
        }
        
        composable(NavigationRoutes.popularBooks) {
            ireader.presentation.core.ui.PopularBooksScreenSpec().Content()
        }
        
        composable(NavigationRoutes.allReviews) {
            ireader.presentation.core.ui.AllReviewsScreenSpec().Content()
        }
        
        // Reading Hub - unified statistics, buddy, and quotes screen
        composable(NavigationRoutes.readingHub) {
            ireader.presentation.core.ui.ReadingHubScreenSpec().Content()
        }
        
        // My Quotes Screen - View and manage saved quotes (replaces old quotes/submit screens)
        composable(NavigationRoutes.myQuotes) {
            val vm: ireader.presentation.ui.quote.MyQuotesViewModel = getIViewModel()
            val navController = requireNotNull(LocalNavigator.current)
            
            ireader.presentation.ui.community.QuotesScreen(
                vm = vm,
                onBack = { navController.popBackStack() }
            )
        }
        
        // Community Hub - parent screen for all community features
        composable(NavigationRoutes.communityHub) {
            ireader.presentation.core.ui.CommunityHubScreenSpec().Content()
        }
        
        // Character Art Gallery
        composable(NavigationRoutes.characterArtGallery) {
            ireader.presentation.core.ui.CharacterArtGalleryScreenSpec().Content()
        }
        
        // Character Art Upload - single route with optional parameters
        composable(
            route = "characterArtUpload?bookTitle={bookTitle}&chapterTitle={chapterTitle}&prompt={prompt}",
            arguments = listOf(
                navArgument("bookTitle") { type = NavType.StringType; defaultValue = "" },
                navArgument("chapterTitle") { type = NavType.StringType; defaultValue = "" },
                navArgument("prompt") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val bookTitle = remember(backStackEntry) {
                (backStackEntry.getStringArg("bookTitle") ?: "").decodeUrlParam()
            }
            val chapterTitle = remember(backStackEntry) {
                (backStackEntry.getStringArg("chapterTitle") ?: "").decodeUrlParam()
            }
            val prompt = remember(backStackEntry) {
                val encoded = backStackEntry.getStringArg("prompt") ?: ""
                try {
                    // Try to decode as Base64 first
                    if (encoded.isNotBlank()) {
                        kotlin.io.encoding.Base64.decode(encoded).decodeToString()
                    } else ""
                } catch (e: Exception) {
                    // Fallback to URL decoding
                    encoded.decodeUrlParam()
                }
            }
            ireader.presentation.core.ui.CharacterArtUploadScreenSpec(
                prefilledBookTitle = bookTitle,
                prefilledChapterTitle = chapterTitle,
                prefilledPrompt = prompt
            ).Content()
        }
        
        // Character Art Detail - with artId parameter
        composable(
            route = "${NavigationRoutes.characterArtDetail}/{artId}",
            arguments = listOf(
                navArgument("artId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val artId = remember(backStackEntry) {
                backStackEntry.getStringArg("artId")
            } ?: return@composable
            ireader.presentation.core.ui.CharacterArtDetailScreenSpec(artId).Content()
        }
        
        // Glossary Screen - Community feature for managing book glossaries
        composable(NavigationRoutes.glossary) {
            ireader.presentation.core.ui.GlossaryScreenSpec().Content()
        }
        
        // Quote Creation Screen - Create quotes from reader copy mode
        composable(
            route = "${NavigationRoutes.quoteCreation}?bookId={bookId}&bookTitle={bookTitle}&chapterTitle={chapterTitle}&chapterNumber={chapterNumber}&author={author}&currentChapterId={currentChapterId}&prevChapterId={prevChapterId}&nextChapterId={nextChapterId}",
            arguments = listOf(
                navArgument("bookId") { type = NavType.StringType },
                navArgument("bookTitle") { type = NavType.StringType },
                navArgument("chapterTitle") { type = NavType.StringType },
                navArgument("chapterNumber") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("author") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("currentChapterId") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("prevChapterId") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("nextChapterId") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            val bookIdValue = remember(backStackEntry) {
                backStackEntry.getStringArg("bookId")?.toLongOrNull()
            }
            val bookTitle = remember(backStackEntry) {
                (backStackEntry.getStringArg("bookTitle") ?: "").decodeUrlParam()
            }
            val chapterTitle = remember(backStackEntry) {
                (backStackEntry.getStringArg("chapterTitle") ?: "").decodeUrlParam()
            }
            val chapterNumber = remember(backStackEntry) {
                backStackEntry.getStringArg("chapterNumber")?.toIntOrNull()
            }
            val author = remember(backStackEntry) {
                backStackEntry.getStringArg("author")?.decodeUrlParam()
            }
            val currentChapterId = remember(backStackEntry) {
                backStackEntry.getStringArg("currentChapterId")?.toLongOrNull()
            }
            val prevChapterId = remember(backStackEntry) {
                backStackEntry.getStringArg("prevChapterId")?.toLongOrNull()
            }
            val nextChapterId = remember(backStackEntry) {
                backStackEntry.getStringArg("nextChapterId")?.toLongOrNull()
            }
            
            // Early return if required params are missing
            val bookId = bookIdValue ?: return@composable
            if (bookTitle.isEmpty() || chapterTitle.isEmpty()) return@composable
            
            val params = ireader.domain.models.quote.QuoteCreationParams(
                bookId = bookId,
                bookTitle = bookTitle,
                chapterTitle = chapterTitle,
                chapterNumber = chapterNumber,
                author = author,
                currentChapterId = currentChapterId,
                prevChapterId = prevChapterId,
                nextChapterId = nextChapterId
            )
            
            val vm: ireader.presentation.ui.quote.QuoteCreationViewModel = getIViewModel(
                parameters = { org.koin.core.parameter.parametersOf(params) }
            )
            val navController = requireNotNull(LocalNavigator.current)
            
            ireader.presentation.ui.quote.QuoteCreationScreen(
                vm = vm,
                onBack = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }
        
        // My Quotes Screen - View and manage saved quotes
        composable(NavigationRoutes.myQuotes) {
            val vm: ireader.presentation.ui.quote.MyQuotesViewModel = getIViewModel()
            val navController = requireNotNull(LocalNavigator.current)
            
            ireader.presentation.ui.community.QuotesScreen(
                vm = vm,
                onBack = { navController.popBackStack() }
            )
        }
        
        // Community Source Config - Configure community translation sharing
        composable(NavigationRoutes.communitySourceConfig) {
            ireader.presentation.ui.settings.community.CommunitySourceConfigScreen().Content()
        }
        
        // Admin User Panel - Admin feature for managing users, badges, and passwords
        composable(NavigationRoutes.adminUserPanel) {
            ireader.presentation.core.ui.AdminUserPanelScreenSpec().Content()
        }
        
        // Feature Store - Plugin monetization marketplace
        composable(NavigationRoutes.featureStore) {
            ireader.presentation.core.ui.FeatureStoreScreenSpec().Content()
        }
        
        // Plugin Repository - Manage plugin sources
        composable(NavigationRoutes.pluginRepository) {
            ireader.presentation.core.ui.PluginRepositoryScreenSpec().Content()
        }
        
        // Plugin Management - Manage installed plugins
        composable(NavigationRoutes.pluginManagement) {
            ireader.presentation.core.ui.PluginManagementScreenSpec().Content()
        }
        
        // Developer Portal - For plugin developers with dev badge
        composable(NavigationRoutes.developerPortal) {
            ireader.presentation.core.ui.DeveloperPortalScreenSpec().Content()
        }
        
        // User Sources - Create custom sources for scraping
        composable(NavigationRoutes.userSources) {
            ireader.presentation.core.ui.UserSourcesListScreenSpec().Content()
        }
        
        // Legado Source Import - Import sources from Legado/阅读 format
        composable(NavigationRoutes.legadoSourceImport) {
            ireader.presentation.core.ui.LegadoSourceImportScreenSpec().Content()
        }
        
        // User Source Creator - Create/edit a user source
        composable(
            route = "userSourceCreator?sourceUrl={sourceUrl}",
            arguments = listOf(
                navArgument("sourceUrl") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val sourceUrl = remember(backStackEntry) {
                backStackEntry.getStringArg("sourceUrl")
            }
            ireader.presentation.core.ui.UserSourceCreatorScreenSpec(sourceUrl).Content()
        }
        
        // Plugin Details - Show detailed plugin information
        composable(
            route = NavigationRoutes.PLUGIN_DETAILS,
            arguments = listOf(
                navArgument("pluginId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val pluginId = backStackEntry.getStringArg("pluginId") ?: return@composable
            ireader.presentation.core.ui.PluginDetailsScreenSpec(pluginId).Content()
        }
        
        composable(NavigationRoutes.translationSettings) {
            TranslationScreenSpec().Content()
        }
        composable(NavigationRoutes.backupRestore) {
            BackupAndRestoreScreenSpec().Content()
        }
        composable(NavigationRoutes.category) {
            CategoryScreenSpec().Content()
        }
        composable(NavigationRoutes.downloader) {
            DownloaderScreenSpec.Content()
        }
        composable(NavigationRoutes.fontSettings) {
            FontScreenSpec().Content()
        }
        composable(NavigationRoutes.about) {
            AboutSettingSpec().Content()
        }
        composable(NavigationRoutes.generalSettings) {
            GeneralScreenSpec().Content()
        }
        composable(NavigationRoutes.readerSettings) {
            ReaderSettingSpec().Content()
        }
        composable(NavigationRoutes.securitySettings) {
            SecuritySettingSpec().Content()
        }
        composable(NavigationRoutes.repository) {
            RepositoryScreenSpec().Content()
        }
        composable(NavigationRoutes.repositoryAdd) {
            RepositoryAddScreenSpec().Content()
        }
        composable(NavigationRoutes.changelog) {
            ChangelogScreenSpec().Content()
        }
        composable(NavigationRoutes.advanceSettings) {
            AdvanceSettingSpec().Content()
        }
        composable(NavigationRoutes.jsPluginSettings) {
            ireader.presentation.core.ui.JSPluginSettingsScreenSpec().Content()
        }
            composable(NavigationRoutes.ttsEngineManager) {
            TTSEngineManagerScreenSpec().Content()
        }
        composable(NavigationRoutes.browseSettings) {
            BrowseSettingsScreenSpec().Content()
        }
        
        composable(NavigationRoutes.trackingSettings) {
            TrackingSettingsScreenSpec().Content()
        }
        
        // Text Replacement Settings
        composable(NavigationRoutes.textReplacement) {
            val viewModel: ireader.presentation.ui.settings.textreplacement.TextReplacementViewModel = getIViewModel()
            ireader.presentation.ui.settings.textreplacement.TextReplacementScreen(vm = viewModel)
        }
        
        // WiFi Sync Settings
        composable(NavigationRoutes.wifiSync) {
            SyncScreenSpec().Content()
        }
        
        // Cloudflare Bypass Settings
        composable(NavigationRoutes.cloudflareBypass) {
            ireader.presentation.core.ui.CloudflareBypassSettingsScreenSpec().Content()
        }
        
        // Routes with parameters
        composable(
            route = "bookDetail/{bookId}",
            arguments = listOf(navArgument("bookId") { 
                type = NavType.StringType 
            })
        ) { backStackEntry ->
            // Use arguments from NavBackStackEntry directly - more reliable than savedStateHandle
            val bookId = remember(backStackEntry) {
                backStackEntry.getStringArg("bookId")?.toLongOrNull()
            }
            if (bookId != null) {
                BookDetailScreenSpec(bookId).Content()
            }
        }
        
        composable(
            route = "reader/{bookId}/{chapterId}",
            arguments = listOf(
                navArgument("bookId") { type = NavType.StringType },
                navArgument("chapterId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val bookId = remember(backStackEntry) {
                backStackEntry.getStringArg("bookId")?.toLongOrNull()
            }
            val chapterId = remember(backStackEntry) {
                backStackEntry.getStringArg("chapterId")?.toLongOrNull()
            }
            if (bookId != null && chapterId != null) {
                // Reader depends on background-loaded modules, show loading until ready
                ireader.presentation.core.ui.ModuleLoadingGuard(
                    loadingMessage = "Preparing reader..."
                ) {
                    ReaderScreenSpec(bookId, chapterId).Content()
                }
            }
        }
        
        composable(
            route = "explore/{sourceId}",
            arguments = listOf(
                navArgument("sourceId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val sourceId = remember(backStackEntry) {
                backStackEntry.getStringArg("sourceId")?.toLongOrNull()
            }
            if (sourceId != null) {
                    ExploreScreenSpec(sourceId, null).Content()
            }
        }
        
        // Global search - base route without query
        composable(route = "globalSearch") {
            GlobalSearchScreenSpec(null).Content()
        }
        
        // Global search with query parameter
        composable(
            route = "globalSearch?query={query}",
            arguments = listOf(
                navArgument("query") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val query = remember(backStackEntry) {
                backStackEntry.getStringArg("query")
            }
            GlobalSearchScreenSpec(query).Content()
        }
        
        composable(
            route = "sourceMigration/{sourceId}",
            arguments = listOf(
                navArgument("sourceId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val sourceId = remember(backStackEntry) {
                backStackEntry.getStringArg("sourceId")?.toLongOrNull()
            }
            if (sourceId != null) {
                    SourceMigrationScreenSpec(
                        sourceId = sourceId,
                        onBackPressed = { navController.safePopBackStack() }
                    ).Content()
            }
        }
        
        composable(
            route = "webView?url={url}&sourceId={sourceId}&bookId={bookId}&chapterId={chapterId}&enableBookFetch={enableBookFetch}&enableChapterFetch={enableChapterFetch}&enableChaptersFetch={enableChaptersFetch}",
            arguments = listOf(
                navArgument("url") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("sourceId") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("bookId") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("chapterId") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("enableBookFetch") { 
                    type = NavType.BoolType
                    defaultValue = false
                },
                navArgument("enableChapterFetch") { 
                    type = NavType.BoolType
                    defaultValue = false
                },
                navArgument("enableChaptersFetch") { 
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val url = remember(backStackEntry) {
                backStackEntry.getStringArg("url")
            }
            val sourceId = remember(backStackEntry) {
                backStackEntry.getStringArg("sourceId")?.toLongOrNull()
            }
            val bookId = remember(backStackEntry) {
                backStackEntry.getStringArg("bookId")?.toLongOrNull()
            }
            val chapterId = remember(backStackEntry) {
                backStackEntry.getStringArg("chapterId")?.toLongOrNull()
            }
            val enableBookFetch = remember(backStackEntry) {
                backStackEntry.getBooleanArg("enableBookFetch", false)
            }
            val enableChapterFetch = remember(backStackEntry) {
                backStackEntry.getBooleanArg("enableChapterFetch", false)
            }
            val enableChaptersFetch = remember(backStackEntry) {
                backStackEntry.getBooleanArg("enableChaptersFetch", false)
            }
            
            WebViewScreenSpec(
                url = url,
                sourceId = sourceId,
                bookId = bookId,
                chapterId = chapterId,
                enableBookFetch = enableBookFetch,
                enableChapterFetch = enableChapterFetch,
                enableChaptersFetch = enableChaptersFetch
            ).Content()
        }
        
        // TTS Screen - Clean architecture v2
        composable(
            route = NavigationRoutes.TTS_V2,
            arguments = listOf(
                navArgument("bookId") { type = NavType.StringType },
                navArgument("chapterId") { type = NavType.StringType },
                navArgument("sourceId") { type = NavType.StringType },
                navArgument("readingParagraph") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val bookId = remember(backStackEntry) {
                backStackEntry.getStringArg("bookId")?.toLongOrNull()
            }
            val chapterId = remember(backStackEntry) {
                backStackEntry.getStringArg("chapterId")?.toLongOrNull()
            }
            val sourceId = remember(backStackEntry) {
                backStackEntry.getStringArg("sourceId")?.toLongOrNull()
            }
            val readingParagraph = remember(backStackEntry) {
                backStackEntry.getStringArg("readingParagraph")?.toIntOrNull() ?: 0
            }
            if (bookId != null && chapterId != null && sourceId != null) {
                TTSV2ScreenSpec(bookId, chapterId, sourceId, readingParagraph).Content()
            }
        }
        
        composable("chatGptLogin") {
            ChatGptLoginScreenSpec().Content()
        }
        
        composable("deepSeekLogin") {
            DeepSeekLoginScreenSpec().Content()
        }
        composable(NavigationRoutes.badgeStore) {
            BadgeStoreScreenSpec().Content()
        }
        composable(NavigationRoutes.badgeManagement) {
            BadgeManagementScreenSpec().Content()
        }
        composable(NavigationRoutes.adminBadgeVerification) {
            AdminBadgeVerificationScreenSpec().Content()
        }
        composable(NavigationRoutes.nftBadge) {
            NFTBadgeScreenSpec().Content()
        }
        
        composable(
            route = "sourceDetail/{sourceId}",
            arguments = listOf(
                navArgument("sourceId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val sourceId = remember(backStackEntry) {
                backStackEntry.getStringArg("sourceId")?.toLongOrNull()
            }
            if (sourceId != null) {
                val catalogStore: CatalogStore = koinInject()
                val catalogRemoteRepository: ireader.domain.catalogs.service.CatalogRemoteRepository = koinInject()
                
                // First check local catalogs, then remote catalogs
                val catalog = remember(sourceId) {
                    catalogStore.catalogs.find { it.sourceId == sourceId }
                }
                
                val remoteCatalog = androidx.compose.runtime.produceState<ireader.domain.models.entities.CatalogRemote?>(null, sourceId) {
                    if (catalog == null) {
                        value = catalogRemoteRepository.getRemoteCatalogs().find { it.sourceId == sourceId }
                    }
                }
                
                val finalCatalog = catalog ?: remoteCatalog.value
                if (finalCatalog != null) {
                    SourceDetailScreen(finalCatalog).Content()
                }
            }
        }
        
        // Allow additional platform-specific routes
        additionalRoutes?.invoke(this)
        
        // Register plugin screens dynamically
        // This allows feature plugins to add their own screens to the navigation graph
        // FeaturePluginIntegration is injected at composable level above
        if (featurePluginIntegration != null) {
            try {
                PluginNavigationExtensions.registerPluginScreens(this, featurePluginIntegration)
            } catch (e: Exception) {
                // Plugin screen registration failed - ignore
            }
        }
        }
    }
}

/**
 * Decode URL-encoded parameter
 */
private fun String.decodeUrlParam(): String {
    return this
        .replace("%26", "&")
        .replace("%3D", "=")
        .replace("%3F", "?")
        .replace("%2F", "/")
        .replace("%20", " ")
}
