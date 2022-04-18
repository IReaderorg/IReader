package org.ireader.domain.view_models.settings.apperance

import androidx.annotation.Keep
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.ireader.core_ui.theme.ThemeMode
import org.ireader.core_ui.theme.UiPreferences
import org.ireader.core_ui.viewmodel.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val uiPreferences: UiPreferences,
) : BaseViewModel() {

    private val _state = mutableStateOf(MainScreenState())
    val state = _state


    fun saveNightModePreferences(mode: ThemeMode) {
        uiPreferences.themeMode().set(mode)
    }
}

@Keep
data class MainScreenState(
    val darkMode: Boolean = true,
)

