package ir.kazemcodes.infinity.explore_feature.presentation.screen.book_detail_screen

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.kazemcodes.infinity.base_feature.util.Constants.TAG
import ir.kazemcodes.infinity.core.Resource
import ir.kazemcodes.infinity.explore_feature.data.model.Book
import ir.kazemcodes.infinity.explore_feature.domain.repository.dataStore
import ir.kazemcodes.infinity.explore_feature.domain.repository.moshi
import ir.kazemcodes.infinity.explore_feature.domain.use_case.RemoteUseCase
import ir.kazemcodes.infinity.library_feature.domain.model.BookEntity
import ir.kazemcodes.infinity.library_feature.domain.model.ChapterEntity
import ir.kazemcodes.infinity.library_feature.domain.use_case.LocalUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    private val localUseCase: LocalUseCase,
    private val remoteUseCase: RemoteUseCase,
    savedStateHandle: SavedStateHandle,
    application: Application
) : ViewModel() {


    private val _detailState = mutableStateOf<DetailState>(DetailState())
    val detailState: State<DetailState> = _detailState

    private val _chapterState = mutableStateOf<ChapterState>(ChapterState())
    val chapterState: State<ChapterState> = _chapterState

    //val book = detailState.value.book


    init {

//        val bookName = application.dataStore.data.map { preferences->
//            preferences[stringPreferencesKey(TEMP_BOOK)]
//        }

        getBookFromDatastore(application)

    }

    fun getBookFromDatastore(context: Context) {

        var jsonBook: String? = null

        context.dataStore.data
            .map { preferences ->
                jsonBook = preferences[stringPreferencesKey(Constants.TEMP_BOOK)]
            }.launchIn(viewModelScope).invokeOnCompletion {
                Log.d(TAG, "getBookFromDatastore: ${jsonBook}")
                val aBook = moshi.adapter(Book::class.java).fromJson(jsonBook ?: "") ?: Book("", "")
                DetailState(
                    book = aBook
                )
                getBookData(book = aBook)
                Log.d(TAG, "getBookFromDatastore: ${detailState.value.book.toString()}")
                Log.d(TAG, "getBookFromDatastore: ${_detailState.value.book.toString()}")
            }


    }

    private fun getBookData(book: Book) {
        val localSavedBook = _detailState.value.book
        getLocalBookByName(book.bookName)
        getLocalChaptersByBookName(bookName = book.bookName)
        val notLoadedCondition = localSavedBook.description.isNullOrEmpty()
        if (notLoadedCondition) {
            getRemoteBookDetail(book = book)
            getRemoteChapterDetail(book = book)
        }


    }

    private fun getLocalBookById(bookId: Int) {

        viewModelScope.launch(Dispatchers.IO) {
            localUseCase.getLocalBookByIdByIdUseCase(bookId).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        if (result.data !=null) {
                            _detailState.value = _detailState.value.copy(book = result.data)
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
            }
        }
    }

    private fun getRemoteBookAndChapter(book: Book) {
        val notLoadedCondition = book.description.isNullOrEmpty() ||
                book.coverLink.isNullOrEmpty()
        if (notLoadedCondition) {
            getRemoteBookDetail(book = book)
            getRemoteChapterDetail(book = book)
        }
    }

    private fun getLocalBookByName(bookName: String) {

        viewModelScope.launch(Dispatchers.IO) {
            localUseCase.getLocalBookByNameUseCase(bookName = bookName).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        if (result.data !=null) {
                            _detailState.value = _detailState.value.copy(
                                book = result.data
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
            }
        }
    }


    private fun getLocalChaptersByBookName(bookName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            localUseCase.getLocalChaptersByBookNameByBookNameUseCase(bookName).onEach { result ->

                when (result) {
                    is Resource.Success -> {
                        if (result.data !=null) {
                            _chapterState.value = _chapterState.value.copy(chapters = result.data)
                        }

                    }
                    is Resource.Error -> {
                        _chapterState.value =
                            ChapterState(error = result.message ?: "An Unknown Error Occurred")
                    }
                    is Resource.Loading -> {

                        _chapterState.value = ChapterState(isLoading = true)
                    }
                }
            }
        }
    }


    fun insertBookDetailToLocal(bookEntity: BookEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            localUseCase.insertLocalBookUserCase(bookEntity)
        }
    }

    fun insertChaptersToLocal(chapterEntities: List<ChapterEntity>, bookName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            localUseCase.insertLocalChapterUseCase(chapterEntities, bookName = bookName)
        }
    }

    fun deleteLocalBook(bookEntity: BookEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            localUseCase.deleteLocalBookUseCase(bookEntity)
        }
    }


    fun getRemoteBookDetail(book: Book) {

        remoteUseCase.getRemoteBookDetailUseCase(book = book).onEach { result ->

            when (result) {
                is Resource.Success -> {
                    result.data?.let {
                        _detailState.value = detailState.value.copy(
                            book = result.data
                        )
                        Log.d(
                            "TAG",
                            "getBookDetail: ${result.data.bookName} Book Detail was Successfully Loaded"
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

    private fun getRemoteChapterDetail(book: Book) {
        remoteUseCase.getRemoteChaptersUseCase(book = book).onEach { result ->

            when (result) {
                is Resource.Success -> {
                    result.data?.let {
                        _chapterState.value = ChapterState(
                            chapters = result.data,
                        )

                        if (book.inLibrary) {
                            Log.d(TAG, "getRemoteBookDetail: ${book.bookName}")
                            book.bookName?.let { it1 ->
                                insertChaptersToLocal(result.data.map {
                                    it.copy(bookName = book.bookName).toChapterEntity()
                                }, bookName = it1)
                            }
                        }
                    }
                    Log.d(
                        "TAG",
                        "getChaptersUseCase: ${result.data?.size} Chapter was Successfully Loaded Remotely"
                    )
                }
                is Resource.Error -> {
                    _chapterState.value =
                        ChapterState(error = result.message ?: "An Unknown Error Occurred")
                    Log.d(
                        "TAG",
                        "getChaptersUseCase: There is an Remote error ${result.message} "
                    )

                }
                is Resource.Loading -> {
                    _chapterState.value = ChapterState(isLoading = true)
                    Log.d(
                        "TAG",
                        "getChaptersUseCase: Loading Remote Chapter"
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

}