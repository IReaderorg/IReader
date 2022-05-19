package org.ireader.core_ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

@Composable
fun TransparentStatusBar(enable:Boolean = false, content: @Composable () -> Unit) {
    if (enable) {
        val state = LocalTransparentStatusBar.current
        DisposableEffect(Unit) {
            state.enabled = true
            onDispose {
                state.enabled = false
            }
        }
    }
    content()
}

val LocalTransparentStatusBar = staticCompositionLocalOf { TransparentStatusBar(false) }

class TransparentStatusBar(enabled: Boolean) {
    var enabled by mutableStateOf(enabled)
}
