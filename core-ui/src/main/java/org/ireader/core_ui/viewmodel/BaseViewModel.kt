package org.ireader.core_ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.ireader.core.prefs.Preference
import org.ireader.core_ui.ui.PreferenceMutableState


abstract class BaseViewModel : androidx.lifecycle.ViewModel() {

    protected val scope: CoroutineScope
        get() = viewModelScope

    private val activeScope = MutableStateFlow<CoroutineScope?>(null)

    final override fun onCleared() {
        onDestroy()
    }

    open fun onDestroy() {
    }

    fun <T> Preference<T>.asState() = PreferenceMutableState(this, scope)

    fun <T> Preference<T>.asState(onChange: (T) -> Unit): PreferenceMutableState<T> {
        this.changes()
            .onEach { onChange(it) }
            .launchIn(scope)
        return PreferenceMutableState(this, scope)
    }

    fun <T> Flow<T>.asState(initialValue: T): State<T> {
        val state = mutableStateOf(initialValue)
        scope.launch {
            collect { state.value = it }
        }
        return state
    }

    fun <T> StateFlow<T>.asState(): State<T> {
        val state = mutableStateOf(value)
        scope.launch {
            collect { state.value = it }
        }
        return state
    }

    fun <T> Flow<T>.launchWhileActive(): Job {
        return activeScope
            .filterNotNull()
            .onEach { launchIn(it) }
            .launchIn(scope)
    }

    internal fun setActive() {
        val currScope = activeScope.value
        if (currScope != null) return
        activeScope.value = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    }

    internal fun setInactive() {
        val currScope = activeScope.value
        currScope?.cancel()
        activeScope.value = null
    }

}