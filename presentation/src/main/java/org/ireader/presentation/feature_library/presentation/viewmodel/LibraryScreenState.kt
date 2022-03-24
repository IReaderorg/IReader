package org.ireader.presentation.feature_library.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.ireader.core.utils.UiText
import org.ireader.domain.R
import org.ireader.domain.models.DisplayMode
import org.ireader.domain.models.FilterType
import org.ireader.domain.models.LayoutType
import org.ireader.domain.models.SortType
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.History
import javax.inject.Inject


interface LibraryState {
    var isLoading: Boolean
    var books: List<Book>
    var searchedBook: List<Book>
    var error: UiText
    var layout: LayoutType
    var inSearchMode: Boolean
    var searchQuery: String
    var sortType: SortType
    var isSortAcs: Boolean
    var unreadFilter: FilterType
    var currentScrollState: Int
    var histories: List<History>
}

open class LibraryStateImpl @Inject constructor() : LibraryState {
    override var isLoading by mutableStateOf<Boolean>(false)
    override var books by mutableStateOf<List<Book>>(emptyList())
    override var searchedBook by mutableStateOf<List<Book>>(emptyList())
    override var error by mutableStateOf<UiText>(UiText.StringResource(R.string.no_error))
    override var layout by mutableStateOf<LayoutType>(DisplayMode.GridLayout.layout)
    override var inSearchMode by mutableStateOf<Boolean>(false)
    override var searchQuery by mutableStateOf<String>("")
    override var sortType by mutableStateOf<SortType>(SortType.LastRead)
    override var isSortAcs by mutableStateOf<Boolean>(false)
    override var unreadFilter by mutableStateOf<FilterType>(FilterType.Disable)
    override var currentScrollState by mutableStateOf<Int>(0)
    override var histories by mutableStateOf<List<History>>(emptyList())
}


