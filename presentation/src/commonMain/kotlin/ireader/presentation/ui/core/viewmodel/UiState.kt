package ireader.presentation.ui.core.viewmodel

import ireader.i18n.UiText

/**
 * Generic sealed class for UI state management
 * Provides consistent state handling across all ViewModels
 */
sealed class UiState<out T> {
    /**
     * Initial state before any data is loaded
     */
    object Idle : UiState<Nothing>()
    
    /**
     * Loading state while data is being fetched
     */
    object Loading : UiState<Nothing>()
    
    /**
     * Success state with data
     */
    data class Success<T>(val data: T) : UiState<T>()
    
    /**
     * Error state with error message
     */
    data class Error(val message: UiText) : UiState<Nothing>()
    
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
     * Get data if state is success, null otherwise
     */
    fun getDataOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }
}

/**
 * Async operation state for tracking progress
 */
sealed class AsyncState {
    object Idle : AsyncState()
    data class InProgress(val progress: Float = 0f) : AsyncState()
    data class Success(val message: UiText? = null) : AsyncState()
    data class Failure(val error: UiText) : AsyncState()
    
    val isInProgress: Boolean get() = this is InProgress
    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure
}
