package ir.kazemcodes.infinity.feature_detail.presentation.book_detail

import android.content.Context
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
import ir.kazemcodes.infinity.feature_activity.domain.service.DownloadService
import ir.kazemcodes.infinity.feature_activity.domain.service.DownloadService.Companion.DOWNLOAD_BOOK_NAME
import ir.kazemcodes.infinity.feature_activity.domain.service.DownloadService.Companion.DOWNLOAD_SERVICE_NAME
import ir.kazemcodes.infinity.feature_activity.domain.service.DownloadService.Companion.DOWNLOAD_SOURCE_NAME
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

class BookDetailViewModel(
    private val source: Source,
    private val book: Book,
    private val preferencesUseCase: PreferencesUseCase,
    private val localBookRepository: LocalBookRepository,
    private val localChapterRepository: LocalChapterRepository,
    private val remoteRepository: RemoteRepository,
    private val isLocal: Boolean,
) : ScopedServices.Registered {
    private val _state = mutableStateOf<DetailState>(DetailState(source = source, book = book))
    val state: State<DetailState> = _state

    private val _chapterState = mutableStateOf<ChapterState>(ChapterState())
    val chapterState: State<ChapterState> = _chapterState
    lateinit var work: OneTimeWorkRequest

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onServiceRegistered() {
        getLocalBookByName()

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
                        } else {
                            getRemoteChapterDetail()
                        }
                    }
                    is Resource.Error -> {
                        getRemoteChapterDetail()
                        _chapterState.value =
                            chapterState.value.copy(
                                error = result.message ?: "An Unknown Error Occurred",
                                isLoading = false,
                            )
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
                        if (!result.data.isNullOrEmpty()) {
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
                        _chapterState.value = chapterState.value.copy(isLoading = true, error = "")
                    }
                }
            }.launchIn(coroutineScope)
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