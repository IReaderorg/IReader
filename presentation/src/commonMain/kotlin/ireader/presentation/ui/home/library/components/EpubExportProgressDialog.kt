package ireader.presentation.ui.home.library.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.i18n.localize
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

/**
 * State for EPUB export progress
 */
data class EpubExportProgress(
    val currentChapter: String,
    val currentChapterIndex: Int,
    val totalChapters: Int,
    val progress: Float,
    val estimatedTimeRemaining: String? = null
)

@Composable
fun EpubExportProgressDialog(
    progress: EpubExportProgress,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCancelConfirmation by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = { showCancelConfirmation = true },
        title = {
            Text(localize(Res.string.exporting_epub))
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Progress indicator
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = localize(Res.string.progress),
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "${(progress.progress * 100).toInt()}%",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    
                    LinearProgressIndicator(
                        progress = { progress.progress },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // Current chapter info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${localize(Res.string.current_chapter)}:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = progress.currentChapter,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${localize(Res.string.chapter)} ${progress.currentChapterIndex} ${localize(Res.string.of)} ${progress.totalChapters}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Estimated time remaining
                progress.estimatedTimeRemaining?.let { time ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${localize(Res.string.estimated_time_remaining)}: $time",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = { showCancelConfirmation = true }) {
                Text(localize(Res.string.cancel))
            }
        },
        modifier = modifier
    )
    
    if (showCancelConfirmation) {
        AlertDialog(
            onDismissRequest = { showCancelConfirmation = false },
            title = { Text(localize(Res.string.cancel_export)) },
            text = { Text(localize(Res.string.cancel_export_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelConfirmation = false
                        onCancel()
                    }
                ) {
                    Text(localize(Res.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelConfirmation = false }) {
                    Text(localize(Res.string.no))
                }
            }
        )
    }
}
