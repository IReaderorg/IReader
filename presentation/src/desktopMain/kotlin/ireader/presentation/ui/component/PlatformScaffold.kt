package ireader.presentation.ui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun PlatformScaffold(modifier: Modifier,
                            topBarScrollBehavior: TopAppBarScrollBehavior,
                            snackbarHostState: SnackbarHostState,
                            topBar: @Composable (TopAppBarScrollBehavior) -> Unit,
                            bottomBar: @Composable () -> Unit,
                            startBar: @Composable () -> Unit,
                            snackbarHost: @Composable () -> Unit,
                            floatingActionButton: @Composable () -> Unit,
                            floatingActionButtonPosition: FabPosition,
                            containerColor: Color, contentColor: Color,
                            contentWindowInsets: WindowInsets,
                            content: @Composable (PaddingValues) -> Unit) {

    Scaffold(
            modifier = modifier,
            contentColor = contentColor,
            containerColor = containerColor,
            content = content,
            floatingActionButton = floatingActionButton,
            snackbarHost = snackbarHost,
            bottomBar = bottomBar,
            topBar = {
                topBar(topBarScrollBehavior)
            },
            contentWindowInsets = contentWindowInsets,

            )
}