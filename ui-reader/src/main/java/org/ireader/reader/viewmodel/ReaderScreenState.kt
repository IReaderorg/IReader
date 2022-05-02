package org.ireader.reader.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import org.ireader.common_models.entities.Book
import org.ireader.common_models.entities.Chapter
import org.ireader.core_api.source.Source
import org.ireader.core_ui.theme.BackgroundColor
import org.ireader.core_ui.theme.FontType
import org.ireader.core_ui.ui.TextAlign
import javax.inject.Inject

open class ReaderScreenStateImpl @Inject constructor() : ReaderScreenState {
    override var isLoading by mutableStateOf<Boolean>(false)
    override var isDrawerAsc by mutableStateOf<Boolean>(true)
    override var drawerChapters: State<List<Chapter>> = derivedStateOf { if (isDrawerAsc) stateChapters else stateChapters.reversed() }
    override var isReaderModeEnable by mutableStateOf<Boolean>(true)
    override var isSettingModeEnable by mutableStateOf<Boolean>(false)
    override var isMainBottomModeEnable by mutableStateOf<Boolean>(false)
    override var currentChapterIndex: Int by mutableStateOf<Int>(0)
    override var source: Source? by mutableStateOf<Source?>(null)
    override var stateChapters: List<Chapter> by mutableStateOf<List<Chapter>>(emptyList())
    override var stateChapter: Chapter? by mutableStateOf<Chapter?>(null)
    override var isChapterLoaded: State<Boolean> = derivedStateOf { stateChapter?.isEmpty() == false }

    override var book: Book? by mutableStateOf<Book?>(null)
    override var stateContent: State<List<String>?>? =
        derivedStateOf { stateChapter?.content?.filter { it.isNotBlank() }?.map { it.trim() } }
}

interface ReaderScreenState {
    var isLoading: Boolean
    var isDrawerAsc: Boolean
    var drawerChapters: State<List<Chapter>>

    var isReaderModeEnable: Boolean
    var isSettingModeEnable: Boolean
    var isMainBottomModeEnable: Boolean

    var currentChapterIndex: Int
    var source: Source?
    var stateChapters: List<Chapter>
    var stateChapter: Chapter?
    var isChapterLoaded: State<Boolean>
    var book: Book?
    var stateContent: State<List<String>?>?
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
    override var showScrollIndicator by mutableStateOf<Boolean>(false)
    override var textAlignment: TextAlign by mutableStateOf<TextAlign>(TextAlign.Right)
    override var autoBrightnessMode by mutableStateOf<Boolean>(false)
    override var immersiveMode by mutableStateOf<Boolean>(false)
    override var selectableMode by mutableStateOf<Boolean>(false)
    override var initialized by mutableStateOf<Boolean>(false)
    override var searchMode by mutableStateOf<Boolean>(false)
    override var expandTopMenu by mutableStateOf<Boolean>(false)
    override var searchQuery by mutableStateOf<String>("")
    override var queriedTextIndex: SnapshotStateList<Int> = mutableStateListOf()
    override var currentViewingSearchResultIndex by mutableStateOf<Int>(0)
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
    var showScrollIndicator: Boolean
    var textAlignment: TextAlign
    var scrollIndicatorWith: Int
    var scrollIndicatorPadding: Int
    var scrollIndicatorDialogShown: Boolean
    var autoScrollOffset: Int
    var autoScrollInterval: Long
    var autoScrollMode: Boolean
    var autoBrightnessMode: Boolean
    var immersiveMode: Boolean
    var selectableMode: Boolean
    var initialized: Boolean
    var searchMode: Boolean
    var expandTopMenu: Boolean
    var searchQuery: String
    var queriedTextIndex: SnapshotStateList<Int>
    var currentViewingSearchResultIndex: Int
}

sealed class Orientation(val index: Int) {
    object Portrait : Orientation(0)
    object Landscape : Orientation(1)
}
