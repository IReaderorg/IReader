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
 * - Smooth fade and slide animations
 * - Consistent behavior across light and dark themes
 * - Optimized transition timing for better UX
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
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        durationMillis = 200,
                        easing = FastOutSlowInEasing
                    )
                ) + slideInHorizontally(
                    initialOffsetX = { it / 10 },
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = FastOutSlowInEasing
                    )
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        durationMillis = 200,
                        easing = FastOutSlowInEasing
                    )
                )
            },
            popEnterTransition = {
                fadeIn(
                    animationSpec = tween(
                        durationMillis = 200,
                        easing = FastOutSlowInEasing
                    )
                )
            },
            popExitTransition = {
                fadeOut(
                    animationSpec = tween(
                        durationMillis = 200,
                        easing = FastOutSlowInEasing
                    )
                ) + slideOutHorizontally(
                    targetOffsetX = { it / 10 },
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = FastOutSlowInEasing
                    )
                )
            },
            builder = builder
        )
    }
}

/**
 * Custom transition extensions for different navigation scenarios
 */

// Fade transition for dialogs and overlays
fun fadeTransition(durationMillis: Int = 200): EnterTransition {
    return fadeIn(animationSpec = tween(durationMillis))
}

fun fadeOutTransition(durationMillis: Int = 200): ExitTransition {
    return fadeOut(animationSpec = tween(durationMillis))
}

// Slide from bottom (for bottom sheets style)
fun slideUpTransition(): EnterTransition {
    return slideInVertically(
        initialOffsetY = { it },
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    ) + fadeIn(animationSpec = tween(200))
}

fun slideDownTransition(): ExitTransition {
    return slideOutVertically(
        targetOffsetY = { it },
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    ) + fadeOut(animationSpec = tween(200))
}

// Scale transition for detail screens
fun scaleInTransition(): EnterTransition {
    return scaleIn(
        initialScale = 0.95f,
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    ) + fadeIn(animationSpec = tween(200))
}

fun scaleOutTransition(): ExitTransition {
    return scaleOut(
        targetScale = 0.95f,
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    ) + fadeOut(animationSpec = tween(200))
}
