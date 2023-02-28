package ireader.presentation.ui.core.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Composable
fun TransparentStatusBar( content: @Composable () -> Unit) {

        val state = LocalTransparentStatusBar.current
        DisposableEffect(Unit) {
            state.enabled = true
            onDispose {
                state.enabled = false
            }
        }
    content()
}

@Composable
fun CustomSystemColor(
    enable: Boolean = false,
    statusBar: Color,
    navigationBar: Color,
    content: @Composable () -> Unit
) {
    if (enable) {
        val state = LocalCustomSystemColor.current
        LaunchedEffect(key1 = statusBar, key2 = navigationBar) {
            state.statusBar = statusBar
            state.navigationBar = navigationBar
        }
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
val LocalCustomSystemColor = staticCompositionLocalOf { CustomStatusBar(false) }
val LocalHideNavigator = staticCompositionLocalOf { mutableStateOf(false) }
class TransparentStatusBar(enabled: Boolean) {
    var enabled by mutableStateOf(enabled)
}

class CustomStatusBar(enabled: Boolean) {
    var enabled by mutableStateOf(enabled)
    var statusBar by mutableStateOf(Color.White)
    var navigationBar by mutableStateOf(Color.White)
}
