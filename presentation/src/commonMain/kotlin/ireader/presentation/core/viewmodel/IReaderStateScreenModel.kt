package ireader.presentation.core.viewmodel

import ireader.core.log.IReaderLog
import ireader.core.util.IO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Base screen model for IReader with state management.
 * Provides utility methods for state management and coroutine handling.
 * This is a multiplatform-compatible implementation without AndroidX dependencies.
 * 
 * @param T The state type managed by this screen model
 * @param initialState The initial state value
 */
abstract class IReaderStateScreenModel<T>(
    initialState: T
) {
    
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<T> = _state.asStateFlow()
    
    /**
     * Coroutine scope for this screen model
     */
    protected val screenModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    /**
     * Update the state using a transform function
     */
    fun updateState(transform: (T) -> T) {
        _state.update(transform)
    }
    
    /**
     * Launch a coroutine in IO context with proper error handling
     */
    protected fun launchIO(block: suspend CoroutineScope.() -> Unit) {
        screenModelScope.launch(Dispatchers.IO) {
            try {
                block()
            } catch (e: Exception) {
                IReaderLog.error("Error in launchIO", e, this@IReaderStateScreenModel::class.simpleName ?: "IReaderStateScreenModel")
                handleError(e)
            }
        }
    }
    
    /**
     * Launch a coroutine in Main context with proper error handling
     */
    protected fun launchMain(block: suspend CoroutineScope.() -> Unit) {
        screenModelScope.launch(Dispatchers.Main) {
            try {
                block()
            } catch (e: Exception) {
                IReaderLog.error("Error in launchMain", e, this@IReaderStateScreenModel::class.simpleName ?: "IReaderStateScreenModel")
                handleError(e)
            }
        }
    }
    
    /**
     * Launch a coroutine in Default context with proper error handling
     */
    protected fun launchDefault(block: suspend CoroutineScope.() -> Unit) {
        screenModelScope.launch(Dispatchers.Default) {
            try {
                block()
            } catch (e: Exception) {
                IReaderLog.error("Error in launchDefault", e, this@IReaderStateScreenModel::class.simpleName ?: "IReaderStateScreenModel")
                handleError(e)
            }
        }
    }
    
    /**
     * Handle errors that occur in coroutines.
     * Override this method to provide custom error handling.
     */
    protected open fun handleError(error: Throwable) {
        // Default implementation - subclasses can override for custom error handling
        IReaderLog.error("Unhandled error in ${this::class.simpleName}", error)
    }
    
    /**
     * Log debug messages with the screen model class name as tag
     */
    protected fun logDebug(message: String) {
        IReaderLog.debug(message, this::class.simpleName ?: "IReaderStateScreenModel")
    }
    
    /**
     * Log info messages with the screen model class name as tag
     */
    protected fun logInfo(message: String) {
        IReaderLog.info(message, this::class.simpleName ?: "IReaderStateScreenModel")
    }
    
    /**
     * Log warning messages with the screen model class name as tag
     */
    protected fun logWarn(message: String, throwable: Throwable? = null) {
        IReaderLog.warn(message, throwable, this::class.simpleName ?: "IReaderStateScreenModel")
    }
    
    /**
     * Log error messages with the screen model class name as tag
     */
    protected fun logError(message: String, throwable: Throwable? = null) {
        IReaderLog.error(message, throwable, this::class.simpleName ?: "IReaderStateScreenModel")
    }
    
    /**
     * Called when the screen model is no longer needed.
     * Cancels all coroutines in the scope.
     */
    open fun onDispose() {
        screenModelScope.cancel()
    }
}