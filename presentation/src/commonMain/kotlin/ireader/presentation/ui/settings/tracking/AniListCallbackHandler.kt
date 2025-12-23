package ireader.presentation.ui.settings.tracking

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Simple handler for AniList OAuth callbacks.
 * This is used to pass the callback URL from the deeplink handler to the ViewModel.
 */
object AniListCallbackHandler {
    private val _pendingCallbackFlow = MutableStateFlow<String?>(null)
    val pendingCallbackFlow: StateFlow<String?> = _pendingCallbackFlow.asStateFlow()
    
    /**
     * Set the pending callback URL from the deeplink handler.
     */
    var pendingCallback: String?
        get() = _pendingCallbackFlow.value
        set(value) {
            _pendingCallbackFlow.value = value
        }
    
    /**
     * Clear the pending callback after it has been processed.
     */
    fun clearPendingCallback() {
        _pendingCallbackFlow.value = null
    }
}
