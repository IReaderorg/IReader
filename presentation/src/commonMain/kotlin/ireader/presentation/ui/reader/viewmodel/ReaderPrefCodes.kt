package ireader.presentation.ui.reader.viewmodel

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable

expect class PlatformReaderSettingReader {

    fun ReaderScreenViewModel.saveBrightness(brightness: Float)
    suspend fun ReaderScreenViewModel.readBrightness()
    suspend fun ReaderScreenViewModel.readOrientation()
    suspend fun ReaderScreenViewModel.readImmersiveMode( onHideNav: (Boolean) -> Unit, onHideStatus: (Boolean) -> Unit)
    fun ReaderScreenViewModel.showSystemBars()
    fun ReaderScreenViewModel.hideSystemBars()

    fun ReaderScreenViewModel.restoreSetting(
            scrollState: ScrollState,
            lazyScrollState: LazyListState
    )
    fun ReaderScreenViewModel.prepareReaderSetting(
            scrollState: ScrollState,
            onHideNav: (Boolean) -> Unit,
            onHideStatus: (Boolean) -> Unit
    )

    @Composable
    fun WebView()

}