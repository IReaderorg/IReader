package org.ireader.presentation.feature_sources.presentation.extension

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.ireader.core.utils.UiEvent
import org.ireader.domain.models.source.Source
import org.ireader.domain.repository.LocalSourceRepository
import org.ireader.domain.source.Extensions
import org.ireader.domain.utils.Resource
import javax.inject.Inject

@HiltViewModel
class ExtensionViewModel @Inject constructor(
    private val localSourceRepository: LocalSourceRepository,
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


    fun updateSource(sources: List<Source>) {
        _state.value = state.value.copy(sources = sources)
    }

    fun getSources() {
        viewModelScope.launch(Dispatchers.IO) {
            localSourceRepository.getSources().onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        if (result.data != null) {
                            _state.value =
                                state.value.copy(sources = result.data!! + extensions.getSources())
                            result.data!!.forEach {
                                extensions.addSource(it)
                            }
                        }
                    }
                    is Resource.Error -> {

                    }


                }
            }.launchIn(viewModelScope)


        }
    }


}

data class ExtensionScreenState(
    val sources: List<Source> = emptyList(),
    val communitySources: List<Source> = emptyList(),
)