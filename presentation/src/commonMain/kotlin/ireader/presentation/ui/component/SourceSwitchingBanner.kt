package ireader.presentation.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ireader.domain.usecases.source.MigrateToSourceUseCase
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

/**
 * Banner that displays when a better source is available for a book
 */
@Composable
fun SourceSwitchingBanner(
    sourceName: String,
    chapterDifference: Int,
    onSwitch: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    visible: Boolean = true
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(),
        exit = shrinkVertically(),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Message
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = localizeHelper.localize(Res.string.better_source_available),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$sourceName has $chapterDifference more chapters",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Actions
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TextButton(
                        onClick = onSwitch,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(localizeHelper.localize(Res.string.switch))
                    }
                    
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = localizeHelper.localize(Res.string.dismiss),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

/**
 * Enhanced dialog showing migration progress with error handling and retry options
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MigrationProgressDialog(
    currentStep: String,
    progress: Float,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    errorType: MigrateToSourceUseCase.MigrationErrorType? = null,
    canRetry: Boolean = false,
    onRetry: (() -> Unit)? = null,
    detailedInfo: String? = null
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val hasError = error != null
    val isComplete = progress >= 1.0f && !hasError
    
    Dialog(onDismissRequest = { 
        // Only allow dismissal if complete or has error
        if (isComplete || hasError) {
            onDismiss()
        }
    }) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Status Icon
                val (icon, iconColor) = when {
                    hasError -> Icons.Default.ErrorOutline to MaterialTheme.colorScheme.error
                    isComplete -> Icons.Default.CheckCircle to Color(0xFF4CAF50)
                    else -> Icons.Default.Sync to MaterialTheme.colorScheme.primary
                }
                
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(48.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Title
                Text(
                    text = when {
                        hasError -> localizeHelper.localize(Res.string.migration_failed)
                        isComplete -> localizeHelper.localize(Res.string.migration_complete)
                        else -> localizeHelper.localize(Res.string.migrating_source)
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Current step
                Text(
                    text = currentStep,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = if (hasError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                
                // Detailed info if available
                if (detailedInfo != null && !hasError) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = detailedInfo,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Progress indicator
                if (!hasError) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = if (isComplete) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Error details
                if (hasError && error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Error type specific icon and message
                    val (errorIcon, errorTitle) = getErrorTypeInfo(errorType)
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = errorIcon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = errorTitle,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = error,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                    
                    // Suggestion based on error type
                    val suggestion = getErrorSuggestion(errorType)
                    if (suggestion != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = suggestion,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (hasError) {
                        // Dismiss button
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(localizeHelper.localize(Res.string.close))
                        }
                        
                        // Retry button (if applicable)
                        if (canRetry && onRetry != null) {
                            Button(
                                onClick = onRetry,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(localizeHelper.localize(Res.string.retry))
                            }
                        }
                    } else if (isComplete) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(localizeHelper.localize(Res.string.done))
                        }
                    } else {
                        // In progress - show cancel option
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(localizeHelper.localize(Res.string.cancel))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Get icon and title for error type
 */
@Composable
private fun getErrorTypeInfo(errorType: MigrateToSourceUseCase.MigrationErrorType?): Pair<ImageVector, String> {
    val localizeHelper = LocalLocalizeHelper.current ?: return Icons.Default.Error to "Error"
    
    return when (errorType) {
        MigrateToSourceUseCase.MigrationErrorType.BOOK_NOT_FOUND -> 
            Icons.Default.Book to localizeHelper.localize(Res.string.book_not_found)
        MigrateToSourceUseCase.MigrationErrorType.SOURCE_NOT_FOUND -> 
            Icons.Default.Source to localizeHelper.localize(Res.string.source_not_found)
        MigrateToSourceUseCase.MigrationErrorType.SOURCE_UNAVAILABLE -> 
            Icons.Default.CloudOff to localizeHelper.localize(Res.string.source_unavailable)
        MigrateToSourceUseCase.MigrationErrorType.NETWORK_ERROR -> 
            Icons.Default.WifiOff to localizeHelper.localize(Res.string.network_error)
        MigrateToSourceUseCase.MigrationErrorType.BOOK_NOT_IN_TARGET_SOURCE -> 
            Icons.Default.SearchOff to localizeHelper.localize(Res.string.book_not_in_target_source)
        MigrateToSourceUseCase.MigrationErrorType.CHAPTER_FETCH_FAILED -> 
            Icons.Default.MenuBook to localizeHelper.localize(Res.string.chapter_fetch_failed)
        MigrateToSourceUseCase.MigrationErrorType.DATABASE_ERROR -> 
            Icons.Default.Storage to localizeHelper.localize(Res.string.database_error)
        MigrateToSourceUseCase.MigrationErrorType.VALIDATION_ERROR -> 
            Icons.Default.Warning to localizeHelper.localize(Res.string.validation_error)
        else -> 
            Icons.Default.Error to localizeHelper.localize(Res.string.unknown_error)
    }
}

/**
 * Get suggestion text for error type
 */
@Composable
private fun getErrorSuggestion(errorType: MigrateToSourceUseCase.MigrationErrorType?): String? {
    val localizeHelper = LocalLocalizeHelper.current ?: return null
    
    return when (errorType) {
        MigrateToSourceUseCase.MigrationErrorType.NETWORK_ERROR -> 
            localizeHelper.localize(Res.string.check_internet_connection)
        MigrateToSourceUseCase.MigrationErrorType.SOURCE_UNAVAILABLE -> 
            localizeHelper.localize(Res.string.try_different_source)
        MigrateToSourceUseCase.MigrationErrorType.BOOK_NOT_IN_TARGET_SOURCE -> 
            localizeHelper.localize(Res.string.search_book_manually)
        MigrateToSourceUseCase.MigrationErrorType.DATABASE_ERROR -> 
            localizeHelper.localize(Res.string.try_again_later)
        else -> null
    }
}
