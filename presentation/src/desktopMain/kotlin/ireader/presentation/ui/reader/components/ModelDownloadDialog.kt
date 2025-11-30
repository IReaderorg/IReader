package ireader.presentation.ui.reader.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.core.log.Log
import ireader.domain.services.tts_service.piper.DownloadProgress
import ireader.domain.services.tts_service.piper.VoiceModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*

/**
 * Dialog showing download progress for a voice model
 */
@Composable
fun ModelDownloadDialog(
    model: VoiceModel,
    downloadFlow: Flow<DownloadProgress>,
    onDismiss: () -> Unit,
    onDownloadComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var progress by remember { mutableStateOf<DownloadProgress?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(downloadFlow) {
        try {
            downloadFlow.collect { downloadProgress ->
                progress = downloadProgress
                
                // Check if download is complete
                if (downloadProgress.status == "Complete") {
                    Log.info { "Download complete for ${model.name}" }
                    onDownloadComplete()
                } else if (downloadProgress.status.startsWith("Error")) {
                    error = downloadProgress.status
                }
            }
        } catch (e: Exception) {
            Log.error { "Download error: ${e.message}" }
            error = "Download failed: ${e.message}"
        }
    }
    
    AlertDialog(
        onDismissRequest = { 
            if (progress?.status == "Complete" || error != null) {
                onDismiss()
            }
        },
        title = {
            Text(
                text = if (error != null) "Download Failed" else "Downloading Voice Model"
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = model.name,
                    style = MaterialTheme.typography.bodyLarge
                )
                
                if (error != null) {
                    Text(
                        text = error!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    progress?.let { prog ->
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LinearProgressIndicator(
                                progress = if (prog.total > 0) {
                                    prog.downloaded.toFloat() / prog.total.toFloat()
                                } else {
                                    0f
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = prog.status,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                if (prog.total > 0) {
                                    Text(
                                        text = "${formatFileSize(prog.downloaded)} / ${formatFileSize(prog.total)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } ?: run {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (progress?.status == "Complete" || error != null) {
                Button(onClick = onDismiss) {
                    Text(localizeHelper.localize(Res.string.close))
                }
            }
        },
        dismissButton = {
            if (progress?.status != "Complete" && error == null) {
                TextButton(onClick = onDismiss) {
                    Text(localizeHelper.localize(Res.string.cancel))
                }
            }
        }
    )
}

private fun formatFileSize(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    
    return when {
        gb >= 1.0 -> String.format("%.2f GB", gb)
        mb >= 1.0 -> String.format("%.2f MB", mb)
        kb >= 1.0 -> String.format("%.2f KB", kb)
        else -> "$bytes B"
    }
}
