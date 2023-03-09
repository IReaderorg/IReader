package org.ireader.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.util.Consumer
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import ireader.core.http.toast
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.usecases.backup.AutomaticBackup
import ireader.domain.usecases.files.GetSimpleStorage
import ireader.domain.utils.extensions.launchIO
import ireader.i18n.Args
import ireader.i18n.SHORTCUTS.SHORTCUT_DETAIL
import ireader.i18n.SHORTCUTS.SHORTCUT_DOWNLOAD
import ireader.i18n.SHORTCUTS.SHORTCUT_READER
import ireader.i18n.SHORTCUTS.SHORTCUT_TTS
import ireader.presentation.R
import ireader.presentation.core.DefaultNavigatorScreenTransition
import ireader.presentation.core.MainStarterScreen
import ireader.presentation.core.theme.AppTheme
import ireader.presentation.core.theme.LocaleHelper
import ireader.presentation.core.ui.BookDetailScreenSpec
import ireader.presentation.core.ui.DownloaderScreenSpec
import ireader.presentation.core.ui.ReaderScreenSpec
import ireader.presentation.core.ui.TTSScreenSpec
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.core.ui.asStateIn
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.ireader.app.initiators.AppInitializers
import org.ireader.app.initiators.GetPermissions
import org.ireader.app.initiators.SecureActivityDelegateImpl
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.compose.withDI
import org.kodein.di.instance
import kotlin.time.Duration.Companion.seconds


class MainActivity : ComponentActivity(), SecureActivityDelegate by SecureActivityDelegateImpl(),
    DIAware {


    private val getSimpleStorage: GetSimpleStorage by instance()
    private val uiPreferences: UiPreferences by instance()
    val initializers: AppInitializers by instance<AppInitializers>()
    private val automaticBackup: AutomaticBackup by instance()
    private val localeHelper: LocaleHelper by instance()
    private var navigator: cafe.adriel.voyager.navigator.Navigator? = null

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerSecureActivity(this, uiPreferences,initializers)
        getSimpleStorage.provideActivity(this, null)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        lifecycleScope.launchIO {
            automaticBackup.initialize()
        }
        localeHelper.setLocaleLang(this)
        installSplashScreen()
        setContent {
            withDI(di) {


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
                                this@MainActivity.navigator = navigator
                                if (savedInstanceState == null) {
                                    // Set start screen
                                    handleIntentAction(intent, navigator)
                                }
                            }
                            IScaffold {
                                DefaultNavigatorScreenTransition(navigator = navigator)
                                GetPermissions(uiPreferences)
                            }

                            HandleOnNewIntent(this, navigator)
                        }

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
    override val di: DI by closestDI()


}


interface SecureActivityDelegate {
    fun registerSecureActivity(activity: ComponentActivity, preferences: UiPreferences,initializers: AppInitializers)
}

