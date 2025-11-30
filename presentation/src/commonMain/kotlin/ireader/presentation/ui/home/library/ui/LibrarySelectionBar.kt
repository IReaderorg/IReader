package ireader.presentation.ui.home.library.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.DoneOutline
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.DownloadForOffline
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.core.toComposeColor
import ireader.presentation.ui.core.theme.AppColors
import ireader.presentation.ui.core.theme.LocalLocalizeHelper


@Composable
internal fun LibrarySelectionBar(
    visible: Boolean,
    onClickChangeCategory: () -> Unit,
    onClickDownload: () -> Unit,
    onClickDownloadUnread: () -> Unit,
    onClickMarkAsRead: () -> Unit,
    onClickMarkAsUnread: () -> Unit,
    onClickDeleteDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Surface(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = AppColors.current.bars.toComposeColor(),
            contentColor = AppColors.current.onBars.toComposeColor(),
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = onClickChangeCategory) {
                    Icon(
                        imageVector = Icons.Outlined.Label,
                        contentDescription = localizeHelper.localize(Res.string.change_category),
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(onClick = onClickDownload) {
                    Icon(
                        imageVector = Icons.Outlined.Download,
                        contentDescription = localize(Res.string.download),
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(onClick = onClickDownloadUnread) {
                    Icon(
                        imageVector = Icons.Outlined.DownloadForOffline,
                        contentDescription = localizeHelper.localize(Res.string.download_unread),
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(onClick = onClickMarkAsRead) {
                    Icon(
                        imageVector = Icons.Outlined.Done,
                        contentDescription = localizeHelper.localize(Res.string.mark_as_read),
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(onClick = onClickMarkAsUnread) {
                    Icon(
                        imageVector = Icons.Outlined.DoneOutline,
                        contentDescription = localizeHelper.localize(Res.string.mark_as_unread),
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(onClick = onClickDeleteDownload) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = localize(Res.string.delete),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}


