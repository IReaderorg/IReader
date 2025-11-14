package org.ireader.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.util.Consumer
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil3.SingletonImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.setSingletonImageLoaderFactory
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import ireader.core.http.toast
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.usecases.backup.AutomaticBackup
import ireader.domain.usecases.files.AndroidGetSimpleStorage
import ireader.domain.utils.extensions.launchIO
import ireader.i18n.Args
import ireader.i18n.SHORTCUTS.SHORTCUT_DETAIL
import ireader.i18n.SHORTCUTS.SHORTCUT_DOWNLOAD
import ireader.i18n.SHORTCUTS.SHORTCUT_READER
import ireader.i18n.SHORTCUTS.SHORTCUT_TTS
import ireader.presentation.core.MainStarterScreen
import ireader.presentation.core.NavigationRoutes
import ireader.presentation.core.ProvideNavigator
import ireader.presentation.core.popUntilRoot
import ireader.presentation.core.theme.AppTheme
import ireader.presentation.core.theme.LocaleHelper
import ireader.presentation.core.ui.BookDetailScreenSpec
import ireader.presentation.core.ui.DownloaderScreenSpec
import ireader.presentation.core.ui.ReaderScreenSpec
import ireader.presentation.core.ui.TTSScreenSpec
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.core.theme.themes
import ireader.presentation.ui.core.ui.asStateIn
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.ireader.app.initiators.AppInitializers
import org.ireader.app.initiators.GetPermissions
import org.ireader.app.initiators.SecureActivityDelegateImpl
import org.koin.android.ext.android.inject
import org.koin.compose.KoinContext
import kotlin.time.Duration.Companion.seconds

class MainActivity : ComponentActivity(), SecureActivityDelegate by SecureActivityDelegateImpl() {


    private val getSimpleStorage: AndroidGetSimpleStorage by inject()
    private val uiPreferences: UiPreferences by inject()
    val initializers: AppInitializers by inject()
    private val automaticBackup: AutomaticBackup by inject()
    private val localeHelper: LocaleHelper by inject()
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalCoilApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerSecureActivity(this, uiPreferences, initializers)
        
        // Provide activity to storage helper
        getSimpleStorage.provideActivity(this, null)
        
        // Set up window to handle gesture navigation
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Initialize automatic backup in the background
        lifecycleScope.launchIO {
            automaticBackup.initialize()
        }
        
        // Set up logging
        Napier.base(DebugAntilog())
        
        // Set locale
        localeHelper.setLocaleLang()
        
        // Install splash screen
        installSplashScreen()

        // Request all necessary permissions early
        lifecycleScope.launch {
            // Delay slightly to let the UI initialize
            delay(500)
            // Request important storage permissions based on Android version
            requestNecessaryPermissions()
        }

        setContent {
            val context = LocalContext.current
            SetDefaultTheme()
            KoinContext {
                setSingletonImageLoaderFactory { context ->
                    (this@MainActivity.application as SingletonImageLoader.Factory).newImageLoader(
                        context = context
                    )
                }
                AppTheme(this.lifecycleScope) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ) {
                        val navController = rememberNavController()
                        
                        ProvideNavigator(navController) {
                            // Handle exit confirmation when on main screen
                            if (navController.previousBackStackEntry == null) {
                                ConfirmExit()
                            }
                            
                            LaunchedEffect(Unit) {
                                handleIntentAction(this@MainActivity.intent, navController)
                            }
                            
                            IScaffold {
                                NavHost(
                                    navController = navController,
                                    startDestination = "main"
                                ) {
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
                                        ireader.presentation.core.ui.SettingScreenSpec().Content()
                                    }
                                    composable(NavigationRoutes.appearance) {
                                        ireader.presentation.core.ui.AppearanceScreenSpec().Content()
                                    }
                                    composable(NavigationRoutes.statistics) {
                                        ireader.presentation.core.ui.StatisticsScreenSpec().Content()
                                    }
                                    composable(NavigationRoutes.donation) {
                                        ireader.presentation.core.ui.DonationScreenSpec().Content()
                                    }
                                    composable(NavigationRoutes.cloudBackup) {
                                        ireader.presentation.core.ui.CloudBackupScreenSpec().Content()
                                    }
                                    composable(NavigationRoutes.supabaseConfig) {
                                        ireader.presentation.ui.settings.sync.SupabaseConfigScreen().Content()
                                    }
                                    composable(NavigationRoutes.translationSettings) {
                                        ireader.presentation.core.ui.TranslationScreenSpec().Content()
                                    }
                                    composable(NavigationRoutes.backupRestore) {
                                        ireader.presentation.core.ui.BackupAndRestoreScreenSpec().Content()
                                    }
                                    composable(NavigationRoutes.category) {
                                        ireader.presentation.core.ui.CategoryScreenSpec().Content()
                                    }
                                    composable(NavigationRoutes.downloader) {
                                        DownloaderScreenSpec().Content()
                                    }
                                    composable(NavigationRoutes.fontSettings) {
                                        ireader.presentation.core.ui.FontScreenSpec().Content()
                                    }
                                    composable(NavigationRoutes.about) {
                                        ireader.presentation.core.ui.AboutSettingSpec().Content()
                                    }
                                    composable(NavigationRoutes.generalSettings) {
                                        ireader.presentation.core.ui.GeneralScreenSpec().Content()
                                    }
                                    composable(NavigationRoutes.readerSettings) {
                                        ireader.presentation.core.ui.ReaderSettingSpec().Content()
                                    }
                                    composable(NavigationRoutes.securitySettings) {
                                        ireader.presentation.core.ui.SecuritySettingSpec().Content()
                                    }
                                    composable(NavigationRoutes.repository) {
                                        ireader.presentation.core.ui.RepositoryScreenSpec().Content()
                                    }
                                    composable(NavigationRoutes.repositoryAdd) {
                                        ireader.presentation.core.ui.RepositoryAddScreenSpec().Content()
                                    }
                                    composable(NavigationRoutes.changelog) {
                                        ireader.presentation.core.ui.ChangelogScreenSpec().Content()
                                    }
                                    composable(NavigationRoutes.advanceSettings) {
                                        ireader.presentation.core.ui.AdvanceSettingSpec().Content()
                                    }
                                    composable(NavigationRoutes.ttsEngineManager) {
                                        ireader.presentation.core.ui.TTSEngineManagerScreenSpec().Content()
                                    }
                                    // Routes with parameters
                                    composable(
                                        route = "bookDetail/{bookId}",
                                        arguments = listOf(androidx.navigation.navArgument("bookId") { 
                                            type = androidx.navigation.NavType.StringType 
                                        })
                                    ) { backStackEntry ->
                                        val bookId = androidx.compose.runtime.remember(backStackEntry) {
                                            backStackEntry.savedStateHandle.get<String>("bookId")?.toLongOrNull()
                                        }
                                        if (bookId != null) {
                                            androidx.compose.runtime.key(bookId) {
                                                BookDetailScreenSpec(bookId).Content()
                                            }
                                        }
                                    }
                                    composable(
                                        route = "reader/{bookId}/{chapterId}",
                                        arguments = listOf(
                                            androidx.navigation.navArgument("bookId") { type = androidx.navigation.NavType.StringType },
                                            androidx.navigation.navArgument("chapterId") { type = androidx.navigation.NavType.StringType }
                                        )
                                    ) { backStackEntry ->
                                        val bookId = androidx.compose.runtime.remember(backStackEntry) {
                                            backStackEntry.savedStateHandle.get<String>("bookId")?.toLongOrNull()
                                        }
                                        val chapterId = androidx.compose.runtime.remember(backStackEntry) {
                                            backStackEntry.savedStateHandle.get<String>("chapterId")?.toLongOrNull()
                                        }
                                        if (bookId != null && chapterId != null) {
                                            androidx.compose.runtime.key(bookId, chapterId) {
                                                ReaderScreenSpec(bookId, chapterId).Content()
                                            }
                                        }
                                    }
                                    composable(
                                        route = "explore/{sourceId}",
                                        arguments = listOf(
                                            androidx.navigation.navArgument("sourceId") { type = androidx.navigation.NavType.StringType }
                                        )
                                    ) { backStackEntry ->
                                        val sourceId = androidx.compose.runtime.remember(backStackEntry) {
                                            backStackEntry.savedStateHandle.get<String>("sourceId")?.toLongOrNull()
                                        }
                                        if (sourceId != null) {
                                            androidx.compose.runtime.key(sourceId) {
                                                ireader.presentation.core.ui.ExploreScreenSpec(sourceId, null).Content()
                                            }
                                        }
                                    }
                                    composable(
                                        route = "globalSearch"
                                    ) { backStackEntry ->
                                        ireader.presentation.core.ui.GlobalSearchScreenSpec(null).Content()
                                    }
                                    composable(
                                        route = "webView"
                                    ) {
                                        // Android WebView - handled by WebViewScreenSpec
                                    }
                                    composable(
                                        route = "tts/{bookId}/{chapterId}/{sourceId}/{readingParagraph}",
                                        arguments = listOf(
                                            androidx.navigation.navArgument("bookId") { type = androidx.navigation.NavType.StringType },
                                            androidx.navigation.navArgument("chapterId") { type = androidx.navigation.NavType.StringType },
                                            androidx.navigation.navArgument("sourceId") { type = androidx.navigation.NavType.StringType },
                                            androidx.navigation.navArgument("readingParagraph") { type = androidx.navigation.NavType.StringType }
                                        )
                                    ) { backStackEntry ->
                                        val bookId = androidx.compose.runtime.remember(backStackEntry) {
                                            backStackEntry.savedStateHandle.get<String>("bookId")?.toLongOrNull()
                                        }
                                        val chapterId = androidx.compose.runtime.remember(backStackEntry) {
                                            backStackEntry.savedStateHandle.get<String>("chapterId")?.toLongOrNull()
                                        }
                                        val sourceId = androidx.compose.runtime.remember(backStackEntry) {
                                            backStackEntry.savedStateHandle.get<String>("sourceId")?.toLongOrNull()
                                        }
                                        val readingParagraph = androidx.compose.runtime.remember(backStackEntry) {
                                            backStackEntry.savedStateHandle.get<String>("readingParagraph")?.toIntOrNull()
                                        }
                                        if (bookId != null && chapterId != null && sourceId != null && readingParagraph != null) {
                                            androidx.compose.runtime.key(bookId, chapterId, sourceId, readingParagraph) {
                                                TTSScreenSpec(bookId, chapterId, sourceId, readingParagraph).Content()
                                            }
                                        }
                                    }
                                    composable("chatGptLogin") {
                                        ireader.presentation.core.ui.ChatGptLoginScreenSpec().Content()
                                    }
                                    composable("deepSeekLogin") {
                                        ireader.presentation.core.ui.DeepSeekLoginScreenSpec().Content()
                                    }
                                    composable(
                                        route = "sourceDetail/{sourceId}",
                                        arguments = listOf(
                                            androidx.navigation.navArgument("sourceId") { type = androidx.navigation.NavType.StringType }
                                        )
                                    ) { backStackEntry ->
                                        val sourceId = androidx.compose.runtime.remember(backStackEntry) {
                                            backStackEntry.savedStateHandle.get<String>("sourceId")?.toLongOrNull()
                                        }
                                        if (sourceId != null) {
                                            val catalogStore: ireader.domain.catalogs.CatalogStore = org.koin.compose.koinInject()
                                            val catalog = androidx.compose.runtime.remember(sourceId) {
                                                catalogStore.catalogs.find { it.sourceId == sourceId }
                                            }
                                            if (catalog != null) {
                                                androidx.compose.runtime.key(sourceId) {
                                                    ireader.presentation.ui.home.sources.extension.SourceDetailScreen(catalog).Content()
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                // Pass the application context to GetPermissions
                                GetPermissions(uiPreferences, context = this@MainActivity)
                            }

                            HandleOnNewIntent(this, navController)
                        }
                    }
                }
            }
        }
    }

    /**
     * Request necessary permissions based on Android version
     */
    private fun requestNecessaryPermissions() {
        // Handled by GetPermissions composable, but we want to 
        // trigger system dialogs as early as possible
        if (!uiPreferences.savedLocalCatalogLocation().get()) {
            // Let the GetPermissions composable handle the actual permission requests
            // as it has proper UI feedback
        }
    }

    @Composable
    private fun SetDefaultTheme() {
        val themeMode by uiPreferences.themeMode().asStateIn(rememberCoroutineScope())
        val isSystemDarkMode = isSystemInDarkTheme()
        LaunchedEffect(themeMode) {
            if (themes.firstOrNull { it.id == uiPreferences.colorTheme().get() } != null) {
                return@LaunchedEffect
            }


            when (themeMode) {
                PreferenceValues.ThemeMode.System -> {
                    themes.find { it.isDark == isSystemDarkMode }?.let { theme ->
                        uiPreferences.colorTheme().set(theme.id)
                    }
                }

                PreferenceValues.ThemeMode.Dark -> {
                    themes.find { it.isDark }?.let { theme ->
                        uiPreferences.colorTheme().set(theme.id)
                    }
                }

                PreferenceValues.ThemeMode.Light -> {
                    themes.find { !it.isDark }?.let { theme ->
                        uiPreferences.colorTheme().set(theme.id)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        println("🔷 MainActivity.onNewIntent() OVERRIDE called")
        println("   Intent: $intent")
        println("   Data: ${intent.data}")
        println("   Action: ${intent.action}")
        println("   Scheme: ${intent.data?.scheme}")
        println("   Host: ${intent.data?.host}")
        
        // CRITICAL: Update the activity's intent
        setIntent(intent)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        getSimpleStorage.simpleStorageHelper.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        getSimpleStorage.simpleStorageHelper.onRestoreInstanceState(savedInstanceState)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Mandatory for Activity, but not for Fragment & ComponentActivity
        getSimpleStorage.simpleStorageHelper.storage.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Mandatory for Activity, but not for Fragment & ComponentActivity
        getSimpleStorage.simpleStorageHelper.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )
    }

    @Composable
    fun HandleOnNewIntent(context: Context, navController: NavHostController) {
        LaunchedEffect(Unit) {
            callbackFlow<Intent> {
                val componentActivity = context as ComponentActivity
                val consumer = Consumer<Intent> { trySend(it) }
                componentActivity.addOnNewIntentListener(consumer)
                awaitClose { componentActivity.removeOnNewIntentListener(consumer) }
            }.collectLatest { 
                // Set the new intent so it can be accessed by the SDK
                setIntent(it)
                handleIntentAction(it, navController) 
            }
        }
    }

    private fun handleIntentAction(intent: Intent, navController: NavHostController): Boolean {
        // Log all intent details for debugging
        println("🔷 handleIntentAction called")
        println("   Action: ${intent.action}")
        println("   Data: ${intent.data}")
        println("   Scheme: ${intent.data?.scheme}")
        println("   Host: ${intent.data?.host}")
        
        // Check if this is a wallet callback
        val uri = intent.data
        if (uri != null) {
            val scheme = uri.scheme
            // MetaMask SDK callback (uses package name as scheme)
            if (scheme == "ir.kazemcodes.infinityreader.debug" ||
                scheme == "ir.kazemcodes.infinityreader" ||
                scheme == "org.ireader.app" ||
                scheme?.startsWith("ir.kazemcodes") == true ||
                scheme?.startsWith("org.ireader") == true) {
                println("✅ MetaMask SDK callback detected: $uri")
                println("   Full URI: $uri")
                println("   Query: ${uri.query}")
                // The MetaMask SDK automatically handles this when the activity's intent is updated
                return true
            }
            
            // Custom wallet callback
            if (uri.scheme == "ireader" && uri.host == "wallet-callback") {
                println("✅ Custom wallet callback detected: $uri")
                return true
            }
        }
        
        return when (intent.action) {
            SHORTCUT_TTS -> {
                val bookId = intent.extras?.getLong(Args.ARG_BOOK_ID)
                val chapterId = intent.extras?.getLong(Args.ARG_CHAPTER_ID)
                val sourceId = intent.extras?.getLong(Args.ARG_SOURCE_ID)
                val readingParagraph = intent.extras?.getLong(Args.ARG_READING_PARAGRAPH)
                if (bookId != null && chapterId != null && sourceId != null) {
                    navController.popUntilRoot()
                    navController.navigate(
                        NavigationRoutes.tts(
                            bookId,
                            chapterId,
                            sourceId,
                            readingParagraph?.toInt() ?: 0
                        )
                    )
                }
                true
            }
            SHORTCUT_READER -> {
                val bookId = intent.extras?.getLong(Args.ARG_BOOK_ID)
                val chapterId = intent.extras?.getLong(Args.ARG_CHAPTER_ID)
                if (bookId != null && chapterId != null) {
                    navController.popUntilRoot()
                    navController.navigate(NavigationRoutes.reader(bookId, chapterId))
                }
                true
            }
            SHORTCUT_DETAIL -> {
                val bookId = intent.extras?.getLong(Args.ARG_BOOK_ID)
                if (bookId != null) {
                    navController.popUntilRoot()
                    navController.navigate(NavigationRoutes.bookDetail(bookId))
                }
                true
            }
            SHORTCUT_DOWNLOAD -> {
                navController.popUntilRoot()
                navController.navigate(NavigationRoutes.downloader)
                true
            }
            else -> false
        }
    }
    @Composable
    private fun ConfirmExit() {
        val scope = rememberCoroutineScope()
        val confirmExit by uiPreferences.confirmExit().asStateIn(scope)
        var waitingConfirmation by remember { mutableStateOf(false) }
        BackHandler(enabled = !waitingConfirmation && confirmExit) {
            scope.launch {
                waitingConfirmation = true
                val toast = toast("Press back again to exit", Toast.LENGTH_LONG)
                delay(2.seconds)
                toast.cancel()
                waitingConfirmation = false
            }
        }
    }


}


interface SecureActivityDelegate {
    fun registerSecureActivity(activity: ComponentActivity, preferences: UiPreferences,initializers: AppInitializers)
}

