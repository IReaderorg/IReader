package org.ireader.library.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ireader.common_models.DisplayMode
import org.ireader.common_models.LayoutType
import org.ireader.common_models.layouts
import org.ireader.core_ui.ui_components.RadioButtonWithTitleComposable
import org.ireader.core_ui.ui_components.TextSection


@Composable
fun DisplayScreen(
    layoutType: LayoutType,
    onLayoutSelected: (DisplayMode) -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        TextSection(
            text = "DISPLAY MODE",
            padding = PaddingValues(vertical = 8.dp, horizontal = 16.dp),
            style = MaterialTheme.typography.subtitle1,
        )
        layouts.forEach { layout ->
            RadioButtonWithTitleComposable(
                text = layout.title,
                selected = layoutType == layout.layout,
                onClick = {
                    onLayoutSelected(layout)
                }
            )
        }
    }
}
