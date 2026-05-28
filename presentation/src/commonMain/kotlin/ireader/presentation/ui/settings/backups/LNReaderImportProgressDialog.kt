package ireader.presentation.ui.settings.backups

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FileDownload
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Modern progress dialog for LNReader import operations
 * Matches the visual style of BackupProgressDialog
 */
@Composable
fun LNReaderImportProgressDialog(
    progress: LNReaderImportProgress,
    onDismiss: () -> Unit
) {
    val isVisible = progress !is LNReaderImportProgress.Idle

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(200)) + scaleIn(tween(200), initialScale = 0.9f),
        exit = fadeOut(tween(200)) + scaleOut(tween(200), targetScale = 0.9f)
    ) {
        Dialog(
            onDismissRequest = {
                if (!progress.isInProgress) {
                    onDismiss()
                }
            },
            properties = DialogProperties(
                dismissOnBackPress = !progress.isInProgress,
                dismissOnClickOutside = !progress.isInProgress
            )
        ) {
            LNReaderImportProgressContent(
                progress = progress,
                onDismiss = onDismiss
            )
        }
    }
}

@Composable
private fun LNReaderImportProgressContent(
    progress: LNReaderImportProgress,
    onDismiss: () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.progress,
        animationSpec = tween(300),
        label = "lnreader_import_progress"
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
            LNReaderImportProgressIcon(progress = progress)

            Spacer(modifier = Modifier.height(16.dp))

            // Title
            Text(
                text = getLNReaderImportProgressTitle(progress),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Message
            Text(
                text = getLNReaderImportProgressMessage(progress),
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
                        color = MaterialTheme.colorScheme.tertiary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Round
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress percentage
                    Text(
                        text = "${(animatedProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Medium
                    )

                    // Novel count for import in progress
                    val countText = getLNReaderImportCountText(progress)
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
            if (!progress.isInProgress && progress !is LNReaderImportProgress.Idle) {
                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (progress is LNReaderImportProgress.Error) "Close" else "Done",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun LNReaderImportProgressIcon(progress: LNReaderImportProgress) {
    val (icon, color, showSpinner) = when (progress) {
        is LNReaderImportProgress.Idle -> Triple(Icons.Default.FileDownload, MaterialTheme.colorScheme.tertiary, false)
        is LNReaderImportProgress.Starting,
        is LNReaderImportProgress.Parsing,
        is LNReaderImportProgress.ImportingNovels,
        is LNReaderImportProgress.ImportingCategories -> Triple(Icons.Default.FileDownload, MaterialTheme.colorScheme.tertiary, true)
        is LNReaderImportProgress.Complete -> Triple(Icons.Default.CheckCircle, MaterialTheme.colorScheme.primary, false)
        is LNReaderImportProgress.Error -> Triple(Icons.Default.Error, MaterialTheme.colorScheme.error, false)
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

private fun getLNReaderImportProgressTitle(progress: LNReaderImportProgress): String {
    return when (progress) {
        is LNReaderImportProgress.Idle -> ""
        is LNReaderImportProgress.Starting -> "Importing from LNReader"
        is LNReaderImportProgress.Parsing -> "Parsing Backup"
        is LNReaderImportProgress.ImportingNovels -> "Importing Novels"
        is LNReaderImportProgress.ImportingCategories -> "Importing Categories"
        is LNReaderImportProgress.Complete -> "Import Complete"
        is LNReaderImportProgress.Error -> "Import Failed"
    }
}

private fun getLNReaderImportProgressMessage(progress: LNReaderImportProgress): String {
    return when (progress) {
        is LNReaderImportProgress.Idle -> ""
        is LNReaderImportProgress.Starting -> progress.message
        is LNReaderImportProgress.Parsing -> progress.message
        is LNReaderImportProgress.ImportingNovels -> progress.novelName.ifEmpty { "Processing novels..." }
        is LNReaderImportProgress.ImportingCategories -> "Processing categories..."
        is LNReaderImportProgress.Complete -> progress.message
        is LNReaderImportProgress.Error -> progress.error
    }
}

private fun getLNReaderImportCountText(progress: LNReaderImportProgress): String? {
    return when (progress) {
        is LNReaderImportProgress.ImportingNovels -> "${progress.current} / ${progress.total} novels"
        is LNReaderImportProgress.ImportingCategories -> "${progress.current} / ${progress.total} categories"
        else -> null
    }
}
