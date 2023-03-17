package ireader.presentation.ui.reader.viewmodel

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable

actual class PlatformReaderSettingReader {


    actual fun ReaderScreenViewModel.saveBrightness(brightness: Float) {
    }

    actual suspend fun ReaderScreenViewModel.readBrightness() {
    }

    actual suspend fun ReaderScreenViewModel.readOrientation() {
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

    @Composable
    actual fun WebView() {
    }

}