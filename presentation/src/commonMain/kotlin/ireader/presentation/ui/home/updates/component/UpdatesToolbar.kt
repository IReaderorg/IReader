

package ireader.presentation.ui.home.updates.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import ireader.domain.models.entities.Category
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.components.Toolbar
import ireader.presentation.ui.component.reusable_composable.AppIcon
import ireader.presentation.ui.component.reusable_composable.BigSizeTextComposable
import ireader.presentation.ui.home.updates.viewmodel.UpdateState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatesToolbar(
        state: UpdateState,
        onClickCancelSelection: () -> Unit,
        onClickSelectAll: () -> Unit,
        onClickFlipSelection: () -> Unit,
        onClickRefresh: () -> Unit,
        onClickDelete: () -> Unit,
        onClickUpdateAll: (() -> Unit)? = null,
        onClickUpdateSelected: (() -> Unit)? = null,
        categories: List<Category> = emptyList(),
        onCategorySelected: (Long?) -> Unit = {},
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
                    onClickUpdateSelected = onClickUpdateSelected,
                    scrollBehavior = scrollBehavior
                )
            }
            else -> {
                UpdatesRegularToolbar(
                    onClickRefresh = onClickRefresh,
                    onClickDelete = onClickDelete,
                    onClickUpdateAll = onClickUpdateAll,
                    categories = categories,
                    selectedCategoryId = state.selectedCategoryId,
                    onCategorySelected = onCategorySelected,
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
    onClickUpdateSelected: (() -> Unit)? = null,
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
            if (onClickUpdateSelected != null) {
                IconButton(onClick = onClickUpdateSelected) {
                    AppIcon(imageVector = Icons.Default.GetApp, contentDescription = localize(Res.string.update_selected))
                }
            }
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
    onClickUpdateAll: (() -> Unit)? = null,
    categories: List<Category> = emptyList(),
    selectedCategoryId: Long? = null,
    onCategorySelected: (Long?) -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    var showCategoryMenu by remember { mutableStateOf(false) }
    
    Toolbar(
        title = { BigSizeTextComposable(text = localize(Res.string.updates_screen_label)) },
        actions = {
            if (categories.isNotEmpty()) {
                Box {
                    IconButton(onClick = { showCategoryMenu = true }) {
                        AppIcon(imageVector = Icons.Default.FilterList, contentDescription = "Filter by category")
                    }
                    DropdownMenu(
                        expanded = showCategoryMenu,
                        onDismissRequest = { showCategoryMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Categories") },
                            onClick = {
                                onCategorySelected(null)
                                showCategoryMenu = false
                            },
                            leadingIcon = {
                                if (selectedCategoryId == null) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            }
                        )
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    onCategorySelected(category.id)
                                    showCategoryMenu = false
                                },
                                leadingIcon = {
                                    if (selectedCategoryId == category.id) {
                                        Icon(Icons.Default.Check, contentDescription = null)
                                    }
                                }
                            )
                        }
                    }
                }
            }
            if (onClickUpdateAll != null) {
                IconButton(onClick = onClickUpdateAll) {
                    AppIcon(imageVector = Icons.Default.GetApp, contentDescription = localize(Res.string.update_all))
                }
            }
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
