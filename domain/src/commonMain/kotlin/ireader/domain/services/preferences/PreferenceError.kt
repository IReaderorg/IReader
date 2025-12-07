package ireader.domain.services.preferences

/**
 * Sealed class representing all possible errors in preference operations.
 * Used for type-safe error handling across the ReaderPreferencesController.
 */
sealed class PreferenceError {
    /**
     * Failed to save a preference to the preference store.
     */
    data class SaveFailed(val key: String, val message: String) : PreferenceError()
    
    /**
     * Failed to load preferences from the preference store.
     */
    data class LoadFailed(val message: String) : PreferenceError()
    
    /**
     * Returns a user-friendly error message.
     */
    fun toUserMessage(): String = when (this) {
        is SaveFailed -> "Failed to save preference '$key': $message"
        is LoadFailed -> "Failed to load preferences: $message"
    }
}
