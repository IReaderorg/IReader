package ir.kazemcodes.infinity.feature_settings.presentation.setting

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.zhuinden.simplestack.ScopedServices
import ir.kazemcodes.infinity.core.domain.use_cases.preferences.reader_preferences.PreferencesUseCase
import ir.kazemcodes.infinity.core.utils.UiEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class SettingViewModel(private val preferencesUseCase: PreferencesUseCase) :
    ScopedServices.Registered {
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

    override fun onServiceRegistered() {
        readDohPref()
    }

    override fun onServiceUnregistered() {

    }

    fun toggleDialog(enable : Boolean?= null) {
        _state.value = state.value.copy(dialogState = enable?:!state.value.dialogState)
    }
}