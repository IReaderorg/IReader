package org.ireader.components.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.ireader.components.reusable_composable.TopAppBarBackButton
import org.ireader.core_ui.ui.EmptyScreen

@Composable
fun EmptyScreenComposable(errorResId: Int,onPopBackStack :() -> Unit) {
    Scaffold(
        topBar = {
            Toolbar(
                title = {},
                navigationIcon = { TopAppBarBackButton(onClick = onPopBackStack) },
            )
        }
    ) { padding ->
        EmptyScreen(text = org.ireader.common_extensions.UiText.StringResource(errorResId), modifier = Modifier.padding(padding))
    }
}
