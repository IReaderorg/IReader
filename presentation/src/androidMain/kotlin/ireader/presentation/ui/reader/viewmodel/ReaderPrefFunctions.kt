package ireader.presentation.ui.reader.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.view.ViewTreeObserver
import android.view.WindowManager
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import ireader.core.http.WebViewManger
import ireader.domain.utils.extensions.brightness
import ireader.domain.utils.extensions.hideSystemUI
import ireader.domain.utils.extensions.isImmersiveModeEnabled
import ireader.domain.utils.extensions.showSystemUI
import ireader.presentation.ui.component.findComponentActivity
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

actual class PlatformReaderSettingReader(
       private val webViewManager: WebViewManger
) {
    private var focusChangeListener: ViewTreeObserver.OnWindowFocusChangeListener? = null


    actual fun ReaderScreenViewModel.saveBrightness(context: Any, brightness: Float) {
        this.brightness.value = brightness
        val activity = (context as Context).findComponentActivity()
        if (activity != null) {
            activity.brightness(brightness)
            readerUseCases.brightnessStateUseCase.saveBrightness(brightness)
        }
    }

    actual suspend fun ReaderScreenViewModel.readImmersiveMode(
        context: Any,
        onHideNav: (Boolean) -> Unit,
        onHideStatus: (Boolean) -> Unit
    ) {
        val activity = (context as? Context)?.findComponentActivity() ?: return
        if (immersiveMode.value) {
            if (isReaderModeEnable) {
                onHideNav(true)
                onHideStatus(true)
                hideSystemBars(context)
            } else {
                onHideNav(false)
                onHideStatus(false)
                showSystemBars(context)
            }
        } else {
            onHideNav(false)
            onHideStatus(false)
            showSystemBars(context)
        }
    }

    actual suspend fun ReaderScreenViewModel.readBrightness(context: Any) {
        val activity = (context as Context).findComponentActivity()
        if (activity != null) {
            val window = activity.window
            if (!autoBrightnessMode.value) {
                val layoutParams: WindowManager.LayoutParams = window.attributes
                layoutParams.screenBrightness = brightness.value
                window.attributes = layoutParams
                // this.brightness = brightness
            } else {
                val layoutParams: WindowManager.LayoutParams = window.attributes
                showSystemBars(context)
                layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                window.attributes = layoutParams
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    @SuppressLint("SourceLockedOrientationActivity")
    actual suspend fun ReaderScreenViewModel.readOrientation(context: Any) {
        val activity = (context as Context).findComponentActivity()
        val lastCheck = Instant.fromEpochMilliseconds(lastOrientationChangedTime.value)
        val now = kotlin.time.Clock.System.now()
        if (activity != null && (now - lastCheck) > 1.seconds) {
            activity.requestedOrientation = orientation.value
            lastOrientationChangedTime.value = kotlin.time.Clock.System.now().toEpochMilliseconds()
        } else {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }


    fun ReaderScreenViewModel.showSystemBars(context: Context) {
        val activity = context.findComponentActivity() ?: return
        activity.window.decorView.post {
            activity.showSystemUI()
        }
    }

    fun ReaderScreenViewModel.hideSystemBars(context: Context) {
        val activity = context.findComponentActivity() ?: return
        activity.window.decorView.post {
            activity.hideSystemUI()
        }
    }

    actual fun ReaderScreenViewModel.restoreSetting(
        context: Any,
        scrollState: ScrollState,
        lazyScrollState: LazyListState
    ) {
        val activity = (context as? Context)?.findComponentActivity()
        if (activity != null) {
            focusChangeListener?.let { listener ->
                activity.window.decorView.viewTreeObserver.removeOnWindowFocusChangeListener(listener)
                focusChangeListener = null
            }
            val window = activity.window
            val layoutParams: WindowManager.LayoutParams = window.attributes
            showSystemBars(activity)
            layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            window.attributes = layoutParams
            screenAlwaysOnUseCase(false)
            
            // Capture scroll position based on reading mode
            val scrollPosition = if (readingMode.value == ireader.domain.preferences.prefs.ReadingMode.Continues) {
                scrollState.value.toLong()
            } else {
                lazyScrollState.firstVisibleItemIndex.toLong()
            }
            
            // Save scroll position for both reading modes
            stateChapter?.let { chapter ->
                // Use ViewModel scope with NonCancellable to ensure save completes
                // even when activity is being destroyed (back press)
                scope.launch(kotlinx.coroutines.NonCancellable) {
                    ireader.core.log.Log.debug { "Saving scroll position for chapter ${chapter.id}: scrollPosition=$scrollPosition" }
                    // Use the dedicated updateLastPageRead method for efficient update
                    saveScrollPosition(scrollPosition)
                    getChapterUseCase.updateLastReadTime(chapter)
                }
            }
        }
    }

    actual fun ReaderScreenViewModel.prepareReaderSetting(
        context: Any,
        scrollState: ScrollState,
        onHideNav: (Boolean) -> Unit,
        onHideStatus: (Boolean) -> Unit
    ) {
        val activity = (context as? Context)?.findComponentActivity()
        if (activity != null) {
            focusChangeListener?.let { oldListener ->
                activity.window.decorView.viewTreeObserver.removeOnWindowFocusChangeListener(oldListener)
            }
            val newListener = ViewTreeObserver.OnWindowFocusChangeListener { hasFocus ->
                if (hasFocus && immersiveMode.value && isReaderModeEnable) {
                    hideSystemBars(activity)
                }
            }
            focusChangeListener = newListener
            activity.window.decorView.viewTreeObserver.addOnWindowFocusChangeListener(newListener)
        }

        scope.launch {
            readImmersiveMode(onHideNav = onHideNav, onHideStatus = onHideStatus, context = context)
        }
        scope.launch {
            readOrientation(context)
        }
        scope.launch {
            kotlin.runCatching {
                stateChapter?.lastPageRead?.let { chapter ->
                    scrollState.scrollTo(chapter.toInt() ?: 1)
                }
            }
        }
    }
    @Composable
    actual fun WebView() {
        ireader.presentation.ui.reader.custom.WebView(preconfigureWebView = webViewManager.webView)
    }
}
