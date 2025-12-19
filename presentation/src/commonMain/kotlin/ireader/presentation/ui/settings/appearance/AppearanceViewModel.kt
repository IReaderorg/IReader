package ireader.presentation.ui.settings.appearance

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.domain.data.repository.ThemeRepository
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.models.theme.ExtraColors
import ireader.domain.models.theme.Theme
import ireader.domain.preferences.prefs.UiPreferences
import ireader.presentation.ui.core.theme.*
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import ireader.presentation.core.toDomainColor



class AppearanceViewModel(
    val uiPreferences: UiPreferences,
    val themeRepository: ThemeRepository,
    val pluginManager: ireader.domain.plugins.PluginManager
) : BaseViewModel() {

    private val _state = mutableStateOf(MainScreenState())
    val state = _state

    val vmThemes = themes
    var themeEditMode by mutableStateOf(false)
    var isSavable by mutableStateOf(false)
    
    // Theme import/export state
    var showImportDialog by mutableStateOf(false)
    var showExportDialog by mutableStateOf(false)
    var importExportResult by mutableStateOf<String?>(null)

    val themeMode = uiPreferences.themeMode().asState()
    val colorTheme = uiPreferences.colorTheme().asState()
    val dynamicColorMode = uiPreferences.dynamicColorMode().asState()
    val useTrueBlack = uiPreferences.useTrueBlack().asState()
    val appUiFont = uiPreferences.appUiFont().asState()
    val dateFormat = uiPreferences.dateFormat().asState()
    val relativeTime = uiPreferences.relativeTime().asState()
    val lightColors = uiPreferences.getLightColors().asState(scope)
    val darkColors = uiPreferences.getDarkColors().asState(scope)
    
    // Novel Info UI Preferences
    val hideNovelBackdrop = uiPreferences.hideNovelBackdrop().asState()
    val useFabInNovelInfo = uiPreferences.useFabInNovelInfo().asState()

    val dateFormats =
        arrayOf("", "MM/dd/yy", "dd/MM/yy", "yyyy-MM-dd", "dd MMM yyyy", "MMM dd, yyyy")
    val relativeTimes = arrayOf(
        PreferenceValues.RelativeTime.Off,
        PreferenceValues.RelativeTime.Day,
        PreferenceValues.RelativeTime.Week
    )
    
    val availableFonts = mapOf(
        "default" to "Default (Roboto)",
        "sans_serif" to "Sans Serif",
        "serif" to "Serif",
        "monospace" to "Monospace",
        "cursive" to "Cursive"
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
    
    /**
     * Export current custom theme to JSON string
     */
    fun exportCurrentTheme(): String? {
        val currentTheme = vmThemes.firstOrNull { it.id == colorTheme.value }
        return if (currentTheme != null && currentTheme.id > 0) {
            ThemeImportExport.exportTheme(currentTheme.toCustomTheme())
        } else {
            null
        }
    }
    
    /**
     * Export all custom themes to JSON string
     */
    fun exportAllCustomThemes(): String {
        val customThemes = vmThemes.filter { it.id > 0 }.map { it.toCustomTheme() }
        return ThemeImportExport.exportThemes(customThemes)
    }
    
    /**
     * Import theme from JSON string
     */
    suspend fun importTheme(jsonString: String): Result<Long> {
        return try {
            val result = ThemeImportExport.importTheme(jsonString)
            if (result.isSuccess) {
                val theme = result.getOrThrow()
                val themeId = themeRepository.insert(theme)
                Result.success(themeId)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Import multiple themes from JSON string
     */
    suspend fun importThemes(jsonString: String): Result<Int> {
        return try {
            val result = ThemeImportExport.importThemes(jsonString)
            if (result.isSuccess) {
                val themes = result.getOrThrow()
                themeRepository.insert(themes)
                Result.success(themes.size)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Create a backup of all custom themes
     */
    fun backupCustomThemes(): String {
        return exportAllCustomThemes()
    }
    
    /**
     * Restore themes from backup
     */
    suspend fun restoreThemesFromBackup(backupJson: String): Result<Int> {
        return importThemes(backupJson)
    }
}

data class MainScreenState(
    val darkMode: Boolean = true,
)
