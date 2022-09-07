package ireader.core.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ireader.common.resources.UiEvent
import ireader.common.resources.UiText
import ireader.core.api.prefs.Preference
import ireader.core.ui.R
import ireader.core.ui.ui.PreferenceMutableState

abstract class BaseViewModel : androidx.lifecycle.ViewModel() {

    protected val scope: CoroutineScope
        get() = viewModelScope

    private val activeScope = MutableStateFlow<CoroutineScope?>(null)

    protected val _eventFlow = MutableSharedFlow<UiEvent>()
    open val eventFlow = _eventFlow.asSharedFlow()

    open fun showSnackBar(message: UiText?) {
        viewModelScope.launch {
            _eventFlow.showSnackBar(message ?: UiText.StringResource(R.string.error_unknown))
        }
    }

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

    fun <T> Flow<T>.asState(initialValue: T, onChange: (T) -> Unit = {}): State<T> {
        val state = mutableStateOf(initialValue)
        scope.launch {
            collect {
                state.value = it
                onChange(it)
            }
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

    @OptIn(ExperimentalCoroutinesApi::class)
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
suspend fun MutableSharedFlow<UiEvent>.showSnackBar(message: UiText?) {
    this.emit(
        UiEvent.ShowSnackbar(
            uiText = message ?: UiText.StringResource(R.string.error_unknown)
        )
    )
}
