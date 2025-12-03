package ireader.presentation.ui.reader.viewmodel

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable

actual class PlatformReaderSettingReader {

    actual fun ReaderScreenViewModel.saveBrightness(context: Any, brightness: Float) {
        // iOS brightness is controlled through UIScreen
    }

    actual suspend fun ReaderScreenViewModel.readBrightness(context: Any) {
        // iOS brightness is controlled through UIScreen
    }

    actual suspend fun ReaderScreenViewModel.readOrientation(context: Any) {
        // iOS orientation is controlled through UIDevice
    }

    actual suspend fun ReaderScreenViewModel.readImmersiveMode(
        context: Any,
        onHideNav: (Boolean) -> Unit,
        onHideStatus: (Boolean) -> Unit
    ) {
        // iOS immersive mode is handled through UIKit
    }

    actual fun ReaderScreenViewModel.restoreSetting(
        context: Any,
        scrollState: ScrollState,
        lazyScrollState: LazyListState
    ) {
        // Restore settings on iOS
    }

    actual fun ReaderScreenViewModel.prepareReaderSetting(
        context: Any,
        scrollState: ScrollState,
        onHideNav: (Boolean) -> Unit,
        onHideStatus: (Boolean) -> Unit
    ) {
        // Prepare reader settings on iOS
    }

    @Composable
    actual fun WebView() {
        // iOS WebView would use WKWebView
    }
}
