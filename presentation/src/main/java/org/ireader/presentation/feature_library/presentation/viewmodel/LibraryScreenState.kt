package org.ireader.presentation.feature_library.presentation.viewmodel

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import org.ireader.core.utils.UiText
import org.ireader.domain.R
import org.ireader.domain.models.DisplayMode
import org.ireader.domain.models.FilterType
import org.ireader.domain.models.LayoutType
import org.ireader.domain.models.SortType
import org.ireader.domain.models.entities.BookItem
import org.ireader.domain.models.entities.History
import javax.inject.Inject


interface LibraryState {
    var isLoading: Boolean
    var books: List<BookItem>
    val isEmpty: Boolean
    var searchedBook: List<BookItem>
    var error: UiText
    var layout: LayoutType
    var inSearchMode: Boolean
    var searchQuery: String
    var sortType: SortType
    var desc: Boolean
    var filters: SnapshotStateList<FilterType>
    var currentScrollState: Int
    var histories: List<History>
    var selection: SnapshotStateList<Long>
    val hasSelection: Boolean
}

open class LibraryStateImpl @Inject constructor() : LibraryState {
    override var isLoading by mutableStateOf<Boolean>(false)
    override var books by mutableStateOf<List<BookItem>>(emptyList())
    override val isEmpty: Boolean by derivedStateOf { books.isEmpty() }
    override var searchedBook by mutableStateOf<List<BookItem>>(emptyList())
    override var error by mutableStateOf<UiText>(UiText.StringResource(R.string.no_error))
    override var layout by mutableStateOf<LayoutType>(DisplayMode.GridLayout.layout)
    override var inSearchMode by mutableStateOf<Boolean>(false)
    override var searchQuery by mutableStateOf<String>("")
    override var sortType by mutableStateOf<SortType>(SortType.LastRead)
    override var desc by mutableStateOf<Boolean>(false)
    override var filters: SnapshotStateList<FilterType> = mutableStateListOf()
    override var currentScrollState by mutableStateOf<Int>(0)
    override var histories by mutableStateOf<List<History>>(emptyList())
    override var selection: SnapshotStateList<Long> = mutableStateListOf()
    override val hasSelection: Boolean by derivedStateOf { selection.isNotEmpty() }
}


