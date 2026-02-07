package ireader.presentation.ui.reader.viewmodel

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import kotlinx.coroutines.launch

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
        // Save scroll position from LazyListState (used by optimized reader)
        val lazyScrollPosition = lazyScrollState.firstVisibleItemIndex.toLong()
        
        stateChapter?.let { chapter ->
            scope.launch(kotlinx.coroutines.NonCancellable) {
                ireader.core.log.Log.debug { "Saving scroll position for chapter ${chapter.id}: lazyScrollPosition=$lazyScrollPosition" }
                // Use the dedicated updateLastPageRead method for efficient update
                saveScrollPosition(lazyScrollPosition)
                getChapterUseCase.updateLastReadTime(chapter)
            }
        }
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
