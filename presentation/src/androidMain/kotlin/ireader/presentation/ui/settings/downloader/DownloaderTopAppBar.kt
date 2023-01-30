package ireader.presentation.ui.settings.downloader

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FlipToBack
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import ireader.i18n.UiText
import ireader.presentation.ui.core.utils.asString
import ireader.presentation.R
import ireader.presentation.ui.component.components.Toolbar
import ireader.presentation.ui.component.reusable_composable.BigSizeTextComposable
import ireader.presentation.ui.component.reusable_composable.BuildDropDownMenu
import ireader.presentation.ui.component.reusable_composable.DropDownMenuItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloaderTopAppBar(
    state: DownloadState,
    onPopBackStack: () -> Unit,
    onCancelAll: () -> Unit,
    onMenuIcon: () -> Unit,
    onDeleteAllDownload: () -> Unit,
    onDelete: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior?
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        when {
            state.hasSelection -> {
                EditModeTopAppBar(
                    selectionSize = state.selection.size,
                    onClickCancelSelection = { state.selection.clear() },
                    onClickSelectAll = {
                        state.selection.clear()
                        state.selection.addAll(state.downloads.map { it.chapterId })
                        state.selection.distinct()
                    },
                    onClickInvertSelection = {
                        val ids: List<Long> =
                            state.downloads.map { it.chapterId }
                                .filterNot { it in state.selection }.distinct()
                        state.selection.clear()
                        state.selection.addAll(ids)
                    },
                    onDelete = onDelete,
                    scrollBehavior = scrollBehavior,
                )
            }
            else -> {
                RegularTopBar(
                    onPopBackStack = onPopBackStack,
                    onCancelAll = onCancelAll,
                    scrollBehavior = scrollBehavior,

                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun RegularTopBar(
    onPopBackStack: () -> Unit,
    onCancelAll: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior?
) {
    var isMenuExpanded by remember {
        mutableStateOf(false)
    }
    Toolbar(
        scrollBehavior = scrollBehavior,
        title = {
            Text(
                text = UiText.StringResource(R.string.downloads_queue).asString(),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis
            )
        },
        actions = {
            val list =
                listOf<DropDownMenuItem>(
                    DropDownMenuItem(
                        stringResource(R.string.cancel_all)
                    ) {
                        onCancelAll()
                    }
                )
            BuildDropDownMenu(list)
        },
        navigationIcon = {
            IconButton(onClick = onPopBackStack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(R.string.return_to_previous_screen),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditModeTopAppBar(
    selectionSize: Int,
    onClickCancelSelection: () -> Unit,
    onClickSelectAll: () -> Unit,
    onClickInvertSelection: () -> Unit,
    onDelete: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior?
) {
    Toolbar(
        scrollBehavior = scrollBehavior,
        title = { BigSizeTextComposable(text = selectionSize.toString()) },
        navigationIcon = {
            IconButton(onClick = onClickCancelSelection) {
                Icon(Icons.Default.Close, contentDescription = null)
            }
        },
        actions = {
            IconButton(onClick = onClickSelectAll) {
                Icon(Icons.Default.SelectAll, contentDescription = null)
            }
            IconButton(onClick = onClickInvertSelection) {
                Icon(Icons.Default.FlipToBack, contentDescription = null)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteForever, contentDescription = null)
            }
        }
    )
}
