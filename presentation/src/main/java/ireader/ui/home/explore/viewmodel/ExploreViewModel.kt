package ireader.ui.home.explore.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ireader.common.extensions.DefaultPaginator
import ireader.common.resources.SourceNotFoundException
import ireader.common.models.DisplayMode
import ireader.common.models.entities.BookItem
import ireader.common.models.entities.toBook
import ireader.common.resources.UiText
import ireader.core.api.log.Log
import ireader.core.api.source.model.Filter
import ireader.core.api.source.model.MangasPageInfo
import ireader.domain.catalogs.interactor.GetLocalCatalogs
import ireader.core.ui.exceptionHandler
import ireader.core.ui.viewmodel.BaseViewModel
import ireader.domain.usecases.local.LocalGetBookUseCases
import ireader.domain.usecases.local.LocalInsertUseCases
import ireader.domain.usecases.preferences.reader_preferences.BrowseScreenPrefUseCase
import ireader.domain.usecases.remote.RemoteUseCases
import ireader.ui.component.Controller
import ireader.presentation.R
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class ExploreViewModel(
    private val state: ExploreStateImpl,
    private val remoteUseCases: RemoteUseCases,
    private val catalogStore: GetLocalCatalogs,
    private val browseScreenPrefUseCase: BrowseScreenPrefUseCase,
    private val getBookUseCases: LocalGetBookUseCases,
    val insertUseCases: LocalInsertUseCases,
    private val param: Param,
) : BaseViewModel(), ExploreState by state {
    data class Param(val sourceId: Long?, val query: String?)
    companion object {
        fun createParam(controller: Controller): Param {
            return Param(
                controller.navBackStackEntry.arguments?.getLong("sourceId"),
                controller.navBackStackEntry.arguments?.getString("query")
            )
        }
    }

    init {

        val sourceId = param.sourceId
        val query = param.query
        val catalog =
            catalogStore.find(sourceId)

        state.catalog = catalog
        val source = state.source
        if (sourceId != null && source != null) {
            if (!query.isNullOrBlank()) {
                toggleSearchMode(true)
                searchQuery = query
                loadItems()
            } else {
                val listings = source.getListings()
                if (listings.isNotEmpty()) {
                    state.stateListing = source.getListings().first()
                    loadItems()
                    viewModelScope.launch {
                        readLayoutType()
                    }
                } else {
                    viewModelScope.launch {
                        showSnackBar(UiText.StringResource(R.string.the_source_is_not_found))
                    }
                }
            }
        } else {
            viewModelScope.launch {
                showSnackBar(UiText.StringResource(R.string.the_source_is_not_found))
            }
        }
    }

    fun loadItems(reset: Boolean = false) {
        getBooksJob?.cancel()
        if (reset) {
            page = 1
            stateItems = emptyList()
        }
        getBooksJob = viewModelScope.launch {
            kotlin.runCatching {
                DefaultPaginator<Int, MangasPageInfo>(
                    initialKey = state.page,
                    onLoadUpdated = {
                        isLoading = it
                    },
                    onRequest = { nextPage ->
                        try {
                            error = null
                            Log.debug { "Explore Request was made - current page:$nextPage" }

                            val query = searchQuery
                            val filters = stateFilters
                            val listing = stateListing
                            val source = catalog
                            if (source != null) {
                                var result = MangasPageInfo(emptyList(), false)
                                remoteUseCases.getRemoteBooks(
                                    searchQuery,
                                    listing,
                                    filters,
                                    source,
                                    page,
                                    onError = {
                                        throw it
                                    },
                                    onSuccess = { res ->
                                        result = res
                                    }
                                )
                                Result.success(result)
                            } else {
                                throw SourceNotFoundException()
                            }
                        } catch (e: Throwable) {
                            Result.failure(e)
                        }
                    },
                    getNextKey = {
                        state.page + 1
                    },
                    onError = { e ->
                        endReached = true
                        stateItems = emptyList()
                        e?.let {
                            error = exceptionHandler(it)
                        }
                    },
                    onSuccess = { items, newKey ->
                        val books = items.mangas.map {
                            it.toBook(
                                sourceId = source?.id ?: -1,
                            )
                        }
                        stateItems = stateItems + books

                        page = newKey
                        endReached = !items.hasNextPage
                        Log.debug { "Request was finished $stateItems" }
                    },
                ).loadNextItems()
            }
        }
    }

    private var getBooksJob: Job? = null

    suspend fun addToFavorite(bookItem: BookItem) {
        getBookUseCases.findBookById(bookItem.id)?.let { book ->

            insertUseCases.insertBook(book.copy(favorite = !book.favorite))
        }
    }

    fun toggleSearchMode(inSearchMode: Boolean) {
        state.isSearchModeEnable = inSearchMode

        if (!inSearchMode && source != null) {
            exitSearchedMode()
            searchQuery?.let { query ->
                stateFilters = listOf(Filter.Title().apply { this.value = query })
                loadItems()
            }
        }
    }

    private fun exitSearchedMode() {
        state.searchQuery = ""
        state.apply {
            searchQuery = ""
            isLoading = false
            error = null
        }
    }

    fun saveLayoutType(layoutType: DisplayMode) {
        state.layout = layoutType
        browseScreenPrefUseCase.browseLayoutTypeUseCase.save(layoutType)
    }

    private suspend fun readLayoutType() {
        state.layout = browseScreenPrefUseCase.browseLayoutTypeUseCase.read()
    }

    fun toggleFilterMode(enable: Boolean? = null) {
        state.isFilterEnable = enable ?: !state.isFilterEnable
    }
}
