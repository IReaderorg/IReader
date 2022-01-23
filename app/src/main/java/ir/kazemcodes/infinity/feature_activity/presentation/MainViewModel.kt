package ir.kazemcodes.infinity.feature_activity.presentation

import androidx.compose.runtime.mutableStateOf
import com.zhuinden.simplestack.ScopedServices
import ir.kazemcodes.infinity.core.domain.use_cases.local.DeleteUseCase
import ir.kazemcodes.infinity.feature_activity.domain.models.BottomNavigationScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber

class MainViewModel(private val deleteUseCase: DeleteUseCase) : ScopedServices.Activated {

    private val _state = mutableStateOf(MainScreenState())
    val state = _state
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    fun onEvent(event: MainScreenEvent) {
        when (event) {
            is MainScreenEvent.NavigateTo -> {
                _state.value = state.value.copy(currentScreen = event.screen)
            }

        }
    }
    override fun onServiceActive() {
        Timber.e("Register")
        setExploreModeOffForInLibraryBooks()
    }

    override fun onServiceInactive() {

    }
    private fun setExploreModeOffForInLibraryBooks() {
        coroutineScope.launch(Dispatchers.IO) {
            deleteUseCase.setExploreModeOffForInLibraryBooks()
        }
    }
}

data class MainScreenState(
    val currentScreen: BottomNavigationScreen = BottomNavigationScreen.Library
)

