package ireader.presentation.ui.home.library.viewmodel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import ireader.domain.models.entities.CategoryWithCount
import ireader.domain.models.entities.History
import ireader.domain.models.entities.LibraryBook
import ireader.domain.models.entities.Chapter
import ireader.domain.models.library.LibrarySort
import ireader.i18n.UiText
import ireader.i18n.resources.Res
import ireader.i18n.resources.*

/**
 * State for undo operations
 */
data class UndoState(
    val previousChapterStates: Map<Long, List<Chapter>>,
    val operationType: UndoOperationType,
    val timestamp: Long
)

enum class UndoOperationType {
    MARK_AS_READ,
    MARK_AS_UNREAD
}


interface LibraryState {
    var isLoading: Boolean
    var books: List<LibraryBook>
    val isEmpty: Boolean
    var searchedBook: List<LibraryBook>
    var error: UiText
    var inSearchMode: Boolean
    var searchQuery: String?
    var sortType: LibrarySort
    var desc: Boolean

    // var filters: SnapshotStateList<FilterType>
    var currentScrollState: Int
    var selectedCategoryIndex: Int
    var histories: List<History>
    var selectedBooks: SnapshotStateList<Long>
    val selectionMode: Boolean
    val inititialized: Boolean
    val categories: List<CategoryWithCount>
    val selectedCategory: CategoryWithCount?
    var batchOperationInProgress: Boolean
    var batchOperationMessage: String?
    var lastUndoState: UndoState?
    var showBatchOperationDialog: Boolean
    
    // New properties for toolbar actions
    var isUpdatingLibrary: Boolean
    var showUpdateCategoryDialog: Boolean
    var importProgress: ImportProgress?
    
    // EPUB import/export state
    var epubImportState: EpubImportState
    var epubExportState: EpubExportState
}

/**
 * Progress state for EPUB import
 */
data class ImportProgress(
    val current: Int,
    val total: Int,
    val currentFileName: String
)

/**
 * Detailed EPUB import state
 */
data class EpubImportState(
    val showPreview: Boolean = false,
    val showProgress: Boolean = false,
    val showSummary: Boolean = false,
    val previewMetadata: List<ireader.presentation.ui.home.library.components.EpubMetadata> = emptyList(),
    val progress: ireader.presentation.ui.home.library.components.EpubImportProgress? = null,
    val summary: ireader.presentation.ui.home.library.components.EpubImportSummary? = null,
    val selectedUris: List<String> = emptyList()
)

/**
 * EPUB export state
 */
data class EpubExportState(
    val showProgress: Boolean = false,
    val showCompletion: Boolean = false,
    val progress: ireader.presentation.ui.home.library.components.EpubExportProgress? = null,
    val result: ireader.presentation.ui.home.library.components.EpubExportResult? = null
)

/**
 * Sync status for remote sync
 */
sealed class SyncStatus {
    object Idle : SyncStatus()
    object Syncing : SyncStatus()
    data class Success(val message: String) : SyncStatus()
    data class Error(val message: String) : SyncStatus()
}


open class LibraryStateImpl : LibraryState {
    override var isLoading by mutableStateOf<Boolean>(false)
    override var books by mutableStateOf<List<LibraryBook>>(emptyList())
    override val isEmpty: Boolean by derivedStateOf { books.isEmpty() }
    override var searchedBook by mutableStateOf<List<LibraryBook>>(emptyList())
    override var error by mutableStateOf<UiText>(UiText.MStringResource(Res.string.no_error))
    override var inSearchMode by mutableStateOf<Boolean>(false)
    override var searchQuery by mutableStateOf<String?>(null)
    override var sortType by mutableStateOf<LibrarySort>(
        LibrarySort(
            LibrarySort.Type.LastRead,
            true
        )
    )
    override var desc by mutableStateOf<Boolean>(false)
    override var inititialized by mutableStateOf<Boolean>(false)

    // override var filters: SnapshotStateList<FilterType> = mutableStateListOf()
    override var currentScrollState by mutableStateOf<Int>(0)
    override var selectedCategoryIndex by mutableStateOf<Int>(0)
    override val selectedCategory by derivedStateOf { categories.getOrNull(selectedCategoryIndex) }
    override var histories by mutableStateOf<List<History>>(emptyList())
    override var selectedBooks: SnapshotStateList<Long> = mutableStateListOf()
    override val selectionMode: Boolean by derivedStateOf { selectedBooks.isNotEmpty() }
    override var categories: List<CategoryWithCount> by mutableStateOf(emptyList())
    override var batchOperationInProgress by mutableStateOf<Boolean>(false)
    override var batchOperationMessage by mutableStateOf<String?>(null)
    override var lastUndoState by mutableStateOf<UndoState?>(null)
    override var showBatchOperationDialog by mutableStateOf<Boolean>(false)
    
    // New properties for toolbar actions
    override var isUpdatingLibrary by mutableStateOf<Boolean>(false)
    override var showUpdateCategoryDialog by mutableStateOf<Boolean>(false)
    override var importProgress by mutableStateOf<ImportProgress?>(null)
    
    // EPUB import/export state
    override var epubImportState by mutableStateOf(EpubImportState())
    override var epubExportState by mutableStateOf(EpubExportState())
}
