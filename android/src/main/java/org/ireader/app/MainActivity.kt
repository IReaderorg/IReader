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
import ireader.domain.usecases.files.AndroidGetSimpleStorage
import ireader.domain.utils.extensions.launchIO
import ireader.i18n.Args
import ireader.i18n.SHORTCUTS.SHORTCUT_DETAIL
import ireader.i18n.SHORTCUTS.SHORTCUT_DOWNLOAD
import ireader.i18n.SHORTCUTS.SHORTCUT_READER
import ireader.i18n.SHORTCUTS.SHORTCUT_TTS
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
import ireader.presentation.ui.core.ui.asStateIn
import ireader.presentation.ui.settings.FirstLaunchDialog
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
    private val supabasePreferences: SupabasePreferences by inject()
    val initializers: AppInitializers by inject()
    private val automaticBackup: AutomaticBackup by inject()
    private val localeHelper: LocaleHelper by inject()
    
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalCoilApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Log intent on create
        println("🔷🔷🔷 MainActivity.onCreate() called")
        println("   Intent: $intent")
        println("   Action: ${intent.action}")
        println("   Data: ${intent.data}")
        println("   Categories: ${intent.categories}")
        
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
        getSimpleStorage.provideActivity(this, null)
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
                        val navController = rememberNavController()
                        
                        ProvideNavigator(navController) {
                            if (navController.previousBackStackEntry == null) {
                                ConfirmExit()
                            }
                            
                            IScaffold {
                                CommonNavHost(navController)
                                GetPermissions(uiPreferences, context = this@MainActivity)
                            }
                            
                            // Handle initial intent after navigation is set up
                            LaunchedEffect(navController) {
                                // Wait for navigation graph to be ready
                                delay(800)
                                val intent = this@MainActivity.intent
                                val uri = intent.data
                                println("🔷 LaunchedEffect: Initial intent data: $uri")
                                println("🔷 LaunchedEffect: NavController current destination: ${navController.currentDestination?.route}")
                                
                                // Handle shortcut deep links
                                if (uri?.scheme == "ireader" && uri.host == "shortcut") {
                                    println("🔷 LaunchedEffect: Calling handleIntentAction for shortcut deep link")
                                    try {
                                        val result = handleIntentAction(intent, navController)
                                        println("🔷 LaunchedEffect: handleIntentAction result: $result")
                                    } catch (e: Exception) {
                                        println("❌ LaunchedEffect: handleIntentAction failed: ${e.message}")
                                        e.printStackTrace()
                                    }
                                }
                            }
                            
                            FirstLaunchDialogHandler()
                            HandleOnNewIntent(this, navController)
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
        // Trigger lazy initialization when app becomes visible
        (application as? MyApplication)?.onAppVisible()
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
        println("🔷 handleEarlyShortcutIntent called")
        println("   Action: ${intent.action}")
        println("   Data: ${intent.data}")
        
        // Handle deep link shortcuts (ireader://shortcut/xxx)
        val uri = intent.data
        if (uri?.scheme == "ireader" && uri.host == "shortcut") {
            when (uri.lastPathSegment) {
                "library" -> {
                    println("🔷 Early handling shortcut/library - setting initial tab to 0")
                    MainStarterScreen.setInitialTab(0)
                }
                "updates" -> {
                    println("🔷 Early handling shortcut/updates - setting initial tab to 1")
                    MainStarterScreen.setInitialTab(1)
                }
                "history" -> {
                    println("🔷 Early handling shortcut/history - setting initial tab to 2")
                    MainStarterScreen.setInitialTab(2)
                }
            }
        }
    }
    
    private fun handleIntentAction(intent: Intent, navController: NavHostController, isNewIntent: Boolean = false): Boolean {
        // Log all intent details for debugging
        println("🔷 handleIntentAction called (isNewIntent=$isNewIntent)")
        println("   Action: ${intent.action}")
        println("   Data: ${intent.data}")
        println("   Scheme: ${intent.data?.scheme}")
        println("   Host: ${intent.data?.host}")
        
        // Handle deep link shortcuts (ireader://shortcut/xxx)
        val uri = intent.data
        if (uri?.scheme == "ireader" && uri.host == "shortcut") {
            println("🔷 Handling shortcut deep link: ${uri.lastPathSegment}")
            return when (uri.lastPathSegment) {
                "search" -> {
                    println("🔷 Navigating to globalSearch")
                    try {
                        navController.navigate("globalSearch") {
                            launchSingleTop = true
                        }
                        println("✅ Navigated to globalSearch")
                    } catch (e: Exception) {
                        println("❌ Navigation failed: ${e.message}")
                        e.printStackTrace()
                    }
                    true
                }
                "library" -> {
                    println("🔷 Switching to library tab")
                    if (isNewIntent) {
                        lifecycleScope.launch { MainStarterScreen.switchToTab(0) }
                    }
                    true
                }
                "updates" -> {
                    println("🔷 Switching to updates tab")
                    if (isNewIntent) {
                        lifecycleScope.launch { MainStarterScreen.switchToTab(1) }
                    }
                    true
                }
                "history" -> {
                    println("🔷 Switching to history tab")
                    if (isNewIntent) {
                        lifecycleScope.launch { MainStarterScreen.switchToTab(2) }
                    }
                    true
                }
                "downloads" -> {
                    println("🔷 Navigating to downloader")
                    try {
                        navController.navigate(NavigationRoutes.downloader) {
                            launchSingleTop = true
                        }
                        println("✅ Navigated to downloader")
                    } catch (e: Exception) {
                        println("❌ Navigation failed: ${e.message}")
                        e.printStackTrace()
                    }
                    true
                }
                else -> false
            }
        }
        
        // Check if this is a wallet callback
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
            // Legacy action-based shortcuts (kept for backward compatibility)
            SHORTCUT_DOWNLOAD -> {
                println("🔷 Handling legacy SHORTCUT_DOWNLOAD")
                try {
                    navController.navigate(NavigationRoutes.downloader) {
                        launchSingleTop = true
                    }
                } catch (e: Exception) {
                    println("❌ Navigation failed: ${e.message}")
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
    
    @Composable
    private fun FirstLaunchDialogHandler() {
        val scope = rememberCoroutineScope()
        var hasCompletedFirstLaunch by remember { 
            mutableStateOf(uiPreferences.hasCompletedFirstLaunch().get()) 
        }
        var supabaseEnabled by remember { 
            mutableStateOf(supabasePreferences.supabaseEnabled().get()) 
        }
        
        if (!hasCompletedFirstLaunch) {
            FirstLaunchDialog(
                supabaseEnabled = supabaseEnabled,
                onSupabaseEnabledChange = { enabled ->
                    supabaseEnabled = enabled
                },
                onDismiss = {
                    // Save preferences and dismiss
                    supabasePreferences.supabaseEnabled().set(supabaseEnabled)
                    uiPreferences.hasCompletedFirstLaunch().set(true)
                    hasCompletedFirstLaunch = true
                },
                onGetStarted = {
                    // Save preferences and dismiss
                    supabasePreferences.supabaseEnabled().set(supabaseEnabled)
                    uiPreferences.hasCompletedFirstLaunch().set(true)
                    hasCompletedFirstLaunch = true
                }
            )
        }
    }

}


interface SecureActivityDelegate {
    fun registerSecureActivity(activity: ComponentActivity, preferences: UiPreferences,initializers: AppInitializers)
}

