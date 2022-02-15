package org.ireader.domain.view_models.base_view_model

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.ireader.core.prefs.Preference
import org.ireader.core_ui.ui.PreferenceMutableState


abstract class BaseViewModel : ViewModel() {

    protected val scope: CoroutineScope
        get() = viewModelScope

    private val activeScope = MutableStateFlow<CoroutineScope?>(null)

    open fun onDestroy() {
        scope.cancel()
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

    fun <T> Flow<T>.launchWhenActive() = channelFlow<T> {
        scope.launch {
            activeScope
            this@launchWhenActive.filterNotNull()
                .first {
                    send(it)
                    true
                }
        }
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