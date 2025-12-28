package ireader.presentation.ui.settings.appearance

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.preferences.prefs.UiPreferences
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for the enhanced appearance settings screen.
 * Manages theme preferences, dynamic colors, typography, and display options.
 */
class SettingsAppearanceViewModel(
    private val uiPreferences: UiPreferences
) : BaseViewModel() {
    
    // Theme preferences
    val themeMode: StateFlow<PreferenceValues.ThemeMode> = uiPreferences.themeMode().stateIn(scope)
    val dynamicColors: StateFlow<Boolean> = uiPreferences.dynamicColorMode().stateIn(scope)
    val amoledMode: StateFlow<Boolean> = uiPreferences.useTrueBlack().stateIn(scope)
    
    // Typography preferences
    val appFont: StateFlow<String> = uiPreferences.appUiFont().stateIn(scope)
    
    // Display preferences
    val hideNovelBackdrop: StateFlow<Boolean> = uiPreferences.hideNovelBackdrop().stateIn(scope)
    val useFabInNovelInfo: StateFlow<Boolean> = uiPreferences.useFabInNovelInfo().stateIn(scope)
    
    // Date & Time preferences
    val relativeTime: StateFlow<PreferenceValues.RelativeTime> = uiPreferences.relativeTime().stateIn(scope)
    
    // Dialog states
    var showThemeModeDialog by mutableStateOf(false)
        private set
    var showFontDialog by mutableStateOf(false)
        private set
    var showRelativeTimeDialog by mutableStateOf(false)
        private set
    
    // Available fonts list
    val availableFonts = listOf(
        "", // System default
        "Roboto",
        "Noto Sans",
        "Open Sans",
        "Source Sans Pro",
        "Lato",
        "Montserrat",
        "Poppins",
        "Inter"
    )
    
    // Theme mode functions
    fun showThemeModeDialog() {
        showThemeModeDialog = true
    }
    
    fun dismissThemeModeDialog() {
        showThemeModeDialog = false
    }
    
    fun setThemeMode(mode: PreferenceValues.ThemeMode) {
        uiPreferences.themeMode().set(mode)
    }
    
    // Dynamic colors functions
    fun setDynamicColors(enabled: Boolean) {
        uiPreferences.dynamicColorMode().set(enabled)
    }
    
    // AMOLED mode functions
    fun setAmoledMode(enabled: Boolean) {
        uiPreferences.useTrueBlack().set(enabled)
    }
    
    // Font functions
    fun showFontDialog() {
        showFontDialog = true
    }
    
    fun dismissFontDialog() {
        showFontDialog = false
    }
    
    fun setAppFont(font: String) {
        uiPreferences.appUiFont().set(font)
    }
    
    // Display functions
    fun setHideNovelBackdrop(enabled: Boolean) {
        uiPreferences.hideNovelBackdrop().set(enabled)
    }
    
    fun setUseFabInNovelInfo(enabled: Boolean) {
        uiPreferences.useFabInNovelInfo().set(enabled)
    }
    
    // Relative time functions
    fun showRelativeTimeDialog() {
        showRelativeTimeDialog = true
    }
    
    fun dismissRelativeTimeDialog() {
        showRelativeTimeDialog = false
    }
    
    fun setRelativeTime(time: PreferenceValues.RelativeTime) {
        uiPreferences.relativeTime().set(time)
    }
    
    // Navigation functions
    private val _navigationEvent = MutableStateFlow<AppearanceNavigationEvent?>(null)
    val navigationEvent: StateFlow<AppearanceNavigationEvent?> = _navigationEvent.asStateFlow()
    
    fun navigateToColorCustomization() {
        _navigationEvent.value = AppearanceNavigationEvent.ColorCustomization
    }
    
    fun navigateToThemeManagement() {
        _navigationEvent.value = AppearanceNavigationEvent.ThemeManagement
    }
    
    fun clearNavigationEvent() {
        _navigationEvent.value = null
    }
}

/**
 * Navigation events for appearance settings
 */
sealed class AppearanceNavigationEvent {
    object ColorCustomization : AppearanceNavigationEvent()
    object ThemeManagement : AppearanceNavigationEvent()
}
