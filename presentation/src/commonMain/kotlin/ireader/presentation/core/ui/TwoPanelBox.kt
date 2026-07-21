package ireader.presentation.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun TwoPanelBox(
    modifier: Modifier = Modifier,
    isExpandedWidth: Boolean = false,
    startContent: @Composable () -> Unit,
    endContent: @Composable () -> Unit,
) {
    if (isExpandedWidth) {
        Row(modifier = modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight()
            ) {
                startContent()
            }
            Box(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxHeight()
            ) {
                endContent()
            }
        }
    } else {
        Box(modifier = modifier.fillMaxSize()) {
            endContent()
        }
    }
}

@Composable
fun TwoPanelBoxWithWeights(
    modifier: Modifier = Modifier,
    isExpandedWidth: Boolean = false,
    startWeight: Float = 0.4f,
    endWeight: Float = 0.6f,
    startContent: @Composable () -> Unit,
    endContent: @Composable () -> Unit,
) {
    if (isExpandedWidth) {
        Row(modifier = modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(startWeight)
                    .fillMaxHeight()
            ) {
                startContent()
            }
            Box(
                modifier = Modifier
                    .weight(endWeight)
                    .fillMaxHeight()
            ) {
                endContent()
            }
        }
    } else {
        Box(modifier = modifier.fillMaxSize()) {
            endContent()
        }
    }
}

@Composable
fun TwoPanelBoxStandalone(
    modifier: Modifier = Modifier,
    isExpandedWidth: Boolean = false,
    startContent: @Composable () -> Unit,
    endContent: @Composable () -> Unit,
) {
    TwoPanelBox(
        modifier = modifier,
        isExpandedWidth = isExpandedWidth,
        startContent = startContent,
        endContent = endContent
    )
}
