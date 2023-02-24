package ireader.presentation.ui.core.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.coroutineScope
import ireader.core.prefs.Preference
import ireader.i18n.UiEvent
import ireader.i18n.UiText
import ireader.presentation.R
import ireader.presentation.ui.core.ui.PreferenceMutableState
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

actual abstract class BaseViewModel : ScreenModel {

    actual val scope: CoroutineScope
        get() = this.coroutineScope

    private val activeScope = MutableStateFlow<CoroutineScope?>(null)

    protected val _eventFlow = MutableSharedFlow<UiEvent>()
    open val eventFlow = _eventFlow.asSharedFlow()

    open fun showSnackBar(message: UiText?) {
        scope.launch {
            _eventFlow.showSnackBar(message ?: UiText.StringResource(R.string.error_unknown))
        }
    }

    //    final override fun onCleared() {
//        onDestroy()
//    }
    override fun onDispose() {
        onDestroy()
        super.onDispose()
    }

    actual open fun onDestroy() {
    }

    actual fun <T> Preference<T>.asState() = PreferenceMutableState(this, scope)

    actual fun <T> Preference<T>.asState(onChange: (T) -> Unit): PreferenceMutableState<T> {
        this.changes()
            .onEach { onChange(it) }
            .launchIn(scope)
        return PreferenceMutableState(this, scope)
    }

    actual fun <T> Flow<T>.asState(initialValue: T, onChange: (T) -> Unit): State<T> {
        val state = mutableStateOf(initialValue)
        scope.launch {
            collect {
                state.value = it
                onChange(it)
            }
        }
        return state
    }

    actual fun <T> StateFlow<T>.asState(): State<T> {
        val state = mutableStateOf(value)
        scope.launch {
            collect { state.value = it }
        }
        return state
    }

    actual fun <T> Flow<T>.launchWhileActive(): Job {
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
