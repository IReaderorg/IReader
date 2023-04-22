package ireader.presentation.ui.reader.viewmodel

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable

actual class PlatformReaderSettingReader {


    actual fun ReaderScreenViewModel.saveBrightness(context: Any, brightness: Float) {
    }

    actual suspend fun ReaderScreenViewModel.readBrightness(context: Any) {
    }

    actual suspend fun ReaderScreenViewModel.readOrientation(context: Any) {
    }

    actual suspend fun ReaderScreenViewModel.readImmersiveMode(
        context: Any,
        onHideNav: (Boolean) -> Unit,
        onHideStatus: (Boolean) -> Unit
    ) {
    }

    actual fun ReaderScreenViewModel.restoreSetting(
        context: Any,
        scrollState: ScrollState,
        lazyScrollState: LazyListState
    ) {
    }

    actual fun ReaderScreenViewModel.prepareReaderSetting(
        context: Any,
        scrollState: ScrollState,
        onHideNav: (Boolean) -> Unit,
        onHideStatus: (Boolean) -> Unit
    ) {
    }

    @Composable
    actual fun WebView() {
    }

}