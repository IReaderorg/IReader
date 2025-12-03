package ireader.presentation.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ireader.presentation.core.ui.TwoPanelBox as CoreTwoPanelBox
import ireader.presentation.core.ui.TwoPanelBoxWithWeights as CoreTwoPanelBoxWithWeights

@Composable
fun TwoPanelBox(
    modifier: Modifier = Modifier,
    startContent: @Composable () -> Unit,
    endContent: @Composable () -> Unit,
) {
    CoreTwoPanelBox(
        modifier = modifier,
        isExpandedWidth = isTableUi(),
        startContent = startContent,
        endContent = endContent
    )
}

@Composable
fun TwoPanelBoxWithWeights(
    modifier: Modifier = Modifier,
    startWeight: Float = 0.4f,
    endWeight: Float = 0.6f,
    startContent: @Composable () -> Unit,
    endContent: @Composable () -> Unit,
) {
    CoreTwoPanelBoxWithWeights(
        modifier = modifier,
        isExpandedWidth = isTableUi(),
        startWeight = startWeight,
        endWeight = endWeight,
        startContent = startContent,
        endContent = endContent
    )
}
