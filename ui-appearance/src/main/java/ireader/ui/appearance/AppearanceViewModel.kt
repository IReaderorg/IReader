package ireader.ui.appearance

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.common.data.repository.ThemeRepository
import ireader.common.models.theme.ExtraColors
import ireader.common.models.theme.Theme
import ireader.core.ui.preferences.PreferenceValues
import ireader.core.ui.preferences.UiPreferences
import ireader.core.ui.theme.CustomizableAppColorsPreferenceState
import ireader.core.ui.theme.asState
import ireader.core.ui.theme.getDarkColors
import ireader.core.ui.theme.getLightColors
import ireader.core.ui.theme.isLight
import ireader.core.ui.theme.themes
import ireader.core.ui.viewmodel.BaseViewModel
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class AppearanceViewModel(
    val uiPreferences: UiPreferences,
    val themeRepository: ThemeRepository
) : BaseViewModel() {

    private val _state = mutableStateOf(MainScreenState())
    val state = _state

    val vmThemes = themes
    var themeEditMode by mutableStateOf(false)
    var isSavable by mutableStateOf(false)

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
    fun getCustomizedColors(): CustomizableAppColorsPreferenceState {
        return if (MaterialTheme.colorScheme.isLight()) lightColors else darkColors
    }

    fun getThemes(id: Long,isLight:Boolean): Theme? {
        val themes = vmThemes.firstOrNull { it.id == id }
        val primary = if (!isLight) {
            darkColors.primary
        } else {
            lightColors.primary
        }
        val secondary = if (!isLight) {
            darkColors.secondary
        } else {
            lightColors.secondary
        }
        val bars = if (!isLight) {
            darkColors.bars
        } else {
            lightColors.bars
        }
        return themes?.copy(
            id = 0,
            materialColors = themes.materialColors.copy(
                primary = primary.value,
                secondary = secondary.value
            ),
            extraColors = ExtraColors(bars = bars.value),
            isDark = !isLight
        )
    }
}

data class MainScreenState(
    val darkMode: Boolean = true,
)
