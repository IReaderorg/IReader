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
import ir.kazemcodes.infinity.core.utils.Resource
import ir.kazemcodes.infinity.core.utils.UiEvent
import ir.kazemcodes.infinity.core.utils.UiText
import ir.kazemcodes.infinity.core.utils.getHtml
import ir.kazemcodes.infinity.feature_activity.domain.service.DownloadService
import ir.kazemcodes.infinity.feature_activity.domain.service.DownloadService.Companion.DOWNLOAD_BOOK_NAME
import ir.kazemcodes.infinity.feature_activity.domain.service.DownloadService.Companion.DOWNLOAD_SERVICE_NAME
import ir.kazemcodes.infinity.feature_activity.domain.service.DownloadService.Companion.DOWNLOAD_SOURCE_NAME
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.jsoup.Jsoup
import timber.log.Timber
import uy.kohesive.injekt.injectLazy

class BookDetailViewModel(
    private val source: Source,
    private val book: Book,
    private val preferencesUseCase: PreferencesUseCase,
    private val localBookRepository: LocalBookRepository,
    private val localChapterRepository: LocalChapterRepository,
    private val remoteRepository: RemoteRepository,
    private val isLocal: Boolean,
) : ScopedServices.Registered,ScopedServices.Activated {
    private val _state = mutableStateOf<DetailState>(DetailState(source = source, book = book))
    val state: State<DetailState> = _state

    private val _chapterState = mutableStateOf<ChapterState>(ChapterState())
    val chapterState: State<ChapterState> = _chapterState
    lateinit var work: OneTimeWorkRequest

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)


    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    override fun onServiceRegistered() {
    }

    override fun onServiceActive() {
        getLocalBookByName()
    }

    override fun onServiceInactive() {

    }


    fun startDownloadService(context: Context) {
        work = OneTimeWorkRequestBuilder<DownloadService>().apply {
            setInputData(
                Data.Builder().apply {
                    putString(DOWNLOAD_BOOK_NAME, book.bookName)
                    putString(DOWNLOAD_SOURCE_NAME, book.source)
                }.build()
            )
        }.build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            DOWNLOAD_SERVICE_NAME, ExistingWorkPolicy.REPLACE, work
        )
    }


    private fun getLocalBookByName() {
        localBookRepository.getLocalBookByName(state.value.book.bookName).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    if (result.data != null && result.data != Book.create()) {
                        _state.value = state.value.copy(
                            book = result.data,
                            error = "",
                            isLoading = false,
                            loaded = true
                        )
                        getLocalChaptersByBookName()
                        if (result.data.inLibrary && !state.value.inLibrary) {
                            toggleInLibrary(true)
                        }
                    } else {
                        if (!state.value.loaded) {
                            getRemoteBookDetail()
                        }
                    }
                }
                is Resource.Error -> {
                    _state.value =
                        state.value.copy(
                            error = result.message ?: "An Unknown Error Occurred",
                            isLoading = false,
                        )
                    getRemoteBookDetail()


                }
                is Resource.Loading -> {
                    _state.value =
                        state.value.copy(isLoading = true, error = "")
                }
            }
        }.launchIn(coroutineScope)
    }


    private fun getLocalChaptersByBookName() {
        localChapterRepository.getChapterByName(state.value.book.bookName, source.name)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        if (!result.data.isNullOrEmpty()) {
                            _chapterState.value = chapterState.value.copy(
                                chapters = result.data,
                                error = "",
                                isLoading = false,
                                loaded = true
                            )
                            getLastChapter()
                            if (book.totalChapters != result.data.size) {
                                localBookRepository.updateLocalBook(book = book.copy(totalChapters = chapterState.value.chapters.size))

                            }
                        } else {
                            getRemoteChapterDetail()
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
                                loaded = true
                            )
                            insertBookDetailToLocal(result.data)
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
                            state.value.copy(isLoading = true, error = "")
                    }
                }
            }.launchIn(coroutineScope)
    }

    fun getRemoteChapterDetail() {
        remoteRepository.getRemoteChaptersUseCase(book = state.value.book, source = source)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        if (result.data != null && result.data.isNotEmpty()) {
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
            localBookRepository.insertBook(book)
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
                    ?: state.value.book).copy(inLibrary = true))
                updateChaptersEntity(true)
            } else {
                localBookRepository.updateLocalBook((book
                    ?: state.value.book).copy(inLibrary = false))
                updateChaptersEntity(false)
            }
        }
    }

    fun insertChaptersToLocal(chapters: List<Chapter>) {
        coroutineScope.launch(Dispatchers.IO) {
            localChapterRepository.deleteChapters(bookName = book.bookName, source = source.name)
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
        Timber.e("UnRegister")
        coroutineScope.cancel()
        _state.value = state.value.copy(loaded = false)
    }


}