package org.ireader.presentation.feature_detail.presentation.book_detail.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ireader.core.R
import org.ireader.core.utils.UiText
import org.ireader.core.utils.removeSameItemsFromList
import org.ireader.core_api.log.Log
import org.ireader.core_api.source.Source
import org.ireader.core_ui.viewmodel.BaseViewModel
import org.ireader.domain.catalog.interactor.GetLocalCatalog
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.models.entities.updateBook
import org.ireader.domain.use_cases.fetchers.FetchUseCase
import org.ireader.domain.use_cases.history.HistoryUseCase
import org.ireader.domain.use_cases.local.DeleteUseCase
import org.ireader.domain.use_cases.local.LocalGetChapterUseCase
import org.ireader.domain.use_cases.local.LocalInsertUseCases
import org.ireader.domain.use_cases.remote.RemoteUseCases

import org.ireader.domain.use_cases.services.ServiceUseCases
import java.util.*
import javax.inject.Inject


@HiltViewModel
class BookDetailViewModel @Inject constructor(
    private val localInsertUseCases: LocalInsertUseCases,
    private val getChapterUseCase: LocalGetChapterUseCase,
    private val getBookUseCases: org.ireader.domain.use_cases.local.LocalGetBookUseCases,
    private val remoteUseCases: RemoteUseCases,
    private val historyUseCase: HistoryUseCase,
    private val deleteUseCase: DeleteUseCase,
    private val fetchUseCase: FetchUseCase,
    savedStateHandle: SavedStateHandle,
    private val getLocalCatalog: GetLocalCatalog,
    private val state: DetailStateImpl,
    private val chapterState: ChapterStateImpl,
    private val serviceUseCases: ServiceUseCases
) : BaseViewModel(), DetailState by state, ChapterState by chapterState {

    var isRefreshing by mutableStateOf(false)
        private set

    var expandedSummary by mutableStateOf(false)
        private set


    var getFromWebViewJob: Job? = null
    var getBookDetailJob: Job? = null
    var getChapterDetailJob: Job? = null

    lateinit var oneTimeWork: OneTimeWorkRequest


    init {
        val bookId = savedStateHandle.get<Long>("bookId")
        val sourceId = savedStateHandle.get<Long>("sourceId")
        if (bookId != null && sourceId != null) {
            val source = getLocalCatalog.get(sourceId)?.source
            this.source = source
            toggleLocalLoading(true)
            toggleChaptersLoading(true)
            viewModelScope.launch {
                getLocalBookById(bookId, source)
                getLocalChaptersByBookId(bookId = bookId)
            }

        } else {
            viewModelScope.launch {
                showSnackBar(UiText.StringResource(R.string.something_is_wrong_with_this_book))
            }
        }
    }

    fun onEvent(event: BookDetailEvent) {
        when (event) {
            is BookDetailEvent.ToggleSummary -> {
                toggleExpandedSummary()
            }

        }
    }


    suspend fun getLocalBookById(bookId: Long, source: Source?) {
        this.toggleLocalLoading(true)
        clearBookError()
        viewModelScope.launch {
            val book = getBookUseCases.findBookById(bookId)
            if (book != null) {
                toggleLocalLoading(false)
                clearBookError()
                setDetailBook(book)
                toggleInLibrary(book.favorite)
                if ((book.lastUpdated < 1L) && !state.detailIsRemoteLoaded && source != null) {
                    getRemoteBookDetail(book, source)
                    getRemoteChapterDetail(book, source)
                }
                isLocalBookLoaded(true)
            } else {
                toggleLocalLoading(false)
                showSnackBar(UiText.StringResource(R.string.no_book_found_error))
            }
        }
    }


    suspend fun getLocalChaptersByBookId(bookId: Long) {
        clearChapterError()
        this.toggleChaptersLoading(true)
        viewModelScope.launch {
            val chapters = getChapterUseCase.findChaptersByBookId(bookId)
            if (chapters.isNotEmpty()) {
                clearChapterError()
                setStateChapters(chapters)
                toggleAreChaptersLoaded(true)
            }
            toggleChaptersLoading(false)
        }
    }


    suspend fun getRemoteBookDetail(book: Book, source: Source) {
        toggleRemoteLoading(true)
        clearBookError()
        isLocalBookLoaded(false)
        getBookDetailJob?.cancel()
        getBookDetailJob = viewModelScope.launch(Dispatchers.IO) {
            remoteUseCases.getBookDetail(
                book = book,
                source = source,
                onError = { message ->

                    toggleRemoteLoading(false)
                    insertBookDetailToLocal(state.book!!)
                    if (message != null) {
                        Log.error { message.toString() }
                        showSnackBar(message)
                    }
                },
                onSuccess = { resultBook ->
                    if (state.book != null) {
                        setDetailBook(
                            book = updateBook(
                                resultBook,
                                book
                            )
                        )
                        toggleRemoteLoading(false)
                        clearBookError()
                        isLocalBookLoaded(true)
                        isRemoteBookLoaded(true)
                        insertBookDetailToLocal(state.book!!)
                    }
                }

            )
        }


    }

    suspend fun getRemoteChapterDetail(book: Book, source: Source) {
        toggleChaptersLoading(true)
        clearChapterError()
        toggleAreChaptersLoaded(false)
        getChapterDetailJob?.cancel()
        getChapterDetailJob = viewModelScope.launch {
            remoteUseCases.getRemoteChapters(
                book = book,
                source = source,
                onError = { message ->
                    Log.error { message.toString() }
                    showSnackBar(message)
                    toggleChaptersLoading(false)
                },
                onSuccess = { result ->
                    val uniqueList =

                        removeSameItemsFromList(chapterState.chapters,
                            result) {
                            it.link
                        }
                    setStateChapters(chapters = uniqueList)
                    clearChapterError()
                    if (uniqueList.isNotEmpty()) {
                        withContext(Dispatchers.IO) {
                            localInsertUseCases.updateChaptersUseCase(book.id, uniqueList)
                            getLocalChaptersByBookId(bookId = book.id)
                        }

                    }
                    toggleChaptersLoading(false)
                }
            )
        }
    }


    private fun insertBookDetailToLocal(book: Book) {
        viewModelScope.launch(Dispatchers.IO) {
            localInsertUseCases.insertBook(book.copy(dataAdded = Calendar.getInstance().timeInMillis))
        }
    }

    private fun updateChaptersEntity(inLibrary: Boolean, bookId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            deleteUseCase.deleteChaptersByBookId(bookId)
            localInsertUseCases.insertChapters(chapterState.chapters.map {
                it.copy(bookId = bookId)
            })
        }
    }

    fun toggleInLibrary(add: Boolean, book: Book) {
        this.inLibrary = add
        viewModelScope.launch(Dispatchers.IO) {
            if (add) {
                insertBookDetailToLocal(
                    book.copy(
                        id = book.id,
                        favorite = true,
                        dataAdded = Calendar.getInstance().timeInMillis,
                    )
                )
                updateChaptersEntity(true, book.id)
            } else {
                insertBookDetailToLocal((
                        book.copy(
                            id = book.id,
                            favorite = false,
                        )))
                updateChaptersEntity(false, book.id)
            }
        }
    }


    private fun insertChaptersToLocal(chapters: List<Chapter>, bookId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            localInsertUseCases.insertChapters(chapters.map { it.copy(bookId = bookId) })
        }
    }

    fun startDownloadService(context: Context, book: Book) {
     serviceUseCases.startDownloadServicesUseCase(bookIds = longArrayOf(book.id))
    }

    fun toggleExpandedSummary() {
        expandedSummary = !expandedSummary
    }

    /************************************************************/


    private fun toggleChaptersLoading(isLoading: Boolean) {
        chapterIsLoading = isLoading
    }

    private fun clearChapterError() {
        chapterError = null
    }

    private fun toggleAreChaptersLoaded(loaded: Boolean) {
        chapterIsLoaded = loaded
    }

    private fun setStateChapters(chapters: List<Chapter>) {
        this.chapters = chapters
    }


    /********************************************************/
    private fun isLocalBookLoaded(loaded: Boolean) {
        this.detailIsLocalLoaded = loaded
    }

    private fun isRemoteBookLoaded(loaded: Boolean) {
        this.detailIsRemoteLoaded = loaded
    }

    private fun toggleLocalLoading(isLoading: Boolean) {
        this.detailIsLocalLoading = isLoading
    }

    private fun toggleRemoteLoading(isLoading: Boolean) {
        this.detailIsRemoteLoading = isLoading
    }


    private fun toggleInLibrary(enable: Boolean) {
        this.inLibrary = enable
    }

    private fun setDetailBook(book: Book) {
        this.book = book
    }

    private fun clearBookError() {
        this.detailError = UiText.DynamicString("")
    }

    override fun onDestroy() {
        getBookDetailJob?.cancel()
        super.onDestroy()
    }
}


