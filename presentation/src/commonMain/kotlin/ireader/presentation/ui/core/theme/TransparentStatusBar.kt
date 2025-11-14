package ireader.presentation.ui.core.theme

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import ireader.i18n.LocalizeHelper
import ireader.presentation.core.theme.IUseController
import kotlinx.coroutines.CoroutineScope

@Composable
fun TransparentStatusBar(content: @Composable () -> Unit) {

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
val LocalISystemUIController: ProvidableCompositionLocal<IUseController?> = staticCompositionLocalOf { IUseController() }
val LocalCustomSystemColor = staticCompositionLocalOf { CustomStatusBar(false) }
val LocalLocalizeHelper: ProvidableCompositionLocal<LocalizeHelper?> = staticCompositionLocalOf {
    null
}

val LocalGlobalCoroutineScope: ProvidableCompositionLocal<CoroutineScope?> = staticCompositionLocalOf {
    null
}

/**
 * Extension properties for safe access to CompositionLocals
 */
@get:Composable
@get:JvmName("getLocalizeHelperCurrentOrThrow")
val ProvidableCompositionLocal<LocalizeHelper?>.currentOrThrow: LocalizeHelper
    get() = this.current ?: error("LocalLocalizeHelper not provided")

@get:Composable
@get:JvmName("getCoroutineScopeCurrentOrThrow")
val ProvidableCompositionLocal<CoroutineScope?>.currentOrThrow: CoroutineScope
    get() = this.current ?: error("LocalGlobalCoroutineScope not provided")

class TransparentStatusBar(enabled: Boolean) {
    var enabled by mutableStateOf(enabled)
}

class CustomStatusBar(enabled: Boolean) {
    var enabled by mutableStateOf(enabled)
    var statusBar by mutableStateOf(Color.White)
    var navigationBar by mutableStateOf(Color.White)
}
