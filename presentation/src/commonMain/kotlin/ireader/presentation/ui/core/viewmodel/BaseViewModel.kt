package ireader.presentation.ui.core.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.coroutineScope
import ireader.core.prefs.Preference
import ireader.domain.utils.extensions.showSnackBar
import ireader.i18n.UiEvent
import ireader.i18n.UiText
import ireader.presentation.ui.core.ui.PreferenceMutableState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

abstract class BaseViewModel : ScreenModel {

 val scope: CoroutineScope = coroutineScope

  protected val _eventFlow = MutableSharedFlow<UiEvent>()
  open val eventFlow = _eventFlow.asSharedFlow()

   open fun showSnackBar(message: UiText?) {
    scope.launch {
        _eventFlow.showSnackBar(message ?: UiText.MStringResource { xml ->
            xml.errorUnknown
        })
    }
  }
  override fun onDispose() {
    onDestroy()
    super.onDispose()
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
   fun <T> Flow<T>.asState(initialValue: T, onChange: (T) -> Unit= {}): State<T> {
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

}

suspend fun MutableSharedFlow<UiEvent>.showSnackBar(message: UiText?) {
    this.emit(
            UiEvent.ShowSnackbar(
                uiText = message ?: UiText.MStringResource { xml ->
                    xml.errorUnknown
                }
            )
    )
}
