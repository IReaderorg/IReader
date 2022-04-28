/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.ireader.updates.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.ireader.core_ui.ui_components.reusable_composable.BigSizeTextComposable
import org.ireader.presentation.presentation.Toolbar
import org.ireader.ui_updates.R
import org.ireader.updates.viewmodel.UpdateState

@Composable
fun UpdatesToolbar(
    state: UpdateState,
    onClickCancelSelection: () -> Unit,
    onClickSelectAll: () -> Unit,
    onClickFlipSelection: () -> Unit,
    onClickRefresh: () -> Unit,
    onClickDelete: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        when {
            state.hasSelection -> {
                UpdatesSelectionToolbar(
                    selectionSize = state.selection.size,
                    onClickCancelSelection = onClickCancelSelection,
                    onClickSelectAll = onClickSelectAll,
                    onClickInvertSelection = onClickFlipSelection
                )
            }
            else -> {
                UpdatesRegularToolbar(
                    onClickRefresh = onClickRefresh,
                    onClickDelete = onClickDelete
                )
            }
        }
    }

}

@Composable
private fun UpdatesSelectionToolbar(
    selectionSize: Int,
    onClickCancelSelection: () -> Unit,
    onClickSelectAll: () -> Unit,
    onClickInvertSelection: () -> Unit,
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
      }
  )
}

@Composable
fun UpdatesRegularToolbar(
    onClickRefresh: () -> Unit,
    onClickDelete: () -> Unit,
) {
    Toolbar(
        title = { BigSizeTextComposable(text = stringResource(id = R.string.updates_screen_label)) },
        actions = {
            IconButton(onClick = onClickRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = null)
            }
            IconButton(onClick = onClickDelete) {
                Icon(Icons.Default.DeleteForever, contentDescription = null)
            }

        }
    )
}
