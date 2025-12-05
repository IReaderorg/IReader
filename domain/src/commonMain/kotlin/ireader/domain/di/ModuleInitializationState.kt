package ireader.domain.di

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Tracks the initialization state of Koin modules.
 * Screens that depend on background-loaded modules should check this state
 * and show a loading indicator until modules are ready.
 */
object ModuleInitializationState {
    
    private val _isFullyInitialized = MutableStateFlow(false)
    
    /**
     * Flow that emits true when all modules (including background modules) are loaded.
     */
    val isFullyInitialized: StateFlow<Boolean> = _isFullyInitialized.asStateFlow()
    
    /**
     * Check if all modules are initialized (non-suspending).
     */
    val isReady: Boolean get() = _isFullyInitialized.value
    
    /**
     * Mark modules as fully initialized.
     * Called after background modules are loaded.
     */
    fun markFullyInitialized() {
        _isFullyInitialized.value = true
    }
    
    /**
     * Reset state (for testing or app restart scenarios).
     */
    fun reset() {
        _isFullyInitialized.value = false
    }
}
