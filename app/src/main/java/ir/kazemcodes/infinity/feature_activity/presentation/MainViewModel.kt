package ir.kazemcodes.infinity.feature_activity.presentation

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.kazemcodes.infinity.core.domain.use_cases.local.DeleteUseCase
import ir.kazemcodes.infinity.core.domain.use_cases.preferences.apperance.NightMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val deleteUseCase: DeleteUseCase,
    private val themeSetting: ThemeSetting,
) : ViewModel() {

    private val _state = mutableStateOf(MainScreenState())
    val state = _state


    fun saveTheme(mode: NightMode) {
        when (mode) {
            is NightMode.FollowSystem -> {
                _state.value =
                    state.value.copy(darkMode = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
            }
            is NightMode.Enable -> {
                _state.value = state.value.copy(darkMode = true)
            }
            is NightMode.Disable -> {
                _state.value = state.value.copy(darkMode = false)
            }
        }

    }


    init {
        setExploreModeOffForInLibraryBooks()
    }

    private fun setExploreModeOffForInLibraryBooks() {
        viewModelScope.launch(Dispatchers.IO) {
            deleteUseCase.setExploreModeOffForInLibraryBooks()
        }
    }

    fun saveNightModePreferences(mode: AppTheme) {
        themeSetting.theme = mode
    }
}

data class MainScreenState(
    val darkMode: Boolean = true,
)

