package ir.kazemcodes.infinity.presentation.book_detail


import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.work.*
import com.zhuinden.simplestack.ScopedServices
import ir.kazemcodes.infinity.data.network.models.Source
import ir.kazemcodes.infinity.domain.models.remote.Book
import ir.kazemcodes.infinity.domain.models.remote.Chapter
import ir.kazemcodes.infinity.domain.use_cases.local.LocalUseCase
import ir.kazemcodes.infinity.domain.use_cases.preferences.PreferencesUseCase
import ir.kazemcodes.infinity.domain.use_cases.remote.RemoteUseCase
import ir.kazemcodes.infinity.service.DownloadService
import ir.kazemcodes.infinity.service.DownloadService.Companion.DOWNLOAD_BOOK_NAME
import ir.kazemcodes.infinity.service.DownloadService.Companion.DOWNLOAD_SERVICE_NAME
import ir.kazemcodes.infinity.service.DownloadService.Companion.DOWNLOAD_SOURCE_NAME
import ir.kazemcodes.infinity.util.Resource
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber


class BookDetailViewModel(
    private val localUseCase: LocalUseCase,
    private val remoteUseCase: RemoteUseCase,
    private val source: Source,
    private val book: Book,
    private val preferencesUseCase: PreferencesUseCase,
) : ScopedServices.Registered {
    private val _state = mutableStateOf<DetailState>(DetailState(source = source, book = book))
    val state: State<DetailState> = _state

    private val _chapterState = mutableStateOf<ChapterState>(ChapterState())
    val chapterState: State<ChapterState> = _chapterState
    lateinit var work: OneTimeWorkRequest

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onServiceRegistered() {

        getLocalBookByName()
        getLocalChaptersByBookName()
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
        localUseCase.getLocalBookByNameUseCase(book = state.value.book).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    if (result.data != null && result.data != Book.create()) {
                        _state.value = state.value.copy(
                            book = result.data,
                            error = "",
                            isLoading = false,
                            loaded = true
                        )
                        if (result.data.inLibrary) {
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

                }
                is Resource.Loading -> {
                    _state.value =
                        state.value.copy(isLoading = true, error = "")
                }
            }
        }.launchIn(coroutineScope)
    }


    private fun getLocalChaptersByBookName() {
        localUseCase.getLocalChaptersByBookNameUseCase(state.value.book.bookName, source.name)
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
                            getLastReadChapter()
                        } else {
                            if (!state.value.loaded) {
                                getRemoteChapterDetail()
                            }
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
                        _chapterState.value =
                            chapterState.value.copy(isLoading = true, error = "")
                    }
                }
            }.launchIn(coroutineScope)
    }


    fun getRemoteBookDetail() {
        remoteUseCase.getRemoteBookDetailUseCase(book = state.value.book, source = source)
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
                            state.value.copy(isLoading = true, error = "",)
                    }
                }
            }.launchIn(coroutineScope)
    }

    fun getRemoteChapterDetail() {
        remoteUseCase.getRemoteChaptersUseCase(book = state.value.book, source = source)
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
                        _chapterState.value =
                            chapterState.value.copy(isLoading = true, error = "")
                    }
                }
            }.launchIn(coroutineScope)
    }

    private fun getLastReadChapter() {
        localUseCase.getLastReadChapterUseCase(bookName = state.value.book.bookName,
            source = source.name).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    if (result.data != null) {
                        _chapterState.value = chapterState.value.copy(
                            lastChapter = result.data,
                        )
                    } else {
                        if (chapterState.value.chapters.isNotEmpty()) {
                            _chapterState.value =
                                chapterState.value.copy(lastChapter = chapterState.value.chapters.first())
                        }
                    }
                }
                is Resource.Error -> {
                    _state.value =
                        state.value.copy(
                            error = result.message ?: "An Unknown Error Occurred",
                            isLoading = false
                        )

                }
                is Resource.Loading -> {
                    _state.value =
                        state.value.copy(isLoading = true, error = "")
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
            localUseCase.insertLocalBookUserCase(book.copy(inLibrary = false))
        }
    }

    fun updateBookDetailToLocal(inLibrary: Boolean) {
        coroutineScope.launch(Dispatchers.IO) {
            localUseCase.updateLocalBookUserCase(state.value.book.copy(inLibrary = inLibrary))
        }
    }

    fun updateChaptersEntity(inLibrary: Boolean) {
        coroutineScope.launch(Dispatchers.IO) {
            localUseCase.updateLocalChaptersContentUseCase(chapterState.value.chapters.map {
                it.copy(inLibrary = inLibrary)
            })
        }
    }

    fun updateAddToLibraryChaptersToLocal() {
        coroutineScope.launch(Dispatchers.IO) {
            localUseCase.updateAddToLibraryChaptersContentUseCase(chapterState.value.chapters.first())
        }
    }

    fun insertChaptersToLocal(chapters: List<Chapter>) {
        coroutineScope.launch(Dispatchers.IO) {
            localUseCase.insertLocalChaptersUseCase(
                chapters,
                state.value.book,
                source = source,
                inLibrary = state.value.inLibrary
            )
        }
    }


    fun deleteLocalBook(bookName: String) {
        coroutineScope.launch(Dispatchers.IO) {
            localUseCase.deleteLocalBookUseCase(bookName)
        }
    }

    fun deleteLocalChapters(bookName: String) {
        coroutineScope.launch(Dispatchers.IO) {
            localUseCase.deleteChaptersUseCase(bookName, source.name)
        }

    }

    private fun deleteNotInLibraryChapters() {
        coroutineScope.launch(Dispatchers.IO) {
            localUseCase.deleteNotInLibraryLocalChaptersUseCase()
        }

    }

    private fun deleteAllBooksNotInLibraryChapters() {
        coroutineScope.launch(Dispatchers.IO) {
            localUseCase.deleteNotInLibraryBooksUseCase()
        }
    }

    fun toggleInLibrary(inLibrary: Boolean) {
        _state.value = state.value.copy(inLibrary = inLibrary)
    }


    override fun onServiceUnregistered() {
        Timber.e("UnRegister")
        coroutineScope.cancel()
        _state.value = state.value.copy(loaded = false)
    }


}