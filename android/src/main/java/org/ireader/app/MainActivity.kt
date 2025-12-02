package org.ireader.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
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
        
        // Install splash screen FIRST - dismiss immediately for fastest startup
        installSplashScreen().apply {
            setKeepOnScreenCondition { false }
        }
        
        // Critical UI setup only
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        
        // MUST be on main thread
        registerSecureActivity(this, uiPreferences, initializers)
        getSimpleStorage.provideActivity(this, null)
        
        // Fast initialization on main thread
        // Kermit logging is auto-configured with platform defaults
        // For debug builds, logs go to Logcat automatically
        localeHelper.setLocaleLang()
        
        // Trigger lazy initialization now that UI is about to be visible
        (application as? MyApplication)?.onAppVisible()
        
        // Defer heavy initialization to background
        lifecycleScope.launchIO {
            delay(2000) // Wait until app is fully loaded
            automaticBackup.initialize()
            org.ireader.app.util.ExtensionCacheValidator.validateAndCleanExtensionCache(this@MainActivity)
        }

        // Request permissions much later
        lifecycleScope.launch {
            delay(3000)
            requestNecessaryPermissions()
        }

        setContent {
            val context = LocalContext.current
            
            // Check if user has enabled max performance mode
            val maxPerformanceEnabled = uiPreferences.maxPerformanceMode().get()
            
            // Get performance config - use MaxPerformance if user enabled it, otherwise device-based
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
                // Provide performance config to entire app for device-appropriate optimizations
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
                            // Handle exit confirmation when on main screen
                            if (navController.previousBackStackEntry == null) {
                                ConfirmExit()
                            }
                            
                            LaunchedEffect(Unit) {
                                handleIntentAction(this@MainActivity.intent, navController)
                            }
                            
                            IScaffold {
                                CommonNavHost(navController)
                                
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

    @Composable
    private fun CustomSplashScreen() {
        val isDark = isSystemInDarkTheme()
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark) Color.Black else Color.White),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Logo
                Image(
                    painter = if (isDark) {
                        painterResource(ireader.i18n.R.drawable.ic_eternity_dark)
                    } else {
                        painterResource(ireader.i18n.R.drawable.ic_eternity_light)
                    },
                    contentDescription = "App Logo",
                    modifier = Modifier.size(160.dp)
                )
                
                Spacer(modifier = Modifier.height(40.dp))
                
                // Tagline
                Text(
                    text = "Created by a reader, for readers",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = if (isDark) Color.White else Color.Black
                )
            }
        }
    }
}


interface SecureActivityDelegate {
    fun registerSecureActivity(activity: ComponentActivity, preferences: UiPreferences,initializers: AppInitializers)
}

