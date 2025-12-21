package ireader.presentation.ui.home.library.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ireader.i18n.localize
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

/**
 * State for individual file import
 */
data class FileImportState(
    val fileName: String,
    val status: ImportStatus,
    val progress: Float = 0f
)

enum class ImportStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}

/**
 * Overall import progress state
 */
data class EpubImportProgress(
    val files: List<FileImportState>,
    val currentFileIndex: Int,
    val overallProgress: Float,
    val isPaused: Boolean = false,
    val estimatedTimeRemaining: String? = null
)

@Composable
fun EpubImportProgressDialog(
    progress: EpubImportProgress,
    onCancel: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCancelConfirmation by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = { showCancelConfirmation = true },
        title = {
            Text(localize(Res.string.importing_epub_files))
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Overall progress
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = localize(Res.string.overall_progress),
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "${(progress.overallProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    
                    LinearProgressIndicator(
                        progress = { progress.overallProgress },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    progress.estimatedTimeRemaining?.let { time ->
                        Text(
                            text = "${localize(Res.string.estimated_time_remaining)}: $time",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Divider()
                
                // File list
                Text(
                    text = localize(Res.string.files),
                    style = MaterialTheme.typography.titleSmall
                )
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(progress.files, key = { it.fileName }) { fileState ->
                        FileImportItem(fileState)
                    }
                }
            }
        },
        confirmButton = {
            if (progress.isPaused) {
                TextButton(onClick = onResume) {
                    Text(localize(Res.string.resume))
                }
            } else {
                TextButton(onClick = onPause) {
                    Text(localize(Res.string.pause))
                }
            }
        },
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
            title = { Text(localize(Res.string.cancel_import)) },
            text = { Text(localize(Res.string.cancel_import_confirmation)) },
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

@Composable
private fun FileImportItem(
    fileState: FileImportState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Status icon
        Icon(
            imageVector = when (fileState.status) {
                ImportStatus.PENDING -> Icons.Default.HourglassEmpty
                ImportStatus.IN_PROGRESS -> Icons.Default.HourglassEmpty
                ImportStatus.COMPLETED -> Icons.Default.CheckCircle
                ImportStatus.FAILED -> Icons.Default.Error
            },
            contentDescription = null,
            tint = when (fileState.status) {
                ImportStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
                ImportStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
                ImportStatus.COMPLETED -> MaterialTheme.colorScheme.primary
                ImportStatus.FAILED -> MaterialTheme.colorScheme.error
            },
            modifier = Modifier.size(20.dp)
        )
        
        // File name and progress
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = fileState.fileName,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            if (fileState.status == ImportStatus.IN_PROGRESS) {
                LinearProgressIndicator(
                    progress = { fileState.progress },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
