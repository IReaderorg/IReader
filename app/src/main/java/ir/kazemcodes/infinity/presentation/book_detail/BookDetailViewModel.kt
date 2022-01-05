package ir.kazemcodes.infinity.presentation.book_detail


import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.work.*
import com.zhuinden.simplestack.ScopedServices
import ir.kazemcodes.infinity.data.network.models.Source
import ir.kazemcodes.infinity.domain.models.local.BookEntity
import ir.kazemcodes.infinity.domain.models.local.ChapterEntity
import ir.kazemcodes.infinity.domain.models.remote.Book
import ir.kazemcodes.infinity.domain.use_cases.datastore.DataStoreUseCase
import ir.kazemcodes.infinity.domain.use_cases.local.LocalUseCase
import ir.kazemcodes.infinity.domain.use_cases.remote.RemoteUseCase
import ir.kazemcodes.infinity.service.DownloadService
import ir.kazemcodes.infinity.service.DownloadService.Companion.DOWNLOAD_BOOK_NAME
import ir.kazemcodes.infinity.service.DownloadService.Companion.DOWNLOAD_SERVICE_NAME
import ir.kazemcodes.infinity.service.DownloadService.Companion.DOWNLOAD_SOURCE_NAME
import ir.kazemcodes.infinity.util.Resource
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


class BookDetailViewModel(
    private val localUseCase: LocalUseCase,
    private val remoteUseCase: RemoteUseCase,
    private val source: Source,
    private val book: Book,
    private val dataStoreUseCase: DataStoreUseCase,
) : ScopedServices.Registered {

    private val _state = mutableStateOf<DetailState>(DetailState())
    val state: State<DetailState> = _state

    private val _chapterState = mutableStateOf<ChapterState>(ChapterState())
    val chapterState: State<ChapterState> = _chapterState
    lateinit var work : OneTimeWorkRequest

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)


    fun onEvent(event: BookDetailEvent) {
        when (event) {
            is BookDetailEvent.ToggleInLibrary -> {
                toggleInLibrary()
            }
        }
    }

    override fun onServiceRegistered() {
        getBookData(book)
    }

    fun getSource(): Source {
        return source
    }


    private fun toggleInLibrary(isAdded: Boolean? = null) {
        if (isAdded != null) {
            _state.value = state.value.copy(inLibrary = isAdded)
        } else {
            _state.value = state.value.copy(inLibrary = !state.value.inLibrary)
        }
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


    private fun getBookData(book: Book) {
        _state.value = DetailState(book = book, error = "", loaded = false, isLoading = false)
        _chapterState.value =
            ChapterState(chapters = emptyList(), loaded = false, isLoading = false, error = "")
        getLocalBookByName()
        getLocalChaptersByBookName()
    }

    private fun getLocalBookByName() {
        localUseCase.getLocalBookByNameUseCase(book = state.value.book).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    if (result.data != Book.create()) {
                        _state.value = state.value.copy(
                            book = result.data ?: state.value.book,
                            error = "",
                            isLoading = false,
                            loaded = true
                        )
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
                            loaded = false
                        )

                }
                is Resource.Loading -> {
                    _state.value =
                        state.value.copy(isLoading = true, error = "", loaded = false)
                }
            }
        }.launchIn(coroutineScope)
    }


    private fun getLocalChaptersByBookName() {
        localUseCase.getLocalChaptersByBookNameByBookNameUseCase(state.value.book.bookName)
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
                            readLastReadBook()
                            toggleInLibrary(true)
                        } else {
                            if (!chapterState.value.loaded) {
                                getRemoteChapterDetail()
                            }
                        }
                    }
                    is Resource.Error -> {
                        _chapterState.value =
                            chapterState.value.copy(
                                error = result.message ?: "An Unknown Error Occurred",
                                isLoading = false,
                                loaded = false
                            )
                    }
                    is Resource.Loading -> {
                        _chapterState.value =
                            chapterState.value.copy(isLoading = true, error = "", loaded = false)
                    }
                }
            }.launchIn(coroutineScope)
    }


    fun getRemoteBookDetail() {
        remoteUseCase.getRemoteBookDetailUseCase(book = state.value.book, source = source)
            .onEach { result ->
                when (result) {

                    is Resource.Success -> {
                        _state.value = state.value.copy(
                            book = result.data ?: state.value.book,
                            isLoading = false,
                            error = "",
                            loaded = true
                        )
                    }
                    is Resource.Error -> {
                        _state.value =
                            state.value.copy(
                                error = result.message ?: "An Unknown Error Occurred",
                                isLoading = false,
                                loaded = false
                            )
                    }
                    is Resource.Loading -> {
                        _state.value =
                            state.value.copy(isLoading = true, error = "", loaded = false)
                    }
                }
            }.launchIn(coroutineScope)
    }

    fun getRemoteChapterDetail() {
        remoteUseCase.getRemoteChaptersUseCase(book = state.value.book, source = source)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        _chapterState.value = chapterState.value.copy(
                            chapters = result.data ?: emptyList(),
                            isLoading = false, error = "",
                            loaded = true
                        )
                    }
                    is Resource.Error -> {
                        _chapterState.value =
                            chapterState.value.copy(
                                error = result.message ?: "An Unknown Error Occurred",
                                isLoading = false,
                                loaded = false
                            )
                    }
                    is Resource.Loading -> {
                        _chapterState.value =
                            chapterState.value.copy(isLoading = true, error = "", loaded = false)
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

    fun insertBookDetailToLocal(bookEntity: BookEntity) {
        coroutineScope.launch(Dispatchers.IO) {
            localUseCase.insertLocalBookUserCase(bookEntity)
        }
    }

    fun insertChaptersToLocal(chapterEntities: List<ChapterEntity>) {
        coroutineScope.launch(Dispatchers.IO) {
            localUseCase.insertLocalChaptersUseCase(chapterEntities)
        }
    }

    fun deleteLocalBook(bookName: String) {
        coroutineScope.launch(Dispatchers.IO) {
            localUseCase.deleteLocalBookUseCase(bookName)
        }
    }

    fun deleteLocalChapters(bookName: String) {
        coroutineScope.launch(Dispatchers.IO) {
            localUseCase.deleteChaptersUseCase(bookName)
        }
    }

    override fun onServiceUnregistered() {
        coroutineScope.cancel()
        _state.value = state.value.copy(loaded = false)
    }

}