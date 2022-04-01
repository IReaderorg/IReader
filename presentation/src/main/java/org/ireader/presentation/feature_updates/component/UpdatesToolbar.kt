/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.ireader.presentation.feature_updates.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlipToBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.ireader.presentation.R
import org.ireader.presentation.feature_updates.viewmodel.UpdateState
import org.ireader.presentation.presentation.Toolbar
import org.ireader.presentation.presentation.reusable_composable.TopAppBarTitle

@Composable
fun UpdatesToolbar(
  state: UpdateState,
  onClickCancelSelection: () -> Unit,
  onClickSelectAll: () -> Unit,
  onClickFlipSelection: () -> Unit,
  onClickRefresh: () -> Unit,
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
                    onClickRefresh = onClickRefresh
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
        title = { TopAppBarTitle(title = "$selectionSize") },
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
fun UpdatesRegularToolbar(onClickRefresh: () -> Unit) {
  Toolbar(
    title = { TopAppBarTitle(title = stringResource(id = R.string.updates_screen_label)) },
    actions = {
      IconButton(onClick = onClickRefresh) {
        Icon(Icons.Default.Refresh, contentDescription = null)
      }
    }
  )
}
