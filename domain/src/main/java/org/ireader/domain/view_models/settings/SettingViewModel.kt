package org.ireader.domain.view_models.settings

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.ireader.core.utils.UiEvent
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(private val preferencesUseCase: org.ireader.domain.use_cases.preferences.reader_preferences.PreferencesUseCase) :
    ViewModel() {
    private val _state = mutableStateOf(SettingState())
    val state: State<SettingState> = _state

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()


    fun setDohPrfUpdate(prefCode: Int) {
        _state.value = state.value.copy(doh = prefCode)
        preferencesUseCase.saveDohPrefUseCase(prefCode)
    }

    private fun readDohPref() {
        _state.value = state.value
            .copy(
                doh = preferencesUseCase.readDohPrefUseCase()
            )
    }

    init {
        readDohPref()
    }

}