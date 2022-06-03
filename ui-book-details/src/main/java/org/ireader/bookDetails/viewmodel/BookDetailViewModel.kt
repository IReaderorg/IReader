package org.ireader.bookDetails.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.ireader.common_extensions.async.ApplicationScope
import org.ireader.common_extensions.async.withIOContext
import org.ireader.common_extensions.launchIO
import org.ireader.common_models.entities.Book
import org.ireader.common_models.entities.CatalogLocal
import org.ireader.common_resources.UiText
import org.ireader.core.R
import org.ireader.core_api.log.Log
import org.ireader.core_api.source.CatalogSource
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
    @ApplicationScope private val applicationScope: CoroutineScope
) : BaseViewModel(), DetailState by state, ChapterState by chapterState {

    var getBookDetailJob: Job? = null
    var getChapterDetailJob: Job? = null

    var initBooks = false

    init {
        val bookId = savedStateHandle.get<Long>("bookId")
        val sourceId = savedStateHandle.get<Long>("sourceId")
        if (bookId != null && sourceId != null) {
            val catalogSource = getLocalCatalog.get(sourceId)
            this.catalogSource = catalogSource

            val source = catalogSource?.source
            if (source is CatalogSource) {
                this.modifiedCommands = source.getCommands()
            }
            toggleBookLoading(true)
            chapterIsLoading = true
            subscribeBook(bookId = bookId)
            subscribeChapters(bookId = bookId)
        } else {
            viewModelScope.launch {
                showSnackBar(UiText.StringResource(R.string.something_is_wrong_with_this_book))
            }
        }
    }

    private fun subscribeBook(bookId: Long) {
        getBookUseCases.subscribeBookById(bookId)
            .onEach { snapshot ->
                state.book = snapshot
                toggleBookLoading(false)
                if (!initBooks) {
                    initBooks = true
                    if (snapshot != null && snapshot.lastUpdate < 1L && source != null) {
                        getRemoteBookDetail(snapshot, catalogSource)
                        getRemoteChapterDetail(snapshot, catalogSource)
                    } else {
                        toggleBookLoading(false)
                        chapterIsLoading = false
                    }
                }
            }.launchIn(scope)
    }

    /**
     * sometimes the chapters snapshot is empty as if they were deleted,
     * I wasn't able to find why, so I decide to insert chapters again.
     *
     */
    private fun subscribeChapters(bookId: Long) {
        getChapterUseCase.subscribeChaptersByBookId(bookId).onEach { snapshot ->
                chapters = snapshot
        }.launchIn(viewModelScope)
    }

    suspend fun getRemoteBookDetail(book: Book, source: CatalogLocal?) {
        toggleBookLoading(true)
        getBookDetailJob?.cancel()
        getBookDetailJob = viewModelScope.launch {
            remoteUseCases.getBookDetail(
                book = book,
                catalog = source,
                onError = { message ->
                    toggleBookLoading(false)
                    if (message != null) {
                        Log.error { message.toString() }
                        showSnackBar(message)
                    }
                },
                onSuccess = { resultBook ->
                    toggleBookLoading(false)
                    localInsertUseCases.updateBook.update(resultBook)
                }

            )
        }
    }

    suspend fun getRemoteChapterDetail(
        book: Book?,
        source: CatalogLocal?,
        commands: CommandList = emptyList()
    ) {
        if (book == null) return
        chapterIsLoading = true
        getChapterDetailJob?.cancel()
        getChapterDetailJob = viewModelScope.launch {
            remoteUseCases.getRemoteChapters(
                book = book,
                catalog = source,
                onError = { message ->
                    Log.error { message.toString() }
                    showSnackBar(message)
                    chapterIsLoading = false
                },
                onSuccess = { result ->
                    withIOContext {
                        localInsertUseCases.insertChapters(result)
                    }
                    chapterIsLoading = false
                },
                commands = commands,
                oldChapters = chapterState.chapters
            )
        }
    }

    fun toggleInLibrary(book: Book, context: Context) {
        this.inLibraryLoading = true
        applicationScope.launchIO {
            if (!book.favorite) {
                localInsertUseCases.updateBook.update(
                    book.copy(
                        favorite = true,
                        dataAdded = Calendar.getInstance().timeInMillis,
                    )
                )
            } else {
                localInsertUseCases.updateBook.update(
                    book.copy(
                        favorite = false,
                    )
                )
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
}
