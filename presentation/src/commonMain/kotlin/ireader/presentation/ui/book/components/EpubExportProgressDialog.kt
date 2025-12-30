package ireader.presentation.ui.book.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
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
 * Progress state for EPUB export operations
 */
sealed class EpubExportProgress {
    object Idle : EpubExportProgress()
    
    data class Starting(val message: String = "Preparing export...") : EpubExportProgress()
    
    data class InProgress(
        val currentChapter: Int,
        val totalChapters: Int,
        val chapterName: String,
        val message: String = "Exporting chapters..."
    ) : EpubExportProgress()
    
    data class Compressing(val message: String = "Creating EPUB file...") : EpubExportProgress()
    
    data class Writing(val message: String = "Saving file...") : EpubExportProgress()
    
    data class Complete(
        val filePath: String,
        val message: String = "Export completed!"
    ) : EpubExportProgress()
    
    data class Error(val error: String) : EpubExportProgress()
    
    val isInProgress: Boolean
        get() = this !is Idle && this !is Complete && this !is Error
    
    val progress: Float
        get() = when (this) {
            is Idle -> 0f
            is Starting -> 0.05f
            is InProgress -> 0.1f + (currentChapter.toFloat() / totalChapters.coerceAtLeast(1)) * 0.7f
            is Compressing -> 0.85f
            is Writing -> 0.95f
            is Complete -> 1f
            is Error -> 0f
        }
}

/**
 * Modern progress dialog for EPUB export operations
 */
@Composable
fun EpubExportProgressDialog(
    progress: EpubExportProgress,
    onDismiss: () -> Unit
) {
    val isVisible = progress !is EpubExportProgress.Idle
    
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
            EpubExportProgressContent(
                progress = progress,
                onDismiss = onDismiss
            )
        }
    }
}

@Composable
private fun EpubExportProgressContent(
    progress: EpubExportProgress,
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
                    
                    // Chapter count for export in progress
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
            if (!progress.isInProgress && progress !is EpubExportProgress.Idle) {
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (progress is EpubExportProgress.Error) "Close" else "Done",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressIcon(progress: EpubExportProgress) {
    val (icon, color, showSpinner) = when (progress) {
        is EpubExportProgress.Idle -> Triple(Icons.Default.Book, MaterialTheme.colorScheme.primary, false)
        is EpubExportProgress.Starting,
        is EpubExportProgress.InProgress,
        is EpubExportProgress.Compressing,
        is EpubExportProgress.Writing -> Triple(Icons.Default.Book, MaterialTheme.colorScheme.primary, true)
        is EpubExportProgress.Complete -> Triple(Icons.Default.CheckCircle, MaterialTheme.colorScheme.primary, false)
        is EpubExportProgress.Error -> Triple(Icons.Default.Error, MaterialTheme.colorScheme.error, false)
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

private fun getProgressTitle(progress: EpubExportProgress): String {
    return when (progress) {
        is EpubExportProgress.Idle -> ""
        is EpubExportProgress.Starting -> "Preparing Export"
        is EpubExportProgress.InProgress -> "Exporting"
        is EpubExportProgress.Compressing -> "Creating EPUB"
        is EpubExportProgress.Writing -> "Saving"
        is EpubExportProgress.Complete -> "Export Complete"
        is EpubExportProgress.Error -> "Export Failed"
    }
}

private fun getProgressMessage(progress: EpubExportProgress): String {
    return when (progress) {
        is EpubExportProgress.Idle -> ""
        is EpubExportProgress.Starting -> progress.message
        is EpubExportProgress.InProgress -> progress.chapterName.ifEmpty { progress.message }
        is EpubExportProgress.Compressing -> progress.message
        is EpubExportProgress.Writing -> progress.message
        is EpubExportProgress.Complete -> progress.message
        is EpubExportProgress.Error -> progress.error
    }
}

private fun getProgressCountText(progress: EpubExportProgress): String? {
    return when (progress) {
        is EpubExportProgress.InProgress -> "${progress.currentChapter} / ${progress.totalChapters} chapters"
        else -> null
    }
}
