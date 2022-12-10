package ireader.presentation.ui.settings.reader

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import ireader.presentation.ui.settings.SettingState
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class ReaderSettingScreenViewModel(
    private val readerPreferences: ReaderPreferences,
) : BaseViewModel() {
    private val _state = mutableStateOf(SettingState())
    val state: State<SettingState> = _state

    val backgroundColor = readerPreferences.backgroundColorReader().asState()
    val textColor = readerPreferences.textColorReader().asState()
    val selectedScrollBarColor = readerPreferences.selectedScrollBarColor().asState()
    val unselectedScrollBarColor = readerPreferences.unselectedScrollBarColor().asState()
    val lineHeight = readerPreferences.lineHeight().asState()
    val paragraphsIndent = readerPreferences.paragraphIndent().asState()
    val showScrollIndicator = readerPreferences.showScrollIndicator().asState()
    val textAlignment = readerPreferences.textAlign().asState()
    val orientation = readerPreferences.orientation().asState()
    val scrollIndicatorWith = readerPreferences.scrollIndicatorWith().asState()
    val scrollIndicatorPadding = readerPreferences.scrollIndicatorPadding().asState()
    val scrollIndicatorAlignment = readerPreferences.scrollBarAlignment().asState()
    val autoScrollOffset = readerPreferences.autoScrollOffset().asState()
    var autoScrollInterval = readerPreferences.autoScrollInterval().asState()
    val autoBrightnessMode = readerPreferences.autoBrightness().asState()
    val immersiveMode = readerPreferences.immersiveMode().asState()
    val scrollbarMode = readerPreferences.scrollbarMode().asState()
    val font = readerPreferences.font().asState()

    val selectableMode = readerPreferences.selectableText().asState()
    val fontSize = readerPreferences.fontSize().asState()
    val distanceBetweenParagraphs = readerPreferences.paragraphDistance().asState()
    val verticalScrolling = readerPreferences.scrollMode().asState()
}
