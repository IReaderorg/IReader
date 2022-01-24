package ir.kazemcodes.infinity.feature_detail.presentation.book_detail

import android.content.Context
import android.webkit.WebView
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.work.*
import com.zhuinden.simplestack.ScopedServices
import ir.kazemcodes.infinity.core.data.network.models.Source
import ir.kazemcodes.infinity.core.data.network.utils.launchIO
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.models.Chapter
import ir.kazemcodes.infinity.core.domain.use_cases.fetchers.FetchUseCase
import ir.kazemcodes.infinity.core.domain.use_cases.local.DeleteUseCase
import ir.kazemcodes.infinity.core.domain.use_cases.local.LocalGetBookUseCases
import ir.kazemcodes.infinity.core.domain.use_cases.local.LocalGetChapterUseCase
import ir.kazemcodes.infinity.core.domain.use_cases.local.LocalInsertUseCases
import ir.kazemcodes.infinity.core.domain.use_cases.preferences.PreferencesUseCase
import ir.kazemcodes.infinity.core.domain.use_cases.remote.RemoteUseCases
import ir.kazemcodes.infinity.core.utils.Resource
import ir.kazemcodes.infinity.core.utils.UiEvent
import ir.kazemcodes.infinity.core.utils.UiText
import ir.kazemcodes.infinity.core.utils.getHtml
import ir.kazemcodes.infinity.feature_activity.domain.service.DownloadService
import ir.kazemcodes.infinity.feature_activity.domain.service.DownloadService.Companion.DOWNLOAD_BOOK_NAME
import ir.kazemcodes.infinity.feature_activity.domain.service.DownloadService.Companion.DOWNLOAD_SERVICE_NAME
import ir.kazemcodes.infinity.feature_activity.domain.service.DownloadService.Companion.DOWNLOAD_SOURCE_NAME
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import org.jsoup.Jsoup
import timber.log.Timber
import uy.kohesive.injekt.injectLazy

class BookDetailViewModel(
    private val source: Source,
    private val bookId: Int,
    private val preferencesUseCase: PreferencesUseCase,
    private val localInsertUseCases: LocalInsertUseCases,
    private val getChapterUseCase: LocalGetChapterUseCase,
    private val getBookUseCases: LocalGetBookUseCases,
    private val remoteUseCases: RemoteUseCases,
    private val deleteUseCase: DeleteUseCase,
    private val fetchUseCase: FetchUseCase,
) : ScopedServices.Registered, ScopedServices.Activated {
    private val _state = mutableStateOf<DetailState>(DetailState(source = source,
        book = Book.create().copy(id = bookId)))
    val state: State<DetailState> = _state

    private val _chapterState = mutableStateOf<ChapterState>(ChapterState())
    val chapterState: State<ChapterState> = _chapterState
    lateinit var work: OneTimeWorkRequest

    val webView by injectLazy<WebView>()


    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)


    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    override fun onServiceRegistered() {

    }

    override fun onServiceActive() {
        getLocalBookById()
    }

    override fun onServiceInactive() {

    }


    fun startDownloadService(context: Context) {
        work = OneTimeWorkRequestBuilder<DownloadService>().apply {
            setInputData(
                Data.Builder().apply {
                    putString(DOWNLOAD_BOOK_NAME, state.value.book.bookName)
                    putString(DOWNLOAD_SOURCE_NAME, state.value.book.source)
                }.build()
            )
        }.build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            DOWNLOAD_SERVICE_NAME, ExistingWorkPolicy.REPLACE, work
        )
    }


    private fun getLocalBookById() {
        coroutineScope.launch(Dispatchers.IO) {
            getBookUseCases.getBookById(id = bookId)
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            if (result.data != null) {
                                _state.value = state.value.copy(
                                    book = result.data,
                                    error = "",
                                    isLoading = false,
                                    isLoaded = true,
                                    inLibrary = result.data.inLibrary,
                                    isExploreMode = result.data.isExploreMode
                                )
                                getLocalChaptersByBookId()
                                if (state.value.isExploreMode) {
                                    _state.value = state.value.copy(isExploreMode = false)
                                    getRemoteBookDetail(state.value.book)
                                    getRemoteChapterDetail(state.value.book)
                                }
                            } else {
                                _state.value = state.value.copy(
                                    error = "",
                                    isLoading = false,
                                    isLoaded = true,

                                    )
                            }
                        }
                        is Resource.Error -> {
                            _state.value =
                                state.value.copy(isLoading = false, error = "can't find the book.")
                        }
                        is Resource.Loading -> {
                            _state.value = state.value.copy(isLoading = true, error = "")
                        }
                    }
                }
        }
    }


    private fun getLocalChaptersByBookId() {
        coroutineScope.launchIO {
            getChapterUseCase.getChaptersByBookId(bookId)
                .collect() { result ->
                    when (result) {
                        is Resource.Success -> {
                            if (result.data != null) {
                                _chapterState.value = chapterState.value.copy(
                                    chapters = result.data,
                                    error = "",
                                    isLoading = false,
                                    loaded = true
                                )
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
                        is Resource.Loading -> {
                            _chapterState.value =
                                chapterState.value.copy(isLoading = true, error = "")
                        }
                    }
                }
        }
    }


    private fun getRemoteBookDetail(book: Book) {
        coroutineScope.launch {
            remoteUseCases.getBookDetail(book = book, source = source)
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
                                        id = bookId,
                                        isExploreMode = state.value.isExploreMode,
                                    ),
                                    isLoading = false,
                                    error = "",
                                    isLoaded = true,
                                )
                                    insertBookDetailToLocal(state.value.book.copy(dataAdded = System.currentTimeMillis()))

                            }
                        }
                        is Resource.Error -> {
                            _state.value =
                                state.value.copy(
                                    error = result.message ?: "An Unknown Error Occurred",
                                    isLoading = false,
                                )
                        }
                        is Resource.Loading -> {
                            _state.value =
                                state.value.copy(isLoading = true, error = "", isLoaded = false)
                        }
                    }
                }
        }
    }

    fun getRemoteChapterDetail(book: Book) {
        coroutineScope.launch {
            remoteUseCases.getRemoteChapters(book = book, source = source)
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            if (result.data != null) {
                                _chapterState.value = chapterState.value.copy(
                                    chapters = result.data.map {
                                        it.copy(bookId = bookId,
                                            bookName = state.value.book.bookName)
                                    },
                                    isLoading = false,
                                    error = "",
                                )
                                insertChaptersToLocal(chapterState.value.chapters)
                            }
                        }
                        is Resource.Error -> {
                            _chapterState.value =
                                chapterState.value.copy(
                                    error = result.message ?: "An Unknown Error Occurred",
                                    isLoading = false,
                                )
                        }
                        is Resource.Loading -> {
                            _chapterState.value = chapterState.value.copy(
                                isLoading = true,
                                error = ""
                            )
                        }
                    }
                }
        }
    }

    @ExperimentalCoroutinesApi
    fun getWebViewData() {
        Timber.e("Step One")
        coroutineScope.launch {
            fetchUseCase.fetchBookDetailAndChapterDetailFromWebView(
                deleteUseCase = deleteUseCase,
                insertUseCases = localInsertUseCases,
                localBook = state.value.book,
                localChapters = chapterState.value.chapters,
                source = source,
                pageSource = webView.getHtml()
            ).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        Timber.e("Step two")
                        if (result.data != null) {
                            Timber.e("Step three")
                            _eventFlow.emit(UiEvent.ShowSnackbar(
                                uiText = result.data
                            ))
                        }
                    }
                    is Resource.Error -> {
                        Timber.e("Step four")
                        _eventFlow.emit(UiEvent.ShowSnackbar(
                            uiText = UiText.DynamicString(result.message.toString())
                        ))
                    }
                    is Resource.Loading -> {
                        Timber.e("Step five")
                        _eventFlow.emit(UiEvent.ShowSnackbar(
                            uiText = UiText.DynamicString("Trying to fetch...")
                        ))
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getFromWebView() {
        val webView by injectLazy<WebView>()
        coroutineScope.launch {
            _eventFlow.emit(UiEvent.ShowSnackbar(
                uiText = UiText.DynamicString("Trying to fetch Details")
            ))
            val book = source.detailParse(Jsoup.parse(webView.getHtml())).book
            val chapters = source.chaptersParse(Jsoup.parse(webView.getHtml()))
            if (!chapters.chapters.isNullOrEmpty()) {
                deleteChapterDetails()
                insertChaptersToLocal(chapters.chapters)
                insertBookDetailToLocal(state.value.book.copy(category = book.category,
                    status = book.status,
                    description = book.description,
                    author = book.author,
                    rating = book.rating))
                _eventFlow.emit(UiEvent.ShowSnackbar(
                    uiText = UiText.DynamicString("${book.bookName} was fetched with ${chapters.chapters.size} chapters")
                ))
            } else {
                _eventFlow.emit(UiEvent.ShowSnackbar(
                    uiText = UiText.DynamicString("Failed to to get the content")
                ))
            }
        }

    }

    fun deleteChapterDetails() {
        coroutineScope.launch(Dispatchers.IO) {
            deleteUseCase.deleteChaptersByBookId(bookId)
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
        coroutineScope.launch(Dispatchers.IO) {
            localInsertUseCases.insertBook(book)
        }
    }

    fun updateChaptersEntity(inLibrary: Boolean) {
        coroutineScope.launch(Dispatchers.IO) {
            localInsertUseCases.insertChapters(chapterState.value.chapters.map {
                it.copy(inLibrary = inLibrary, bookId = bookId)
            })
        }
    }

    fun toggleInLibrary(add: Boolean, book: Book? = null) {
        _state.value = state.value.copy(inLibrary = add)
        coroutineScope.launch(Dispatchers.IO) {
            if (add) {
                insertBookDetailToLocal(book
                    ?: state.value.book.copy(id = bookId,
                        inLibrary = true,
                        dataAdded = System.currentTimeMillis()))
                updateChaptersEntity(true)
            } else {
                insertBookDetailToLocal((book
                    ?: state.value.book).copy(id = bookId, inLibrary = false))
                updateChaptersEntity(false)
            }
        }
    }

    fun updateInLibrary(isIn: Boolean) {
        _state.value = state.value.copy(inLibrary = isIn)
    }

    fun insertChaptersToLocal(chapters: List<Chapter>) {
        coroutineScope.launch(Dispatchers.IO) {
            deleteUseCase.deleteChaptersByBookId(bookId = bookId)
            localInsertUseCases.insertChapters(chapters.map { it.copy(bookId = bookId) })
        }
    }

    private fun getLastChapter() {
        coroutineScope.launch(Dispatchers.IO) {
            getChapterUseCase.getLastReadChapter(bookId)
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
                        is Resource.Loading -> {
                        }
                    }
                }
        }

    }


    override fun onServiceUnregistered() {
        coroutineScope.cancel()
    }


}