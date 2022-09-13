package ireader.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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