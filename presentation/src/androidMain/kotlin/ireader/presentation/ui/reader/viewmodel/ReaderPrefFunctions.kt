package ireader.presentation.ui.reader.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.view.WindowManager
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import ireader.core.http.WebViewManger
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.preferences.prefs.ReadingMode
import ireader.domain.usecases.preferences.AndroidReaderPrefUseCases
import ireader.domain.utils.extensions.brightness
import ireader.domain.utils.extensions.hideSystemUI
import ireader.domain.utils.extensions.isImmersiveModeEnabled
import ireader.domain.utils.extensions.showSystemUI
import ireader.presentation.ui.component.findComponentActivity
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.seconds

actual class PlatformReaderSettingReader(
       private val context:Context,
       private val androidReaderPreferences : AndroidReaderPrefUseCases,
       private val webViewManager: WebViewManger
) {
    actual fun ReaderScreenViewModel.toggleReaderMode(enable: Boolean?) {
        isReaderModeEnable = enable ?: !state.isReaderModeEnable
        isMainBottomModeEnable = true
        isSettingModeEnable = false
    }

    actual fun ReaderScreenViewModel.saveBrightness(brightness: Float, ) {
        this.brightness.value = brightness
        val activity = context.findComponentActivity()
        if (activity != null) {
            activity.brightness(brightness)
            readerUseCases.brightnessStateUseCase.saveBrightness(brightness)
        }
    }

    actual suspend fun ReaderScreenViewModel.readImmersiveMode(
            onHideNav: (Boolean) -> Unit,
            onHideStatus: (Boolean) -> Unit
    ) {
        context.findComponentActivity()?.let { activity ->

            if (immersiveMode.value) {
                onHideNav(true)
                onHideStatus(true)
                hideSystemBars()
            } else if (activity.isImmersiveModeEnabled) {
                onHideNav(false)
                onHideStatus(false)
                showSystemBars()
            }
        }
    }

    actual fun ReaderScreenViewModel.toggleAutoScrollMode() {
        autoScrollMode = !autoScrollMode
    }

    actual suspend fun ReaderScreenViewModel.readBrightness() {
        val activity = context.findComponentActivity()
        if (activity != null) {
            val window = activity.window
            if (!autoBrightnessMode.value) {
                val layoutParams: WindowManager.LayoutParams = window.attributes
                layoutParams.screenBrightness = brightness.value
                window.attributes = layoutParams
                // this.brightness = brightness
            } else {
                val layoutParams: WindowManager.LayoutParams = window.attributes
                showSystemBars()
                layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                window.attributes = layoutParams
            }
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    actual suspend fun ReaderScreenViewModel.readOrientation() {
        val activity = context.findComponentActivity()
        val lastCheck = Instant.fromEpochMilliseconds(lastOrientationChangedTime.value)
        val now = Clock.System.now()
        if (activity != null && (now - lastCheck) > 1.seconds) {
            activity.requestedOrientation = orientation.value
            lastOrientationChangedTime.value = Clock.System.now().toEpochMilliseconds()
        } else {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    actual fun ReaderScreenViewModel.changeBackgroundColor(themeId:Long) {
        readerColors.firstOrNull { it.id == themeId }?.let { theme ->
            readerTheme.value = theme
            val bgColor = theme.backgroundColor
            val textColor = theme.onTextColor
            backgroundColor.value = bgColor
            this.textColor.value = textColor
            setReaderBackgroundColor(bgColor)
            setReaderTextColor(textColor)
        }

    }

    actual fun ReaderScreenViewModel.setReaderBackgroundColor(color: Color) {
        androidReaderPreferences.backgroundColorUseCase.save(color)
    }

    actual fun ReaderScreenViewModel.setReaderTextColor(color: Color) {
        androidReaderPreferences.textColorUseCase.save(color)
    }

    actual fun ReaderScreenViewModel.showSystemBars() {
        context.findComponentActivity()?.showSystemUI()
    }

    actual fun ReaderScreenViewModel.hideSystemBars() {
        context.findComponentActivity()?.hideSystemUI()
    }

    actual fun ReaderScreenViewModel.restoreSetting(scrollState: ScrollState, lazyScrollState: LazyListState) {
        val activity = context.findComponentActivity()
        if (activity != null) {
            val window = activity.window
            val layoutParams: WindowManager.LayoutParams = window.attributes
            showSystemBars()
            layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            window.attributes = layoutParams
            screenAlwaysOnUseCase(false)
            when (readingMode.value) {
                ReadingMode.Page -> {
                    stateChapter?.let { chapter ->
                        activity.lifecycleScope.launch {
                            insertUseCases.insertChapter(chapter.copy(lastPageRead = scrollState.value.toLong()))
                            getChapterUseCase.updateLastReadTime(chapter)
                        }
                    }
                }
                ReadingMode.Continues -> {
                    val index = stateChapters.indexOfFirst { it.id == stateChapter?.id }
                    if (index != -1) {
                        stateChapters.getOrNull(index)?.let { chapter ->
                            activity.lifecycleScope.launch {
                                getChapterUseCase.updateLastReadTime(chapter)
                            }
                        }
                    }
                }
            }
        }
    }

    actual fun ReaderScreenViewModel.prepareReaderSetting(scrollState: ScrollState, onHideNav: (Boolean) -> Unit, onHideStatus: (Boolean) -> Unit) {
        scope.launch {
            readImmersiveMode( onHideNav = onHideNav, onHideStatus = onHideStatus)
        }
        scope.launch {
            readOrientation()
        }
        scope.launch {
            kotlin.runCatching {
                stateChapter?.lastPageRead?.let { chapter ->
                    scrollState.scrollTo(chapter.toInt() ?: 1)
                }
            }
        }
    }

    actual fun saveTextAlignment(textAlign: PreferenceValues.PreferenceTextAlignment) {
        androidReaderPreferences.textAlignmentUseCase.save(textAlign)
    }

    @Composable
    actual fun WebView() {
        ireader.presentation.ui.reader.custom.WebView(preconfigureWebView = webViewManager.webView)
    }

}