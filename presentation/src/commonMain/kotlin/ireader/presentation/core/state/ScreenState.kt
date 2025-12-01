package ireader.presentation.core.state

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

/**
 * Base sealed interface for screen states following Mihon's pattern.
 * 
 * Using sealed interface with @Immutable annotations enables Compose compiler
 * optimizations and prevents unnecessary recomposition.
 * 
 * Usage:
 * ```
 * sealed interface State {
 *     @Immutable data object Loading : State
 *     @Immutable data class Success(val data: ImmutableList<Item>) : State
 *     @Immutable data class Error(val message: String) : State
 * }
 * ```
 */
@Stable
sealed interface ScreenState<out T> {
    
    /**
     * Initial loading state - shown when screen first loads
     */
    @Immutable
    data object Loading : ScreenState<Nothing>
    
    /**
     * Success state with data
     */
    @Immutable
    data class Success<T>(val data: T) : ScreenState<T>
    
    /**
     * Error state with message
     */
    @Immutable
    data class Error(val message: String, val throwable: Throwable? = null) : ScreenState<Nothing>
}

/**
 * Extension to check if state is loading
 */
val <T> ScreenState<T>.isLoading: Boolean
    get() = this is ScreenState.Loading

/**
 * Extension to check if state is success
 */
val <T> ScreenState<T>.isSuccess: Boolean
    get() = this is ScreenState.Success

/**
 * Extension to check if state is error
 */
val <T> ScreenState<T>.isError: Boolean
    get() = this is ScreenState.Error

/**
 * Extension to get data or null
 */
val <T> ScreenState<T>.dataOrNull: T?
    get() = (this as? ScreenState.Success)?.data

/**
 * Extension to get error message or null
 */
val <T> ScreenState<T>.errorOrNull: String?
    get() = (this as? ScreenState.Error)?.message
