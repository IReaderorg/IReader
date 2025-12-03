package ireader.presentation.core

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

/**
 * Animated NavHost with beautiful transitions for both Android and Desktop platforms.
 * 
 * Provides smooth slide-in animations combined with fade for polished navigation.
 * Enhanced with persistent background to eliminate white flashes.
 * 
 * Optimized animation specs are remembered to prevent recreation on each recomposition.
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
    // Remember animation specs to prevent recreation on each recomposition
    val slideAnimationSpec: FiniteAnimationSpec<IntOffset> = remember {
        tween(
            durationMillis = 250,
            easing = FastOutSlowInEasing
        )
    }
    val fadeAnimationSpec: FiniteAnimationSpec<Float> = remember {
        tween(
            durationMillis = 250,
            easing = FastOutSlowInEasing
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
                // Slide in from right + fade in (reduced offset for smoother feel)
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth / 6 },
                    animationSpec = slideAnimationSpec
                ) + fadeIn(animationSpec = fadeAnimationSpec)
            },
            exitTransition = {
                // Slide out to left + fade out
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth / 6 },
                    animationSpec = slideAnimationSpec
                ) + fadeOut(animationSpec = fadeAnimationSpec)
            },
            popEnterTransition = {
                // Slide in from left + fade in (going back)
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> -fullWidth / 6 },
                    animationSpec = slideAnimationSpec
                ) + fadeIn(animationSpec = fadeAnimationSpec)
            },
            popExitTransition = {
                // Slide out to right + fade out (going back)
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth / 6 },
                    animationSpec = slideAnimationSpec
                ) + fadeOut(animationSpec = fadeAnimationSpec)
            },
            builder = builder
        )
    }
}
