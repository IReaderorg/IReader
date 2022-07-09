package org.ireader.appearance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dagger.hilt.android.lifecycle.HiltViewModel
import org.ireader.common_data.repository.ThemeRepository
import org.ireader.common_models.theme.BaseTheme
import org.ireader.common_models.theme.ExtraColors
import org.ireader.core_ui.preferences.PreferenceValues
import org.ireader.core_ui.preferences.UiPreferences
import org.ireader.core_ui.theme.CustomizableAppColorsPreferenceState
import org.ireader.core_ui.theme.asState
import org.ireader.core_ui.theme.getDarkColors
import org.ireader.core_ui.theme.getLightColors
import org.ireader.core_ui.viewmodel.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class AppearanceViewModel @Inject constructor(
    val uiPreferences: UiPreferences,
    val themeRepository: ThemeRepository
) : BaseViewModel() {

    private val _state = mutableStateOf(MainScreenState())
    val state = _state

    val vmThemes = themeRepository.subscribe().asState(emptyList())
    var themeEditMode by mutableStateOf(false)

    init {

    }



    val themeMode = uiPreferences.themeMode().asState()
    val colorTheme = uiPreferences.colorTheme().asState()
    val dateFormat = uiPreferences.dateFormat().asState()
    val relativeTime = uiPreferences.relativeTime().asState()
    val lightColors = uiPreferences.getLightColors().asState(scope)
    val darkColors = uiPreferences.getDarkColors().asState(scope)

    val dateFormats =
        arrayOf("", "MM/dd/yy", "dd/MM/yy", "yyyy-MM-dd", "dd MMM yyyy", "MMM dd, yyyy")
    val relativeTimes = arrayOf(
        PreferenceValues.RelativeTime.Off,
        PreferenceValues.RelativeTime.Day,
        PreferenceValues.RelativeTime.Week
    )

    fun saveNightModePreferences(mode: PreferenceValues.ThemeMode) {
        uiPreferences.themeMode().set(mode)
    }

    @Composable
    fun getCustomizedColors(): State<CustomizableAppColorsPreferenceState> {
        return remember(themeMode.value) {
            mutableStateOf(if (themeMode.value == PreferenceValues.ThemeMode.Light) lightColors else darkColors)
        }
    }


    fun getThemes(id: Long): BaseTheme? {
        val themes = vmThemes.value.firstOrNull{ it.id == id}
        val primary = if (themeMode.value == PreferenceValues.ThemeMode.Dark) {
            darkColors.primary
        } else {
            lightColors.primary
        }
        val secondary = if (themeMode.value == PreferenceValues.ThemeMode.Dark) {
            darkColors.secondary
        } else {
            lightColors.secondary
        }
        val bars = if (themeMode.value == PreferenceValues.ThemeMode.Dark) {
            darkColors.bars
        } else {
            lightColors.bars
        }
        return themes?.copy(
            id = 0,
            lightColor = themes.lightColor.copy(
                primary = primary.value,
                secondary = secondary.value
            ),
            darkColor = themes.darkColor.copy(primary = primary.value, secondary = secondary.value),
            lightExtraColors = ExtraColors(bars = bars.value),
            darkExtraColors = ExtraColors(bars = bars.value),
            default = false
        )
    }
}

data class MainScreenState(
    val darkMode: Boolean = true,
    var isSavable:Boolean = false
)
