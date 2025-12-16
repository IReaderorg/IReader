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
import androidx.navigation.compose.rememberNavController
import coil3.SingletonImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.setSingletonImageLoaderFactory
import ireader.core.http.toast
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.preferences.prefs.SupabasePreferences
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.usecases.backup.AutomaticBackup

import ireader.domain.utils.extensions.launchIO
import ireader.i18n.Args
import ireader.i18n.SHORTCUTS.SHORTCUT_DETAIL
import ireader.i18n.SHORTCUTS.SHORTCUT_DOWNLOAD
import ireader.i18n.SHORTCUTS.SHORTCUT_DOWNLOADS
import ireader.i18n.SHORTCUTS.SHORTCUT_HISTORY
import ireader.i18n.SHORTCUTS.SHORTCUT_LIBRARY
import ireader.i18n.SHORTCUTS.SHORTCUT_READER
import ireader.i18n.SHORTCUTS.SHORTCUT_SEARCH
import ireader.i18n.SHORTCUTS.SHORTCUT_TTS
import ireader.i18n.SHORTCUTS.SHORTCUT_TTS_V2
import ireader.i18n.SHORTCUTS.SHORTCUT_UPDATES
import ireader.presentation.core.CommonNavHost
import ireader.presentation.core.MainStarterScreen
import ireader.presentation.core.NavigationRoutes
import ireader.presentation.core.ProvideNavigator
import ireader.presentation.core.popUntilRoot
import ireader.presentation.core.theme.AppTheme
import ireader.presentation.core.theme.LocaleHelper
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.LocalPerformanceConfig
import ireader.presentation.ui.component.PerformanceConfig
import ireader.presentation.ui.component.getPerformanceConfigForDevice
import ireader.presentation.ui.core.theme.themes
import ireader.presentation.ui.settings.storage.StorageFolderPickerScreen
import ireader.presentation.ui.core.ui.asStateIn
// FirstLaunchDialog removed - now handled in OnboardingScreen
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

    private val uiPreferences: UiPreferences by inject()
    private val supabasePreferences: SupabasePreferences by inject()
    val initializers: AppInitializers by inject()
    private val automaticBackup: AutomaticBackup by inject()
    private val localeHelper: LocaleHelper by inject()
    
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalCoilApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Install splash screen
        var isContentReady = false
        installSplashScreen().apply {
            setKeepOnScreenCondition { !isContentReady }
        }
        
        // Critical UI setup
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        
        // MUST be on main thread
        registerSecureActivity(this, uiPreferences, initializers)
        // Note: SimpleStorage.provideActivity removed - FileKit handles file picking via Compose
        localeHelper.setLocaleLang()
        
        // Trigger lazy initialization
        (application as? MyApplication)?.onAppVisible()
        
        // Defer heavy initialization to background
        lifecycleScope.launchIO {
            delay(2000)
            automaticBackup.initialize()
            org.ireader.app.util.ExtensionCacheValidator.validateAndCleanExtensionCache(this@MainActivity)
        }
        
        // Handle app shortcuts early - set initial tab before UI is composed
        handleEarlyShortcutIntent(intent)
        
        setContent {
            // Mark content ready immediately
            LaunchedEffect(Unit) {
                isContentReady = true
            }
            
            // Full UI
            val context = LocalContext.current
            val maxPerformanceEnabled = uiPreferences.maxPerformanceMode().get()
            val performanceConfig = remember(maxPerformanceEnabled) { 
                if (maxPerformanceEnabled) {
                    PerformanceConfig.MaxPerformance
                } else {
                    getPerformanceConfigForDevice(this@MainActivity)
                }
            }
            
            SetDefaultTheme()
            KoinContext {
                setSingletonImageLoaderFactory { context ->
                    (this@MainActivity.application as SingletonImageLoader.Factory).newImageLoader(
                        context = context
                    )
                }
                androidx.compose.runtime.CompositionLocalProvider(
                    LocalPerformanceConfig provides performanceConfig
                ) {
                    AppTheme(this.lifecycleScope) {
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ) {
                        // Check if we need to show onboarding screen
                        var hasCompletedOnboarding by remember { 
                            mutableStateOf(uiPreferences.hasCompletedOnboarding().get()) 
                        }
                        var showOnboarding by remember { mutableStateOf(!hasCompletedOnboarding) }
                        
                        if (showOnboarding) {
                            // Show unified onboarding screen (language + storage + cloud setup)
                            ireader.presentation.ui.onboarding.OnboardingScreen(
                                uiPreferences = uiPreferences,
                                supabasePreferences = supabasePreferences,
                                localeHelper = localeHelper,
                                onFolderUriSelected = { uriString ->
                                    // Take persistent SAF permissions for the selected folder
                                    try {
                                        android.util.Log.d("MainActivity", "Folder selected: $uriString")
                                        if (uriString.startsWith("content://")) {
                                            val uri = android.net.Uri.parse(uriString)
                                            ireader.domain.storage.SecureStorageHelper.takePersistentPermissions(
                                                this@MainActivity,
                                                uri
                                            )
                                        }
                                        // Clear cached directory so new path is used
                                        ireader.domain.storage.SecureStorageHelper.clearCache()
                                        android.util.Log.d("MainActivity", "SecureStorageHelper cache cleared")
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                },
                                onComplete = {
                                    hasCompletedOnboarding = true
                                    showOnboarding = false
                                }
                            )
                        } else {
                            // Show main app content
                            val navController = rememberNavController()
                            
                            ProvideNavigator(navController) {
                                if (navController.previousBackStackEntry == null) {
                                    ConfirmExit()
                                }
                                
                                IScaffold {
                                    CommonNavHost(navController)
                                    // Legacy permission handler for users who skipped initial permission
                                    GetPermissions(uiPreferences, context = this@MainActivity)
                                }
                                
                                // Handle initial intent after navigation is set up (like Mihon)
                                LaunchedEffect(navController) {
                                    // Wait for navigation graph to be ready
                                    delay(500)
                                    handleIntentAction(this@MainActivity.intent, navController)
                                }
                                
                                // FirstLaunchDialog removed - now handled in OnboardingScreen
                                HandleOnNewIntent(this, navController)
                            }
                        }
                    }
                }
                }
            }
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
        // CRITICAL: Update the activity's intent
        setIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        // Trigger lazy initialization when app becomes visible
        (application as? MyApplication)?.onAppVisible()
    }

    // Note: SimpleStorage lifecycle methods removed - FileKit handles file picking via Compose

    @Composable
    fun HandleOnNewIntent(context: Context, navController: NavHostController) {
        LaunchedEffect(Unit) {
            callbackFlow<Intent> {
                val componentActivity = context as ComponentActivity
                val consumer = Consumer<Intent> { trySend(it) }
                componentActivity.addOnNewIntentListener(consumer)
                awaitClose { componentActivity.removeOnNewIntentListener(consumer) }
            }.collectLatest { intent ->
                // Set the new intent so it can be accessed by the SDK
                setIntent(intent)
                handleIntentAction(intent, navController, isNewIntent = true) 
            }
        }
    }

    /**
     * Handle shortcut intents early, before the UI is composed.
     * This sets the initial tab for tab-switching shortcuts.
     */
    private fun handleEarlyShortcutIntent(intent: Intent) {
        // Set initial tab based on shortcut action
        when (intent.action) {
            SHORTCUT_LIBRARY -> {
                MainStarterScreen.setInitialTab(0)
            }
            SHORTCUT_UPDATES -> {
                MainStarterScreen.setInitialTab(1)
            }
            SHORTCUT_HISTORY -> {
                MainStarterScreen.setInitialTab(2)
            }
        }
    }
    
    private fun handleIntentAction(intent: Intent, navController: NavHostController, isNewIntent: Boolean = false): Boolean {
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
                return true
            }
            
            // Custom wallet callback
            if (uri.scheme == "ireader" && uri.host == "wallet-callback") {
                println("✅ Custom wallet callback detected: $uri")
                return true
            }
        }
        
        // Handle shortcut actions (like Mihon)
        return when (intent.action) {
            // App Shortcuts Menu actions
            SHORTCUT_SEARCH -> {
                navController.navigate("globalSearch") {
                    launchSingleTop = true
                }
                true
            }
            SHORTCUT_LIBRARY -> {
                if (isNewIntent) {
                    lifecycleScope.launch { MainStarterScreen.switchToTab(0) }
                }
                // For initial launch, tab is already set by handleEarlyShortcutIntent
                true
            }
            SHORTCUT_UPDATES -> {
                if (isNewIntent) {
                    lifecycleScope.launch { MainStarterScreen.switchToTab(1) }
                }
                true
            }
            SHORTCUT_HISTORY -> {
                if (isNewIntent) {
                    lifecycleScope.launch { MainStarterScreen.switchToTab(2) }
                }
                true
            }
            SHORTCUT_DOWNLOADS -> {
                navController.navigate(NavigationRoutes.downloader) {
                    launchSingleTop = true
                }
                true
            }
            // Legacy shortcuts (for notifications, widgets, etc.)
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
            SHORTCUT_TTS_V2 -> {
                val bookId = intent.extras?.getLong(Args.ARG_BOOK_ID)
                val chapterId = intent.extras?.getLong(Args.ARG_CHAPTER_ID)
                val sourceId = intent.extras?.getLong(Args.ARG_SOURCE_ID)
                val readingParagraph = intent.extras?.getLong(Args.ARG_READING_PARAGRAPH)
                if (bookId != null && chapterId != null && sourceId != null) {
                    navController.popUntilRoot()
                    navController.navigate(
                        NavigationRoutes.ttsV2(
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
                navController.navigate(NavigationRoutes.downloader) {
                    launchSingleTop = true
                }
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
    
    // FirstLaunchDialogHandler removed - now handled in OnboardingScreen

}


interface SecureActivityDelegate {
    fun registerSecureActivity(activity: ComponentActivity, preferences: UiPreferences,initializers: AppInitializers)
}

