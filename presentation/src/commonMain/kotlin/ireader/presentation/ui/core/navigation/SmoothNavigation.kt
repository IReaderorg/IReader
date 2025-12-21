package ireader.presentation.ui.core.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

/**
 * Smooth navigation host that eliminates white flashes during screen transitions.
 * 
 * This wrapper provides:
 * - Persistent background color to prevent white flashes
 * - Ultra-fast fade animations for native-like feel
 * - Consistent behavior across light and dark themes
 * - Optimized transition timing (100ms) matching native Android
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SmoothNavigationHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    builder: NavGraphBuilder.() -> Unit
) {
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
            // Native-like transitions: 100ms fade only, no slide (slide causes jank)
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        durationMillis = 100,
                        easing = LinearEasing
                    )
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        durationMillis = 100,
                        easing = LinearEasing
                    )
                )
            },
            popEnterTransition = {
                fadeIn(
                    animationSpec = tween(
                        durationMillis = 100,
                        easing = LinearEasing
                    )
                )
            },
            popExitTransition = {
                fadeOut(
                    animationSpec = tween(
                        durationMillis = 100,
                        easing = LinearEasing
                    )
                )
            },
            builder = builder
        )
    }
}

/**
 * Custom transition extensions for different navigation scenarios
 * All optimized for native-like 100ms transitions
 */

// Fade transition for dialogs and overlays
fun fadeTransition(durationMillis: Int = 100): EnterTransition {
    return fadeIn(animationSpec = tween(durationMillis, easing = LinearEasing))
}

fun fadeOutTransition(durationMillis: Int = 100): ExitTransition {
    return fadeOut(animationSpec = tween(durationMillis, easing = LinearEasing))
}

// Slide from bottom (for bottom sheets style) - faster for native feel
fun slideUpTransition(): EnterTransition {
    return slideInVertically(
        initialOffsetY = { it },
        animationSpec = tween(150, easing = LinearEasing)
    ) + fadeIn(animationSpec = tween(100))
}

fun slideDownTransition(): ExitTransition {
    return slideOutVertically(
        targetOffsetY = { it },
        animationSpec = tween(150, easing = LinearEasing)
    ) + fadeOut(animationSpec = tween(100))
}

// Scale transition for detail screens - subtle and fast
fun scaleInTransition(): EnterTransition {
    return scaleIn(
        initialScale = 0.97f,
        animationSpec = tween(100, easing = LinearEasing)
    ) + fadeIn(animationSpec = tween(100))
}

fun scaleOutTransition(): ExitTransition {
    return scaleOut(
        targetScale = 0.97f,
        animationSpec = tween(100, easing = LinearEasing)
    ) + fadeOut(animationSpec = tween(100))
}
