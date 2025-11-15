package ireader.presentation.ui.component.components

import ireader.domain.preferences.prefs.UiPreferences

/**
 * Utility object for managing haptic feedback throughout the application.
 * 
 * This object provides a centralized way to perform haptic feedback that respects
 * the user's preference setting for disabling haptic feedback.
 * 
 * Platform-specific implementations should be provided for actual haptic feedback.
 * This common interface allows checking the preference setting across all platforms.
 * 
 * Usage:
 * ```kotlin
 * if (HapticFeedback.isEnabled(uiPreferences)) {
 *     // Perform platform-specific haptic feedback
 * }
 * ```
 */
object HapticFeedback {
    
    /**
     * Checks if haptic feedback is enabled in preferences.
     * 
     * @param uiPreferences The UI preferences to check the setting
     * @return true if haptic feedback is enabled, false otherwise
     */
    fun isEnabled(uiPreferences: UiPreferences): Boolean {
        return !uiPreferences.disableHapticFeedback().get()
    }
    
    /**
     * Suspending version of isEnabled for use in coroutines.
     * 
     * @param uiPreferences The UI preferences to check the setting
     * @return true if haptic feedback is enabled, false otherwise
     */
    suspend fun isEnabledAsync(uiPreferences: UiPreferences): Boolean {
        return !uiPreferences.disableHapticFeedback().get()
    }
}

/**
 * Platform-specific haptic feedback should be implemented in platform source sets.
 * 
 * Example for Android (in androidMain):
 * ```kotlin
 * fun View.performHapticFeedbackIfEnabled(
 *     uiPreferences: UiPreferences,
 *     feedbackType: Int = HapticFeedbackConstants.CONTEXT_CLICK
 * ) {
 *     if (HapticFeedback.isEnabled(uiPreferences)) {
 *         performHapticFeedback(feedbackType)
 *     }
 * }
 * ```
 * 
 * Example for Desktop (in desktopMain):
 * ```kotlin
 * // Desktop typically doesn't have haptic feedback, so this would be a no-op
 * fun performHapticFeedbackIfEnabled(uiPreferences: UiPreferences) {
 *     // No-op on desktop
 * }
 * ```
 */
