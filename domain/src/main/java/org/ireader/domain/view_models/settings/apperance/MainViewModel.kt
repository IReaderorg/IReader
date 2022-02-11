package org.ireader.domain.view_models.settings.apperance

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.ireader.core.prefs.ThemeSetting
import org.ireader.core_ui.theme.ThemeMode
import org.ireader.domain.ui.UiPreferences
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val themeSetting: ThemeSetting,
    private val uiPreferences: UiPreferences,
) : ViewModel() {

    private val _state = mutableStateOf(MainScreenState())
    val state = _state


    fun saveNightModePreferences(mode: ThemeMode) {
        uiPreferences.themeMode().set(mode)
    }
}

data class MainScreenState(
    val darkMode: Boolean = true,
)

