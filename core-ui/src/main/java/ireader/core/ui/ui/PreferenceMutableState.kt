package ireader.core.ui.ui

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ireader.core.api.prefs.Preference

class PreferenceMutableState<T>(
    private val preference: Preference<T>,
    scope: CoroutineScope
) : MutableState<T> {

    private val state = mutableStateOf(preference.get())
    private val lazyState = mutableStateOf(preference.get())

    init {
        scope.launch(Dispatchers.Main) {
            preference.changes()
                .collect {
                    kotlin.runCatching {
                        state.value = it
                    }
                }
        }
    }

    /**
     * we can use this for times that we need to update UI immediately
     * and changes happens too fast like using it in slider
     */
    var lazyValue: T
        get() = lazyState.value
        set(value) {
            lazyState.value = value
            preference.set(value)
        }

    override var value: T
        get() = state.value
        set(value) {
            preference.set(value)
        }

    override fun component1(): T {
        return state.value
    }

    override fun component2(): (T) -> Unit {
        return { preference.set(it) }
    }
}

fun <T> Preference<T>.asStateIn(scope: CoroutineScope): PreferenceMutableState<T> {
    return PreferenceMutableState(this, scope)
}
