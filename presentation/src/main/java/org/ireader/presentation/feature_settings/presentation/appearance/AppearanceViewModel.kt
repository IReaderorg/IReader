package org.ireader.presentation.feature_settings.presentation.appearance

import androidx.annotation.Keep
import androidx.compose.runtime.mutableStateOf
import dagger.hilt.android.lifecycle.HiltViewModel
import org.ireader.core_ui.theme.ThemeMode
import org.ireader.core_ui.theme.UiPreferences
import org.ireader.core_ui.viewmodel.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class AppearanceViewModel @Inject constructor(
    private val uiPreferences: UiPreferences,
) : BaseViewModel() {

    private val _state = mutableStateOf(MainScreenState())
    val state = _state


    fun saveNightModePreferences(mode: ThemeMode) {
        uiPreferences.themeMode().set(mode)
    }
}


data class MainScreenState(
    val darkMode: Boolean = true,
)

