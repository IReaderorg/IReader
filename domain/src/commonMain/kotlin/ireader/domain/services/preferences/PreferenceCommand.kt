package ireader.domain.services.preferences

import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.preferences.models.FontType
import ireader.domain.preferences.prefs.ReadingMode

/**
 * Sealed class representing all commands that can be dispatched to the ReaderPreferencesController.
 * All preference operations are expressed as commands for predictable state management.
 */
sealed class PreferenceCommand {
    // ========== Display Preferences ==========
    
    /**
     * Set the font size for reader text.
     */
    data class SetFontSize(val size: Int) : PreferenceCommand()
    
    /**
     * Set the line height for reader text.
     */
    data class SetLineHeight(val height: Int) : PreferenceCommand()
    
    /**
     * Set the text alignment for reader text.
     */
    data class SetTextAlignment(val alignment: PreferenceValues.PreferenceTextAlignment) : PreferenceCommand()
    
    /**
     * Set the font type for reader text.
     */
    data class SetFont(val fontType: FontType?) : PreferenceCommand()
    
    // ========== Color Preferences ==========
    
    /**
     * Set the background color for the reader.
     */
    data class SetBackgroundColor(val color: Long) : PreferenceCommand()
    
    /**
     * Set the text color for the reader.
     */
    data class SetTextColor(val color: Long) : PreferenceCommand()
    
    /**
     * Set the reader theme index.
     */
    data class SetReaderTheme(val themeIndex: Int) : PreferenceCommand()
    
    // ========== Layout Preferences ==========
    
    /**
     * Set all margins at once.
     */
    data class SetMargins(
        val top: Int,
        val bottom: Int,
        val left: Int,
        val right: Int
    ) : PreferenceCommand()
    
    /**
     * Set the paragraph spacing.
     */
    data class SetParagraphSpacing(val spacing: Int) : PreferenceCommand()
    
    /**
     * Set the paragraph indent.
     */
    data class SetParagraphIndent(val indent: Int) : PreferenceCommand()
    
    // ========== Screen Preferences ==========
    
    /**
     * Set the screen brightness.
     */
    data class SetBrightness(val brightness: Float) : PreferenceCommand()
    
    /**
     * Set whether the screen should always stay on.
     */
    data class SetScreenAlwaysOn(val enabled: Boolean) : PreferenceCommand()
    
    /**
     * Set whether immersive mode is enabled.
     */
    data class SetImmersiveMode(val enabled: Boolean) : PreferenceCommand()
    
    // ========== Reading Mode Preferences ==========
    
    /**
     * Set the reading mode (Page or Continuous).
     */
    data class SetReadingMode(val mode: ReadingMode) : PreferenceCommand()
    
    /**
     * Set whether vertical scrolling is enabled.
     */
    data class SetScrollMode(val vertical: Boolean) : PreferenceCommand()
    
    // ========== Batch Operations ==========
    
    /**
     * Apply multiple preference changes atomically.
     */
    data class BatchUpdate(val updates: List<PreferenceCommand>) : PreferenceCommand()
    
    // ========== Lifecycle ==========
    
    /**
     * Reload all preferences from the preference store.
     */
    object Reload : PreferenceCommand()
}
