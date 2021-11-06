package ir.kazemcodes.infinity.presentation.screen.browse_screen

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.kazemcodes.infinity.common.Resource
import ir.kazemcodes.infinity.domain.use_case.remote.GetLatestBooksUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class BrowseViewModel @Inject constructor(
    private val getLatestBooksUseCase: GetLatestBooksUseCase
) : ViewModel() {


    private val _state = mutableStateOf<BrowseScreenState>(BrowseScreenState())
    val state: State<BrowseScreenState> = _state


    init {
        //getBooks()
    }


    private fun getBooks(page: Int = 1) {


        getLatestBooksUseCase(page = page).onEach { result ->

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