package ir.kazemcodes.infinity.feature_library.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ir.kazemcodes.infinity.core.presentation.layouts.layouts
import ir.kazemcodes.infinity.feature_library.presentation.LibraryViewModel


@Composable
fun DisplayScreen(viewModel: LibraryViewModel) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        layouts.forEach { layout ->
            RadioButtonWithTitleComposable(
                text = layout.title,
                selected = viewModel.state.value.layout == layout.layout,
                onClick = {
                    viewModel.onEvent(LibraryEvents.UpdateLayoutType(layout))
                }
            )
        }
    }
}
