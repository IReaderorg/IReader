package ireader.presentation.ui.settings.reader

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.preferences.prefs.PlatformUiPreferences
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.presentation.ui.core.ui.asStateIn


class ReaderSettingScreenViewModel(
    private val readerPreferences: ReaderPreferences,
    private val androidUiPreferences: AppPreferences,
    private val platformUiPreferences: PlatformUiPreferences
) : ireader.presentation.ui.core.viewmodel.BaseViewModel() {
    private val _state = mutableStateOf(SettingState())
    val state: State<SettingState> = _state

    val backgroundColor = androidUiPreferences.backgroundColorReader().asStateIn(scope)
    val textColor = androidUiPreferences.textColorReader().asStateIn(scope)
    val selectedScrollBarColor = androidUiPreferences.selectedScrollBarColor().asStateIn(scope)
    val unselectedScrollBarColor = androidUiPreferences.unselectedScrollBarColor().asStateIn(scope)
    val lineHeight = readerPreferences.lineHeight().asStateIn(scope)
    val paragraphsIndent = readerPreferences.paragraphIndent().asStateIn(scope)
    val showScrollIndicator = readerPreferences.showScrollIndicator().asStateIn(scope)
    val textAlignment = readerPreferences.textAlign().asStateIn(scope)
    val orientation = androidUiPreferences.orientation().asStateIn(scope)
    val scrollIndicatorWith = readerPreferences.scrollIndicatorWith().asStateIn(scope)
    val scrollIndicatorPadding = readerPreferences.scrollIndicatorPadding().asStateIn(scope)
    val scrollIndicatorAlignment = readerPreferences.scrollBarAlignment().asStateIn(scope)
    val autoScrollOffset = readerPreferences.autoScrollOffset().asStateIn(scope)
    var autoScrollInterval = readerPreferences.autoScrollInterval().asState()
    val autoBrightnessMode = readerPreferences.autoBrightness().asState()
    val immersiveMode = readerPreferences.immersiveMode().asState()
    val scrollbarMode = readerPreferences.scrollbarMode().asState()
    val font = platformUiPreferences.font()?.asState()

    val selectableMode = readerPreferences.selectableText().asState()
    val fontSize = readerPreferences.fontSize().asState()
    val distanceBetweenParagraphs = readerPreferences.paragraphDistance().asState()
    val verticalScrolling = readerPreferences.scrollMode().asState()
    val autoPreloadNextChapter = readerPreferences.autoPreloadNextChapter().asState()
    val preloadOnlyOnWifi = readerPreferences.preloadOnlyOnWifi().asState()
    val readingSpeedWPM = readerPreferences.readingSpeedWPM().asState()
}
