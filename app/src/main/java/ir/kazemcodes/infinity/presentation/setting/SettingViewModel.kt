package ir.kazemcodes.infinity.presentation.setting

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.zhuinden.simplestack.ScopedServices
import ir.kazemcodes.infinity.domain.use_cases.preferences.PreferencesUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class SettingViewModel(private val preferencesUseCase: PreferencesUseCase) :
    ScopedServices.Registered {
    private val _state = mutableStateOf<SettingState>(SettingState())
    val state: State<SettingState> = _state

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)


    fun setDohPrfUpdate(prefCode: Int) {
        _state.value = state.value.copy(doh = prefCode)
        preferencesUseCase.saveDohPrefUseCase(prefCode)
    }

    fun readDohPref() {
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