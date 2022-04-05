package org.ireader.presentation.feature_reader.presentation.reader.viewmodel


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import org.ireader.core.utils.UiText
import org.ireader.core_ui.theme.BackgroundColor
import org.ireader.core_ui.theme.FontType
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Chapter
import tachiyomi.source.Source
import javax.inject.Inject

//data class ReaderScreenState(
//    val isLoading: Boolean = false,
//    val isRemoteLoading: Boolean = false,
//    val isLocalLoaded: Boolean = false,
//    val isRemoteLoaded: Boolean = false,
//    val enable: Boolean = true,
//    val book: Book? = null,
//    val isDrawerAsc: Boolean = true,
//    val isBookLoaded: Boolean = false,
//    val isChapterLoaded: Boolean = false,
//    val chapter: Chapter? = null,
//    val chapters: List<Chapter> = emptyList(),
//    val error: UiText = UiText.StringResource(R.string.no_error),
//    val source: Source? = null,
//    val isReaderModeEnable: Boolean = true,
//    val isSettingModeEnable: Boolean = false,
//    val isMainBottomModeEnable: Boolean = true,
//    val currentChapterIndex: Int = 0,
//    val isWebViewEnable: Boolean = false,
//)

open class ReaderScreenStateImpl @Inject constructor() : ReaderScreenState {
    override var isLoading by mutableStateOf<Boolean>(false)
    override var isRemoteLoading by mutableStateOf<Boolean>(false)
    override var isLocalLoaded by mutableStateOf<Boolean>(false)
    override var isRemoteLoaded by mutableStateOf<Boolean>(false)
    override var enable by mutableStateOf<Boolean>(true)
    override var isDrawerAsc by mutableStateOf<Boolean>(true)
    override var isBookLoaded by mutableStateOf<Boolean>(false)
    override var isReaderModeEnable by mutableStateOf<Boolean>(true)
    override var isSettingModeEnable by mutableStateOf<Boolean>(false)
    override var isMainBottomModeEnable by mutableStateOf<Boolean>(false)
    override var currentChapterIndex: Int by mutableStateOf<Int>(0)
    override var source: Source? by mutableStateOf<Source?>(null)
    override var error: UiText by mutableStateOf<UiText>(UiText.DynamicString(""))
    override var stateChapters: List<Chapter> by mutableStateOf<List<Chapter>>(emptyList())
    override var stateChapter: Chapter? by mutableStateOf<Chapter?>(null)
    override var book: Book? by mutableStateOf<Book?>(null)
    override var currentReadingParagraph: Int by mutableStateOf<Int>(0)
    override var isPlaying by mutableStateOf<Boolean>(false)
    override var voiceMode by mutableStateOf<Boolean>(false)


}


interface ReaderScreenState {
    var isLoading: Boolean
    var isRemoteLoading: Boolean
    var isLocalLoaded: Boolean
    var isRemoteLoaded: Boolean
    var enable: Boolean
    var isDrawerAsc: Boolean
    var isBookLoaded: Boolean
    var isReaderModeEnable: Boolean
    var isSettingModeEnable: Boolean
    var isMainBottomModeEnable: Boolean
    var currentChapterIndex: Int
    var source: Source?
    var error: UiText
    var stateChapters: List<Chapter>
    var stateChapter: Chapter?
    var book: Book?
    var currentReadingParagraph: Int
    var isPlaying: Boolean
    var voiceMode: Boolean
}


open class ReaderScreenPreferencesStateImpl @Inject constructor() : ReaderScreenPreferencesState {
    override var isAsc by mutableStateOf<Boolean>(true)
    override var fontSize by mutableStateOf<Int>(18)
    override var font by mutableStateOf<FontType>(FontType.Poppins)
    override var brightness by mutableStateOf<Float>(0.3f)
    override var distanceBetweenParagraphs by mutableStateOf<Int>(2)
    override var paragraphsIndent by mutableStateOf<Int>(8)
    override var lineHeight by mutableStateOf<Int>(25)
    override var backgroundColor by mutableStateOf<Color>(BackgroundColor.Black.color)
    override var textColor by mutableStateOf<Color>(BackgroundColor.Black.onTextColor)
    override var orientation by mutableStateOf<Orientation>(Orientation.Portrait)
    override var isChaptersReversed by mutableStateOf<Boolean>(false)
    override var isChapterReversingInProgress by mutableStateOf<Boolean>(false)
    override var verticalScrolling by mutableStateOf<Boolean>(true)
    override var scrollIndicatorWith by mutableStateOf<Int>(2)
    override var scrollIndicatorPadding by mutableStateOf<Int>(4)
    override var scrollIndicatorDialogShown by mutableStateOf<Boolean>(false)
    override var autoScrollOffset by mutableStateOf<Int>(500)
    override var autoScrollInterval by mutableStateOf<Long>(2000)
    override var autoScrollMode by mutableStateOf<Boolean>(false)
    override var autoBrightnessMode by mutableStateOf<Boolean>(false)
    override var immersiveMode by mutableStateOf<Boolean>(false)
    override var initialized by mutableStateOf<Boolean>(false)

}


interface ReaderScreenPreferencesState {
    var isAsc: Boolean
    var fontSize: Int
    var font: FontType
    var brightness: Float
    var distanceBetweenParagraphs: Int
    var paragraphsIndent: Int
    var lineHeight: Int
    var backgroundColor: Color
    var textColor: Color
    var orientation: Orientation
    var isChaptersReversed: Boolean
    var isChapterReversingInProgress: Boolean
    var verticalScrolling: Boolean
    var scrollIndicatorWith: Int
    var scrollIndicatorPadding: Int
    var scrollIndicatorDialogShown: Boolean
    var autoScrollOffset: Int
    var autoScrollInterval: Long
    var autoScrollMode: Boolean
    var autoBrightnessMode: Boolean
    var immersiveMode: Boolean
    var initialized: Boolean

}

sealed class Orientation(val index: Int) {
    object Portrait : Orientation(0)
    object Landscape : Orientation(1)
}