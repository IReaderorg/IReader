package ireader.presentation.ui.home.explore.viewmodel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.CatalogLocal
import ireader.i18n.UiText
import ireader.core.source.model.Filter
import ireader.core.source.model.Listing
import ireader.domain.models.DisplayMode
import ireader.domain.utils.extensions.replaceFirst
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Single

interface ExploreState {
    var isLoading: Boolean
    var error: UiText?
    val layout: DisplayMode
    val isSearchModeEnable: Boolean
    var searchQuery: String?
    val source: ireader.core.source.CatalogSource?
    val catalog: CatalogLocal?
    val isFilterEnable: Boolean
    var topMenuEnable: Boolean

    // var listing: Listing?
    var modifiedFilter: List<Filter<*>>

    var page: Int
    //var stateItems: List<Book>
    var endReached: Boolean

    var stateFilters: List<Filter<*>>?
    var stateListing: Listing?
}
@Factory
open class ExploreStateImpl: ExploreState {
    override var isLoading by mutableStateOf<Boolean>(false)
    override var error by mutableStateOf<UiText?>(null)
    override var layout by mutableStateOf<DisplayMode>(DisplayMode.ComfortableGrid)
    override var isSearchModeEnable by mutableStateOf<Boolean>(false)
    override var searchQuery by mutableStateOf<String?>(null)
    override val source: ireader.core.source.CatalogSource? by derivedStateOf {
        val source = catalog?.source
        if (source is ireader.core.source.CatalogSource) source else null
    }
    override var catalog by mutableStateOf<CatalogLocal?>(null)
    override var isFilterEnable by mutableStateOf<Boolean>(false)
    override var topMenuEnable: Boolean by mutableStateOf<Boolean>(false)
    override var modifiedFilter by mutableStateOf(emptyList<Filter<*>>())
    override var page by mutableStateOf<Int>(1)
  //  override var stateItems by mutableStateOf<List<Book>>(emptyList())
    override var endReached by mutableStateOf(false)
    override var stateFilters by mutableStateOf<List<Filter<*>>?>(null)
    override var stateListing by mutableStateOf<Listing?>(null)
}

@Single
class BooksState {
    var books : List<Book> by mutableStateOf(emptyList())
    var book : Book? by mutableStateOf(null)

    fun replaceBook(book: Book?) {
        if (book != null) {
            books =  books.replaceFirst({
                it.key == book.key
            }, book)
            if ((this@BooksState.book == null) || (this@BooksState.book?.id == book.id)) {
                this@BooksState.book = book
            }
        }

    }

    fun empty() {
        books = emptyList()
        book = null
    }
}