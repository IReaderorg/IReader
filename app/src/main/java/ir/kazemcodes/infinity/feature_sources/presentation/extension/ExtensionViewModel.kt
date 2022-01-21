package ir.kazemcodes.infinity.feature_sources.presentation.extension

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.zhuinden.simplestack.ScopedServices
import ir.kazemcodes.infinity.core.data.network.models.Source
import ir.kazemcodes.infinity.core.utils.UiEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class ExtensionViewModel: ScopedServices.Registered {

    private val _state =
        mutableStateOf(ExtensionScreenState())

    val state: State<ExtensionScreenState> = _state

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()


    override fun onServiceRegistered() {

    }

    override fun onServiceUnregistered() {
    }

    fun updateSource(sources: List<Source>) {
        _state.value = state.value.copy(sources=sources)
    }




}

data class ExtensionScreenState(
    val sources: List<Source> = emptyList()
)