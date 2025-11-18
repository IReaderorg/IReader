package ireader.presentation.ui.settings.reader

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.core.prefs.PreferenceStore
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for the enhanced reader settings screen.
 * Manages comprehensive reader preferences following Mihon's 50+ customization options.
 */
class SettingsReaderViewModel(
    private val preferenceStore: PreferenceStore
) : BaseViewModel() {
    
    // Reading mode preferences
    val readingMode: StateFlow<String> = preferenceStore.getString("reader_mode", "pager").stateIn(scope)
    val pageTransitions: StateFlow<String> = preferenceStore.getString("page_transitions", "slide").stateIn(scope)
    
    // Display preferences
    val doubleTapZoom: StateFlow<Boolean> = preferenceStore.getBoolean("double_tap_zoom", true).stateIn(scope)
    val showPageNumber: StateFlow<Boolean> = preferenceStore.getBoolean("show_page_number", true).stateIn(scope)
    val fullscreen: StateFlow<Boolean> = preferenceStore.getBoolean("reader_fullscreen", true).stateIn(scope)
    val keepScreenOn: StateFlow<Boolean> = preferenceStore.getBoolean("keep_screen_on", true).stateIn(scope)
    val showStatusBar: StateFlow<Boolean> = preferenceStore.getBoolean("show_status_bar", false).stateIn(scope)
    val showNavigationBar: StateFlow<Boolean> = preferenceStore.getBoolean("show_navigation_bar", false).stateIn(scope)
    
    // Orientation & Layout preferences
    val cutoutShort: StateFlow<Boolean> = preferenceStore.getBoolean("cutout_short", true).stateIn(scope)
    val landscapeZoom: StateFlow<Boolean> = preferenceStore.getBoolean("landscape_zoom", true).stateIn(scope)
    
    // Navigation preferences
    val navigationMode: StateFlow<String> = preferenceStore.getString("navigation_mode", "tap").stateIn(scope)
    val volumeKeysEnabled: StateFlow<Boolean> = preferenceStore.getBoolean("volume_keys_enabled", false).stateIn(scope)
    val invertTapping: StateFlow<Boolean> = preferenceStore.getBoolean("invert_tapping", false).stateIn(scope)
    
    // Visual effects preferences
    val flashOnPageChange: StateFlow<Boolean> = preferenceStore.getBoolean("flash_on_page_change", false).stateIn(scope)
    
    // Color filter preferences
    val customBrightness: StateFlow<Boolean> = preferenceStore.getBoolean("custom_brightness", false).stateIn(scope)
    val customBrightnessValue: StateFlow<Int> = preferenceStore.getInt("custom_brightness_value", 0).stateIn(scope)
    val colorFilter: StateFlow<Boolean> = preferenceStore.getBoolean("color_filter", false).stateIn(scope)
    val colorFilterValue: StateFlow<Int> = preferenceStore.getInt("color_filter_value", 0).stateIn(scope)
    val grayscale: StateFlow<Boolean> = preferenceStore.getBoolean("grayscale", false).stateIn(scope)
    val invertedColors: StateFlow<Boolean> = preferenceStore.getBoolean("inverted_colors", false).stateIn(scope)
    
    // Image scaling preferences
    val scaleType: StateFlow<String> = preferenceStore.getString("scale_type", "fit_screen").stateIn(scope)
    val zoomStartPosition: StateFlow<String> = preferenceStore.getString("zoom_start_position", "automatic").stateIn(scope)
    val cropBorders: StateFlow<Boolean> = preferenceStore.getBoolean("crop_borders", false).stateIn(scope)
    val sidePadding: StateFlow<String> = preferenceStore.getString("side_padding", "none").stateIn(scope)
    
    // Dialog states
    var showReadingModeDialog by mutableStateOf(false)
        private set
    var showPageTransitionsDialog by mutableStateOf(false)
        private set
    var showNavigationModeDialog by mutableStateOf(false)
        private set
    var showScaleTypeDialog by mutableStateOf(false)
        private set
    var showZoomStartDialog by mutableStateOf(false)
        private set
    var showSidePaddingDialog by mutableStateOf(false)
        private set
    
    // Reading mode functions
    fun showReadingModeDialog() {
        showReadingModeDialog = true
    }
    
    fun dismissReadingModeDialog() {
        showReadingModeDialog = false
    }
    
    fun setReadingMode(mode: String) {
        preferenceStore.getString("reader_mode", "pager").set(mode)
    }
    
    // Page transitions functions
    fun showPageTransitionsDialog() {
        showPageTransitionsDialog = true
    }
    
    fun dismissPageTransitionsDialog() {
        showPageTransitionsDialog = false
    }
    
    fun setPageTransitions(transitions: String) {
        preferenceStore.getString("page_transitions", "slide").set(transitions)
    }
    
    // Display functions
    fun setDoubleTapZoom(enabled: Boolean) {
        preferenceStore.getBoolean("double_tap_zoom", true).set(enabled)
    }
    
    fun setShowPageNumber(enabled: Boolean) {
        preferenceStore.getBoolean("show_page_number", true).set(enabled)
    }
    
    fun setFullscreen(enabled: Boolean) {
        preferenceStore.getBoolean("reader_fullscreen", true).set(enabled)
    }
    
    fun setKeepScreenOn(enabled: Boolean) {
        preferenceStore.getBoolean("keep_screen_on", true).set(enabled)
    }
    
    fun setShowStatusBar(enabled: Boolean) {
        preferenceStore.getBoolean("show_status_bar", false).set(enabled)
    }
    
    fun setShowNavigationBar(enabled: Boolean) {
        preferenceStore.getBoolean("show_navigation_bar", false).set(enabled)
    }
    
    // Orientation & Layout functions
    fun setCutoutShort(enabled: Boolean) {
        preferenceStore.getBoolean("cutout_short", true).set(enabled)
    }
    
    fun setLandscapeZoom(enabled: Boolean) {
        preferenceStore.getBoolean("landscape_zoom", true).set(enabled)
    }
    
    // Navigation functions
    fun showNavigationModeDialog() {
        showNavigationModeDialog = true
    }
    
    fun dismissNavigationModeDialog() {
        showNavigationModeDialog = false
    }
    
    fun setNavigationMode(mode: String) {
        preferenceStore.getString("navigation_mode", "tap").set(mode)
    }
    
    fun setVolumeKeysEnabled(enabled: Boolean) {
        preferenceStore.getBoolean("volume_keys_enabled", false).set(enabled)
    }
    
    fun setInvertTapping(enabled: Boolean) {
        preferenceStore.getBoolean("invert_tapping", false).set(enabled)
    }
    
    // Visual effects functions
    fun setFlashOnPageChange(enabled: Boolean) {
        preferenceStore.getBoolean("flash_on_page_change", false).set(enabled)
    }
    
    // Color filter functions
    fun setCustomBrightness(enabled: Boolean) {
        preferenceStore.getBoolean("custom_brightness", false).set(enabled)
    }
    
    fun setCustomBrightnessValue(value: Int) {
        preferenceStore.getInt("custom_brightness_value", 0).set(value)
    }
    
    fun setColorFilter(enabled: Boolean) {
        preferenceStore.getBoolean("color_filter", false).set(enabled)
    }
    
    fun setColorFilterValue(value: Int) {
        preferenceStore.getInt("color_filter_value", 0).set(value)
    }
    
    fun setGrayscale(enabled: Boolean) {
        preferenceStore.getBoolean("grayscale", false).set(enabled)
    }
    
    fun setInvertedColors(enabled: Boolean) {
        preferenceStore.getBoolean("inverted_colors", false).set(enabled)
    }
    
    // Image scaling functions
    fun showScaleTypeDialog() {
        showScaleTypeDialog = true
    }
    
    fun dismissScaleTypeDialog() {
        showScaleTypeDialog = false
    }
    
    fun setScaleType(type: String) {
        preferenceStore.getString("scale_type", "fit_screen").set(type)
    }
    
    fun showZoomStartDialog() {
        showZoomStartDialog = true
    }
    
    fun dismissZoomStartDialog() {
        showZoomStartDialog = false
    }
    
    fun setZoomStartPosition(position: String) {
        preferenceStore.getString("zoom_start_position", "automatic").set(position)
    }
    
    fun setCropBorders(enabled: Boolean) {
        preferenceStore.getBoolean("crop_borders", false).set(enabled)
    }
    
    fun showSidePaddingDialog() {
        showSidePaddingDialog = true
    }
    
    fun dismissSidePaddingDialog() {
        showSidePaddingDialog = false
    }
    
    fun setSidePadding(padding: String) {
        preferenceStore.getString("side_padding", "none").set(padding)
    }
    
    // Navigation functions
    fun navigateToColorFilters() {
        // TODO: Implement navigation to color filters screen
    }
    
    fun navigateToImageScaling() {
        // TODO: Implement navigation to image scaling screen
    }
    
    fun navigateToTapZones() {
        // TODO: Implement navigation to tap zones customization screen
    }
}