package ireader.domain.services.preferences

import androidx.compose.runtime.Immutable
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.preferences.models.FontType
import ireader.domain.preferences.prefs.ReadingMode

/**
 * Immutable data class representing the complete state of reader preferences.
 * This is the single source of truth for all reader preference data across screens.
 */
@Immutable
data class PreferenceState(
    // ========== Display Preferences ==========
    
    /**
     * Font size in sp.
     */
    val fontSize: Int = 18,
    
    /**
     * Line height in sp.
     */
    val lineHeight: Int = 25,
    
    /**
     * Text alignment setting.
     */
    val textAlignment: PreferenceValues.PreferenceTextAlignment = PreferenceValues.PreferenceTextAlignment.Left,
    
    /**
     * Selected font type, or null for system default.
     */
    val font: FontType? = null,
    
    /**
     * Selected font ID string.
     */
    val selectedFontId: String = "",
    
    // ========== Color Preferences ==========
    
    /**
     * Background color as ARGB long value.
     */
    val backgroundColor: Long = 0xFFFFFFFF,
    
    /**
     * Text color as ARGB long value.
     */
    val textColor: Long = 0xFF000000,
    
    /**
     * Reader theme index.
     */
    val readerTheme: Int = 1,
    
    // ========== Layout Preferences ==========
    
    /**
     * Top margin in dp.
     */
    val topMargin: Int = 0,
    
    /**
     * Bottom margin in dp.
     */
    val bottomMargin: Int = 0,
    
    /**
     * Left margin in dp.
     */
    val leftMargin: Int = 0,
    
    /**
     * Right margin in dp.
     */
    val rightMargin: Int = 0,
    
    /**
     * Paragraph spacing in dp.
     */
    val paragraphSpacing: Int = 2,
    
    /**
     * Paragraph indent in dp.
     */
    val paragraphIndent: Int = 8,
    
    // ========== Screen Preferences ==========
    
    /**
     * Screen brightness (0.0 to 1.0).
     */
    val brightness: Float = 0.5f,
    
    /**
     * Whether auto brightness is enabled.
     */
    val autoBrightness: Boolean = true,
    
    /**
     * Whether the screen should always stay on.
     */
    val screenAlwaysOn: Boolean = false,
    
    /**
     * Whether immersive mode is enabled.
     */
    val immersiveMode: Boolean = false,
    
    // ========== Reading Mode Preferences ==========
    
    /**
     * Current reading mode (Page or Continuous).
     */
    val readingMode: ReadingMode = ReadingMode.Page,
    
    /**
     * Whether vertical scrolling is enabled.
     */
    val verticalScrolling: Boolean = true,
    
    // ========== Loading State ==========
    
    /**
     * Whether preferences are currently being loaded.
     */
    val isLoading: Boolean = false,
    
    /**
     * Current error, or null if no error.
     */
    val error: PreferenceError? = null
) {
    // ========== Computed Properties ==========
    
    /**
     * Whether custom brightness is being used (not auto).
     */
    val isCustomBrightness: Boolean
        get() = !autoBrightness
    
    /**
     * Whether there is an active error.
     */
    val hasError: Boolean
        get() = error != null
}
