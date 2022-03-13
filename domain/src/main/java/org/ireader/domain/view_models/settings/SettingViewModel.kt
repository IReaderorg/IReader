package org.ireader.domain.view_models.settings

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.ireader.core.utils.UiEvent
import org.ireader.domain.use_cases.preferences.reader_preferences.DohPrefUseCase
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val dohPrefUseCase: DohPrefUseCase,
) :
    ViewModel() {
    private val _state = mutableStateOf(SettingState())
    val state: State<SettingState> = _state

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()


    fun setDohPrfUpdate(prefCode: Int) {
        //TODO Need to implement this in the more efficent way.
        _state.value = state.value.copy(doh = prefCode)
        dohPrefUseCase.save(prefCode)
    }

    private fun readDohPref() {
        _state.value = state.value
            .copy(
                doh = dohPrefUseCase.read()
            )
    }

    init {
        readDohPref()
    }

}