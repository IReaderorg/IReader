package ir.kazemcodes.infinity.feature_detail.presentation.book_detail

import android.content.Context
import android.webkit.WebView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.models.Chapter
import ir.kazemcodes.infinity.core.domain.use_cases.fetchers.FetchUseCase
import ir.kazemcodes.infinity.core.domain.use_cases.local.DeleteUseCase
import ir.kazemcodes.infinity.core.domain.use_cases.local.LocalGetBookUseCases
import ir.kazemcodes.infinity.core.domain.use_cases.local.LocalGetChapterUseCase
import ir.kazemcodes.infinity.core.domain.use_cases.local.LocalInsertUseCases
import ir.kazemcodes.infinity.core.domain.use_cases.remote.RemoteUseCases
import ir.kazemcodes.infinity.core.ui.NavigationArgs.bookId
import ir.kazemcodes.infinity.core.ui.NavigationArgs.sourceId
import ir.kazemcodes.infinity.core.utils.*
import ir.kazemcodes.infinity.feature_services.DownloaderService.DownloadService
import ir.kazemcodes.infinity.feature_services.DownloaderService.DownloadService.Companion.DOWNLOADER_BOOK_ID
import ir.kazemcodes.infinity.feature_services.DownloaderService.DownloadService.Companion.DOWNLOADER_SERVICE_NAME
import ir.kazemcodes.infinity.feature_services.DownloaderService.DownloadService.Companion.DOWNLOADER_SOURCE_ID
import ir.kazemcodes.infinity.feature_sources.sources.Extensions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import javax.inject.Inject


@HiltViewModel
class BookDetailViewModel @Inject constructor(
    private val localInsertUseCases: LocalInsertUseCases,
    private val getChapterUseCase: LocalGetChapterUseCase,
    private val getBookUseCases: LocalGetBookUseCases,
    private val remoteUseCases: RemoteUseCases,
    private val deleteUseCase: DeleteUseCase,
    private val fetchUseCase: FetchUseCase,
    savedStateHandle: SavedStateHandle,
    extensions: Extensions
) : ViewModel(), KoinComponent {

    var state by mutableStateOf(DetailState())
        private set

    var chapterState by mutableStateOf(ChapterState())
        private set

    lateinit var work: OneTimeWorkRequest

    val webView: WebView by inject()


    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        val bookId = savedStateHandle.get<Int>(bookId.name)
        val sourceId = savedStateHandle.get<Long>(sourceId.name)
        if (bookId != null && sourceId != null) {
            val source = extensions.mappingSourceNameToSource(sourceId)
            state = state.copy(source = source)
            state = state.copy(book = state.book.copy(id = bookId))
            state = state.copy(isLocalLoading = true)
            chapterState = chapterState.copy(isLocalLoading = true)
            getLocalBookById(state.book.id)
        }
    }


    fun getLocalBookById(bookId: Int) {
        state = state.copy(isLocalLoading = true, error = UiText.noError())

        getBookUseCases.getBookById(id = bookId)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        if (result.data != null) {
                            state = state.copy(
                                book = result.data,
                                error = UiText.noError(),
                                isLocalLoading = false,
                                isLocalLoaded = true,
                                inLibrary = result.data.inLibrary,
                                isExploreMode = result.data.isExploreMode,
                            )
                            if (state.isExploreMode && !state.isRemoteLoaded) {
                                getRemoteBookDetail(state.book)
                                getRemoteChapterDetail(state.book)
                            }
                            getLocalChaptersByBookId(bookId = bookId)
                        }
                    }
                    is Resource.Error -> {
                        state = state.copy(isLocalLoading = false)
                        _eventFlow.emit(UiEvent.ShowSnackbar(result.uiText
                            ?: UiText.unknownError().asString()))
                    }
                }
            }.launchIn(viewModelScope)

    }


    private fun getLocalChaptersByBookId(bookId: Int) {
        chapterState = chapterState.copy(isLocalLoading = true, error = "")

            getChapterUseCase.getChaptersByBookId(
                bookId = bookId,
                isAsc = true)
                .onEach { result ->
                    when (result) {
                        is Resource.Success -> {
                            if (result.data != null) {
                                chapterState = chapterState.copy(
                                    error = "",
                                    isLocalLoading = false,
                                    loaded = true
                                )
                                if (result.data.isNotEmpty()) {
                                    chapterState = chapterState.copy(
                                        chapters = result.data,
                                        error = "",
                                        isLocalLoading = false,
                                        loaded = true
                                    )
                                }

                                if (state.book.totalChapters != result.data.size) {
                                    insertBookDetailToLocal(state.book.copy(
                                        totalChapters = chapterState.chapters.size))
                                }
                            }
                        }
                        is Resource.Error -> {
                            chapterState = chapterState.copy(
                                isLocalLoading = false,
                            )
                            getRemoteChapterDetail(book = state.book)
                        }
                    }
                }.launchIn(viewModelScope)
    }


    private fun getRemoteBookDetail(book: Book) {
        state = state.copy(isRemoteLoading = true, error = UiText.noError(), isLocalLoaded = false)
            remoteUseCases.getBookDetail(book = book, source = state.source)
                .onEach { result ->
                    when (result) {
                        is Resource.Success -> {
                            if (result.data != null) {
                                /**
                                 * isExploreMode is needed for inserting because,
                                 * the explore screen get a snapshot of explore books
                                 * so the inserted book need to have an id and a explore mode.
                                 * note: explore mode will toggle off when the user goes back to
                                 * main screen
                                 */
                                state = state.copy(
                                    book = result.data.copy(
                                        id = state.book.id,
                                        isExploreMode = state.isExploreMode,
                                        dataAdded = System.currentTimeMillis()
                                    ),
                                    isRemoteLoading = false,
                                    error = UiText.noError(),
                                    isLocalLoaded = true,
                                    isRemoteLoaded = true
                                )
                                insertBookDetailToLocal(state.book)

                            }
                        }
                        is Resource.Error -> {
                            state =
                                state.copy(
                                    isRemoteLoading = false,
                                )
                            _eventFlow.emit(
                                UiEvent.ShowSnackbar(
                                    uiText = result.uiText ?: UiText.unknownError().asString()
                                )
                            )
                        }
                    }
                }.launchIn(viewModelScope)

    }

    fun getRemoteChapterDetail(book: Book) {
        chapterState = chapterState.copy(
            isRemoteLoading = true,
            error = "",
            loaded = false
        )

            remoteUseCases.getRemoteChapters(book = book, source = state.source)
                .onEach { result ->
                    when (result) {
                        is Resource.Success -> {
                            if (result.data != null) {
                                val uniqueList =
                                    removeSameItemsFromList(chapterState.chapters,
                                        result.data.map {
                                            it.copy(
                                                bookId = book.id,
                                                bookName = book.bookName)
                                        }) {
                                        it.title
                                    }
                                chapterState = chapterState.copy(
                                    chapters = result.data.map {
                                        it.copy(bookId = state.book.id,
                                            bookName = state.book.bookName)
                                    },
                                    isRemoteLoading = false,
                                    error = "",
                                )
                                deleteUseCase.deleteChaptersByBookId(state.book.id)
                                insertChaptersToLocal(uniqueList)
                                getLocalChaptersByBookId(bookId = state.book.id)
                            }
                        }
                        is Resource.Error -> {
                            chapterState =
                                chapterState.copy(
                                    isRemoteLoading = false,
                                )

                            _eventFlow.emit(
                                UiEvent.ShowSnackbar(
                                    uiText = result.uiText ?: UiText.unknownError().asString()
                                )
                            )
                        }
                    }
                }.launchIn(viewModelScope)

    }

    @ExperimentalCoroutinesApi
    fun getWebViewData() {
        viewModelScope.launch {
            _eventFlow.emit(UiEvent.ShowSnackbar(
                uiText = UiText.DynamicString("Trying to fetch...").asString()
            ))
            fetchUseCase.fetchBookDetailAndChapterDetailFromWebView(
                deleteUseCase = deleteUseCase,
                insertUseCases = localInsertUseCases,
                localBook = state.book,
                localChapters = chapterState.chapters,
                source = state.source,
                pageSource = webView.getHtml()
            ).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        if (result.data != null) {
                            _eventFlow.emit(UiEvent.ShowSnackbar(
                                uiText = result.data.asString()
                            ))
                            state = state.copy(isRemoteLoading = false)
                            chapterState = chapterState.copy(isRemoteLoading = false)
                            getLocalChaptersByBookId(bookId = state.book.id)
                        }
                    }
                    is Resource.Error -> {
                        _eventFlow.emit(UiEvent.ShowSnackbar(
                            uiText = result.uiText ?: UiText.unknownError().asString()
                        ))
                    }
                }
            }.launchIn(viewModelScope)
        }
    }


    private fun insertBookDetailToLocal(book: Book) {
        viewModelScope.launch(Dispatchers.IO) {
            localInsertUseCases.insertBook(book)
        }
    }

    private fun updateChaptersEntity(inLibrary: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            localInsertUseCases.insertChapters(chapterState.chapters.map {
                it.copy(inLibrary = inLibrary, bookId = state.book.id)
            })
        }
    }

    fun toggleInLibrary(add: Boolean, book: Book? = null) {
        state = state.copy(inLibrary = add)
        viewModelScope.launch(Dispatchers.IO) {
            if (add) {
                insertBookDetailToLocal(
                    book
                        ?: state.book.copy(id = state.book.id,
                            inLibrary = true,
                            dataAdded = System.currentTimeMillis()),
                )
                updateChaptersEntity(true)
            } else {
                insertBookDetailToLocal((
                        book
                            ?: state.book).copy(id = state.book.id, inLibrary = false))
                updateChaptersEntity(false)
            }
        }
    }


    private fun insertChaptersToLocal(chapters: List<Chapter>) {
        viewModelScope.launch(Dispatchers.IO) {
            deleteUseCase.deleteChaptersByBookId(bookId = state.book.id)
            localInsertUseCases.insertChapters(chapters.map { it.copy(bookId = state.book.id) })
        }
    }

    fun startDownloadService(context: Context) {
        work = OneTimeWorkRequestBuilder<DownloadService>().apply {
            setInputData(
                Data.Builder().apply {
                    putInt(DOWNLOADER_BOOK_ID, state.book.id)
                    putLong(DOWNLOADER_SOURCE_ID, state.book.sourceId)
                }.build()
            )
            addTag(DOWNLOADER_SERVICE_NAME)
        }.build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            DOWNLOADER_SERVICE_NAME.plus(state.book.id + state.book.sourceId),
            ExistingWorkPolicy.REPLACE,
            work
        )
    }
}