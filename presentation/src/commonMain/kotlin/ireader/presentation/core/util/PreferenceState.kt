package ireader.presentation.core.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Preference state utilities following Mihon's pattern.
 * 
 * These utilities provide efficient ways to observe preferences
 * in Compose without unnecessary recomposition.
 */

/**
 * A MutableState that is backed by a preference.
 * Changes to the state are automatically persisted to the preference.
 * 
 * Usage:
 * ```
 * val showChapterNumber by readerPreferences.showChapterNumber().asState(scope)
 * ```
 */
class PreferenceMutableState<T>(
    private val getValue: () -> T,
    private val setValue: (T) -> Unit,
    flow: Flow<T>,
    scope: CoroutineScope,
) : MutableState<T> {

    private val state = mutableStateOf(getValue())

    init {
        flow
            .onEach { state.value = it }
            .launchIn(scope)
    }

    override var value: T
        get() = state.value
        set(value) {
            setValue(value)
        }

    override fun component1(): T = state.value

    override fun component2(): (T) -> Unit = { value = it }
}

/**
 * Collects a Flow as Compose State with an initial value.
 * 
 * Usage:
 * ```
 * val theme by themePreference.changes().collectAsState(initial = themePreference.get())
 * ```
 */
@Composable
fun <T> Flow<T>.collectAsStateWithInitial(initial: T): State<T> {
    return collectAsState(initial = initial)
}

/**
 * Remembers a derived state that only recomputes when the key changes.
 * Useful for expensive computations that depend on a single value.
 */
@Composable
inline fun <T, R> rememberDerivedState(
    key: T,
    crossinline calculation: (T) -> R,
): State<R> {
    return remember(key) {
        androidx.compose.runtime.derivedStateOf { calculation(key) }
    }
}
