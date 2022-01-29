package ir.kazemcodes.infinity.feature_activity.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.kazemcodes.infinity.core.domain.use_cases.local.DeleteUseCase
import ir.kazemcodes.infinity.feature_activity.domain.models.BottomNavigationScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val deleteUseCase: DeleteUseCase) : ViewModel() {

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
    init {
        setExploreModeOffForInLibraryBooks()
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

