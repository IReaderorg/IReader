package ireader.presentation.core.ui

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.NavigationRoutes
import ireader.presentation.core.safePopBackStack
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.settings.downloader.DownloaderScreen
import ireader.presentation.ui.settings.downloader.DownloaderViewModel

object DownloaderScreenSpec {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val vm: DownloaderViewModel = getIViewModel()
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val scrollState = rememberLazyListState()
        
        // Collect state for top bar
        val stats by vm.stats.collectAsState()
        val hasSelection = vm.hasSelection
        val selectionCount = vm.selection.size
        
        IScaffold(
            topBar = { scrollBehavior ->
                DownloaderTopBar(
                    hasSelection = hasSelection,
                    selectionCount = selectionCount,
                    onBack = { navController.safePopBackStack() },
                    onSelectAll = { vm.selectAll() },
                    onClearSelection = { vm.clearSelection() },
                    onDeleteSelected = { vm.removeSelectedDownloads() },
                    scrollBehavior = scrollBehavior
                )
            }
        ) { padding ->
            DownloaderScreen(
                vm = vm,
                onNavigateToBook = { bookId ->
                    navController.navigate(NavigationRoutes.bookDetail(bookId))
                },
                padding = padding,
                scrollState = scrollState
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DownloaderTopBar(
    hasSelection: Boolean,
    selectionCount: Int,
    onBack: () -> Unit,
    onSelectAll: () -> Unit,
    onClearSelection: () -> Unit,
    onDeleteSelected: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    TopAppBar(
        title = {
            Text(
                text = if (hasSelection) {
                    "$selectionCount selected"
                } else {
                    "Downloads"
                }
            )
        },
        navigationIcon = {
            IconButton(onClick = {
                if (hasSelection) {
                    onClearSelection()
                } else {
                    onBack()
                }
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            if (hasSelection) {
                IconButton(onClick = onSelectAll) {
                    Icon(
                        imageVector = Icons.Default.SelectAll,
                        contentDescription = "Select All"
                    )
                }
                IconButton(onClick = onDeleteSelected) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Selected"
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors()
    )
}
