package ireader.presentation.ui.home.updates

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.GetApp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.UpdatesWithRelations
import ireader.i18n.localize
import ireader.i18n.resources.MR
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.core.ui.EmptyScreen
import ireader.presentation.ui.core.ui.LoadingScreen
import ireader.presentation.ui.home.updates.component.UpdatesContent
import ireader.presentation.ui.home.updates.viewmodel.UpdateState
import ireader.presentation.ui.home.updates.viewmodel.UpdatesViewModel


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
                isEmpty -> EmptyScreen(text = localize(MR.strings.no_new_update_available))
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
                    contentDescription = localize(MR.strings.download),
                    onClick = onBottomBarDownload
                )
            }
            AppIconButton(
                imageVector = Icons.Default.BookmarkBorder,
                contentDescription = localize(MR.strings.bookmark),
                onClick = onBottomBookMark
            )

            AppIconButton(
                imageVector = Icons.Default.Done,
                contentDescription = localize(MR.strings.mark_as_read),
                onClick = onBottomBarMarkAsRead
            )
        }
    }
}
