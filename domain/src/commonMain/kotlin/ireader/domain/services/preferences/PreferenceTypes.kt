package ireader.domain.services.preferences

import androidx.compose.runtime.Immutable
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.preferences.models.FontType
import ireader.domain.preferences.prefs.ReadingMode

/**
 * Consolidated types for the ReaderPreferencesController.
 * Contains Command, State, Event, and Error definitions.
 */

// ============================================================================
// COMMANDS
// ============================================================================

/**
 * Sealed class representing all commands that can be dispatched to the ReaderPreferencesController.
 * All preference operations are expressed as commands for predictable state management.
 */
sealed class PreferenceCommand {
    // Display Preferences
    data class SetFontSize(val size: Int) : PreferenceCommand()
    data class SetLineHeight(val height: Int) : PreferenceCommand()
    data class SetTextAlignment(val alignment: PreferenceValues.PreferenceTextAlignment) : PreferenceCommand()
    data class SetFont(val fontType: FontType?) : PreferenceCommand()
    
    // Color Preferences
    data class SetBackgroundColor(val color: Long) : PreferenceCommand()
    data class SetTextColor(val color: Long) : PreferenceCommand()
    data class SetReaderTheme(val themeIndex: Int) : PreferenceCommand()
    
    // Layout Preferences
    data class SetMargins(val top: Int, val bottom: Int, val left: Int, val right: Int) : PreferenceCommand()
    data class SetParagraphSpacing(val spacing: Int) : PreferenceCommand()
    data class SetParagraphIndent(val indent: Int) : PreferenceCommand()
    
    // Screen Preferences
    data class SetBrightness(val brightness: Float) : PreferenceCommand()
    data class SetScreenAlwaysOn(val enabled: Boolean) : PreferenceCommand()
    data class SetImmersiveMode(val enabled: Boolean) : PreferenceCommand()
    
    // Reading Mode Preferences
    data class SetReadingMode(val mode: ReadingMode) : PreferenceCommand()
    data class SetScrollMode(val vertical: Boolean) : PreferenceCommand()
    
    // Batch Operations
    data class BatchUpdate(val updates: List<PreferenceCommand>) : PreferenceCommand()
    
    // Lifecycle
    object Reload : PreferenceCommand()
}

// ============================================================================
// STATE
// ============================================================================

/**
 * Immutable data class representing the complete state of reader preferences.
 * This is the single source of truth for all reader preference data across screens.
 */
@Immutable
data class PreferenceState(
    // Display Preferences
    val fontSize: Int = 18,
    val lineHeight: Int = 25,
    val textAlignment: PreferenceValues.PreferenceTextAlignment = PreferenceValues.PreferenceTextAlignment.Left,
    val font: FontType? = null,
    val selectedFontId: String = "",
    
    // Color Preferences
    val backgroundColor: Long = 0xFFFFFFFF,
    val textColor: Long = 0xFF000000,
    val readerTheme: Int = 1,
    
    // Layout Preferences
    val topMargin: Int = 0,
    val bottomMargin: Int = 0,
    val leftMargin: Int = 0,
    val rightMargin: Int = 0,
    val paragraphSpacing: Int = 2,
    val paragraphIndent: Int = 8,
    
    // Screen Preferences
    val brightness: Float = 0.5f,
    val autoBrightness: Boolean = true,
    val screenAlwaysOn: Boolean = false,
    val immersiveMode: Boolean = false,
    
    // Reading Mode Preferences
    val readingMode: ReadingMode = ReadingMode.Page,
    val verticalScrolling: Boolean = true,
    
    // Loading State
    val isLoading: Boolean = false,
    val error: PreferenceError? = null
) {
    // Computed Properties
    val isCustomBrightness: Boolean get() = !autoBrightness
    val hasError: Boolean get() = error != null
}

// ============================================================================
// EVENTS
// ============================================================================

/**
 * Sealed class representing one-time events emitted by the ReaderPreferencesController.
 * These events are used for UI feedback and should be consumed once.
 */
sealed class PreferenceEvent {
    /** An error occurred during a preference operation. */
    data class Error(val error: PreferenceError) : PreferenceEvent()
    /** A preference was successfully saved to the preference store. */
    data class PreferenceSaved(val key: String) : PreferenceEvent()
    /** All preferences were successfully loaded from the preference store. */
    object PreferencesLoaded : PreferenceEvent()
}

// ============================================================================
// ERRORS
// ============================================================================

/**
 * Sealed class representing all possible errors in preference operations.
 * Used for type-safe error handling across the ReaderPreferencesController.
 */
sealed class PreferenceError {
    /** Failed to save a preference to the preference store. */
    data class SaveFailed(val key: String, val message: String) : PreferenceError()
    /** Failed to load preferences from the preference store. */
    data class LoadFailed(val message: String) : PreferenceError()
    
    /** Returns a user-friendly error message. */
    fun toUserMessage(): String = when (this) {
        is SaveFailed -> "Failed to save preference '$key': $message"
        is LoadFailed -> "Failed to load preferences: $message"
    }
}
