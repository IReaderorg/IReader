package org.ireader.presentation.presentation

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.ireader.core.utils.UiText
import org.ireader.core_ui.ui.EmptyScreen
import org.ireader.presentation.presentation.reusable_composable.TopAppBarBackButton

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