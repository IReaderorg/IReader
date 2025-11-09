package ireader.presentation.ui.reader.viewmodel

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
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.models.entities.Chapter
import ireader.core.source.Source
import ireader.core.source.model.Page


open class ReaderScreenStateImpl: ReaderScreenState {
    override var isLoading by mutableStateOf<Boolean>(false)

    override var readerScrollState by mutableStateOf<ScrollState?>(null)
    @ExperimentalMaterialApi
    override var modalBottomSheetState by mutableStateOf<ModalBottomSheetState?>(null)
    override var isDrawerAsc by mutableStateOf<Boolean>(true)
    override var drawerChapters: State<List<Chapter>> = derivedStateOf { if (isDrawerAsc) stateChapters else stateChapters.reversed() }
    override var isReaderModeEnable by mutableStateOf<Boolean>(true)
    override var isSettingModeEnable by mutableStateOf<Boolean>(false)
    override var isMainBottomModeEnable by mutableStateOf<Boolean>(false)
    override var showSettingsBottomSheet by mutableStateOf<Boolean>(false)
    override var currentChapterIndex: Int by mutableStateOf<Int>(0)
    override var maxScrollstate: Int by mutableStateOf<Int>(0)
    override val source: Source? by derivedStateOf { catalog?.source }
    override var catalog: CatalogLocal? by mutableStateOf<CatalogLocal?>(null)
    override var stateChapters: List<Chapter> by mutableStateOf<List<Chapter>>(emptyList())
    override var stateChapter: Chapter? by mutableStateOf<Chapter?>(null)
    override var isChapterLoaded: State<Boolean> = derivedStateOf { stateChapter?.isEmpty() == false }

    override var book: Book? by mutableStateOf<Book?>(null)

    override val chapterShell: SnapshotStateList<Chapter> = mutableStateListOf()
    override val stateContent: List<Page> by derivedStateOf { stateChapter?.content  ?: emptyList() }
    
    // Function to get content (will be overridden in ViewModel to support translation)
    override fun getCurrentContent(): List<Page> = stateContent
}

interface ReaderScreenState {
    var isLoading: Boolean
    var isDrawerAsc: Boolean
    var drawerChapters: State<List<Chapter>>

    var isReaderModeEnable: Boolean

    var isSettingModeEnable: Boolean
    var isMainBottomModeEnable: Boolean
    var showSettingsBottomSheet: Boolean

    var currentChapterIndex: Int
    var maxScrollstate: Int
    val source: Source?
    var catalog: CatalogLocal?
    var stateChapters: List<Chapter>
    var stateChapter: Chapter?
    var isChapterLoaded: State<Boolean>
    var book: Book?
    val stateContent: List<Page>
    val chapterShell: SnapshotStateList<Chapter>
    
    // Function to get content (can be overridden to support translation)
    fun getCurrentContent(): List<Page>

    var readerScrollState: ScrollState?
    @OptIn(ExperimentalMaterialApi::class) var modalBottomSheetState: ModalBottomSheetState?
}

open class ReaderScreenPreferencesStateImpl() : ReaderScreenPreferencesState {
    override var isAsc by mutableStateOf<Boolean>(true)

    override var isChaptersReversed by mutableStateOf<Boolean>(false)

    override var isChapterReversingInProgress by mutableStateOf<Boolean>(false)

    override var autoScrollMode: Boolean by mutableStateOf<Boolean>(false)

    override var initialized by mutableStateOf<Boolean>(false)

    override var searchQuery by mutableStateOf<String>("")
    override var currentViewingSearchResultIndex by mutableStateOf<Int>(0)
    override var expandTopMenu by mutableStateOf<Boolean>(false)

    override var scrollMode by mutableStateOf<Boolean>(false)
    
    // Find in chapter state
    override var showFindInChapter by mutableStateOf<Boolean>(false)
    override var findQuery by mutableStateOf<String>("")
    override var findMatches by mutableStateOf<List<IntRange>>(emptyList())
    override var currentFindMatchIndex by mutableStateOf<Int>(0)
    
    // Report broken chapter state
    override var showReportDialog by mutableStateOf<Boolean>(false)
    
    // Reading time estimation state
    override var showReadingTime by mutableStateOf<Boolean>(true)
    override var estimatedReadingMinutes by mutableStateOf<Int>(0)
    override var wordsRemaining by mutableStateOf<Int>(0)
    override var totalWords by mutableStateOf<Int>(0)
    
    // Paragraph translation state
    override var showParagraphTranslationDialog by mutableStateOf<Boolean>(false)
    override var paragraphToTranslate by mutableStateOf<String>("")
    override var translatedParagraph by mutableStateOf<String?>(null)
    override var isParagraphTranslating by mutableStateOf<Boolean>(false)
    override var paragraphTranslationError by mutableStateOf<String?>(null)
    override var showTranslationApiKeyPrompt by mutableStateOf<Boolean>(false)
}

interface ReaderScreenPreferencesState {
    var isAsc: Boolean

    var isChaptersReversed: Boolean
    var isChapterReversingInProgress: Boolean

    var autoScrollMode: Boolean

    var initialized: Boolean
    var expandTopMenu: Boolean
    var searchQuery: String
    var currentViewingSearchResultIndex: Int

    var scrollMode: Boolean
    
    // Find in chapter state
    var showFindInChapter: Boolean
    var findQuery: String
    var findMatches: List<IntRange>
    var currentFindMatchIndex: Int
    
    // Report broken chapter state
    var showReportDialog: Boolean
    
    // Reading time estimation state
    var showReadingTime: Boolean
    var estimatedReadingMinutes: Int
    var wordsRemaining: Int
    var totalWords: Int
    
    // Paragraph translation state
    var showParagraphTranslationDialog: Boolean
    var paragraphToTranslate: String
    var translatedParagraph: String?
    var isParagraphTranslating: Boolean
    var paragraphTranslationError: String?
    var showTranslationApiKeyPrompt: Boolean
    //  val isVerticalScrolling : Boolean
}
