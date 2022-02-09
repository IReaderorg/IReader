package org.ireader.domain.view_models.settings.apperance

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.ireader.core.prefs.AppTheme
import org.ireader.core.prefs.ThemeSetting
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

