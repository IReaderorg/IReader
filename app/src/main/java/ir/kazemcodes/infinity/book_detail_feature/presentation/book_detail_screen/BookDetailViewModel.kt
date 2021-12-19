package ir.kazemcodes.infinity.book_detail_feature.presentation.book_detail_screen

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.kazemcodes.infinity.api_feature.network.FreeWebNovel
import ir.kazemcodes.infinity.api_feature.network.InfinityInstance
import ir.kazemcodes.infinity.api_feature.network.ParsedHttpSource
import ir.kazemcodes.infinity.core.Resource
import ir.kazemcodes.infinity.explore_feature.data.model.Book
import ir.kazemcodes.infinity.explore_feature.domain.use_case.RemoteUseCase
import ir.kazemcodes.infinity.local_feature.domain.model.BookEntity
import ir.kazemcodes.infinity.local_feature.domain.model.ChapterEntity
import ir.kazemcodes.infinity.local_feature.domain.use_case.LocalUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookDetailViewModel @Inject constructor(
        private val localUseCase: LocalUseCase,
        private val remoteUseCase: RemoteUseCase
) : ViewModel() {
    private val _detailState = mutableStateOf<DetailState>(DetailState())
    val detailState: State<DetailState> = _detailState

    private val _chapterState = mutableStateOf<ChapterState>(ChapterState())
    val chapterState: State<ChapterState> = _chapterState


    private val _inLibrary = mutableStateOf<Boolean>(false)
    val inLibrary = _inLibrary

    private val _api = mutableStateOf<ParsedHttpSource>(FreeWebNovel())
    val api = _api.value


    fun toggleInLibrary() {
        _inLibrary.value = !inLibrary.value
    }


    fun getBookData(book: Book) {
        _detailState.value = DetailState(book = book,error = "",loaded = false,isLoading = false)
        _chapterState.value = ChapterState(chapters = emptyList(), loaded = false,isLoading = false,error = "")
        InfinityInstance.inDetailScreen = true
        getLocalBookByName()
        getLocalChaptersByBookName()
    }

    fun clearStates() {
        _detailState.value = DetailState()
        _chapterState.value = ChapterState()
    }


    private fun getLocalBookByName() {
        localUseCase.getLocalBookByNameUseCase(book = detailState.value.book).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    if (result.data != Book.create()) {
                        _detailState.value = detailState.value.copy(
                                book = result.data ?: detailState.value.book,
                                error = "",
                                isLoading = false,
                                loaded = true
                        )
                    } else {
                        if (!detailState.value.loaded) {
                            getRemoteBookDetail()
                        }
                    }
                }
                is Resource.Error -> {
                    _detailState.value =
                            detailState.value.copy(
                                    error = result.message ?: "An Unknown Error Occurred",
                                    isLoading = false,
                                    loaded = false
                            )

                }
                is Resource.Loading -> {
                    _detailState.value =
                            detailState.value.copy(isLoading = true, error = "", loaded = false)
                }
            }
        }.launchIn(viewModelScope)
    }


    private fun getLocalChaptersByBookName() {
        localUseCase.getLocalChaptersByBookNameByBookNameUseCase(detailState.value.book.bookName)
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
                                _inLibrary.value = true
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
                }.launchIn(viewModelScope)
    }


    fun getRemoteBookDetail() {
        remoteUseCase.getRemoteBookDetailUseCase(book = detailState.value.book, api)
                .onEach { result ->
                    when (result) {

                        is Resource.Success -> {
                            _detailState.value = detailState.value.copy(
                                    book = result.data ?: detailState.value.book,
                                    isLoading = false,
                                    error = "",
                                    loaded = true
                            )
                        }
                        is Resource.Error -> {
                            _detailState.value =
                                    detailState.value.copy(
                                            error = result.message ?: "An Unknown Error Occurred",
                                            isLoading = false,
                                            loaded = false
                                    )
                        }
                        is Resource.Loading -> {
                            _detailState.value =
                                    detailState.value.copy(isLoading = true, error = "", loaded = false)
                        }
                    }
                }.launchIn(viewModelScope)
    }

    private fun getRemoteChapterDetail() {
        remoteUseCase.getRemoteChaptersUseCase(book = detailState.value.book, api)
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
                }.launchIn(viewModelScope)
    }

    fun insertBookDetailToLocal(bookEntity: BookEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            localUseCase.insertLocalBookUserCase(bookEntity)
        }
    }

    fun insertChaptersToLocal(chapterEntities: List<ChapterEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            localUseCase.insertLocalChaptersUseCase(chapterEntities)
        }
    }

    fun deleteLocalBook(bookName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            localUseCase.deleteLocalBookUseCase(bookName)
        }
    }

    fun deleteLocalChapters(bookName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            localUseCase.deleteChaptersUseCase(bookName)
        }
    }

}