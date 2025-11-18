package ireader.presentation.ui.core.utils

import ireader.core.prefs.Preference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/**
 * Utility extension functions for converting Preference to StateFlow.
 * 
 * These extensions provide a convenient way to convert preferences to StateFlow
 * for use in ViewModels and Compose UI.
 */

/**
 * Converts a Preference to a StateFlow bound to the given scope.
 * 
 * @param scope The CoroutineScope to bind the StateFlow to
 * @return A StateFlow that emits preference value changes
 */
fun <T> Preference<T>.asStateFlow(scope: CoroutineScope): StateFlow<T> {
    return this.stateIn(scope)
}
