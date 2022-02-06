package ir.kazemcodes.infinity.feature_activity.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val themeSetting: ThemeSetting,
) : ViewModel() {

    private val _state = mutableStateOf(MainScreenState())
    val state = _state


    fun saveNightModePreferences(mode: AppTheme) {
        themeSetting.theme = mode
    }
}

data class MainScreenState(
    val darkMode: Boolean = true,
)

