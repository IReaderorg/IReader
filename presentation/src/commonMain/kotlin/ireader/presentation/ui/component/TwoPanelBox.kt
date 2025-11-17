package ireader.presentation.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * TwoPanelBox for responsive tablet layouts following Mihon's patterns.
 * Automatically switches between single and dual-panel layouts based on screen size.
 * Uses IReader's isTableUi() function for proper tablet detection.
 */
@Composable
fun TwoPanelBox(
    modifier: Modifier = Modifier,
    startContent: @Composable () -> Unit,
    endContent: @Composable () -> Unit,
) {
    val isExpandedWidth = isTableUi()
    
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

/**
 * TwoPanelBox with custom weight distribution
 */
@Composable
fun TwoPanelBoxWithWeights(
    modifier: Modifier = Modifier,
    startWeight: Float = 0.4f,
    endWeight: Float = 0.6f,
    startContent: @Composable () -> Unit,
    endContent: @Composable () -> Unit,
) {
    val isExpandedWidth = isTableUi()
    
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

/**
 * TwoPanelBox that always shows dual panels (for specific use cases)
 */
@Composable
fun ForcedTwoPanelBox(
    modifier: Modifier = Modifier,
    startWeight: Float = 0.4f,
    endWeight: Float = 0.6f,
    startContent: @Composable () -> Unit,
    endContent: @Composable () -> Unit,
) {
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
}