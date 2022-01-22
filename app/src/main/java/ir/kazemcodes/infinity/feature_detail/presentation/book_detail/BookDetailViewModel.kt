package ir.kazemcodes.infinity.feature_detail.presentation.book_detail

import android.content.Context
import android.webkit.WebView
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.work.*
import com.zhuinden.simplestack.ScopedServices
import ir.kazemcodes.infinity.core.data.network.models.Source
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.models.Chapter
import ir.kazemcodes.infinity.core.domain.repository.LocalBookRepository
import ir.kazemcodes.infinity.core.domain.repository.LocalChapterRepository
import ir.kazemcodes.infinity.core.domain.repository.RemoteRepository
import ir.kazemcodes.infinity.core.domain.use_cases.preferences.PreferencesUseCase
import ir.kazemcodes.infinity.core.presentation.components.WebViewFetcher
import ir.kazemcodes.infinity.core.utils.Resource
import ir.kazemcodes.infinity.core.utils.UiEvent
import ir.kazemcodes.infinity.core.utils.UiText
import ir.kazemcodes.infinity.core.utils.getHtml
import ir.kazemcodes.infinity.feature_activity.domain.service.DownloadService
import ir.kazemcodes.infinity.feature_activity.domain.service.DownloadService.Companion.DOWNLOAD_BOOK_NAME
import ir.kazemcodes.infinity.feature_activity.domain.service.DownloadService.Companion.DOWNLOAD_SERVICE_NAME
import ir.kazemcodes.infinity.feature_activity.domain.service.DownloadService.Companion.DOWNLOAD_SOURCE_NAME
import ir.kazemcodes.infinity.feature_sources.sources.models.FetchType
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.jsoup.Jsoup
import uy.kohesive.injekt.injectLazy

class BookDetailViewModel(
    private val source: Source,
    private val bookId: Int,
    private val preferencesUseCase: PreferencesUseCase,
    private val localBookRepository: LocalBookRepository,
    private val localChapterRepository: LocalChapterRepository,
    private val remoteRepository: RemoteRepository,
) : ScopedServices.Registered, ScopedServices.Activated {
    private val _state = mutableStateOf<DetailState>(DetailState(source = source,
        book = Book.create().copy(id = bookId)))
    val state: State<DetailState> = _state

    private val _chapterState = mutableStateOf<ChapterState>(ChapterState())
    val chapterState: State<ChapterState> = _chapterState
    lateinit var work: OneTimeWorkRequest

    val webView by injectLazy<WebView>()


    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    val viewModelExt = WebViewFetcher(coroutineScope = coroutineScope,
        source = source,
        fetcher = FetchType.Detail,
        localBookRepository = localBookRepository,
        localChapterRepository = localChapterRepository,
        url = state.value.book.link,
        webView = webView
    )

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    override fun onServiceRegistered() {
    }

    override fun onServiceActive() {
       getLocalBookById()
    }

    override fun onServiceInactive() {
        coroutineScope.cancel()
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
        localBookRepository.getBookById(id = bookId)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        if (result.data != null) {
                            _state.value = state.value.copy(
                                book = result.data,
                                error = "",
                                isLoading = false,
                                isLoaded = true
                            )
                            if (result.data.inLibrary && !state.value.inLibrary) {
                                toggleInLibrary(true)
                            }
                            if (result.data.isExploreMode) {
                                getRemoteBookDetail()
                                getRemoteChapterDetail()
                            }
                            getLocalChaptersByBookName()
                        }
                    }
                    is Resource.Error -> {
                        _state.value = state.value.copy(isLoading = false, error = "can't find the book.")
                    }
                    is Resource.Loading -> {
                        _state.value = state.value.copy(isLoading = true, error = "")
                    }
                }
            }.launchIn(coroutineScope)
    }


    private fun getLocalChaptersByBookName() {
        localChapterRepository.getChapterByName(state.value.book.bookName, source.name)
            .onEach { result ->
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
                                localBookRepository.updateLocalBook(book = state.value.book.copy(
                                    totalChapters = chapterState.value.chapters.size))
                            }
                        }
                    }
                    is Resource.Error -> {
                        getRemoteChapterDetail()
                    }
                    is Resource.Loading -> {
                        _chapterState.value =
                            chapterState.value.copy(isLoading = true, error = "")
                    }
                }
            }.launchIn(coroutineScope)
    }


    fun getRemoteBookDetail() {
        remoteRepository.getRemoteBookDetail(book = state.value.book, source = source)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        if (result.data != null) {
                            _state.value = state.value.copy(
                                book = result.data,
                                isLoading = false,
                                error = "",
                                isLoaded = true
                            )
                            insertBookDetailToLocal(result.data.copy(id = state.value.book.id))
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
                        _state.value = state.value.copy(isLoading = true, error = "", isLoaded = false)
                    }
                }
            }.launchIn(coroutineScope)
    }

    fun getRemoteChapterDetail() {
        remoteRepository.getRemoteChaptersUseCase(book = state.value.book, source = source)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        if (result.data != null) {
                            _chapterState.value = chapterState.value.copy(
                                chapters = result.data,
                                isLoading = false,
                                error = "",
                            )
                            insertChaptersToLocal(result.data)
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
            }.launchIn(coroutineScope)
    }

    fun getWebViewData() {
        viewModelExt.fetchInfo(state.value.book,chapterState.value.chapters).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    if (result.data != null) {
                        _eventFlow.emit(UiEvent.ShowSnackbar(
                            uiText = result.data
                        ))
                    }
                }
                is Resource.Error -> {
                    _eventFlow.emit(UiEvent.ShowSnackbar(
                        uiText = UiText.DynamicString(result.message.toString())
                    ))
                }
                is Resource.Loading -> {
                    _eventFlow.emit(UiEvent.ShowSnackbar(
                        uiText = UiText.DynamicString("Trying to fetch...")
                    ))
                }
            }
        }.launchIn(coroutineScope)

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
            localChapterRepository.deleteChapters(state.value.book.bookName, source.name)
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
            localBookRepository.insertBook(book.copy(inLibrary = true))
        }
    }

    fun updateChaptersEntity(inLibrary: Boolean) {
        coroutineScope.launch(Dispatchers.IO) {
            localChapterRepository.updateChapters(chapterState.value.chapters.map {
                it.copy(inLibrary = inLibrary)
            })
        }
    }

    fun toggleInLibrary(add: Boolean, book: Book? = null) {
        _state.value = state.value.copy(inLibrary = add)
        coroutineScope.launch(Dispatchers.IO) {
            if (add) {
                localBookRepository.updateLocalBook((book
                    ?: state.value.book).copy(inLibrary = true, isExploreMode = false))
                updateChaptersEntity(true)
            } else {
                localBookRepository.updateLocalBook((book
                    ?: state.value.book).copy(inLibrary = false, isExploreMode = true))
                updateChaptersEntity(false)
            }
        }
    }

    fun insertChaptersToLocal(chapters: List<Chapter>) {
        coroutineScope.launch(Dispatchers.IO) {
            localChapterRepository.deleteChapters(bookName = state.value.book.bookName,
                source = source.name)
            localChapterRepository.insertChapters(
                chapters,
                state.value.book,
                source = source,
                inLibrary = state.value.inLibrary
            )
        }
    }

    private fun getLastChapter() {
        coroutineScope.launch(Dispatchers.IO) {
            localChapterRepository.getLastReadChapter(state.value.book.bookName, source.name)
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