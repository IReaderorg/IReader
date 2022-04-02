package org.ireader.presentation.feature_library.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ireader.domain.models.layouts
import org.ireader.presentation.feature_library.presentation.viewmodel.LibraryEvents
import org.ireader.presentation.feature_library.presentation.viewmodel.LibraryViewModel
import org.ireader.presentation.feature_sources.presentation.extension.composables.TextSection


@Composable
fun DisplayScreen(viewModel: LibraryViewModel) {
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
                selected = viewModel.layout == layout.layout,
                onClick = {
                    viewModel.onEvent(LibraryEvents.OnLayoutTypeChange(layout))
                }
            )
        }
//        Spacer(modifier = Modifier.height(8.dp))
//        TextSection(text = "BADGES")
//        Spacer(modifier = Modifier.height(8.dp))
//        TextSection(text = "TABS")
//        Spacer(modifier = Modifier.height(8.dp))
    }
}
