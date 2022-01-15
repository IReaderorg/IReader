package ir.kazemcodes.infinity.feature_activity.presentation

import androidx.compose.runtime.mutableStateOf
import com.zhuinden.simplestack.ScopedServices

class MainViewModel : ScopedServices.Registered {

    private val _state = mutableStateOf(MainScreenState())
    val state = _state


    fun onEvent(event: MainScreenEvent) {
        when (event) {
            is MainScreenEvent.ChangeScreenIndex -> {
                _state.value = state.value.copy(index = event.index)
            }

        }
    }

    override fun onServiceRegistered() {}

    override fun onServiceUnregistered() {}
}

data class MainScreenState(
    val index: Int = 0,
)

