

package org.ireader.core_api.os

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Suppress("ObjectPropertyName")
object AppState : DefaultLifecycleObserver {

    private val _networkFlow = MutableStateFlow(false)
    val networkFlow: StateFlow<Boolean> get() = _networkFlow

    private val _foregroundFlow = MutableStateFlow(false)
    val foregroundFlow: StateFlow<Boolean> get() = _foregroundFlow

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        Log.d("", "Application now in foreground")
        _foregroundFlow.value = true
    }

    override fun onStop(owner: LifecycleOwner) {
        Log.d("", "Application went to background")
        _foregroundFlow.value = false
    }
}
