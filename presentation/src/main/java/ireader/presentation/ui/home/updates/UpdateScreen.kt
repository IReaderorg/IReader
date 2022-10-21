package ireader.presentation.ui.home.updates

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
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.GetApp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ireader.common.models.entities.UpdatesWithRelations
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.core.ui.EmptyScreen
import ireader.presentation.ui.core.ui.LoadingScreen
import ireader.presentation.ui.home.updates.component.UpdatesContent
import ireader.presentation.ui.home.updates.viewmodel.UpdateState
import ireader.presentation.ui.home.updates.viewmodel.UpdatesViewModel
import ireader.presentation.R


@Composable
fun UpdateScreen(
    modifier: Modifier = Modifier,
    state: UpdatesViewModel,
    onUpdate: (UpdatesWithRelations) -> Unit,
    onLongUpdate: (UpdatesWithRelations) -> Unit,
    onCoverUpdate: (UpdatesWithRelations) -> Unit,
    onDownloadUpdate: (UpdatesWithRelations) -> Unit,
    onBottomBarDownload: () -> Unit,
    onBottomBarMarkAsRead: () -> Unit,
    onBottomBookMark: () -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize()

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
                    .map { it.chapterId }
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
        }
    }
}
