package org.ireader.bookDetails.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ireader.common_extensions.removeSameItemsFromList
import org.ireader.common_models.entities.Book
import org.ireader.common_models.entities.Chapter
import org.ireader.common_resources.UiText
import org.ireader.core.R
import org.ireader.core_api.log.Log
import org.ireader.core_api.source.CatalogSource
import org.ireader.core_api.source.Source
import org.ireader.core_api.source.model.CommandList
import org.ireader.core_catalogs.interactor.GetLocalCatalog
import org.ireader.core_ui.viewmodel.BaseViewModel
import org.ireader.domain.use_cases.local.LocalGetChapterUseCase
import org.ireader.domain.use_cases.local.LocalInsertUseCases
import org.ireader.domain.use_cases.remote.RemoteUseCases
import org.ireader.domain.use_cases.services.ServiceUseCases
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    private val localInsertUseCases: LocalInsertUseCases,
    private val getChapterUseCase: LocalGetChapterUseCase,
    private val getBookUseCases: org.ireader.domain.use_cases.local.LocalGetBookUseCases,
    private val remoteUseCases: RemoteUseCases,
    savedStateHandle: SavedStateHandle,
    private val getLocalCatalog: GetLocalCatalog,
    val state: DetailStateImpl,
    val chapterState: ChapterStateImpl,
    private val serviceUseCases: ServiceUseCases,
) : BaseViewModel(), DetailState by state, ChapterState by chapterState {

    var getBookDetailJob: Job? = null
    var getChapterDetailJob: Job? = null

    var initBooks = false
    var initChapters = false

    init {
        val bookId = savedStateHandle.get<Long>("bookId")
        val sourceId = savedStateHandle.get<Long>("sourceId")
        if (bookId != null && sourceId != null) {
            val source = getLocalCatalog.get(sourceId)?.source
            this.source = source
            if (source is CatalogSource) {
                this.modifiedCommands = source.getCommands()
            }
            toggleBookLoading(true)
            chapterIsLoading = true
            subscribeBook(bookId = bookId, onSuccess = { book ->
                setDetailBook(book)
                toggleBookLoading(false)
                if (!initBooks) {
                    initBooks = true
                    if (book.lastUpdated < 1L && source != null) {
                        getRemoteBookDetail(book, source)
                        getRemoteChapterDetail(book, source)
                    } else {
                        toggleBookLoading(false)
                        chapterIsLoading = false
                    }
                }
            })
            subscribeChapters(bookId = bookId, onSuccess = { snapshot ->
                chapters = snapshot
            })
        } else {
            viewModelScope.launch {
                showSnackBar(UiText.StringResource(R.string.something_is_wrong_with_this_book))
            }
        }
    }

    private fun subscribeBook(bookId: Long, onSuccess: suspend (Book) -> Unit) {
        getBookUseCases.subscribeBookById(bookId)
            .onEach { snapshot ->
                snapshot?.let { book ->
                    onSuccess(book)
                }
            }.launchIn(viewModelScope)
    }

    private fun subscribeChapters(bookId: Long, onSuccess: suspend (List<Chapter>) -> Unit = {}) {
        getChapterUseCase.subscribeChaptersByBookId(bookId)
            .onEach { snapshot ->
                if (snapshot.isNotEmpty()) {
                    onSuccess(snapshot)
                }
            }.launchIn(viewModelScope)
    }

    suspend fun getRemoteBookDetail(book: Book, source: Source) {
        toggleBookLoading(true)
        getBookDetailJob?.cancel()
        getBookDetailJob = viewModelScope.launch(Dispatchers.IO) {
            remoteUseCases.getBookDetail(
                book = book,
                source = source,
                onError = { message ->
                    toggleBookLoading(false)
                    insertBookDetailToLocal(state.book!!)
                    if (message != null) {
                        Log.error { message.toString() }
                        showSnackBar(message)
                    }
                },
                onSuccess = { resultBook ->
                    if (state.book != null) {
                        setDetailBook(
                            book = resultBook
                        )
                        toggleBookLoading(false)
                        insertBookDetailToLocal(resultBook)
                    }
                }

            )
        }
    }

    suspend fun getRemoteChapterDetail(
        book: Book,
        source: Source,
        commands: CommandList = emptyList()
    ) {
        chapterIsLoading = true
        getChapterDetailJob?.cancel()
        getChapterDetailJob = viewModelScope.launch {
            remoteUseCases.getRemoteChapters(
                book = book,
                source = source,
                onError = { message ->
                    Log.error { message.toString() }
                    showSnackBar(message)
                    chapterIsLoading = false
                },
                onSuccess = { result ->
                    val uniqueList =
                        removeSameItemsFromList(
                            chapterState.chapters,
                            result
                        ) {
                            it.link
                        }
                    this@BookDetailViewModel.chapters = uniqueList
                    if (uniqueList.isNotEmpty()) {
                        withContext(Dispatchers.IO) {
                            localInsertUseCases.updateChaptersUseCase(book.id, uniqueList)
                        }
                    }
                    chapterIsLoading = false
                },
                commands = commands
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
            localInsertUseCases.insertChapters(
                chapterState.chapters.map {
                    it.copy(bookId = bookId)
                }
            )
        }
    }

    fun toggleInLibrary(book: Book) {
        this.inLibraryLoading = true
        viewModelScope.launch(Dispatchers.IO) {
            if (!book.favorite) {
                insertBookDetailToLocal(
                    book.copy(
                        id = book.id,
                        favorite = true,
                        dataAdded = Calendar.getInstance().timeInMillis,
                    )
                )
                updateChaptersEntity(true, book.id)
            } else {
                insertBookDetailToLocal(
                    (
                        book.copy(
                            id = book.id,
                            favorite = false,
                        )
                        )
                )
                updateChaptersEntity(false, book.id)
            }
            this@BookDetailViewModel.inLibraryLoading = false
        }
    }

    fun startDownloadService(book: Book) {
        serviceUseCases.startDownloadServicesUseCase(bookIds = longArrayOf(book.id))
    }

    private fun toggleBookLoading(isLoading: Boolean) {
        this.detailIsLoading = isLoading
    }

    private fun setDetailBook(book: Book) {
        this.book = book
    }

    override fun onDestroy() {
        getBookDetailJob?.cancel()
        super.onDestroy()
    }
}
