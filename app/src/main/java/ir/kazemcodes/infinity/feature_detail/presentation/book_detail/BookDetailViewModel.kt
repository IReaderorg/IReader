package ir.kazemcodes.infinity.feature_detail.presentation.book_detail

import android.content.Context
import android.webkit.WebView
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.kazemcodes.infinity.core.data.network.models.Source
import ir.kazemcodes.infinity.core.data.network.utils.launchIO
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.models.Chapter
import ir.kazemcodes.infinity.core.domain.use_cases.fetchers.FetchUseCase
import ir.kazemcodes.infinity.core.domain.use_cases.local.DeleteUseCase
import ir.kazemcodes.infinity.core.domain.use_cases.local.LocalGetBookUseCases
import ir.kazemcodes.infinity.core.domain.use_cases.local.LocalGetChapterUseCase
import ir.kazemcodes.infinity.core.domain.use_cases.local.LocalInsertUseCases
import ir.kazemcodes.infinity.core.domain.use_cases.preferences.reader_preferences.PreferencesUseCase
import ir.kazemcodes.infinity.core.domain.use_cases.remote.RemoteUseCases
import ir.kazemcodes.infinity.core.utils.*
import ir.kazemcodes.infinity.feature_activity.presentation.NavigationArgs.bookId
import ir.kazemcodes.infinity.feature_activity.presentation.NavigationArgs.sourceId
import ir.kazemcodes.infinity.feature_services.DownloaderService.DownloadService
import ir.kazemcodes.infinity.feature_services.DownloaderService.DownloadService.Companion.DOWNLOADER_BOOK_ID
import ir.kazemcodes.infinity.feature_services.DownloaderService.DownloadService.Companion.DOWNLOADER_SERVICE_NAME
import ir.kazemcodes.infinity.feature_services.DownloaderService.DownloadService.Companion.DOWNLOADER_SOURCE_ID
import ir.kazemcodes.infinity.feature_sources.sources.Extensions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class BookDetailViewModel @Inject constructor(
    private val preferencesUseCase: PreferencesUseCase,
    private val localInsertUseCases: LocalInsertUseCases,
    private val getChapterUseCase: LocalGetChapterUseCase,
    private val getBookUseCases: LocalGetBookUseCases,
    private val remoteUseCases: RemoteUseCases,
    private val deleteUseCase: DeleteUseCase,
    private val fetchUseCase: FetchUseCase,
    private val savedStateHandle: SavedStateHandle,
    private val extensions: Extensions
) : ViewModel(), KoinComponent {

    lateinit var source : Source
    init {
        val bookId = savedStateHandle.get<Int>(bookId.name)
        val sourceId = savedStateHandle.get<Long>(sourceId.name)
        source = sourceId?.let { extensions.mappingSourceNameToSource(it) }!!
        if (bookId != null) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    source = extensions.mappingSourceNameToSource(sourceId)
                    _state.value = state.value.copy(source =source )
                    _state.value = state.value.copy(book = state.value.book.copy(id = bookId))
                    getLocalBookById()

                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        _state.value = state.value.copy(source =source)
                        _state.value = state.value.copy(book = state.value.book.copy(id = bookId))
                        getLocalBookById()
                    }
                }
            }
        }
    }

    private val _state = mutableStateOf<DetailState>(DetailState(source=source))
    val state: State<DetailState> = _state

    private val _chapterState = mutableStateOf<ChapterState>(ChapterState())
    val chapterState: State<ChapterState> = _chapterState
    lateinit var work: OneTimeWorkRequest

    val webView: WebView by inject<WebView>()



    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()





    fun startDownloadService(context: Context) {
        work = OneTimeWorkRequestBuilder<DownloadService>().apply {
            setInputData(
                Data.Builder().apply {
                    putInt(DOWNLOADER_BOOK_ID, state.value.book.id)
                    putLong(DOWNLOADER_SOURCE_ID, state.value.book.sourceId)
                }.build()
            )
        }.build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            DOWNLOADER_SERVICE_NAME, ExistingWorkPolicy.REPLACE, work
        )
    }


    private fun getLocalBookById() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = state.value.copy(isLoading = true, error = UiText.noError())
            getBookUseCases.getBookById(id = state.value.book.id)
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            if (result.data != null) {
                                _state.value = state.value.copy(
                                    book = result.data,
                                    error = UiText.noError(),
                                    isLoading = false,
                                    isLoaded = true,
                                    inLibrary = result.data.inLibrary,
                                    isExploreMode = result.data.isExploreMode
                                )
                                if (state.value.isExploreMode) {
                                    _state.value = state.value.copy(isExploreMode = false)
                                    getRemoteBookDetail(state.value.book)
                                    getRemoteChapterDetail(state.value.book)
                                }
                                getLocalChaptersByBookId()
                            } else {
                                _state.value = state.value.copy(
                                    error = UiText.noError(),
                                    isLoading = false,
                                    isLoaded = true,

                                    )
                            }
                        }
                        is Resource.Error -> {
                            _state.value =
                                state.value.copy(isLoading = false)
                            _eventFlow.emit(UiEvent.ShowSnackbar(result.uiText
                                ?: UiText.unknownError().asString()))
                        }
                    }
                }
        }
    }


    private fun getLocalChaptersByBookId() {
        viewModelScope.launchIO {
            _chapterState.value =
                chapterState.value.copy(isLoading = true, error = "")
            getChapterUseCase.getChaptersByBookId(bookId = state.value.book.id,
                isAsc = state.value.book.areChaptersReversed)
                .collect() { result ->
                    when (result) {
                        is Resource.Success -> {
                            if (result.data != null) {
                                _chapterState.value = chapterState.value.copy(
                                    error = "",
                                    isLoading = false,
                                    loaded = true
                                )
                                if (result.data.isNotEmpty()) {
                                    _chapterState.value = chapterState.value.copy(
                                        chapters = result.data,
                                        error = "",
                                        isLoading = false,
                                        loaded = true
                                    )
                                }
                                getLastChapter()
                                if (state.value.book.totalChapters != result.data.size) {
                                    insertBookDetailToLocal(state.value.book.copy(
                                        totalChapters = chapterState.value.chapters.size))
                                }
                            } else {
                                _chapterState.value = chapterState.value.copy(
                                    error = "",
                                    isLoading = false,
                                    loaded = true
                                )
                            }
                        }
                        is Resource.Error -> {
                            getRemoteChapterDetail(book = state.value.book)
                        }
                    }
                }
        }
    }


    private fun getRemoteBookDetail(book: Book) {
        viewModelScope.launch {
            _state.value =
                state.value.copy(isLoading = true, error = UiText.noError(), isLoaded = false)
            remoteUseCases.getBookDetail(book = book, source = state.value.source)
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            if (result.data != null) {
                                /**
                                 * isExploreMode is needed for inserting because,
                                 * the explore screen get a snpashot of explore books
                                 * so the inserted book need to have an id and a explore mode.
                                 * note: explore mode will toggle off when the user goes back to
                                 * main screen
                                 */
                                _state.value = state.value.copy(
                                    book = result.data.copy(
                                        id = state.value.book.id,
                                        isExploreMode = state.value.isExploreMode,
                                    ),
                                    isLoading = false,
                                    error = UiText.noError(),
                                    isLoaded = true,
                                )
                                insertBookDetailToLocal(state.value.book.copy(dataAdded = System.currentTimeMillis()))

                            }
                        }
                        is Resource.Error -> {
                            _state.value =
                                state.value.copy(
                                    isLoading = false,
                                )
                            _eventFlow.emit(
                                UiEvent.ShowSnackbar(
                                    uiText = result.uiText ?: UiText.unknownError().asString()
                                )
                            )
                        }
                    }
                }
        }
    }

    fun getRemoteChapterDetail(book: Book) {
        viewModelScope.launch {
            _chapterState.value = chapterState.value.copy(
                isLoading = true,
                error = ""
            )
            remoteUseCases.getRemoteChapters(book = book, source = state.value.source)
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            if (result.data != null) {
                                val uniqueList =
                                    removeSameItemsFromList(chapterState.value.chapters,
                                        result.data.map {
                                            it.copy(bookId = state.value.book.id,
                                                bookName = state.value.book.bookName)
                                        }) {
                                        it.title
                                    }
                                _chapterState.value = chapterState.value.copy(
                                    chapters = result.data.map {
                                        it.copy(bookId = state.value.book.id,
                                            bookName = state.value.book.bookName)
                                    },
                                    isLoading = false,
                                    error = "",
                                )
                                deleteUseCase.deleteChaptersByBookId(state.value.book.id)
                                insertChaptersToLocal(uniqueList)
                                getLocalChaptersByBookId()
                            }
                        }
                        is Resource.Error -> {
                            _chapterState.value =
                                chapterState.value.copy(
                                    isLoading = false,
                                )

                            _eventFlow.emit(
                                UiEvent.ShowSnackbar(
                                    uiText = result.uiText ?: UiText.unknownError().asString()
                                )
                            )
                        }
                    }
                }
        }
    }

    @ExperimentalCoroutinesApi
    fun getWebViewData() {
        Timber.e("Step One")
        viewModelScope.launch {
            _eventFlow.emit(UiEvent.ShowSnackbar(
                uiText = UiText.DynamicString("Trying to fetch...").asString()
            ))
            fetchUseCase.fetchBookDetailAndChapterDetailFromWebView(
                deleteUseCase = deleteUseCase,
                insertUseCases = localInsertUseCases,
                localBook = state.value.book,
                localChapters = chapterState.value.chapters,
                source = state.value.source,
                pageSource = webView.getHtml()
            ).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        Timber.e("Step two")
                        if (result.data != null) {
                            Timber.e("Step three")
                            _eventFlow.emit(UiEvent.ShowSnackbar(
                                uiText = result.data.asString()
                            ))
                            getLocalChaptersByBookId()
                        }
                    }
                    is Resource.Error -> {
                        Timber.e("Step four")
                        _eventFlow.emit(UiEvent.ShowSnackbar(
                            uiText = result.uiText ?: UiText.unknownError().asString()
                        ))
                    }
                }
            }
        }
    }


    fun deleteChapterDetails() {
        viewModelScope.launch(Dispatchers.IO) {
            deleteUseCase.deleteChaptersByBookId(state.value.book.id)
        }
    }

    private fun readLastReadBook() {
        val lastChapter = chapterState.value.chapters.findLast {
            it.lastRead
        }
        _chapterState.value = chapterState.value.copy(lastChapter = lastChapter
            ?: chapterState.value.chapters.first())
    }

    fun insertBookDetailToLocal(book: Book) {
        viewModelScope.launch(Dispatchers.IO) {
            localInsertUseCases.insertBook(book)
        }
    }

    fun updateChaptersEntity(inLibrary: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            localInsertUseCases.insertChapters(chapterState.value.chapters.map {
                it.copy(inLibrary = inLibrary, bookId = state.value.book.id)
            })
        }
    }

    fun toggleInLibrary(add: Boolean, book: Book? = null) {
        _state.value = state.value.copy(inLibrary = add)
        viewModelScope.launch(Dispatchers.IO) {
            if (add) {
                insertBookDetailToLocal(book
                    ?: state.value.book.copy(id = state.value.book.id,
                        inLibrary = true,
                        dataAdded = System.currentTimeMillis()))
                updateChaptersEntity(true)
            } else {
                insertBookDetailToLocal((book
                    ?: state.value.book).copy(id = state.value.book.id, inLibrary = false))
                updateChaptersEntity(false)
            }
        }
    }

    fun updateInLibrary(isIn: Boolean) {
        _state.value = state.value.copy(inLibrary = isIn)
    }

    fun insertChaptersToLocal(chapters: List<Chapter>) {
        viewModelScope.launch(Dispatchers.IO) {
            deleteUseCase.deleteChaptersByBookId(bookId = state.value.book.id)
            localInsertUseCases.insertChapters(chapters.map { it.copy(bookId = state.value.book.id) })
        }
    }

    private fun getLastChapter() {
        viewModelScope.launch(Dispatchers.IO) {
            getChapterUseCase.getLastReadChapter(state.value.book.id)
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            if (result.data != null) {
                                _chapterState.value = chapterState.value.copy(
                                    lastChapter = result.data,
                                )
                            }
                        }
                        is Resource.Error -> {
                        }
                    }
                }
        }

    }

}