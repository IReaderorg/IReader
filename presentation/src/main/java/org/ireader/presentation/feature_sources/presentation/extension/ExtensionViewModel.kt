package org.ireader.presentation.feature_sources.presentation.extension

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.ireader.core.utils.UiEvent
import org.ireader.domain.extensions.cataloge_service.CatalogStore
import org.ireader.domain.models.entities.CatalogLocal
import org.ireader.domain.source.Extensions
import org.ireader.source.core.Source
import javax.inject.Inject

@HiltViewModel
class ExtensionViewModel @Inject constructor(
    private val extensions: Extensions,
    private val catalogStore: CatalogStore,
) :
    ViewModel() {

    var state by mutableStateOf(ExtensionScreenState())
        private set


    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()


    init {
        getSources()
        subscribeSources()
    }

    fun getSources() {
        state = state.copy(sources = extensions.getSources())

    }

    fun subscribeSources() {
        viewModelScope.launch {
            catalogStore.getCatalogsFlow().collect { catalog ->
                state = state.copy(catalogLocal = catalog)
                catalog.forEach {
                    state = state.copy(sources = state.sources.plus(it.source))
                }
            }
        }

    }


}

data class ExtensionScreenState(
    val sources: List<Source> = emptyList(),
    val communitySources: List<Source> = emptyList(),
    val catalogLocal: List<CatalogLocal> = emptyList(),
)