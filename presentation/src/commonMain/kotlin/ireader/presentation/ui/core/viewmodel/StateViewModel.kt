package ireader.presentation.ui.core.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Base ViewModel class that provides state management functionality
 * This replaces the old StateScreenModel from Voyager
 * 
 * Usage:
 * ```
 * class MyViewModel : StateViewModel<MyViewModel.State>(State()) {
 *     data class State(
 *         val isLoading: Boolean = false,
 *         val data: String = ""
 *     )
 *     
 *     fun updateData(newData: String) {
 *         updateState { it.copy(data = newData) }
 *     }
 * }
 * 
 * // In Composable:
 * @Composable
 * fun MyScreen(viewModel: MyViewModel = getViewModel()) {
 *     val state by viewModel.state.collectAsState()
 *     // Use state.isLoading, state.data, etc.
 * }
 * ```
 */
abstract class StateViewModel<S>(initialState: S) : BaseViewModel() {
    
    /**
     * Mutable state flow for internal state updates
     */
    protected val mutableState = MutableStateFlow(initialState)
    
    /**
     * Public state flow that can be collected in Composables
     */
    val state: StateFlow<S> = mutableState.asStateFlow()
    
    /**
     * Update the state using a lambda function
     * This is the preferred way to update state
     * 
     * Example:
     * ```
     * updateState { currentState ->
     *     currentState.copy(isLoading = true)
     * }
     * ```
     */
    protected fun updateState(update: (S) -> S) {
        mutableState.update(update)
    }
    
    /**
     * Directly set the state value
     * Use updateState() instead when possible for better immutability
     */
    protected fun setState(newState: S) {
        mutableState.value = newState
    }
    
    /**
     * Get the current state value synchronously
     */
    protected val currentState: S
        get() = mutableState.value
}

/**
 * Extension function to collect state as Compose State
 * This allows using `val state by viewModel.state.collectAsState()`
 */
@Composable
fun <S> StateViewModel<S>.collectState(): State<S> {
    return state.collectAsState()
}
