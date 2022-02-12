package org.ireader.presentation.feature_sources.presentation.extension

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.ireader.core.utils.UiEvent
import org.ireader.domain.models.source.Source
import org.ireader.domain.source.Extensions
import javax.inject.Inject

@HiltViewModel
class ExtensionViewModel @Inject constructor(
    private val extensions: Extensions,
) :
    ViewModel() {

    private val _state =
        mutableStateOf(ExtensionScreenState())

    val state: State<ExtensionScreenState> = _state

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()


    init {
        getSources()
    }

    fun getSources() {
        _state.value = state.value.copy(sources = extensions.getSources())

    }


}

data class ExtensionScreenState(
    val sources: List<Source> = emptyList(),
    val communitySources: List<Source> = emptyList(),
)