package ir.kazemcodes.infinity.explore_feature.presentation.screen.browse_screen

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.kazemcodes.infinity.base_feature.util.merge
import ir.kazemcodes.infinity.core.Resource
import ir.kazemcodes.infinity.explore_feature.data.model.Book
import ir.kazemcodes.infinity.explore_feature.domain.repository.dataStore
import ir.kazemcodes.infinity.explore_feature.domain.repository.moshi
import ir.kazemcodes.infinity.explore_feature.domain.use_case.RemoteUseCase
import ir.kazemcodes.infinity.explore_feature.presentation.screen.book_detail_screen.Constants.TEMP_BOOK
import ir.kazemcodes.infinity.library_feature.domain.model.BookEntity
import ir.kazemcodes.infinity.library_feature.domain.use_case.LocalUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class BrowseViewModel @Inject constructor(
    private val remoteUseCase: RemoteUseCase,
    private val localUseCase: LocalUseCase
) : ViewModel() {


    private val _state = mutableStateOf<BrowseScreenState>(BrowseScreenState())
    val state: State<BrowseScreenState> = _state
    var currentPage = mutableStateOf(1)
    init {
        getBooks(
            url = "https://readwebnovels.net/"
        )
    }


    fun getBooks(url: String) {
        remoteUseCase.getRemoteBooksUseCase(url).onEach { result ->
            Timber.d("getRemoteBooksUseCase is called")
            when (result) {
                is Resource.Success -> {
                    _state.value = state.value.copy(
                        books = merge(result.data ?: emptyList(), state.value.books),
                        isLoading = false,
                        error = ""
                    )
                    currentPage.value++
                }
                is Resource.Error -> {
                    _state.value =
                        state.value.copy(error = result.message ?: "An Unknown Error Occurred", isLoading = false)
                }
                is Resource.Loading -> {

                    _state.value = state.value.copy(isLoading = true, error = "")
                }
            }
        }.launchIn(viewModelScope)
    }

    fun insertLocalBooks(bookEntity: BookEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            localUseCase.insertLocalBookUserCase(bookEntity)

        }
    }
    fun insertTODataStore(context : Context , book: Book) {
            val jsonBook = moshi.adapter(Book::class.java).toJson(book)
        viewModelScope.launch(Dispatchers.IO) {
            context.dataStore.edit { shared ->
                shared[stringPreferencesKey(TEMP_BOOK)] = jsonBook
            }
        }
    }

}