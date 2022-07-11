package org.ireader.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.DoneOutline
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ireader.components.reusable_composable.AppIconButton
import org.ireader.core_ui.theme.AppColors

@Composable
internal fun LibrarySelectionBar(
    visible: Boolean,
    onClickChangeCategory: () -> Unit,
    onClickDownload: () -> Unit,
    onClickMarkAsRead: () -> Unit,
    onClickMarkAsUnread: () -> Unit,
    onClickDeleteDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            color = AppColors.current.bars,
            contentColor = AppColors.current.onBars,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                AppIconButton(imageVector = Icons.Outlined.Label, onClick = onClickChangeCategory)
                AppIconButton(imageVector = Icons.Outlined.Download, onClick = onClickDownload)
                AppIconButton(imageVector = Icons.Outlined.Done, onClick = onClickMarkAsRead)
                AppIconButton(
                    imageVector = Icons.Outlined.DoneOutline,
                    onClick = onClickMarkAsUnread
                )
                AppIconButton(imageVector = Icons.Outlined.Delete, onClick = onClickDeleteDownload)
            }
        }
    }
}
