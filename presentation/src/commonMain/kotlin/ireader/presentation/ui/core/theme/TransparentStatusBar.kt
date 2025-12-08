package ireader.presentation.ui.core.theme

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import ireader.i18n.LocalizeHelper
import ireader.presentation.core.theme.IUseController
import kotlinx.coroutines.CoroutineScope

/**
 * Composable that enables transparent status bar while in composition.
 * 
 * Uses reference counting to handle nested usage correctly - the status bar
 * stays transparent as long as at least one TransparentStatusBar is active.
 */
@Composable
fun TransparentStatusBar(content: @Composable () -> Unit) {
    val state = LocalTransparentStatusBar.current
    
    DisposableEffect(Unit) {
        state.acquire()
        onDispose {
            state.release()
        }
    }
    
    content()
}

/**
 * Composable that sets custom system bar colors while in composition.
 * 
 * @param enable Whether to apply custom colors
 * @param statusBar Color for the status bar
 * @param navigationBar Color for the navigation bar
 */
@Composable
fun CustomSystemColor(
    enable: Boolean = false,
    statusBar: Color,
    navigationBar: Color,
    content: @Composable () -> Unit
) {
    val state = LocalCustomSystemColor.current
    
    DisposableEffect(enable, statusBar, navigationBar) {
        if (enable) {
            state.acquire(statusBar, navigationBar)
        }
        onDispose {
            if (enable) {
                state.release()
            }
        }
    }
    
    content()
}

// ==================== CompositionLocals ====================

val LocalTransparentStatusBar = staticCompositionLocalOf { TransparentStatusBarState() }
val LocalISystemUIController: ProvidableCompositionLocal<IUseController?> = staticCompositionLocalOf { IUseController() }
val LocalCustomSystemColor = staticCompositionLocalOf { CustomSystemColorState() }
val LocalLocalizeHelper: ProvidableCompositionLocal<LocalizeHelper?> = staticCompositionLocalOf { null }
val LocalGlobalCoroutineScope: ProvidableCompositionLocal<CoroutineScope?> = staticCompositionLocalOf { null }

// ==================== Extension Properties ====================

/**
 * Extension properties for safe access to CompositionLocals
 */
@get:Composable
val ProvidableCompositionLocal<LocalizeHelper?>.currentOrThrow: LocalizeHelper
    get() = this.current ?: error("LocalLocalizeHelper not provided")

@get:Composable
val ProvidableCompositionLocal<CoroutineScope?>.currentOrThrow: CoroutineScope
    get() = this.current ?: error("LocalGlobalCoroutineScope not provided")

// ==================== State Classes ====================

/**
 * State holder for transparent status bar with reference counting.
 * 
 * Multiple composables can request transparent status bar simultaneously.
 * The status bar stays transparent as long as refCount > 0.
 */
@Stable
class TransparentStatusBarState {
    private var refCount by mutableIntStateOf(0)
    
    /**
     * Whether transparent status bar is currently enabled.
     * True when at least one composable has acquired it.
     */
    val enabled: Boolean
        get() = refCount > 0
    
    /**
     * Request transparent status bar. Call [release] when done.
     */
    fun acquire() {
        refCount++
    }
    
    /**
     * Release transparent status bar request.
     */
    fun release() {
        refCount = maxOf(0, refCount - 1)
    }
}

/**
 * State holder for custom system bar colors with reference counting.
 * 
 * Supports stacking - the most recent acquire() colors are used.
 * When released, falls back to previous colors or defaults.
 */
@Stable
class CustomSystemColorState {
    private var refCount by mutableIntStateOf(0)
    private val colorStack = mutableStateListOf<Pair<Color, Color>>()
    
    /**
     * Whether custom colors are currently enabled.
     */
    val enabled: Boolean
        get() = refCount > 0
    
    /**
     * Current status bar color (or default if not enabled).
     */
    val statusBar: Color
        get() = colorStack.lastOrNull()?.first ?: Color.Transparent
    
    /**
     * Current navigation bar color (or default if not enabled).
     */
    val navigationBar: Color
        get() = colorStack.lastOrNull()?.second ?: Color.Transparent
    
    /**
     * Request custom system colors. Call [release] when done.
     */
    fun acquire(statusBarColor: Color, navigationBarColor: Color) {
        colorStack.add(statusBarColor to navigationBarColor)
        refCount++
    }
    
    /**
     * Release custom system colors request.
     */
    fun release() {
        if (colorStack.isNotEmpty()) {
            colorStack.removeAt(colorStack.lastIndex)
        }
        refCount = maxOf(0, refCount - 1)
    }
}


