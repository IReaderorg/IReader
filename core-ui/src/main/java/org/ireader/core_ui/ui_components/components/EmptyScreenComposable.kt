package org.ireader.presentation.presentation

import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import org.ireader.core.utils.UiText
import org.ireader.core_ui.ui.EmptyScreen
import org.ireader.core_ui.ui_components.reusable_composable.TopAppBarBackButton

@Composable
fun EmptyScreenComposable(navController: NavController, errorResId: Int) {
    Scaffold(
        topBar = {
            Toolbar(
                title = {},
                navigationIcon = { TopAppBarBackButton(navController = navController) },
            )
        }
    ) { padding ->
        EmptyScreen(text = UiText.StringResource(errorResId))
    }
}