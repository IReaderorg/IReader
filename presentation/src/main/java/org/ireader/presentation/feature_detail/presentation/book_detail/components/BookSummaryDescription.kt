package org.ireader.presentation.feature_detail.presentation.book_detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.unit.dp


private const val COLLAPSED_MAX_LINES = 3

@Composable
fun BookSummaryDescription(
    description: String,
    isExpandable: Boolean?,
    setIsExpandable: (Boolean) -> Unit,
    isExpanded: Boolean,
    onClickToggle: () -> Unit,
) {
    Layout(
        modifier = Modifier.clickable(enabled = isExpandable == true, onClick = onClickToggle),
        measurePolicy = { measurables, constraints ->
            val textPlaceable = measurables.first { it.layoutId == "text" }.measure(constraints)

            if (isExpandable != true) {
                layout(constraints.maxWidth, textPlaceable.height) {
                    textPlaceable.placeRelative(0, 0)
                }
            } else {
                val iconPlaceable = measurables.first { it.layoutId == "icon" }.measure(constraints)

                val layoutHeight = textPlaceable.height +
                        if (isExpanded) iconPlaceable.height else iconPlaceable.height / 2

                val scrimPlaceable = measurables.find { it.layoutId == "scrim" }
                    ?.measure(constraints.copy(maxHeight = layoutHeight / 2))

                layout(constraints.maxWidth, layoutHeight) {
                    textPlaceable.placeRelative(0, 0)
                    scrimPlaceable?.placeRelative(0, layoutHeight - scrimPlaceable.height)
                    iconPlaceable.placeRelative(
                        x = constraints.maxWidth / 2 - iconPlaceable.width / 2,
                        y = layoutHeight - iconPlaceable.height
                    )
                }
            }
        },
        content = {
            Text(
                text = description,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .layoutId("text"),
                maxLines = if (!isExpanded) COLLAPSED_MAX_LINES else Int.MAX_VALUE,
                onTextLayout = { result ->
                    if (isExpandable == null) setIsExpandable(result.didOverflowHeight)
                },
            )
            if (isExpandable == true) {
                if (!isExpanded) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    0f to Color.Transparent,
                                    0.4f to MaterialTheme.colors.background.copy(alpha = 0.9f),
                                    0.5f to MaterialTheme.colors.background
                                )
                            )
                            .layoutId("scrim")
                    )
                }
                IconButton(
                    onClick = onClickToggle,
                    modifier = Modifier.layoutId("icon")
                ) {
                    Icon(if (!isExpanded) Icons.Outlined.ExpandMore else Icons.Outlined.ExpandLess,
                        null)
                }
            }
        }
    )
}