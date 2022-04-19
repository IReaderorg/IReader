package org.ireader.presentation.feature_updates

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.GetApp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ireader.core.utils.UiText
import org.ireader.core_ui.ui.EmptyScreen
import org.ireader.core_ui.ui.LoadingScreen
import org.ireader.domain.models.entities.UpdateWithInfo
import org.ireader.presentation.feature_updates.component.UpdatesContent
import org.ireader.presentation.feature_updates.component.UpdatesToolbar
import org.ireader.presentation.feature_updates.viewmodel.UpdateState
import org.ireader.presentation.presentation.reusable_composable.AppIconButton

@Composable
fun UpdateScreen(
    state: UpdateState,
    onAppbarCancelSelection:() -> Unit,
    onAppbarSelectAll:() -> Unit,
    onAppbarFilipSelection:() -> Unit,
    onAppbarRefresh:() -> Unit,
    onAppbarDeleteAll:() -> Unit,
    onUpdate:(UpdateWithInfo) -> Unit,
    onLongUpdate:(UpdateWithInfo) -> Unit,
    onCoverUpdate:(UpdateWithInfo) -> Unit,
    onDownloadUpdate:(UpdateWithInfo) -> Unit,
    onBottomBarDownload:() -> Unit,
    onBottomBarMarkAsRead:() -> Unit,
    onBottomBarDelete:() -> Unit,
    onBottomBookMark:() -> Unit,
) {
    Scaffold(
        topBar = {
            UpdatesToolbar(
                state = state,
                onClickCancelSelection = onAppbarCancelSelection,
                onClickSelectAll = onAppbarSelectAll,
                onClickFlipSelection = onAppbarFilipSelection,
                onClickRefresh = onAppbarRefresh,
                onClickDelete = onAppbarDeleteAll
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            Crossfade(targetState = Pair(state.isLoading, state.isEmpty)) { (isLoading, isEmpty) ->
                when {
                    isLoading -> LoadingScreen()
                    isEmpty -> EmptyScreen(UiText.DynamicString("No New Updates is Available."))
                    else -> UpdatesContent(
                        state = state,
                        onClickItem = onUpdate,
                        onLongClickItem = onLongUpdate,
                        onClickCover = onCoverUpdate,
                        onClickDownload = onDownloadUpdate
                    )
                }
                when {
                    state.hasSelection -> {
                        UpdateEditBar(
                            state,
                            onBottomBarDownload,
                            onBottomBarMarkAsRead,
                            onBottomBarDelete,
                            onBottomBookMark
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxScope.UpdateEditBar(
    state: UpdateState,
    onBottomBarDownload:() -> Unit,
    onBottomBarMarkAsRead:() -> Unit,
    onBottomBarDelete:() -> Unit,
    onBottomBookMark:() -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .align(Alignment.BottomCenter)
            .padding(8.dp)
            .background(MaterialTheme.colors.background)
            .border(width = 1.dp,
                color = MaterialTheme.colors.onBackground.copy(.1f))
            .clickable(enabled = false) {},
    ) {
        Row(modifier = Modifier
            .fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (state.selection.any { selectionId ->
                    selectionId in state.updates.values.flatten().filter { !it.downloaded }.map { it.id }
                }) {
                AppIconButton(imageVector = Icons.Default.GetApp,
                    title = "Download",
                    onClick = onBottomBarDownload)
            }
            AppIconButton(imageVector = Icons.Default.BookmarkBorder,
                title = "Bookmark",
                onClick = onBottomBookMark)

            AppIconButton(imageVector = Icons.Default.Done,
                title = "Mark as read",
                onClick = onBottomBarMarkAsRead)

            AppIconButton(imageVector = Icons.Default.Delete,
                title = "Delete Update",
                onClick = onBottomBarDelete)
        }
    }
}