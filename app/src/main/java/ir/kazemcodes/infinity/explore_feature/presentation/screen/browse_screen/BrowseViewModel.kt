package ir.kazemcodes.infinity.explore_feature.presentation.screen.browse_screen

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.kazemcodes.infinity.core.Resource
import ir.kazemcodes.infinity.explore_feature.domain.use_case.GetBooksUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class BrowseViewModel @Inject constructor(
    private val getBooksUseCase: GetBooksUseCase
) : ViewModel() {


    private val _state = mutableStateOf<BrowseScreenState>(BrowseScreenState())
    val state: State<BrowseScreenState> = _state


    init {
            getBooks(url = "https://readwebnovels.net/" , headers = mutableMapOf(
                Pair<String, String>("Referer","https://readwebnovels.net/")
            ))
        //_state.value = BrowseScreenState(books = BookTest.booksTest)
    }


    private fun getBooks(url: String , headers : Map<String,String>) {


        getBooksUseCase(url , headers ).onEach { result ->

            when (result) {
                is Resource.Success -> {
                    _state.value = BrowseScreenState(
                        books = result.data ?: emptyList()
                    )
                }
                is Resource.Error -> {
                    _state.value =
                        BrowseScreenState(error = result.message ?: "An Unknown Error Occurred")
                }
                is Resource.Loading -> {

                    _state.value = BrowseScreenState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }

}