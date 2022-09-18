package ireader.ui.home.library.viewmodel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import ireader.common.models.entities.CategoryWithCount
import ireader.common.models.entities.History
import ireader.common.models.entities.LibraryBook
import ireader.common.models.library.LibrarySort
import ireader.i18n.UiText
import ireader.presentation.R
import org.koin.core.annotation.Single

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
}

    @Single
open class LibraryStateImpl : LibraryState {
    override var isLoading by mutableStateOf<Boolean>(false)
    override var books by mutableStateOf<List<LibraryBook>>(emptyList())
    override val isEmpty: Boolean by derivedStateOf { books.isEmpty() }
    override var searchedBook by mutableStateOf<List<LibraryBook>>(emptyList())
    override var error by mutableStateOf<UiText>(UiText.StringResource(R.string.no_error))
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
}
