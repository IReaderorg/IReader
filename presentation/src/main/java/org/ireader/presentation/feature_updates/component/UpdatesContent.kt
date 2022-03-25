/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.ireader.presentation.feature_updates.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import org.ireader.domain.models.entities.Update
import org.ireader.presentation.feature_sources.presentation.extension.composables.TextSection
import org.ireader.presentation.feature_updates.viewmodel.UpdateState


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UpdatesContent(
    state: UpdateState,
    onClickItem: (Update) -> Unit,
    onLongClickItem: (Update) -> Unit,
    onClickCover: (Update) -> Unit,
    onClickDownload: (Update) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(
            bottom = 16.dp,
            top = 8.dp
        )
    ) {
        state.updates.forEach { (date, updates) ->
            item {
                TextSection(
                    text = date
                )
            }
            items(
                count = updates.size,
            ) { index ->
                UpdatesItem(
                    book = updates[index],
                    isSelected = updates[index].chapterId in state.selection,
                    onClickItem = onClickItem,
                    onLongClickItem = onLongClickItem,
                    onClickCover = onClickCover,
                    onClickDownload = onClickDownload
                )
            }
        }
    }
}
