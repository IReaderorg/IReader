package ir.kazemcodes.infinity.presentation.library

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.zhuinden.simplestack.ScopedServices
import ir.kazemcodes.infinity.domain.local_feature.domain.use_case.LocalUseCase
import ir.kazemcodes.infinity.domain.models.Resource
import ir.kazemcodes.infinity.presentation.library.components.LibraryEvents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


class LibraryViewModel (
    private val localUseCase: LocalUseCase
) : ScopedServices.Registered {


    private val _state = mutableStateOf<LibraryState>(LibraryState())
    val state: State<LibraryState> = _state
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)


    override fun onServiceRegistered() {
        onEvent(LibraryEvents.GetLocalBooks)
    }
    override fun onServiceUnregistered() {
        coroutineScope.cancel()
    }

    fun onEvent(event: LibraryEvents) {
        when(event) {
            is  LibraryEvents.GetLocalBooks -> {
                getLocalBooks()
            }
        }
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
        }.launchIn(coroutineScope)
    }



}