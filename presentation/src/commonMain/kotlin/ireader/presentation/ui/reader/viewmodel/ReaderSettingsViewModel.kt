package ireader.presentation.ui.reader.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.preferences.prefs.PlatformUiPreferences
import ireader.domain.services.platform.SystemInteractionService
import ireader.domain.services.common.ServiceResult
import ireader.domain.usecases.preferences.reader_preferences.ReaderPrefUseCases
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import ireader.presentation.core.toComposeColor
import ireader.presentation.core.toDomainColor
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * ViewModel responsible for reader settings and preferences
 * 
 * Handles:
 * - Brightness control
 * - Font settings
 * - Color themes
 * - Layout preferences
 * - Screen settings (secure screen, keep awake)
 */
class ReaderSettingsViewModel(
    private val readerPreferences: ReaderPreferences,
    private val androidUiPreferences: AppPreferences,
    private val platformUiPreferences: PlatformUiPreferences,
    private val readerUseCases: ReaderPrefUseCases,
    private val systemInteractionService: SystemInteractionService,
    private val fontUseCase: ireader.domain.usecases.fonts.FontUseCase,
) : BaseViewModel() {
    
    // Brightness settings - debounced for smooth slider interaction
    val brightness = readerPreferences.brightness().asStateDebounced()
    val autoBrightnessMode = readerPreferences.autoBrightness().asState()
    var showBrightnessControl by mutableStateOf(false)
    
    // Font settings - debounced for sliders
    val fontSize = readerPreferences.fontSize().asStateDebounced()
    val font = platformUiPreferences.font()?.asState()
    val lineHeight = readerPreferences.lineHeight().asStateDebounced()
    val betweenLetterSpaces = readerPreferences.betweenLetterSpaces().asStateDebounced()
    val textWeight = readerPreferences.textWeight().asStateDebounced()
    var showFontSizeAdjuster by mutableStateOf(false)
    var showFontPicker by mutableStateOf(false)
    
    // Font list (Google Fonts)
    var fonts by mutableStateOf<List<String>>(emptyList())
        private set
    var fontsLoading by mutableStateOf(false)
        private set
    
    init {
        loadFonts()
    }
    
    private fun loadFonts() {
        scope.launch {
            fontsLoading = true
            try {
                val remoteFonts = fontUseCase.getRemoteFonts()
                val popularFonts = listOf("Roboto", "Open Sans", "Lato", "Montserrat", "Poppins", "Raleway", "Merriweather", "PT Serif", "Playfair Display", "Noto Sans", "Ubuntu", "Nunito", "Source Sans Pro")
                fonts = (popularFonts + remoteFonts).distinct().sorted()
            } catch (e: Exception) {
                ireader.core.log.Log.error("Failed to load fonts", e)
                fonts = listOf("Roboto", "Open Sans", "Lato", "Montserrat", "Poppins", "Raleway", "Merriweather", "PT Serif", "Playfair Display", "Noto Sans", "Ubuntu", "Nunito", "Source Sans Pro", "Crimson Text", "Libre Baskerville", "Lora")
            } finally {
                fontsLoading = false
            }
        }
    }
    
    // Color settings
    val backgroundColor = androidUiPreferences.backgroundColorReader().asState()
    val textColor = androidUiPreferences.textColorReader().asState()
    val readerTheme = androidUiPreferences.readerTheme().asState()
    
    // Layout settings - debounced for slider controls
    val textAlignment = readerPreferences.textAlign().asState()
    val paragraphsIndent = readerPreferences.paragraphIndent().asStateDebounced()
    val distanceBetweenParagraphs = readerPreferences.paragraphDistance().asStateDebounced()
    val topContentPadding = readerPreferences.topContentPadding().asStateDebounced()
    val bottomContentPadding = readerPreferences.bottomContentPadding().asStateDebounced()
    val topMargin = readerPreferences.topMargin().asStateDebounced()
    val leftMargin = readerPreferences.leftMargin().asStateDebounced()
    val rightMargin = readerPreferences.rightMargin().asStateDebounced()
    val bottomMargin = readerPreferences.bottomMargin().asStateDebounced()
    
    // Screen settings
    val screenAlwaysOn = readerPreferences.screenAlwaysOn().asState()
    val immersiveMode = readerPreferences.immersiveMode().asState()
    val orientation = androidUiPreferences.orientation().asState()
    
    // Scroll settings - debounced for slider controls
    val showScrollIndicator = readerPreferences.showScrollIndicator().asState()
    val scrollIndicatorWith = readerPreferences.scrollIndicatorWith().asStateDebounced()
    val scrollIndicatorPadding = readerPreferences.scrollIndicatorPadding().asStateDebounced()
    val scrollIndicatorAlignment = readerPreferences.scrollBarAlignment().asState()
    val isScrollIndicatorDraggable = readerPreferences.scrollbarMode().asState()
    val selectedScrollBarColor = androidUiPreferences.selectedScrollBarColor().asState()
    val unselectedScrollBarColor = androidUiPreferences.unselectedScrollBarColor().asState()
    
    // Reading mode settings
    val verticalScrolling = readerPreferences.scrollMode().asState()
    val selectableMode = readerPreferences.selectableText().asState()
    val bionicReadingMode = readerPreferences.bionicReading().asState()
    
    // Auto-scroll settings - debounced for slider controls
    val autoScrollOffset = readerPreferences.autoScrollOffset().asStateDebounced()
    val autoScrollInterval = readerPreferences.autoScrollInterval().asStateDebounced()
    var autoScrollMode by mutableStateOf(false)
    
    // WebView settings
    val webViewIntegration = readerPreferences.webViewIntegration().asState()
    val webViewBackgroundMode = readerPreferences.webViewBackgroundMode().asState()
    
    // ==================== Brightness Control ====================
    
    /**
     * Update brightness using platform service
     */
    fun updateBrightness(newBrightness: Float) {
        scope.launch {
            when (val result = systemInteractionService.setBrightness(newBrightness)) {
                is ServiceResult.Success -> {
                    readerUseCases.brightnessStateUseCase.saveBrightness(newBrightness)
                }
                is ServiceResult.Error -> {
                    // Still save to preferences even if system update fails
                    readerUseCases.brightnessStateUseCase.saveBrightness(newBrightness)
                    showSnackBar(ireader.i18n.UiText.DynamicString("Failed to set brightness: ${result.message ?: "Unknown error"}"))
                }
                else -> {}
            }
        }
    }
    
    /**
     * Get current system brightness
     */
    suspend fun getCurrentBrightness(): Float {
        return systemInteractionService.getBrightness()
    }
    
    /**
     * Toggle brightness control panel
     */
    fun toggleBrightnessControl() {
        showBrightnessControl = !showBrightnessControl
    }
    
    // ==================== Font Settings ====================
    
    /**
     * Update font size
     */
    fun updateFontSize(size: Int) {
        scope.launch {
            readerPreferences.fontSize().set(size)
        }
    }
    
    /**
     * Increase font size
     */
    fun increaseFontSize() {
        val current = fontSize.value
        if (current < 40) {
            updateFontSize(current + 1)
        }
    }
    
    /**
     * Decrease font size
     */
    fun decreaseFontSize() {
        val current = fontSize.value
        if (current > 8) {
            updateFontSize(current - 1)
        }
    }
    
    /**
     * Toggle font size adjuster
     */
    fun toggleFontSizeAdjuster() {
        showFontSizeAdjuster = !showFontSizeAdjuster
    }
    
    /**
     * Toggle font picker
     */
    fun toggleFontPicker() {
        showFontPicker = !showFontPicker
    }
    
    // ==================== Color Settings ====================
    
    /**
     * Set reader background color
     */
    fun setReaderBackgroundColor(color: Color) {
        readerUseCases.backgroundColorUseCase.save(color.toDomainColor())
    }
    
    /**
     * Set reader text color
     */
    fun setReaderTextColor(color: Color) {
        readerUseCases.textColorUseCase.save(color.toDomainColor())
    }
    
    /**
     * Change background color theme
     */
    fun changeBackgroundColor(themeId: Long, readerColors: List<ireader.domain.preferences.models.ReaderColors>) {
        readerColors.firstOrNull { it.id == themeId }?.let { theme ->
            val bgColor = theme.backgroundColor
            val txtColor = theme.onTextColor
            setReaderBackgroundColor(bgColor.toComposeColor())
            setReaderTextColor(txtColor.toComposeColor())
        }
    }
    
    // ==================== Layout Settings ====================
    
    /**
     * Save text alignment
     */
    fun saveTextAlignment(textAlign: PreferenceValues.PreferenceTextAlignment) {
        readerUseCases.textAlignmentUseCase.save(textAlign)
    }
    
    /**
     * Update paragraph indent
     */
    fun updateParagraphIndent(indent: Int) {
        scope.launch {
            readerPreferences.paragraphIndent().set(indent)
        }
    }
    
    /**
     * Update paragraph distance
     */
    fun updateParagraphDistance(distance: Int) {
        scope.launch {
            readerPreferences.paragraphDistance().set(distance)
        }
    }
    
    // ==================== Screen Settings ====================
    
    /**
     * Enable or disable secure screen (prevents screenshots)
     */
    fun setSecureScreen(enabled: Boolean) {
        scope.launch {
            when (val result = systemInteractionService.setSecureScreen(enabled)) {
                is ServiceResult.Success -> {
                    // Success
                }
                is ServiceResult.Error -> {
                    showSnackBar(ireader.i18n.UiText.DynamicString("Failed to set secure screen: ${result.message ?: "Unknown error"}"))
                }
                else -> {}
            }
        }
    }
    
    /**
     * Keep screen on during reading
     */
    fun setKeepScreenOn(enabled: Boolean) {
        scope.launch {
            when (val result = systemInteractionService.setKeepScreenOn(enabled)) {
                is ServiceResult.Success -> {
                    readerPreferences.screenAlwaysOn().set(enabled)
                }
                is ServiceResult.Error -> {
                    showSnackBar(ireader.i18n.UiText.DynamicString("Failed to set keep screen on: ${result.message ?: "Unknown error"}"))
                }
                else -> {}
            }
        }
    }
    
    /**
     * Toggle immersive mode
     */
    fun toggleImmersiveMode(enabled: Boolean) {
        scope.launch {
            readerPreferences.immersiveMode().set(enabled)
        }
    }
    
    // ==================== Auto-scroll Settings ====================
    
    /**
     * Toggle auto-scroll
     */
    fun toggleAutoScroll() {
        autoScrollMode = !autoScrollMode
    }
    
    /**
     * Increase auto-scroll speed
     */
    fun increaseAutoScrollSpeed() {
        val currentSpeed = autoScrollOffset.value
        if (currentSpeed < 20) {
            val newSpeed = (currentSpeed + 1).coerceAtMost(100)
            scope.launch {
                readerPreferences.autoScrollOffset().set(newSpeed)
            }
        }
    }
    
    /**
     * Decrease auto-scroll speed
     */
    fun decreaseAutoScrollSpeed() {
        val currentSpeed = autoScrollOffset.value
        if (currentSpeed > 1) {
            val newSpeed = (currentSpeed - 1).coerceAtLeast(1)
            scope.launch {
                readerPreferences.autoScrollOffset().set(newSpeed)
            }
        }
    }
    
    // ==================== Utility ====================
    
    /**
     * Check if device is in landscape orientation
     */
    fun isLandscape(): Boolean {
        return systemInteractionService.isLandscape()
    }
    
    /**
     * Check if device is a tablet
     */
    fun isTablet(): Boolean {
        return systemInteractionService.isTablet()
    }
}
