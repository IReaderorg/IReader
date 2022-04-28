package org.ireader.downloader

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import org.ireader.core_ui.ui_components.reusable_composable.AppIconButton
import org.ireader.core_ui.ui_components.reusable_composable.BigSizeTextComposable
import org.ireader.core_ui.ui_components.reusable_composable.BuildDropDownMenu
import org.ireader.core_ui.ui_components.reusable_composable.DropDownMenuItem
import org.ireader.presentation.presentation.Toolbar
import org.ireader.ui_downloader.R


@Composable
fun DownloaderTopAppBar(
    state: DownloadState,
    onPopBackStack: () -> Unit,
    onCancelAll: () -> Unit,
    onMenuIcon: () -> Unit,
    onDeleteAllDownload: () -> Unit,
    onDelete: () -> Unit,
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
                    onDelete = onDelete)
            }
            else -> {
                RegularTopBar(
                    onPopBackStack=onPopBackStack,
                    onCancelAll = onCancelAll,

                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun RegularTopBar(
    onPopBackStack: () -> Unit,
    onCancelAll: () -> Unit,
) {
    var isMenuExpanded by remember {
        mutableStateOf(false)
    }
    Toolbar(
        title = {
            Text(
                text = "Downloads queue",
                color = MaterialTheme.colors.onBackground,
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis
            )
        },
        actions = {
            AppIconButton(
                imageVector = Icons.Default.MoreVert,
                title = "Menu Icon",
                onClick = {
                    isMenuExpanded = true
                },
            )
            val list =
                listOf<DropDownMenuItem>(
                    DropDownMenuItem(
                        stringResource(R.string.cancel_all)
                    ) {
                        onCancelAll()
                    })
            BuildDropDownMenu(list, enable = isMenuExpanded, onEnable = { isMenuExpanded = it })
        },
        navigationIcon = {
            IconButton(onClick = onPopBackStack ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "ArrowBack Icon",
                    tint = MaterialTheme.colors.onBackground,
                )
            }
        }
    )
}

@Composable
private fun EditModeTopAppBar(
    selectionSize: Int,
    onClickCancelSelection: () -> Unit,
    onClickSelectAll: () -> Unit,
    onClickInvertSelection: () -> Unit,
    onDelete: () -> Unit
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