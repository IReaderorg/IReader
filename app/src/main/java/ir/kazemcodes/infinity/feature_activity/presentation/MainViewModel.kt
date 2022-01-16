package ir.kazemcodes.infinity.feature_activity.presentation

import androidx.compose.runtime.mutableStateOf
import com.zhuinden.simplestack.ScopedServices
import ir.kazemcodes.infinity.feature_activity.domain.models.BottomNavigationScreen

class MainViewModel : ScopedServices.Registered {

    private val _state = mutableStateOf(MainScreenState())
    val state = _state


    fun onEvent(event: MainScreenEvent) {
        when (event) {
            is MainScreenEvent.NavigateTo -> {
                _state.value = state.value.copy(currentScreen = event.screen)
            }

        }
    }

    override fun onServiceRegistered() {}

    override fun onServiceUnregistered() {}
}

data class MainScreenState(
    val currentScreen: BottomNavigationScreen = BottomNavigationScreen.Library
)

