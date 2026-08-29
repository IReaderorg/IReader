package ireader.presentation.ui.settings.backups

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.usecases.backup.v2.BackupOptions

@Composable
fun BackupOptionsDialog(
    options: BackupOptions,
    onOptionChanged: ((BackupOptions) -> BackupOptions) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Full Backup Options",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Choose which items to include in your backup. By default, everything is selected to fully duplicate your app state.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onSelectAll) {
                        Text("Select All")
                    }
                    TextButton(onClick = onDeselectAll) {
                        Text("Deselect All")
                    }
                }

                BackupOptionItem(
                    title = "Books & Library",
                    subtitle = "Novel metadata, covers, and library flags",
                    checked = options.includeBooks,
                    onCheckedChange = { checked ->
                        onOptionChanged { it.copy(includeBooks = checked) }
                    }
                )

                BackupOptionItem(
                    title = "Chapters & Read Status",
                    subtitle = "Chapter list, bookmarks, and read states",
                    checked = options.includeChapters,
                    onCheckedChange = { checked ->
                        onOptionChanged { it.copy(includeChapters = checked) }
                    }
                )

                BackupOptionItem(
                    title = "Chapter Content",
                    subtitle = "Downloaded chapter text (larger backup file)",
                    checked = options.includeChapterContent,
                    onCheckedChange = { checked ->
                        onOptionChanged { it.copy(includeChapterContent = checked) }
                    }
                )

                BackupOptionItem(
                    title = "Reading History",
                    subtitle = "Reading timestamps, durations, and page progress",
                    checked = options.includeHistory,
                    onCheckedChange = { checked ->
                        onOptionChanged { it.copy(includeHistory = checked) }
                    }
                )

                BackupOptionItem(
                    title = "Categories",
                    subtitle = "Custom library categories and book assignments",
                    checked = options.includeCategories,
                    onCheckedChange = { checked ->
                        onOptionChanged { it.copy(includeCategories = checked) }
                    }
                )

                BackupOptionItem(
                    title = "Tracking Services",
                    subtitle = "AniList, MyAnimeList entries, ratings, and progress",
                    checked = options.includeTracks,
                    onCheckedChange = { checked ->
                        onOptionChanged { it.copy(includeTracks = checked) }
                    }
                )

                BackupOptionItem(
                    title = "Reader Themes",
                    subtitle = "Custom reader color palettes and themes",
                    checked = options.includeThemes,
                    onCheckedChange = { checked ->
                        onOptionChanged { it.copy(includeThemes = checked) }
                    }
                )

                BackupOptionItem(
                    title = "Settings & Preferences",
                    subtitle = "Typography, layout, and reader configurations",
                    checked = options.includeSettings,
                    onCheckedChange = { checked ->
                        onOptionChanged { it.copy(includeSettings = checked) }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Proceed to Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun BackupOptionItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
