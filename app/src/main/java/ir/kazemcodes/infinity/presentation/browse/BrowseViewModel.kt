package ir.kazemcodes.infinity.presentation.browse

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.kazemcodes.infinity.domain.network.models.HttpSource
import ir.kazemcodes.infinity.domain.network.models.ParsedHttpSource
import ir.kazemcodes.infinity.domain.utils.merge
import ir.kazemcodes.infinity.domain.models.Resource
import ir.kazemcodes.infinity.domain.use_cases.remote.RemoteUseCase
import ir.kazemcodes.infinity.domain.local_feature.domain.use_case.LocalUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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

    fun changeApi(api: HttpSource) {
        _state.value =state.value.copy(api = api)
    }

    fun cleanState() {
        _state.value = BrowseScreenState()
        currentPage.value = 1
    }
    fun getBooks(source: ParsedHttpSource) {
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