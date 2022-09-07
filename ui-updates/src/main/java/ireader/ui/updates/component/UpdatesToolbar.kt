

package ireader.ui.updates.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FlipToBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ireader.ui.component.components.Toolbar
import ireader.ui.component.reusable_composable.AppIcon
import ireader.ui.component.reusable_composable.BigSizeTextComposable
import ireader.ui.updates.R
import ireader.ui.updates.viewmodel.UpdateState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatesToolbar(
    state: UpdateState,
    onClickCancelSelection: () -> Unit,
    onClickSelectAll: () -> Unit,
    onClickFlipSelection: () -> Unit,
    onClickRefresh: () -> Unit,
    onClickDelete: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        when {
            state.hasSelection -> {
                UpdatesSelectionToolbar(
                    selectionSize = state.selection.size,
                    onClickCancelSelection = onClickCancelSelection,
                    onClickSelectAll = onClickSelectAll,
                    onClickInvertSelection = onClickFlipSelection,
                    scrollBehavior = scrollBehavior
                )
            }
            else -> {
                UpdatesRegularToolbar(
                    onClickRefresh = onClickRefresh,
                    onClickDelete = onClickDelete,
                    scrollBehavior = scrollBehavior
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UpdatesSelectionToolbar(
    selectionSize: Int,
    onClickCancelSelection: () -> Unit,
    onClickSelectAll: () -> Unit,
    onClickInvertSelection: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    Toolbar(
        title = { BigSizeTextComposable(text = "$selectionSize") },
        navigationIcon = {
            IconButton(onClick = onClickCancelSelection) {
                Icon(Icons.Default.Close, contentDescription = null)
            }
        },
        actions = {
            IconButton(onClick = onClickSelectAll) {
                AppIcon(imageVector = Icons.Default.SelectAll, contentDescription = null)
            }
            IconButton(onClick = onClickInvertSelection) {
                AppIcon(imageVector = Icons.Default.FlipToBack, contentDescription = null)
            }
        },
        scrollBehavior = scrollBehavior
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatesRegularToolbar(
    onClickRefresh: () -> Unit,
    onClickDelete: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    Toolbar(
        title = { BigSizeTextComposable(text = stringResource(R.string.updates_screen_label)) },
        actions = {
            IconButton(onClick = onClickRefresh) {
                AppIcon(imageVector = Icons.Default.Refresh, contentDescription = null)
            }
            IconButton(onClick = onClickDelete) {
                AppIcon(imageVector = Icons.Default.DeleteForever, contentDescription = null)
            }
        },
        scrollBehavior = scrollBehavior
    )
}
