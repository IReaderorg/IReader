package ireader.presentation.ui.reader.viewmodel

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable

expect class PlatformReaderSettingReader {

    fun ReaderScreenViewModel.saveBrightness(context: Any, brightness: Float)
    suspend fun ReaderScreenViewModel.readBrightness(context: Any)
    suspend fun ReaderScreenViewModel.readOrientation(context: Any)
    suspend fun ReaderScreenViewModel.readImmersiveMode(
        context: Any,
        onHideNav: (Boolean) -> Unit,
        onHideStatus: (Boolean) -> Unit
    )

    fun ReaderScreenViewModel.restoreSetting(
        context: Any,
        scrollState: ScrollState,
        lazyScrollState: LazyListState
    )

    fun ReaderScreenViewModel.prepareReaderSetting(
        context: Any,
        scrollState: ScrollState,
        onHideNav: (Boolean) -> Unit,
        onHideStatus: (Boolean) -> Unit,
    )

    @Composable
    fun WebView()

}
