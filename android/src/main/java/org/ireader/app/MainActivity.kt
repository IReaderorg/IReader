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
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
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
import ireader.i18n.R
import ireader.i18n.SHORTCUTS.SHORTCUT_DETAIL
import ireader.i18n.SHORTCUTS.SHORTCUT_DOWNLOAD
import ireader.i18n.SHORTCUTS.SHORTCUT_READER
import ireader.i18n.SHORTCUTS.SHORTCUT_TTS
import ireader.presentation.core.DefaultNavigatorScreenTransition
import ireader.presentation.core.MainStarterScreen
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
                        Navigator(
                            screen = MainStarterScreen,
                            disposeBehavior = NavigatorDisposeBehavior(
                                disposeNestedNavigators = false,
                                disposeSteps = true
                            ),
                        ) { navigator ->
                            if (navigator.size == 1) {
                                ConfirmExit()
                            }
                            LaunchedEffect(navigator) {
                                handleIntentAction(this@MainActivity.intent, navigator)
                            }
                            IScaffold {
                                DefaultNavigatorScreenTransition(navigator = navigator)
                                // Pass the application context to GetPermissions
                                GetPermissions(uiPreferences, context = this@MainActivity)
                            }

                            HandleOnNewIntent(this, navigator)
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
    fun HandleOnNewIntent(context: Context, navigator: Navigator) {
        LaunchedEffect(Unit) {
            callbackFlow<Intent> {
                val componentActivity = context as ComponentActivity
                val consumer = Consumer<Intent> { trySend(it) }
                componentActivity.addOnNewIntentListener(consumer)
                awaitClose { componentActivity.removeOnNewIntentListener(consumer) }
            }.collectLatest { handleIntentAction(it, navigator) }
        }
    }

    private fun handleIntentAction(intent: Intent, navigator: Navigator): Boolean {
        return when (intent.action) {
            SHORTCUT_TTS -> {
                val bookId = intent.extras?.getLong(Args.ARG_BOOK_ID)
                val chapterId = intent.extras?.getLong(Args.ARG_CHAPTER_ID)
                val sourceId = intent.extras?.getLong(Args.ARG_SOURCE_ID)
                val readingParagraph = intent.extras?.getLong(Args.ARG_READING_PARAGRAPH)
                if (bookId != null && chapterId != null && sourceId != null) {
                    val screen = TTSScreenSpec(
                        bookId,
                        chapterId,
                        sourceId,
                        readingParagraph?.toInt() ?: 0
                    )
                    navigator.popUntilRoot()
                    navigator.push(
                        screen
                    )

                }
                true
            }
            SHORTCUT_READER -> {
                val bookId = intent.extras?.getLong(Args.ARG_BOOK_ID)
                val chapterId = intent.extras?.getLong(Args.ARG_CHAPTER_ID)
                if (bookId != null && chapterId != null) {
                    navigator.popUntilRoot()
                    navigator.push(ReaderScreenSpec(bookId, chapterId))
                }
                true
            }
            SHORTCUT_DETAIL -> {
                val bookId = intent.extras?.getLong(Args.ARG_BOOK_ID)
                if (bookId != null) {
                    navigator.popUntilRoot()
                    navigator.push(BookDetailScreenSpec(bookId))
                }
                true
            }
            SHORTCUT_DOWNLOAD -> {
                navigator.popUntilRoot()
                navigator.push(DownloaderScreenSpec())
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
                val toast = toast(R.string.confirm_exit, Toast.LENGTH_LONG)
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

