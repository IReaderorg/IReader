package ireader.presentation.core

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

/**
 * Animated NavHost with smooth transitions optimized for performance.
 * 
 * PERFORMANCE OPTIMIZED for smooth 60fps transitions:
 * - Uses fade + subtle scale instead of slide for GPU-friendly animation
 * - Slide animations can cause jank on mid-range devices due to layout recalculation
 * - Scale + fade is composited on GPU without layout changes
 * - Very short duration (150ms) for snappy feel
 * - Linear easing for predictable, smooth motion
 * - Persistent background to eliminate white flashes
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    builder: NavGraphBuilder.() -> Unit
) {
    // Fast-out slow-in: reads as natural motion even at short durations.
    // LinearEasing at 100ms reads as a hard cut / flash - the reported "glitch".
    val transitionEasing = remember { FastOutSlowInEasing }

    val enterSpec: FiniteAnimationSpec<Float> = remember { tween(220, easing = transitionEasing) }
    val exitSpec: FiniteAnimationSpec<Float> = remember { tween(180, easing = transitionEasing) }

    // Persistent background to prevent white flashes
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize(),
            enterTransition = {
                fadeIn(enterSpec)
            },
            exitTransition = {
                fadeOut(exitSpec)
            },
            popEnterTransition = {
                fadeIn(enterSpec)
            },
            popExitTransition = {
                fadeOut(exitSpec)
            },
            builder = builder
        )
    }
}
