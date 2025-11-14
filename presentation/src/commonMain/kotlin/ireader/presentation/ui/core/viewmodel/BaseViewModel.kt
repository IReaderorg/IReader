package ireader.presentation.ui.core.viewmodel
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ireader.core.prefs.Preference
import ireader.domain.utils.extensions.showSnackBar
import ireader.i18n.UiEvent
import ireader.i18n.UiText
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.core.ui.PreferenceMutableState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {

 val scope: CoroutineScope = viewModelScope

  protected val _eventFlow = MutableSharedFlow<UiEvent>()
  open val eventFlow = _eventFlow.asSharedFlow()

   open fun showSnackBar(message: UiText?) {
    scope.launch {
      _eventFlow.showSnackBar(message ?: UiText.MStringResource(Res.string.error_unknown))
    }
  }
  
  override fun onCleared() {
    onDestroy()
    super.onCleared()
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
                    uiText = message ?: UiText.MStringResource(Res.string.error_unknown)
            )
    )
}
