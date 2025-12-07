package ireader.domain.services.preferences

/**
 * Sealed class representing one-time events emitted by the ReaderPreferencesController.
 * These events are used for UI feedback and should be consumed once.
 */
sealed class PreferenceEvent {
    /**
     * An error occurred during a preference operation.
     */
    data class Error(val error: PreferenceError) : PreferenceEvent()
    
    /**
     * A preference was successfully saved to the preference store.
     */
    data class PreferenceSaved(val key: String) : PreferenceEvent()
    
    /**
     * All preferences were successfully loaded from the preference store.
     */
    object PreferencesLoaded : PreferenceEvent()
}
