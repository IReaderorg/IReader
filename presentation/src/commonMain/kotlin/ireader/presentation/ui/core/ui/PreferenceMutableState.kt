package ireader.presentation.ui.core.ui

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import ireader.core.prefs.Preference
import ireader.core.util.DefaultDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A MutableState wrapper for Preference that provides:
 * - Immediate UI updates via lazyValue (no persistence delay)
 * - Debounced persistence to avoid excessive writes during rapid changes (e.g., slider drag)
 * - Automatic sync with preference changes from other sources
 */
class PreferenceMutableState<T>(
    private val preference: Preference<T>,
    private val scope: CoroutineScope,
    private val debounceMs: Long = 0L
) : MutableState<T> {

    private val state = mutableStateOf(preference.get())
    private val lazyState = mutableStateOf(preference.get())
    private var debounceJob: Job? = null

    init {
        scope.launch(DefaultDispatcher) {
            preference.changes()
                .collect {
                    kotlin.runCatching {
                        state.value = it
                        // Sync lazy state if not currently being modified
                        if (debounceJob?.isActive != true) {
                            lazyState.value = it
                        }
                    }
                }
        }
    }

    /**
     * Use this for times when UI needs immediate updates but persistence can be delayed.
     * Ideal for sliders and other continuous input controls.
     * Updates UI immediately, debounces persistence.
     */
    var lazyValue: T
        get() = lazyState.value
        set(value) {
            lazyState.value = value
            if (debounceMs > 0) {
                debounceJob?.cancel()
                debounceJob = scope.launch {
                    delay(debounceMs)
                    preference.set(value)
                }
            } else {
                preference.set(value)
            }
        }
    
    /**
     * Immediately persist the current lazy value without waiting for debounce.
     * Call this when slider drag ends or when you need to ensure persistence.
     */
    fun commitLazyValue() {
        debounceJob?.cancel()
        preference.set(lazyState.value)
    }

    override var value: T
        get() = state.value
        set(value) {
            debounceJob?.cancel()
            preference.set(value)
        }

    override fun component1(): T {
        return state.value
    }

    override fun component2(): (T) -> Unit {
        return { 
            debounceJob?.cancel()
            preference.set(it) 
        }
    }
}

/**
 * Creates a PreferenceMutableState without debouncing.
 * Use for preferences that don't need debounced writes.
 */
fun <T> Preference<T>.asStateIn(scope: CoroutineScope): PreferenceMutableState<T> {
    return PreferenceMutableState(this, scope, debounceMs = 0L)
}

/**
 * Creates a PreferenceMutableState with debounced persistence.
 * Use for sliders and other continuous input controls to avoid excessive writes.
 * 
 * @param debounceMs Delay in milliseconds before persisting changes (default: 150ms)
 */
fun <T> Preference<T>.asStateInDebounced(
    scope: CoroutineScope, 
    debounceMs: Long = 150L
): PreferenceMutableState<T> {
    return PreferenceMutableState(this, scope, debounceMs = debounceMs)
}
