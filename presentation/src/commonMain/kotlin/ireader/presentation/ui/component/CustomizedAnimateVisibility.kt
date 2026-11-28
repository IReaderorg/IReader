package ireader.presentation.ui.component

import ireader.presentation.ui.component.animations.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import ireader.presentation.ui.component.animations.slideInVertically
import ireader.presentation.ui.component.animations.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun CustomizeAnimateVisibility(
    modifier: Modifier = Modifier,
    visible:Boolean,
    goUp :Boolean = true,
    content: @Composable() AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        modifier =  modifier,
        visible = visible,
        enter = slideInVertically(initialOffsetY = { if(goUp) -it else it }),
        exit = slideOutVertically(targetOffsetY = { if(goUp) -it else it }),
        content =content,
    )
}
