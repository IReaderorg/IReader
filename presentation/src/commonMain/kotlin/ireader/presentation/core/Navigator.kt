package ireader.presentation.core

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition


abstract class VoyagerScreen : Screen {


}


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DefaultNavigatorScreenTransition(navigator: Navigator) {
    SlideTransition(navigator, animationSpec = spring(
            stiffness = Spring.StiffnessMedium,
            visibilityThreshold = IntOffset.VisibilityThreshold
    ),)
}
