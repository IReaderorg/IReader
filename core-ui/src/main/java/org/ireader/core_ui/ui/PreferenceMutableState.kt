package org.ireader.core_ui.ui

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import tachiyomi.core.prefs.Preference

class PreferenceMutableState<T>(
    private val preference: Preference<T>,
    scope: CoroutineScope,
) : MutableState<T> {

    private val state = mutableStateOf(preference.get())

    init {
        try {
            preference.changes().distinctUntilChanged()
                .onEach { value ->
                    state.value = value
                }.launchIn(scope)
        } catch (e: Exception) {
            Log.e("PreferenceMutableState", e.toString())
        }
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
