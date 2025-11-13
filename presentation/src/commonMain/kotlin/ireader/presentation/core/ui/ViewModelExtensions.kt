package ireader.presentation.core.ui

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Common ViewModel extensions to reduce duplication across ViewModels
 */

/**
 * Execute an async operation and update UI state
 */
fun <T, S> StateScreenModel<S>.executeAsync(
    stateFlow: MutableStateFlow<UiState<T>>,
    operation: suspend () -> T,
    onSuccess: ((T) -> Unit)? = null,
    onError: ((Throwable) -> Unit)? = null
) {
    screenModelScope.launch {
        stateFlow.value = UiState.Loading
        try {
            val result = operation()
            stateFlow.value = UiState.Success(result)
            onSuccess?.invoke(result)
        } catch (e: Exception) {
            stateFlow.value = UiState.Error(e.message ?: "Unknown error", e)
            onError?.invoke(e)
        }
    }
}

/**
 * Execute an async operation with Result type and update UI state
 */
fun <T, S> StateScreenModel<S>.executeAsyncResult(
    stateFlow: MutableStateFlow<UiState<T>>,
    operation: suspend () -> Result<T>,
    onSuccess: ((T) -> Unit)? = null,
    onError: ((Throwable) -> Unit)? = null
) {
    screenModelScope.launch {
        stateFlow.value = UiState.Loading
        val result = operation()
        result.fold(
            onSuccess = { data ->
                stateFlow.value = UiState.Success(data)
                onSuccess?.invoke(data)
            },
            onFailure = { error ->
                stateFlow.value = UiState.Error(error.message ?: "Unknown error", error)
                onError?.invoke(error)
            }
        )
    }
}

/**
 * Launch a coroutine in the ViewModel scope
 */
fun <S> StateScreenModel<S>.launchInScope(block: suspend CoroutineScope.() -> Unit) {
    screenModelScope.launch(block = block)
}
