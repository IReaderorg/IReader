package ireader.presentation.ui.home.updates

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.UpdatesWithRelations
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.check_for_updates
import ireader.i18n.resources.refresh
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import ireader.presentation.ui.home.updates.component.UpdateEditBar
import ireader.presentation.ui.home.updates.component.UpdateProgressIndicator
import ireader.presentation.ui.home.updates.component.UpdatesContent
import ireader.presentation.ui.home.updates.component.UpdatesEmptyState
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
    val screenState by state.state.collectAsState()
    val isLoading = screenState.isLoading
    val isEmpty = screenState.isEmpty
    val hasSelection = screenState.hasSelection
    val isRefreshing = screenState.isRefreshing
    val updateProgress = screenState.updateProgress

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        when {
            isEmpty && isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 2.5.dp
                    )
                }
            }
            isEmpty -> UpdatesEmptyState()
            else -> UpdatesContent(
                state = state,
                onClickItem = onUpdate,
                onLongClickItem = onLongUpdate,
                onClickCover = onCoverUpdate,
                onClickDownload = onDownloadUpdate
            )
        }
        
        if (hasSelection) {
            UpdateEditBar(
                state = state,
                onBottomBarDownload = onBottomBarDownload,
                onBottomBarMarkAsRead = onBottomBarMarkAsRead,
                onBottomBookMark = onBottomBookMark
            )
        }

        // Subtle top progress line when loading in background - zero layout shift
        if (isLoading && !isEmpty) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .align(Alignment.TopCenter)
            )
        }
        
        // Progress indicator when refreshing
        if (isRefreshing || updateProgress != null) {
            UpdateProgressIndicator(
                progress = updateProgress,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        
        // FAB for refresh action
        if (isEmpty && !isLoading && !isRefreshing) {
            ExtendedFloatingActionButton(
                onClick = onRefresh,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(16.dp)
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
