package org.ireader.presentation.feature_settings.presentation.setting.downloader

import androidx.compose.foundation.background
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
import org.ireader.core.utils.Constants
import org.ireader.presentation.R
import org.ireader.presentation.presentation.Toolbar
import org.ireader.presentation.presentation.reusable_composable.AppIconButton
import org.ireader.presentation.presentation.reusable_composable.BigSizeTextComposable
import org.ireader.presentation.presentation.reusable_composable.MidSizeTextComposable


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
            DropdownMenu(
                modifier = Modifier.background(MaterialTheme.colors.background),
                expanded = isMenuExpanded,//viewModel.state.isMenuExpanded,
                onDismissRequest = {
                    isMenuExpanded = false
                },
            ) {
                DropdownMenuItem(onClick = {
                    isMenuExpanded = false
                    onCancelAll()
                }) {
                    MidSizeTextComposable(text = stringResource(R.string.cancel_all))
                }
            }
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
        elevation = Constants.DEFAULT_ELEVATION,
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