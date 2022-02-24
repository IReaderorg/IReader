package org.ireader.presentation.feature_sources.presentation.extension

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.ireader.core.utils.UiEvent
import org.ireader.core_ui.viewmodel.BaseViewModel
import org.ireader.domain.models.entities.CatalogLocal
import org.ireader.domain.source.Extensions
import org.ireader.source.core.CatalogSource
import javax.inject.Inject

@HiltViewModel
class ExtensionViewModel @Inject constructor(
    private val extensions: Extensions,

    ) : BaseViewModel() {

    var state by mutableStateOf(ExtensionScreenState())
        private set


    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()


    init {
        getSources()
        //subscribeSources()
    }

    fun getSources() {
        state = state.copy(sources = extensions.getSources())
//        viewModelScope.launch {
//            catalogStore.getCatalogsFlow().collect {
//                Timber.e(it.toString())
//            }
//        }


    }



}

data class ExtensionScreenState(
    val sources: List<CatalogSource> = emptyList(),
    val communitySources: List<CatalogSource> = emptyList(),
    val catalogLocal: List<CatalogLocal> = emptyList(),
)