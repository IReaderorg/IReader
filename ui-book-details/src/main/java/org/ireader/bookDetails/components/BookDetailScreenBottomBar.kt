package org.ireader.bookDetails.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Icon
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
import org.ireader.core_ui.ui_components.ButtonWithIconAndText
import org.ireader.core_ui.ui_components.components.showLoading

@Composable
fun BookDetailScreenBottomBar(
    modifier: Modifier = Modifier,
    isInLibrary: Boolean,
    isInLibraryInProgress: Boolean,
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
                icon = {
                    if (isInLibraryInProgress) {
                        showLoading()
                    } else {
                        Icon(
                            imageVector = if (!isInLibrary) Icons.Default.AddCircleOutline else Icons.Default.Check,
                            contentDescription = "toggle in library",
                            tint = MaterialTheme.colors.onBackground
                        )
                    }
                },
                onClick = {
                    if (!isInLibraryInProgress) {
                        onToggleInLibrary()
                    }
                },
            )

            ButtonWithIconAndText(
                modifier = Modifier.weight(1F),
                text = if (isRead) "Continue Reading" else "Read",
                icon = {
                    Icon(
                        imageVector = Icons.Default.AutoStories,
                        contentDescription = "Continue Reading",
                        tint = MaterialTheme.colors.onBackground
                    )
                },
                onClick = {
                    onRead()
                }
            )

            ButtonWithIconAndText(
                modifier = Modifier.weight(1F),
                text = "Download",
                icon = {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = "Download",
                        tint = MaterialTheme.colors.onBackground
                    )

                },
                onClick = {
                    onDownload()
                }
            )
        }
    }
}