package ireader.presentation.ui.reader.viewmodel

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import ireader.domain.models.prefs.PreferenceValues

actual class PlatformReaderSettingReader {
    actual fun ReaderScreenViewModel.toggleReaderMode(enable: Boolean?) {
    }

    actual fun ReaderScreenViewModel.saveBrightness(brightness: Float) {
    }

    actual fun ReaderScreenViewModel.toggleAutoScrollMode() {
    }

    actual fun ReaderScreenViewModel.changeBackgroundColor(themeId: Long) {
    }

    actual fun ReaderScreenViewModel.setReaderBackgroundColor(color: Color) {
    }

    actual suspend fun ReaderScreenViewModel.readBrightness() {
    }

    actual suspend fun ReaderScreenViewModel.readOrientation() {
    }

    actual fun ReaderScreenViewModel.setReaderTextColor(color: Color) {
    }

    actual suspend fun ReaderScreenViewModel.readImmersiveMode(onHideNav: (Boolean) -> Unit, onHideStatus: (Boolean) -> Unit) {
    }

    actual fun ReaderScreenViewModel.showSystemBars() {
    }

    actual fun ReaderScreenViewModel.hideSystemBars() {
    }

    actual fun ReaderScreenViewModel.restoreSetting(scrollState: ScrollState, lazyScrollState: LazyListState) {
    }

    actual fun ReaderScreenViewModel.prepareReaderSetting(scrollState: ScrollState, onHideNav: (Boolean) -> Unit, onHideStatus: (Boolean) -> Unit) {
    }

    actual fun saveTextAlignment(textAlign: PreferenceValues.PreferenceTextAlignment) {
    }

    @Composable
    actual fun WebView() {
    }

}