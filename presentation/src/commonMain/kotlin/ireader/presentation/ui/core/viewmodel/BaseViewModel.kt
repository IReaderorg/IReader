package ireader.presentation.ui.core.viewmodel
import androidx.compose.runtime.State
import ireader.core.prefs.Preference
import ireader.presentation.ui.core.ui.PreferenceMutableState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow


expect abstract class BaseViewModel {

  protected val scope: CoroutineScope

  open fun onDestroy()

  fun <T> Preference<T>.asState(): PreferenceMutableState<T>

  fun <T> Preference<T>.asState(onChange: (T) -> Unit): PreferenceMutableState<T>
  fun <T> Flow<T>.asState(initialValue: T, onChange: (T) -> Unit = {}): State<T>

  fun <T> StateFlow<T>.asState(): State<T>

  fun <T> Flow<T>.launchWhileActive(): Job

}
