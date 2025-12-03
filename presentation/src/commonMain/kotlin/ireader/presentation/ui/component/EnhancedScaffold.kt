package ireader.presentation.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll

/**
 * Enhanced IReader Scaffold following Mihon's Material Design 3 patterns.
 * Provides consistent scaffold with responsive design and proper scroll behavior.
 * 
 * This is an enhanced version that uses the new presentation-core components
 * and provides better tablet support with TwoPanelBox integration.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedIReaderScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable (TopAppBarScrollBehavior) -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    startBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = { SnackbarHost(remember { SnackbarHostState() }) },
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = contentColorFor(containerColor),
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    content: @Composable (PaddingValues) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { topBar(scrollBehavior) },
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        containerColor = containerColor,
        contentColor = contentColor,
        contentWindowInsets = contentWindowInsets,
        content = { paddingValues ->
            Row(modifier = Modifier.fillMaxSize()) {
                startBar()
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(containerColor),
                ) {
                    content(paddingValues)
                }
            }
        }
    )
}

/**
 * Two-panel responsive scaffold for tablet layouts using TwoPanelBox
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwoPanelScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable (TopAppBarScrollBehavior) -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = { SnackbarHost(remember { SnackbarHostState() }) },
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = contentColorFor(containerColor),
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    startContent: @Composable (PaddingValues) -> Unit,
    endContent: @Composable (PaddingValues) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { topBar(scrollBehavior) },
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        containerColor = containerColor,
        contentColor = contentColor,
        contentWindowInsets = contentWindowInsets,
        content = { paddingValues ->
            TwoPanelBox(
                modifier = Modifier.fillMaxSize(),
                startContent = { startContent(paddingValues) },
                endContent = { endContent(paddingValues) }
            )
        }
    )
}
