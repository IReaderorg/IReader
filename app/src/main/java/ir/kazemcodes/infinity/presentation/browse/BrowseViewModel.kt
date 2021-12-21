package ir.kazemcodes.infinity.presentation.browse

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.kazemcodes.infinity.domain.local_feature.domain.use_case.LocalUseCase
import ir.kazemcodes.infinity.domain.models.Resource
import ir.kazemcodes.infinity.domain.network.models.Source
import ir.kazemcodes.infinity.domain.use_cases.remote.RemoteUseCase
import ir.kazemcodes.infinity.domain.utils.merge
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber


class BrowseViewModel(
    private val localUseCase: LocalUseCase,
    private val remoteUseCase: RemoteUseCase,
    private val source: Source
) : ViewModel() {
    private val _state = mutableStateOf<BrowseScreenState>(BrowseScreenState())
    val state: State<BrowseScreenState> = _state
    var currentPage = mutableStateOf(1)

    init {
        Timber.d("Timber browse viewmodel")
        getBooks(source = source)
    }
    fun getSource() : Source {
        return source
    }


    fun getBooks(source: Source) {
        remoteUseCase.getRemoteBooksUseCase(page =  currentPage.value, source = source).onEach { result ->
            Timber.d("TAG getRemoteBooksUseCase is called")
            when (result) {
                is Resource.Success -> {
                    _state.value = state.value.copy(
                        books = merge(state.value.books , result.data ?: emptyList() ),
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

}