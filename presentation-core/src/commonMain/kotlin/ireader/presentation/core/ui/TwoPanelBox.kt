package ireader.presentation.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * TwoPanelBox for responsive tablet layouts following Mihon's patterns.
 * Automatically switches between single and dual-panel layouts based on screen size.
 * 
 * Note: This component needs to be used within the presentation module context
 * where isTableUi() is available. For standalone usage, use TwoPanelBoxStandalone.
 */
@Composable
fun TwoPanelBox(
    modifier: Modifier = Modifier,
    startContent: @Composable () -> Unit,
    endContent: @Composable () -> Unit,
) {
    // This will be properly implemented when used in the presentation module
    // For now, default to single panel
    Box(modifier = modifier.fillMaxSize()) {
        endContent()
    }
}

/**
 * Standalone TwoPanelBox that doesn't depend on presentation module utilities
 */
@Composable
fun TwoPanelBoxStandalone(
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

/**
 * TwoPanelBox with custom weight distribution
 */
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