package ireader.presentation.core

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import ireader.domain.catalogs.CatalogStore
import ireader.presentation.core.ui.AboutSettingSpec
import ireader.presentation.core.ui.AdvanceSettingSpec
import ireader.presentation.core.ui.AppearanceScreenSpec
import ireader.presentation.core.ui.BackupAndRestoreScreenSpec
import ireader.presentation.core.ui.BadgeManagementScreenSpec
import ireader.presentation.core.ui.BadgeStoreScreenSpec
import ireader.presentation.core.ui.BookDetailScreenSpec
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
import ireader.presentation.core.ui.NFTBadgeScreenSpec
import ireader.presentation.core.ui.ReaderScreenSpec
import ireader.presentation.core.ui.ReaderSettingSpec
import ireader.presentation.core.ui.RepositoryAddScreenSpec
import ireader.presentation.core.ui.RepositoryScreenSpec
import ireader.presentation.core.ui.SecuritySettingSpec
import ireader.presentation.core.ui.SettingScreenSpec
import ireader.presentation.core.ui.SourceMigrationScreenSpec
import ireader.presentation.core.ui.StatisticsScreenSpec
import ireader.presentation.core.ui.TTSEngineManagerScreenSpec
import ireader.presentation.core.ui.TTSScreenSpec
import ireader.presentation.core.ui.TranslationScreenSpec
import ireader.presentation.core.ui.WebViewScreenSpec
import ireader.presentation.ui.home.sources.extension.SourceDetailScreen
import org.koin.compose.koinInject

/**
 * Common navigation setup for both Android and Desktop platforms.
 * Provides a unified navigation graph with beautiful animations.
 */
@ExperimentalMaterial3Api
@Composable
fun CommonNavHost(
    navController: NavHostController,
    startDestination: String = "main",
    additionalRoutes: (NavGraphBuilder.() -> Unit)? = null
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
        composable(NavigationRoutes.statistics) {
            StatisticsScreenSpec().Content()
        }
        composable(NavigationRoutes.donation) {
            DonationScreenSpec().Content()
        }
        composable(NavigationRoutes.cloudBackup) {
            CloudBackupScreenSpec().Content()
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
            DownloaderScreenSpec().Content()
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
        
        // Routes with parameters
        composable(
            route = "bookDetail/{bookId}",
            arguments = listOf(navArgument("bookId") { 
                type = NavType.StringType 
            })
        ) { backStackEntry ->
            val bookId = remember(backStackEntry) {
                backStackEntry.savedStateHandle.get<String>("bookId")?.toLongOrNull()
            }
            if (bookId != null) {
                key(bookId) {
                    BookDetailScreenSpec(bookId).Content()
                }
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
                backStackEntry.savedStateHandle.get<String>("bookId")?.toLongOrNull()
            }
            val chapterId = remember(backStackEntry) {
                backStackEntry.savedStateHandle.get<String>("chapterId")?.toLongOrNull()
            }
            if (bookId != null && chapterId != null) {
                key(bookId, chapterId) {
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
                backStackEntry.savedStateHandle.get<String>("sourceId")?.toLongOrNull()
            }
            if (sourceId != null) {
                key(sourceId) {
                    ExploreScreenSpec(sourceId, null).Content()
                }
            }
        }
        
        composable(route = "globalSearch") {
            GlobalSearchScreenSpec(null).Content()
        }
        
        composable(
            route = "sourceMigration/{sourceId}",
            arguments = listOf(
                navArgument("sourceId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val sourceId = remember(backStackEntry) {
                backStackEntry.savedStateHandle.get<String>("sourceId")?.toLongOrNull()
            }
            if (sourceId != null) {
                key(sourceId) {
                    SourceMigrationScreenSpec(sourceId).Content()
                }
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
                backStackEntry.savedStateHandle.get<String>("url")
            }
            val sourceId = remember(backStackEntry) {
                backStackEntry.savedStateHandle.get<String>("sourceId")?.toLongOrNull()
            }
            val bookId = remember(backStackEntry) {
                backStackEntry.savedStateHandle.get<String>("bookId")?.toLongOrNull()
            }
            val chapterId = remember(backStackEntry) {
                backStackEntry.savedStateHandle.get<String>("chapterId")?.toLongOrNull()
            }
            val enableBookFetch = remember(backStackEntry) {
                backStackEntry.savedStateHandle.get<Boolean>("enableBookFetch") ?: false
            }
            val enableChapterFetch = remember(backStackEntry) {
                backStackEntry.savedStateHandle.get<Boolean>("enableChapterFetch") ?: false
            }
            val enableChaptersFetch = remember(backStackEntry) {
                backStackEntry.savedStateHandle.get<Boolean>("enableChaptersFetch") ?: false
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
        
        composable(
            route = "tts/{bookId}/{chapterId}/{sourceId}/{readingParagraph}",
            arguments = listOf(
                navArgument("bookId") { type = NavType.StringType },
                navArgument("chapterId") { type = NavType.StringType },
                navArgument("sourceId") { type = NavType.StringType },
                navArgument("readingParagraph") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val bookId = remember(backStackEntry) {
                backStackEntry.savedStateHandle.get<String>("bookId")?.toLongOrNull()
            }
            val chapterId = remember(backStackEntry) {
                backStackEntry.savedStateHandle.get<String>("chapterId")?.toLongOrNull()
            }
            val sourceId = remember(backStackEntry) {
                backStackEntry.savedStateHandle.get<String>("sourceId")?.toLongOrNull()
            }
            val readingParagraph = remember(backStackEntry) {
                backStackEntry.savedStateHandle.get<String>("readingParagraph")?.toIntOrNull()
            }
            if (bookId != null && chapterId != null && sourceId != null && readingParagraph != null) {
                key(bookId, chapterId, sourceId, readingParagraph) {
                    TTSScreenSpec(bookId, chapterId, sourceId, readingParagraph).Content()
                }
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
                backStackEntry.savedStateHandle.get<String>("sourceId")?.toLongOrNull()
            }
            if (sourceId != null) {
                val catalogStore: CatalogStore = koinInject()
                val catalog = remember(sourceId) {
                    catalogStore.catalogs.find { it.sourceId == sourceId }
                }
                if (catalog != null) {
                    key(sourceId) {
                        SourceDetailScreen(catalog).Content()
                    }
                }
            }
        }
        
        // Allow additional platform-specific routes
        additionalRoutes?.invoke(this)
    }
}
