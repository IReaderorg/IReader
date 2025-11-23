package ireader.presentation.ui.home.updates

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.GetApp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.UpdatesWithRelations
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.core.theme.ContentAlpha
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
        onRefresh: () -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize()

    ) {
        Crossfade(targetState = Pair(state.isLoading, state.isEmpty)) { (isLoading, isEmpty) ->
            when {
                isLoading -> LoadingScreen()
                isEmpty -> UpdatesEmptyState()
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
        
        // Progress indicator when refreshing
        if (state.isRefreshing || state.updateProgress != null) {
            UpdateProgressIndicator(
                progress = state.updateProgress,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        
        // FAB for refresh action
        if (state.isEmpty && !state.isLoading && !state.isRefreshing) {
            ExtendedFloatingActionButton(
                onClick = onRefresh,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(16)

            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = localize(Res.string.refresh)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = localize(Res.string.check_for_updates))
            }
        }
    }
}

@Composable
private fun UpdatesEmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = LocalContentColor.current.copy(alpha = ContentAlpha.medium())
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = localize(Res.string.no_new_update_available),
            style = MaterialTheme.typography.titleMedium.copy(
                color = LocalContentColor.current.copy(alpha = ContentAlpha.medium())
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = localize(Res.string.no_updates_hint),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = LocalContentColor.current.copy(alpha = ContentAlpha.disabled())
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
private fun UpdateProgressIndicator(
    progress: ireader.presentation.ui.home.updates.viewmodel.UpdateProgress?,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Surface(
        modifier = modifier
            .padding(32.dp),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp)
            )
            
            if (progress != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Checking for updates...",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = progress.currentBook,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${progress.currentIndex} / ${progress.totalBooks}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (progress.estimatedTimeRemaining != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Est. ${progress.estimatedTimeRemaining}s remaining",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Checking for updates...",
                    style = MaterialTheme.typography.titleMedium
                )
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
                .fillMaxSize()
                .padding(horizontal = 8.dp),
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
                    contentDescription = localize(Res.string.download),
                    onClick = onBottomBarDownload
                )
            }
            AppIconButton(
                imageVector = Icons.Default.BookmarkBorder,
                contentDescription = localize(Res.string.bookmark),
                onClick = onBottomBookMark
            )

            AppIconButton(
                imageVector = Icons.Default.Done,
                contentDescription = localize(Res.string.mark_as_read),
                onClick = onBottomBarMarkAsRead
            )
        }
    }
}
