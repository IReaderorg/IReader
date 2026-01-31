package ireader.presentation.ui.reader.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.view.WindowManager
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import ireader.core.http.WebViewManger
import ireader.domain.preferences.prefs.ReadingMode
import ireader.domain.utils.extensions.brightness
import ireader.domain.utils.extensions.hideSystemUI
import ireader.domain.utils.extensions.isImmersiveModeEnabled
import ireader.domain.utils.extensions.showSystemUI
import ireader.presentation.ui.component.findComponentActivity
import kotlinx.coroutines.launch
import kotlin.time.Instant
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

actual class PlatformReaderSettingReader(
       private val webViewManager: WebViewManger
) {


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
        (context as Context).findComponentActivity()!!
            .let { activity ->
                if (immersiveMode.value) {
                    onHideNav(true)
                    onHideStatus(true)
                    hideSystemBars(context)
                } else if (activity.isImmersiveModeEnabled) {
                    onHideNav(false)
                    onHideStatus(false)
                    showSystemBars(context)
                } else {
                    print("")
                }
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
        context.findComponentActivity()?.showSystemUI()
    }

    fun ReaderScreenViewModel.hideSystemBars(context: Context) {
        context.findComponentActivity()?.hideSystemUI()
    }

    actual fun ReaderScreenViewModel.restoreSetting(
        context: Any,
        scrollState: ScrollState,
        lazyScrollState: LazyListState
    ) {
        val activity = (context as Context).findComponentActivity()
        if (activity != null) {
            val window = activity.window
            val layoutParams: WindowManager.LayoutParams = window.attributes
            showSystemBars(context)
            layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            window.attributes = layoutParams
            screenAlwaysOnUseCase(false)
            
            // Capture scroll position from LazyListState (used by optimized reader)
            // We save the first visible item index which can be restored on re-entry
            val lazyScrollPosition = lazyScrollState.firstVisibleItemIndex.toLong()
            
            when (readingMode.value) {
                ReadingMode.Page -> {
                    stateChapter?.let { chapter ->
                        // Use ViewModel scope with NonCancellable to ensure save completes
                        // even when activity is being destroyed (back press)
                        scope.launch(kotlinx.coroutines.NonCancellable) {
                            ireader.core.log.Log.debug { "Saving scroll position for chapter ${chapter.id}: lazyScrollPosition=$lazyScrollPosition" }
                            insertUseCases.insertChapter(chapter.copy(lastPageRead = lazyScrollPosition))
                            getChapterUseCase.updateLastReadTime(chapter)
                        }
                    }
                }
                ReadingMode.Continues -> {
                    // Save scroll position in Continues mode too (fixes issue where position was lost)
                    stateChapter?.let { chapter ->
                        // Use ViewModel scope with NonCancellable to ensure save completes
                        scope.launch(kotlinx.coroutines.NonCancellable) {
                            ireader.core.log.Log.debug { "Saving scroll position for chapter ${chapter.id}: lazyScrollPosition=$lazyScrollPosition" }
                            insertUseCases.insertChapter(chapter.copy(lastPageRead = lazyScrollPosition))
                            getChapterUseCase.updateLastReadTime(chapter)
                        }
                    }
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
