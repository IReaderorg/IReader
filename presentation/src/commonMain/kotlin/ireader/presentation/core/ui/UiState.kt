package ireader.presentation.core.ui

/**
 * Consolidated UI state pattern to reduce duplication across ViewModels
 * 
 * This provides a consistent way to represent loading, success, and error states
 * throughout the application.
 */
sealed class UiState<out T> {
    /**
     * Initial state before any operation
     */
    object Idle : UiState<Nothing>()
    
    /**
     * Loading state during async operations
     */
    object Loading : UiState<Nothing>()
    
    /**
     * Success state with data
     */
    data class Success<T>(val data: T) : UiState<T>()
    
    /**
     * Error state with message
     */
    data class Error(val message: String, val throwable: Throwable? = null) : UiState<Nothing>()
    
    /**
     * Check if state is loading
     */
    val isLoading: Boolean
        get() = this is Loading
    
    /**
     * Check if state is success
     */
    val isSuccess: Boolean
        get() = this is Success
    
    /**
     * Check if state is error
     */
    val isError: Boolean
        get() = this is Error
    
    /**
     * Check if state is idle
     */
    val isIdle: Boolean
        get() = this is Idle
    
    /**
     * Get data if success, null otherwise
     */
    fun getDataOrNull(): T? {
        return when (this) {
            is Success -> data
            else -> null
        }
    }
    
    /**
     * Get error message if error, null otherwise
     */
    fun getErrorOrNull(): String? {
        return when (this) {
            is Error -> message
            else -> null
        }
    }
}

/**
 * Extension function to map success data
 */
fun <T, R> UiState<T>.map(transform: (T) -> R): UiState<R> {
    return when (this) {
        is UiState.Idle -> UiState.Idle
        is UiState.Loading -> UiState.Loading
        is UiState.Success -> UiState.Success(transform(data))
        is UiState.Error -> UiState.Error(message, throwable)
    }
}

/**
 * Extension function to handle each state
 */
inline fun <T> UiState<T>.onEach(
    onIdle: () -> Unit = {},
    onLoading: () -> Unit = {},
    onSuccess: (T) -> Unit = {},
    onError: (String, Throwable?) -> Unit = { _, _ -> }
) {
    when (this) {
        is UiState.Idle -> onIdle()
        is UiState.Loading -> onLoading()
        is UiState.Success -> onSuccess(data)
        is UiState.Error -> onError(message, throwable)
    }
}

/**
 * Convert Result to UiState
 */
fun <T> Result<T>.toUiState(): UiState<T> {
    return fold(
        onSuccess = { UiState.Success(it) },
        onFailure = { UiState.Error(it.message ?: "Unknown error", it) }
    )
}
