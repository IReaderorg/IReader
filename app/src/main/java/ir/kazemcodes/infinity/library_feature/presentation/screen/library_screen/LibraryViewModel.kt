package ir.kazemcodes.infinity.library_feature.presentation.screen.library_screen

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.kazemcodes.infinity.core.Resource
import ir.kazemcodes.infinity.library_feature.domain.use_case.LocalUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val localUseCase: LocalUseCase
) : ViewModel() {


    private val _state = mutableStateOf<LibraryState>(LibraryState())
    val state: State<LibraryState> = _state


    init {
        getLocalBooks()
//        val request = GET("https://www.google.com/",headers =  Headers.Builder().apply {
//            add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:86.0) Gecko/20100101 Firefox/86.0 ")
//            add("Referer", "https://www.google.com/")
//        }.build(), )
//        Timber.d("GET REQUEST " + request.)
    }


    private fun getLocalBooks() {

        localUseCase.getLocalBooksUseCase().onEach { result ->

            when (result) {
                is Resource.Success -> {
                    _state.value = LibraryState(
                        books = result.data?.map { it.copy(inLibrary = true) } ?: emptyList()
                    )
                }
                is Resource.Error -> {
                    _state.value =
                        LibraryState(error = result.message ?: "Empty")
                }
                is Resource.Loading -> {

                    _state.value = LibraryState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }

}