package org.ireader.reader.viewmodel

import androidx.compose.foundation.ScrollState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import org.ireader.common_models.entities.Book
import org.ireader.common_models.entities.CatalogLocal
import org.ireader.common_models.entities.Chapter
import org.ireader.core_api.source.Source
import javax.inject.Inject

open class ReaderScreenStateImpl @Inject constructor() : ReaderScreenState {
    override var isLoading by mutableStateOf<Boolean>(false)

    override var readerScrollState by mutableStateOf<ScrollState?>(null)
    @ExperimentalMaterialApi
    override var modalBottomSheetState by mutableStateOf<ModalBottomSheetState?>(null)
    override var isDrawerAsc by mutableStateOf<Boolean>(true)
    override var drawerChapters: State<List<Chapter>> = derivedStateOf { if (isDrawerAsc) stateChapters else stateChapters.reversed() }
    override var isReaderModeEnable by mutableStateOf<Boolean>(true)
    override var isSettingModeEnable by mutableStateOf<Boolean>(false)
    override var isMainBottomModeEnable by mutableStateOf<Boolean>(false)
    override var currentChapterIndex: Int by mutableStateOf<Int>(0)
    override var maxScrollstate: Int by mutableStateOf<Int>(0)
    override val source: Source? by derivedStateOf { catalog?.source }
    override var catalog: CatalogLocal? by mutableStateOf<CatalogLocal?>(null)
    override var stateChapters: List<Chapter> by mutableStateOf<List<Chapter>>(emptyList())
    override var stateChapter: Chapter? by mutableStateOf<Chapter?>(null)
    override var isChapterLoaded: State<Boolean> = derivedStateOf { stateChapter?.isEmpty() == false }

    override var book: Book? by mutableStateOf<Book?>(null)

    override val chapterShell : SnapshotStateList<Chapter> = mutableStateListOf()
    override val stateContent: List<String> by derivedStateOf { stateChapter?.content?.filter { it.isNotBlank() }?.map { it.trim() }?: emptyList() }

}

interface ReaderScreenState {
    var isLoading: Boolean
    var isDrawerAsc: Boolean
    var drawerChapters: State<List<Chapter>>

    var isReaderModeEnable: Boolean

    var isSettingModeEnable: Boolean
    var isMainBottomModeEnable: Boolean

    var currentChapterIndex: Int
    var maxScrollstate: Int
    val source: Source?
    var catalog: CatalogLocal?
    var stateChapters: List<Chapter>
    var stateChapter: Chapter?
    var isChapterLoaded: State<Boolean>
    var book: Book?
    val stateContent: List<String>
    val chapterShell : SnapshotStateList<Chapter>


    var readerScrollState:ScrollState?
    @OptIn(ExperimentalMaterialApi::class) var modalBottomSheetState: ModalBottomSheetState?
}

open class ReaderScreenPreferencesStateImpl @Inject constructor() : ReaderScreenPreferencesState {
    override var isAsc by mutableStateOf<Boolean>(true)

    override var isChaptersReversed by mutableStateOf<Boolean>(false)

    override var isChapterReversingInProgress by mutableStateOf<Boolean>(false)

    override var autoScrollMode: Boolean by mutableStateOf<Boolean>(false)

    override var initialized by mutableStateOf<Boolean>(false)
    override var searchMode by mutableStateOf<Boolean>(false)
    override var searchQuery by mutableStateOf<String>("")
    override var queriedTextIndex: SnapshotStateList<Int> = mutableStateListOf()
    override var currentViewingSearchResultIndex by mutableStateOf<Int>(0)
    override var expandTopMenu by mutableStateOf<Boolean>(false)


    override var scrollMode by mutableStateOf<Boolean>(false)

}

interface ReaderScreenPreferencesState {
    var isAsc: Boolean




    var isChaptersReversed: Boolean
    var isChapterReversingInProgress: Boolean





    var autoScrollMode:Boolean

    var initialized: Boolean
    var searchMode: Boolean
    var expandTopMenu: Boolean
    var searchQuery: String
    var queriedTextIndex: SnapshotStateList<Int>
    var currentViewingSearchResultIndex: Int

    var scrollMode : Boolean
  //  val isVerticalScrolling : Boolean
}

sealed class Orientation(val index: Int) {
    object Portrait : Orientation(0)
    object Landscape : Orientation(1)
}
