package org.ireader.updates

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.GetApp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.ireader.common_models.entities.UpdateWithInfo
import org.ireader.components.reusable_composable.AppIconButton
import org.ireader.core_ui.ui.EmptyScreen
import org.ireader.core_ui.ui.LoadingScreen
import org.ireader.ui_updates.R
import org.ireader.updates.component.UpdatesContent
import org.ireader.updates.viewmodel.UpdateState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateScreen(
    modifier: Modifier = Modifier,
    state: UpdateState,
    onUpdate: (UpdateWithInfo) -> Unit,
    onLongUpdate: (UpdateWithInfo) -> Unit,
    onCoverUpdate: (UpdateWithInfo) -> Unit,
    onDownloadUpdate: (UpdateWithInfo) -> Unit,
    onBottomBarDownload: () -> Unit,
    onBottomBarMarkAsRead: () -> Unit,
    onBottomBarDelete: () -> Unit,
    onBottomBookMark: () -> Unit,
) {
    Box(
       modifier =  modifier.fillMaxSize()

    ) {
        Crossfade(targetState = Pair(state.isLoading, state.isEmpty)) { (isLoading, isEmpty) ->
            when {
                isLoading -> LoadingScreen()
                isEmpty -> EmptyScreen(text = stringResource(R.string.no_new_update_available))
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

@Composable
private fun BoxScope.UpdateEditBar(
    state: UpdateState,
    onBottomBarDownload: () -> Unit,
    onBottomBarMarkAsRead: () -> Unit,
    onBottomBarDelete: () -> Unit,
    onBottomBookMark: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .align(Alignment.BottomCenter)
            .padding(8.dp)
            .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
            .border(
                width = 1.dp,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground.copy(.1f)
            )
            .clickable(enabled = false) {},
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (state.selection.any { selectionId ->
                    selectionId in state.updates.values.flatten().filter { !it.downloaded }
                        .map { it.id }
                }
            ) {
                AppIconButton(
                    imageVector = Icons.Default.GetApp,
                    contentDescription = stringResource(R.string.download),
                    onClick = onBottomBarDownload
                )
            }
            AppIconButton(
                imageVector = Icons.Default.BookmarkBorder,
                contentDescription = stringResource(R.string.bookmark),
                onClick = onBottomBookMark
            )

            AppIconButton(
                imageVector = Icons.Default.Done,
                contentDescription = stringResource(R.string.mark_as_read),
                onClick = onBottomBarMarkAsRead
            )

            AppIconButton(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(R.string.delete_update),
                onClick = onBottomBarDelete
            )
        }
    }
}
