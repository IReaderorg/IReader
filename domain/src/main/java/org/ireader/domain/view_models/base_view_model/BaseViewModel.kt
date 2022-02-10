package org.ireader.domain.view_models.base_view_model

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.ireader.core.prefs.Preference
import org.ireader.core_ui.ui.PreferenceMutableState


abstract class BaseViewModel {

    protected val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val activeScope = MutableStateFlow<CoroutineScope?>(null)

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

}

fun <T> Flow<T>.asState(initialValue: T, scope: CoroutineScope): State<T> {
    val state = mutableStateOf(initialValue)
    scope.launch {
        collect { state.value = it }
    }
    return state
}


fun <T> StateFlow<T>.asState(scope: CoroutineScope): State<T> {
    val state = mutableStateOf(value)
    scope.launch {
        collect { state.value = it }
    }
    return state
}

fun <T> Flow<T>.launchWhileActive(
    activeScope: MutableStateFlow<CoroutineScope?>,
    scope: CoroutineScope,
): Job {
    return activeScope
        .filterNotNull()
        .onEach { launchIn(it) }
        .launchIn(scope)
}