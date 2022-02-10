package org.ireader.presentation.feature_detail.presentation.book_detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.BottomAppBar
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BookDetailScreenBottomBar(
    modifier: Modifier = Modifier,
    isInLibrary: Boolean,
    onToggleInLibrary: () -> Unit,
    onDownload: () -> Unit,
    isRead: Boolean,
    onRead: () -> Unit,
) {
    BottomAppBar(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colors.background,
        contentColor = MaterialTheme.colors.onBackground,
        elevation = 8.dp,
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically

        ) {
            ButtonWithIconAndText(
                modifier = Modifier.weight(1F),
                text = if (!isInLibrary) "Add to Library" else "Added To Library",
                imageVector = if (!isInLibrary) Icons.Default.AddCircleOutline else Icons.Default.Check,
                onClick = {
                    onToggleInLibrary()
                },
            )

            ButtonWithIconAndText(
                modifier = Modifier.weight(1F),
                text = if (isRead) "Continue Reading" else "Read",
                imageVector = Icons.Default.AutoStories,
                onClick = {
                    onRead()
                }
            )

            ButtonWithIconAndText(
                modifier = Modifier.weight(1F),
                text = "Download",
                imageVector = Icons.Default.FileDownload,
                onClick = {
                    onDownload()
                }
            )
        }
    }
}