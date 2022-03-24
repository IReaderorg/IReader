/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.ireader.presentation.feature_updates.component


//@OptIn(ExperimentalFoundationApi::class)
//@Composable
//fun UpdatesContent(
//  state: UpdateState,
//  onClickItem: (Update) -> Unit,
//  onLongClickItem: (Update) -> Unit,
//  onClickCover: (Update) -> Unit,
//  onClickDownload: (Update) -> Unit
//) {
//  LazyColumn(
//    contentPadding = PaddingValues(
//      bottom = 16.dp,
//      top = 8.dp
//    )
//  ) {
//    state.updates.forEach { update ->
//      stickyHeader {
//        RelativeTimeText(
//          date = update.date,
//          modifier = Modifier
//            .background(MaterialTheme.colors.background)
//            .padding(horizontal = 16.dp, vertical = 4.dp)
//            .fillMaxWidth()
//        )
//      }
//
//      items(update) { book ->
//        UpdatesItem(
//          book = book,
//          isSelected = update.chapterId in state.selection,
//          onClickItem = onClickItem,
//          onLongClickItem = onLongClickItem,
//          onClickCover = onClickCover,
//          onClickDownload = onClickDownload
//        )
//      }
//    }
//  }
//}
