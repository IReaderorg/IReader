package ir.kazemcodes.infinity.explore_feature.presentation.screen.book_detail_screen

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.kazemcodes.infinity.core.Resource
import ir.kazemcodes.infinity.explore_feature.data.ParsedHttpSource
import ir.kazemcodes.infinity.explore_feature.data.model.Book
import ir.kazemcodes.infinity.explore_feature.domain.use_case.GetBookDetailUseCase
import ir.kazemcodes.infinity.explore_feature.domain.use_case.GetChaptersUseCase
import ir.kazemcodes.infinity.explore_feature.presentation.screen.book_detail_screen.Constants.PARAM_BOOK_TITLE
import ir.kazemcodes.infinity.explore_feature.presentation.screen.book_detail_screen.Constants.PARAM_BOOK_URL
import ir.kazemcodes.infinity.library_feature.domain.model.BookEntity
import ir.kazemcodes.infinity.library_feature.domain.use_case.LocalUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    private val getBookDetailUseCase: GetBookDetailUseCase,
    private val getChaptersUseCase: GetChaptersUseCase,
    private val localUseCase: LocalUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {


    private val _detailState = mutableStateOf<DetailState>(DetailState())
    val detailState: State<DetailState> = _detailState

    private val _chapterState = mutableStateOf<ChapterState>(ChapterState())
    val chapterState: State<ChapterState> = _chapterState


    init {
        savedStateHandle.get<String>(PARAM_BOOK_URL)?.let { bookUrl ->
            savedStateHandle.get<String>(PARAM_BOOK_TITLE)?.let { bookTitle ->
                getBookDetail(
                    Book(
                        name = bookTitle,
                        link = bookUrl
                    ),
                    url = bookUrl,
                    headers = mutableMapOf(
                        Pair<String, String>("Referer", "https://readwebnovels.net/")
                    )
                )
                getChapters(
                    book = Book(
                        name = bookTitle,
                        link = bookUrl
                    ),
                    mutableMapOf(
                        Pair<String, String>("Referer", "https://readwebnovels.net/"),
                        Pair<String, String>("User-Agent", ParsedHttpSource.DEFAULT_USER_AGENT),
                    )
                )

            }
        }

    }

    fun insertBookDetail(bookEntity: BookEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            localUseCase.insertLocalBookUserCase(bookEntity)
        }
    }

    fun deleteBook(bookEntity: BookEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            localUseCase.deleteLocalBookUseCase(bookEntity)
        }
    }
    fun getChapters(book: Book, headers: Map<String, String>) {

        getChaptersUseCase(book = book, headers = headers).onEach { result ->

            when (result) {
                is Resource.Success -> {

                    _chapterState.value = ChapterState(
                        chapters = result.data ?: emptyList()
                    )
                }
                is Resource.Error -> {
                    _chapterState.value =
                        ChapterState(error = result.message ?: "An Unknown Error Occurred")
                }
                is Resource.Loading -> {
                    _chapterState.value = ChapterState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)


    }


    fun getBookDetail(book: Book, url: String, headers: Map<String, String>) {

        getBookDetailUseCase(book = book, url, headers = headers).onEach { result ->

            when (result) {
                is Resource.Success -> {
                    result.data?.let { book ->
                        _detailState.value = DetailState(
                            book = book
                        )
                    }
                }
                is Resource.Error -> {
                    _detailState.value =
                        DetailState(error = result.message ?: "An Unknown Error Occurred")
                }
                is Resource.Loading -> {

                    _detailState.value = DetailState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)


    }

}