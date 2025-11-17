package ireader.presentation.core.viewmodel

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import ireader.presentation.core.log.IReaderLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Base StateScreenModel for IReader following Mihon's pattern.
 * Provides utility methods for state management and coroutine handling.
 * 
 * @param T The state type managed by this screen model
 * @param initialState The initial state value
 */
abstract class IReaderStateScreenModel<T>(
    initialState: T
) : StateScreenModel<T>(initialState) {
    
    /**
     * Update the state using a transform function
     */
    protected fun updateState(transform: (T) -> T) {
        mutableState.update(transform)
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
}