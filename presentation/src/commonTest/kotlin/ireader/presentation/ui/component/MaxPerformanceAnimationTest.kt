package ireader.presentation.ui.component

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.SnapSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween as composeTween
import ireader.presentation.ui.component.animations.fadeIn
import ireader.presentation.ui.component.animations.infiniteRepeatable
import ireader.presentation.ui.component.animations.slideInVertically
import ireader.presentation.ui.component.animations.tween
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Regression tests for max performance mode.
 *
 * The preference promises that animations are disabled. These tests pin the
 * runtime policy that all animation wrappers consult.
 */
class MaxPerformanceAnimationTest {

    @Test
    fun `max performance config overrides platform config when enabled`() {
        val platformConfig = PerformanceConfig.HighQuality

        assertEquals(
            expected = PerformanceConfig.MaxPerformance,
            actual = resolvePerformanceConfig(platformConfig, maxPerformanceEnabled = true),
        )
        assertEquals(
            expected = platformConfig,
            actual = resolvePerformanceConfig(platformConfig, maxPerformanceEnabled = false),
        )
    }

    @Test
    fun `policy flag is snapshot backed so toggling it recomposes animation wrappers`() {
        withAnimationsEnabled(false) {
            assertFalse(AnimationPolicy.animationsEnabledState.value)
        }
        assertTrue(AnimationPolicy.animationsEnabledState.value)
    }

    @Test
    fun `max performance preference alone controls animation policy`() {
        // LowEnd devices have animationDurationMs == 0, but that must not make the
        // max performance toggle impossible to turn back off.
        assertTrue(resolveAnimationsEnabled(maxPerformanceEnabled = false))
        assertFalse(resolveAnimationsEnabled(maxPerformanceEnabled = true))
    }

    @Test
    fun `animation specs snap when animations are disabled`() {
        withAnimationsEnabled(false) {
            val spec = tween<Float>(durationMillis = 400)
            assertTrue(spec is SnapSpec, "Expected snap spec, got ${spec::class.simpleName}")
            assertEquals(0, spec.delay)
        }
    }

    @Test
    fun `animation specs animate when animations are enabled`() {
        withAnimationsEnabled(true) {
            val spec = tween<Float>(durationMillis = 400)
            assertFalse(spec is SnapSpec)
        }
    }

    @Test
    fun `infinite animations snap when animations are disabled`() {
        withAnimationsEnabled(false) {
            val spec = infiniteRepeatable(composeTween<Float>(1000))
            assertTrue(
                spec.animation is TweenSpec,
                "Expected instant inner spec, got ${spec.animation::class.simpleName}",
            )
            assertEquals(1, (spec.animation as TweenSpec<Float>).durationMillis)
        }
    }

    @Test
    fun `enter and exit transitions are disabled with animations off`() {
        withAnimationsEnabled(false) {
            assertEquals(EnterTransition.None, fadeIn())
            assertEquals(ExitTransition.None, ireader.presentation.ui.component.animations.fadeOut())
            assertEquals(EnterTransition.None, slideInVertically(initialOffsetY = { it }))
        }
    }
}
