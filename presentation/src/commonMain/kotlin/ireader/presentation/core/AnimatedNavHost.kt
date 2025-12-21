package ireader.presentation.core

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import ireader.presentation.core.safePopBackStack
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
    // Linear easing is smoother for short animations - no acceleration/deceleration jank
    val smoothEasing = remember { LinearEasing }
    
    // Ultra-short duration for native-like instant response
    // 100ms is the sweet spot - fast enough to feel instant, smooth enough to perceive
    // Native Android Settings uses ~100ms for most transitions
    val fadeAnimationSpec: FiniteAnimationSpec<Float> = remember {
        tween(
            durationMillis = 100,
            easing = smoothEasing
        )
    }
    
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
                // Simple fade in - GPU composited, no layout changes
                // Scale from 97% adds subtle depth without jank
                fadeIn(animationSpec = fadeAnimationSpec) + scaleIn(
                    initialScale = 0.97f,
                    animationSpec = fadeAnimationSpec,
                    transformOrigin = TransformOrigin.Center
                )
            },
            exitTransition = {
                // Quick fade out - keeps focus on incoming screen
                fadeOut(animationSpec = fadeAnimationSpec)
            },
            popEnterTransition = {
                // Going back - simple fade in
                fadeIn(animationSpec = fadeAnimationSpec)
            },
            popExitTransition = {
                // Scale out slightly + fade for depth perception
                fadeOut(animationSpec = fadeAnimationSpec) + scaleOut(
                    targetScale = 0.97f,
                    animationSpec = fadeAnimationSpec,
                    transformOrigin = TransformOrigin.Center
                )
            },
            builder = builder
        )
    }
}
