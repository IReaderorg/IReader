package ir.kazemcodes.infinity.feature_detail.presentation.book_detail.components

import androidx.compose.runtime.*

@Composable
fun TransparentStatusBar(content: @Composable () -> Unit) { val state = LocalTransparentStatusBar.current
    DisposableEffect(Unit) {
        state.enabled = true
        onDispose {
            state.enabled = false
        }
    }
    content()
}

val LocalTransparentStatusBar = staticCompositionLocalOf { TransparentStatusBar(false) }

class TransparentStatusBar(enabled: Boolean) {
    var enabled by mutableStateOf(enabled)
}
