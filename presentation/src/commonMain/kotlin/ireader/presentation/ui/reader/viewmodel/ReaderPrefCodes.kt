package ireader.presentation.ui.reader.viewmodel

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import ireader.domain.models.prefs.PreferenceValues

expect class PlatformReaderSettingReader {
    fun ReaderScreenViewModel.toggleReaderMode(enable: Boolean? = null)
    fun ReaderScreenViewModel.saveBrightness(brightness: Float)

    fun ReaderScreenViewModel.toggleAutoScrollMode()
    fun ReaderScreenViewModel.changeBackgroundColor(themeId: Long)
    fun ReaderScreenViewModel.setReaderBackgroundColor(color: Color)
    suspend fun ReaderScreenViewModel.readBrightness()
    suspend fun ReaderScreenViewModel.readOrientation()
    fun ReaderScreenViewModel.setReaderTextColor(color: Color)
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
    fun saveTextAlignment(textAlign: PreferenceValues.PreferenceTextAlignment)

    @Composable
    fun WebView()

}