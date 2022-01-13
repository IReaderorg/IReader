package ir.kazemcodes.infinity.presentation.setting

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.zhuinden.simplestack.ScopedServices
import ir.kazemcodes.infinity.domain.use_cases.preferences.PreferencesUseCase

class SettingViewModel(private val preferencesUseCase: PreferencesUseCase) :
    ScopedServices.Registered {
    private val _state = mutableStateOf(SettingState())
    val state: State<SettingState> = _state



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
}