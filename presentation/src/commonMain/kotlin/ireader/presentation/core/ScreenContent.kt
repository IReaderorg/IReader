package ireader.presentation.core

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import ireader.i18n.*

@Composable
expect fun PlatformModalSheets(
    modifier: Modifier,
    state: Any,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = MaterialTheme.colorScheme.onBackground,
    sheetContent: @Composable (modifier: Modifier) -> Unit,
    content: @Composable () -> Unit,
)

@Composable
fun IModalSheets(
    modifier: Modifier = Modifier,
    bottomSheetState: Any,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = MaterialTheme.colorScheme.onBackground,
    sheetContent: @Composable (modifier: Modifier) -> Unit,
    content: @Composable () -> Unit,
) = PlatformModalSheets(
    modifier, bottomSheetState,backgroundColor,contentColor, sheetContent, content
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IModalDrawer(
    modifier: Modifier = Modifier,
    state: DrawerState,
    sheetContent: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    ModalNavigationDrawer(
        modifier = Modifier,
        drawerState = state,
        drawerContent = {
            DismissibleDrawerSheet(
                drawerContentColor = MaterialTheme.colorScheme.onSurface,
                drawerContainerColor = MaterialTheme.colorScheme.surface,

                ) {
                sheetContent()
            }
        },
        scrimColor = Color.Transparent,
        content = content,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IBottomAppBar(
    modifier: Modifier = Modifier,
    sheetContent: @Composable () -> Unit,
) {
    BottomAppBar(
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        content = {
            sheetContent()
        }
    )
}
