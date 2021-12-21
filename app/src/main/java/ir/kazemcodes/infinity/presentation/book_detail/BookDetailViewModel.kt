package ir.kazemcodes.infinity.presentation.book_detail

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.zhuinden.simplestack.ScopedServices
import ir.kazemcodes.infinity.domain.local_feature.domain.model.BookEntity
import ir.kazemcodes.infinity.domain.local_feature.domain.model.ChapterEntity
import ir.kazemcodes.infinity.domain.local_feature.domain.use_case.LocalUseCase
import ir.kazemcodes.infinity.domain.models.Book
import ir.kazemcodes.infinity.domain.models.Resource
import ir.kazemcodes.infinity.domain.network.models.Source
import ir.kazemcodes.infinity.domain.use_cases.remote.RemoteUseCase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


class BookDetailViewModel(
        private val localUseCase: LocalUseCase,
        private val remoteUseCase: RemoteUseCase,
        private val  source: Source,
        private val book: Book
) : ScopedServices.Registered{

    private val _state = mutableStateOf<DetailState>(DetailState())
    val state: State<DetailState> = _state

    private val _chapterState = mutableStateOf<ChapterState>(ChapterState())
    val chapterState: State<ChapterState> = _chapterState



    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)


    fun onEvent(event: BookDetailEvent) {
        when(event) {
            is BookDetailEvent.ToggleInLibrary -> {
                toggleInLibrary()
            }
        }
    }
    override fun onServiceRegistered() {
        getBookData(book)
    }

    fun getSource() : Source {
        return source
    }


    private fun toggleInLibrary(isAdded: Boolean? = null) {
        if (isAdded != null) {
            _state.value = state.value.copy(inLibrary = isAdded)
        } else {
            _state.value = state.value.copy(inLibrary = !state.value.inLibrary)
        }
    }


    private fun getBookData(book: Book) {
        _state.value = DetailState(book = book,error = "",loaded = false,isLoading = false)
        _chapterState.value = ChapterState(chapters = emptyList(), loaded = false,isLoading = false,error = "")
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


    private fun getRemoteBookDetail() {
        remoteUseCase.getRemoteBookDetailUseCase(book = state.value.book, source= source)
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

    private fun getRemoteChapterDetail() {
        remoteUseCase.getRemoteChaptersUseCase(book = state.value.book, source= source)
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
    }

}