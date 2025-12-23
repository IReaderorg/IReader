package ireader.presentation.ui.settings.backups

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Modern progress dialog for backup/restore operations
 */
@Composable
fun BackupProgressDialog(
    progress: BackupRestoreProgress,
    onDismiss: () -> Unit
) {
    val isVisible = progress !is BackupRestoreProgress.Idle
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(200)) + scaleIn(tween(200), initialScale = 0.9f),
        exit = fadeOut(tween(200)) + scaleOut(tween(200), targetScale = 0.9f)
    ) {
        Dialog(
            onDismissRequest = {
                // Only allow dismiss when complete or error
                if (!progress.isInProgress) {
                    onDismiss()
                }
            },
            properties = DialogProperties(
                dismissOnBackPress = !progress.isInProgress,
                dismissOnClickOutside = !progress.isInProgress
            )
        ) {
            BackupProgressContent(
                progress = progress,
                onDismiss = onDismiss
            )
        }
    }
}

@Composable
private fun BackupProgressContent(
    progress: BackupRestoreProgress,
    onDismiss: () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.progress,
        animationSpec = tween(300),
        label = "progress"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon
            ProgressIcon(progress = progress)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Title
            Text(
                text = getProgressTitle(progress),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Message
            Text(
                text = getProgressMessage(progress),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Progress indicator
            if (progress.isInProgress) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Round
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Progress percentage
                    Text(
                        text = "${(animatedProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    
                    // Book count for restore/backup in progress
                    val countText = getProgressCountText(progress)
                    if (countText != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = countText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Dismiss button (only when complete or error)
            if (!progress.isInProgress && progress !is BackupRestoreProgress.Idle) {
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (progress is BackupRestoreProgress.BackupError || 
                                  progress is BackupRestoreProgress.RestoreError) "Close" else "Done",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressIcon(progress: BackupRestoreProgress) {
    val (icon, color, showSpinner) = when (progress) {
        is BackupRestoreProgress.Idle -> Triple(Icons.Default.Backup, MaterialTheme.colorScheme.primary, false)
        is BackupRestoreProgress.BackupStarting,
        is BackupRestoreProgress.BackupInProgress,
        is BackupRestoreProgress.BackupCompressing,
        is BackupRestoreProgress.BackupWriting -> Triple(Icons.Default.Backup, MaterialTheme.colorScheme.primary, true)
        is BackupRestoreProgress.BackupComplete -> Triple(Icons.Default.CheckCircle, MaterialTheme.colorScheme.primary, false)
        is BackupRestoreProgress.BackupError -> Triple(Icons.Default.Error, MaterialTheme.colorScheme.error, false)
        is BackupRestoreProgress.RestoreStarting,
        is BackupRestoreProgress.RestoreDecompressing,
        is BackupRestoreProgress.RestoreParsing,
        is BackupRestoreProgress.RestoreInProgress -> Triple(Icons.Default.Restore, MaterialTheme.colorScheme.secondary, true)
        is BackupRestoreProgress.RestoreComplete -> Triple(Icons.Default.CheckCircle, MaterialTheme.colorScheme.primary, false)
        is BackupRestoreProgress.RestoreError -> Triple(Icons.Default.Error, MaterialTheme.colorScheme.error, false)
    }
    
    Box(
        modifier = Modifier.size(72.dp),
        contentAlignment = Alignment.Center
    ) {
        // Background circle
        Surface(
            modifier = Modifier.size(72.dp),
            shape = CircleShape,
            color = color.copy(alpha = 0.1f)
        ) {}
        
        // Spinner for in-progress states
        if (showSpinner) {
            CircularProgressIndicator(
                modifier = Modifier.size(72.dp),
                color = color.copy(alpha = 0.3f),
                strokeWidth = 4.dp
            )
        }
        
        // Icon
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(36.dp),
            tint = color
        )
    }
}

private fun getProgressTitle(progress: BackupRestoreProgress): String {
    return when (progress) {
        is BackupRestoreProgress.Idle -> ""
        is BackupRestoreProgress.BackupStarting -> "Creating Backup"
        is BackupRestoreProgress.BackupInProgress -> "Backing Up"
        is BackupRestoreProgress.BackupCompressing -> "Compressing"
        is BackupRestoreProgress.BackupWriting -> "Saving"
        is BackupRestoreProgress.BackupComplete -> "Backup Complete"
        is BackupRestoreProgress.BackupError -> "Backup Failed"
        is BackupRestoreProgress.RestoreStarting -> "Restoring Backup"
        is BackupRestoreProgress.RestoreDecompressing -> "Decompressing"
        is BackupRestoreProgress.RestoreParsing -> "Parsing Data"
        is BackupRestoreProgress.RestoreInProgress -> "Restoring"
        is BackupRestoreProgress.RestoreComplete -> "Restore Complete"
        is BackupRestoreProgress.RestoreError -> "Restore Failed"
    }
}

private fun getProgressMessage(progress: BackupRestoreProgress): String {
    return when (progress) {
        is BackupRestoreProgress.Idle -> ""
        is BackupRestoreProgress.BackupStarting -> progress.message
        is BackupRestoreProgress.BackupInProgress -> progress.bookName.ifEmpty { progress.message }
        is BackupRestoreProgress.BackupCompressing -> progress.message
        is BackupRestoreProgress.BackupWriting -> progress.message
        is BackupRestoreProgress.BackupComplete -> progress.message
        is BackupRestoreProgress.BackupError -> progress.error
        is BackupRestoreProgress.RestoreStarting -> progress.message
        is BackupRestoreProgress.RestoreDecompressing -> progress.message
        is BackupRestoreProgress.RestoreParsing -> progress.message
        is BackupRestoreProgress.RestoreInProgress -> progress.bookName.ifEmpty { progress.message }
        is BackupRestoreProgress.RestoreComplete -> progress.message
        is BackupRestoreProgress.RestoreError -> progress.error
    }
}

private fun getProgressCountText(progress: BackupRestoreProgress): String? {
    return when (progress) {
        is BackupRestoreProgress.BackupInProgress -> "${progress.currentBook} / ${progress.totalBooks} books"
        is BackupRestoreProgress.RestoreInProgress -> "${progress.currentBook} / ${progress.totalBooks} books"
        else -> null
    }
}
