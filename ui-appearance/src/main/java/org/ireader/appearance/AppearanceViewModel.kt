package org.ireader.appearance

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import dagger.hilt.android.lifecycle.HiltViewModel
import org.ireader.core_ui.preferences.UiPreferences
import org.ireader.core_ui.theme.CustomizableAppColorsPreferenceState
import org.ireader.core_ui.theme.ThemeMode
import org.ireader.core_ui.theme.asState
import org.ireader.core_ui.theme.getDarkColors
import org.ireader.core_ui.theme.getLightColors
import org.ireader.core_ui.theme.isLight
import org.ireader.core_ui.viewmodel.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class AppearanceViewModel @Inject constructor(
    val uiPreferences: UiPreferences,
) : BaseViewModel() {

    private val _state = mutableStateOf(MainScreenState())
    val state = _state

    val themeMode = uiPreferences.themeMode().asState()
    val lightTheme = uiPreferences.lightTheme().asState()
    val darkTheme = uiPreferences.darkTheme().asState()
    val lightColors = uiPreferences.getLightColors().asState(scope)
    val darkColors = uiPreferences.getDarkColors().asState(scope)

    fun saveNightModePreferences(mode: ThemeMode) {
        uiPreferences.themeMode().set(mode)
    }
    @Composable
    fun getCustomizedColors(): CustomizableAppColorsPreferenceState {
        return if (MaterialTheme.colorScheme.isLight()) lightColors else darkColors
    }
}

data class MainScreenState(
    val darkMode: Boolean = true,
)
