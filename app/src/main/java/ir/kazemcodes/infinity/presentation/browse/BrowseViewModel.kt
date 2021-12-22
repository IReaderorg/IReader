package ir.kazemcodes.infinity.presentation.browse

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.zhuinden.simplestack.ScopedServices
import ir.kazemcodes.infinity.domain.local_feature.domain.use_case.LocalUseCase
import ir.kazemcodes.infinity.domain.models.Resource
import ir.kazemcodes.infinity.domain.network.models.Source
import ir.kazemcodes.infinity.domain.use_cases.remote.RemoteUseCase
import ir.kazemcodes.infinity.domain.utils.merge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber


class BrowseViewModel(
    private val localUseCase: LocalUseCase,
    private val remoteUseCase: RemoteUseCase,
    private val source: Source
) : ScopedServices.Registered {

    private val _state = mutableStateOf<BrowseScreenState>(BrowseScreenState())

    val state: State<BrowseScreenState> = _state


    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onServiceRegistered() {
        getBooks(source = source)
    }

    fun getSource() : Source {
        return source
    }
    fun onEvent(event: BrowseScreenEvents) {
        when(event) {
            is BrowseScreenEvents.UpdatePage -> {
                _state.value = state.value.copy(page = event.page)
            }
            is BrowseScreenEvents.UpdateLayoutType -> {
                updateLayoutType(event.layoutType)
            }
            is BrowseScreenEvents.ToggleMenuDropDown -> {
                toggleMenuDropDown(isShown = event.isShown)
            }
            is BrowseScreenEvents.GetBooks -> {
                getBooks(event.source)
            }
        }
    }
    private fun updateLayoutType(layoutType: LayoutType) {
        _state.value = state.value.copy(layout = layoutType)
    }

    private fun toggleMenuDropDown(isShown : Boolean) {
        _state.value = state.value.copy(isMenuDropDownShown = isShown)
    }

    private fun getBooks(source: Source) {
        remoteUseCase.getRemoteBooksUseCase(page =  state.value.page, source = source).onEach { result ->
            Timber.d("TAG getRemoteBooksUseCase is called")
            when (result) {
                is Resource.Success -> {
                    _state.value = state.value.copy(
                        books = merge(state.value.books , result.data ?: emptyList() ),
                        isLoading = false,
                        error = ""
                    )
                    onEvent(BrowseScreenEvents.UpdatePage(state.value.page + 1))
                }
                is Resource.Error -> {
                    _state.value =
                        state.value.copy(error = result.message ?: "An Unknown Error Occurred", isLoading = false)
                }
                is Resource.Loading -> {

                    _state.value = state.value.copy(isLoading = true, error = "")
                }
            }
        }.launchIn(coroutineScope)
    }



    override fun onServiceUnregistered() {
        coroutineScope.cancel()
    }

}