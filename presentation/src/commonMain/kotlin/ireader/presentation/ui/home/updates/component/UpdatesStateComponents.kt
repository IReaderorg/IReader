package ireader.presentation.ui.home.updates.component

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.GetApp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.bookmark
import ireader.i18n.resources.checking_for_updates_1
import ireader.i18n.resources.download
import ireader.i18n.resources.mark_as_read
import ireader.i18n.resources.no_new_update_available
import ireader.i18n.resources.no_updates_hint
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.core.theme.ContentAlpha
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.home.updates.viewmodel.UpdateProgress
import ireader.presentation.ui.home.updates.viewmodel.UpdatesViewModel

@Composable
fun UpdatesEmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        androidx.compose.material3.Icon(
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
fun UpdateProgressIndicator(
    progress: UpdateProgress?,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Surface(
        modifier = modifier.padding(32.dp),
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
                    text = localizeHelper.localize(Res.string.checking_for_updates_1),
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
                    text = localizeHelper.localize(Res.string.checking_for_updates_1),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
fun BoxScope.UpdateEditBar(
    state: UpdatesViewModel,
    onBottomBarDownload: () -> Unit,
    onBottomBarMarkAsRead: () -> Unit,
    onBottomBookMark: () -> Unit,
) {
    val screenState by state.state.collectAsState()
    val selection = screenState.selectedChapterIds
    val updates = screenState.updates
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .align(Alignment.BottomCenter)
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.background)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onBackground.copy(.1f)
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
            if (selection.any { selectionId ->
                selectionId in updates.values.flatten().filter { !it.downloaded }
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
