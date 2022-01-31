package ir.kazemcodes.infinity.feature_activity.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.kazemcodes.infinity.core.domain.use_cases.local.DeleteUseCase
import ir.kazemcodes.infinity.core.domain.use_cases.preferences.apperance.NightMode
import ir.kazemcodes.infinity.core.domain.use_cases.preferences.reader_preferences.PreferencesUseCase
import ir.kazemcodes.infinity.feature_activity.domain.models.BottomNavigationScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val deleteUseCase: DeleteUseCase,
    private val preferencesUseCase: PreferencesUseCase,
) : ViewModel() {

    private val _state = mutableStateOf(MainScreenState())
    val state = _state

    val theme = preferencesUseCase.readNightModePreferences()

    fun saveTheme(mode: NightMode) {
        preferencesUseCase.saveNightModePreferences(mode = mode)
    }

    private fun getTheme() {
        viewModelScope.launch {
            preferencesUseCase.readNightModePreferences().collect { result ->
                    _state.value = state.value.copy(theme = result)
            }
        }

    }

    fun onEvent(event: MainScreenEvent) {
        when (event) {
            is MainScreenEvent.NavigateTo -> {
                _state.value = state.value.copy(currentScreen = event.screen)
            }
        }
    }

    init {
        setExploreModeOffForInLibraryBooks()
        getTheme()
    }

    private fun setExploreModeOffForInLibraryBooks() {
        viewModelScope.launch(Dispatchers.IO) {
            deleteUseCase.setExploreModeOffForInLibraryBooks()
        }
    }

    fun saveNightModePreferences(mode: NightMode) {
        preferencesUseCase.saveNightModePreferences(mode)
    }
}

data class MainScreenState(
    val currentScreen: BottomNavigationScreen = BottomNavigationScreen.Library,
    val theme : NightMode = NightMode.FollowSystem
)

