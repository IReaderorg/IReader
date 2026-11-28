package ireader.presentation.ui.component

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

/**
 * Process-wide animation switch.
 *
 * Compose does not expose a public CompositionLocal for animation specs, and
 * Material components own their motion internally, so animation entry points
 * are routed through the wrappers in [ireader.presentation.ui.component.animations].
 * AppTheme flips this flag whenever max performance mode changes.
 */
object AnimationPolicy {
    private val _animationsEnabled = mutableStateOf(true)

    /**
     * Snapshot-backed state so reads inside composition observe changes. Without this, a toggle
     * from max performance mode back to normal would update the flag after composition and leave
     * the already-composed UI using instant specs until something else recomposed it.
     */
    val animationsEnabledState: State<Boolean> = _animationsEnabled

    var animationsEnabled: Boolean
        get() = _animationsEnabled.value
        set(value) {
            _animationsEnabled.value = value
        }

    val animationsDisabled: Boolean
        get() = !animationsEnabled
}

/** Runs [block] with a temporarily overridden animation policy. */
inline fun <T> withAnimationsEnabled(enabled: Boolean, block: () -> T): T {
    val previous = AnimationPolicy.animationsEnabled
    AnimationPolicy.animationsEnabled = enabled
    return try {
        block()
    } finally {
        AnimationPolicy.animationsEnabled = previous
    }
}

/** Returns [spec] normally, or an instant spec when animations are disabled. */
fun <T> resolveAnimationSpec(spec: FiniteAnimationSpec<T>): FiniteAnimationSpec<T> =
    if (AnimationPolicy.animationsEnabled) spec else snap()

/**
 * The animation switch follows the max performance preference only.
 *
 * Device-tier configs can have `animationDurationMs == 0` on their own (for example
 * [PerformanceConfig.LowEnd]), so deriving this from the resolved config would make
 * the setting impossible to turn back off on those devices.
 */
fun resolveAnimationsEnabled(maxPerformanceEnabled: Boolean): Boolean = !maxPerformanceEnabled

/**
 * Max performance mode replaces the device-detected config entirely.
 *
 * Keeping this decision in one pure function lets AppTheme (Android, desktop
 * and iOS) apply the preference reactively instead of relying on a one-time
 * read in MainActivity.
 */
fun resolvePerformanceConfig(
    base: PerformanceConfig,
    maxPerformanceEnabled: Boolean,
): PerformanceConfig = if (maxPerformanceEnabled) PerformanceConfig.MaxPerformance else base
