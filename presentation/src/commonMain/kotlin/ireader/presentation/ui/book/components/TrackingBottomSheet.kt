package ireader.presentation.ui.book.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.domain.models.entities.Book
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.core.utils.formatDecimal
import org.jetbrains.compose.resources.painterResource

/**
 * Tracking service data for display
 */
data class TrackingServiceInfo(
    val id: String,
    val name: String,
    val isLoggedIn: Boolean,
    val isTracked: Boolean,
    val status: String? = null,
    val progress: Int? = null,
    val score: Float? = null,
    val totalChapters: Int? = null
)

/**
 * Bottom sheet for managing book tracking on external services
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingBottomSheet(
    book: Book,
    services: List<TrackingServiceInfo>,
    onServiceClick: (String) -> Unit,
    onSearchOnService: (String) -> Unit,
    onRemoveTracking: (String) -> Unit,
    onUpdateStatus: (String, String) -> Unit,
    onUpdateProgress: (String, Int) -> Unit,
    onUpdateScore: (String, Float) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = localizeHelper.localize(Res.string.tracking),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = localizeHelper.localize(Res.string.close)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Services list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(services, key = { it.id }) { service ->
                TrackingServiceCard(
                    service = service,
                    onServiceClick = { onServiceClick(service.id) },
                    onSearchClick = { onSearchOnService(service.id) },
                    onRemoveClick = { onRemoveTracking(service.id) },
                    onUpdateStatus = { status -> onUpdateStatus(service.id, status) },
                    onUpdateProgress = { progress -> onUpdateProgress(service.id, progress) },
                    onUpdateScore = { score -> onUpdateScore(service.id, score) }
                )
            }
            
            if (services.isEmpty()) {
                item {
                    EmptyTrackingState()
                }
            }
        }
    }
}

@Composable
private fun TrackingServiceCard(
    service: TrackingServiceInfo,
    onServiceClick: () -> Unit,
    onSearchClick: () -> Unit,
    onRemoveClick: () -> Unit,
    onUpdateStatus: (String) -> Unit,
    onUpdateProgress: (Int) -> Unit,
    onUpdateScore: (Float) -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    
    // Dialog states
    var showStatusDialog by remember { mutableStateOf(false) }
    var showProgressDialog by remember { mutableStateOf(false) }
    var showScoreDialog by remember { mutableStateOf(false) }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Service header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Service icon - using sync icon for all tracking services
                    Image(
                        painter = painterResource(Res.drawable.ic_sync),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(
                            if (service.isTracked) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        ),
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Column {
                        Text(
                            text = service.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Text(
                            text = when {
                                !service.isLoggedIn -> "Not logged in"
                                service.isTracked -> getStatusDisplayName(service.status)
                                else -> "Not tracked"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = when {
                                !service.isLoggedIn -> MaterialTheme.colorScheme.error
                                service.isTracked -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
                
                // Action button
                if (service.isLoggedIn) {
                    if (service.isTracked) {
                        IconButton(onClick = onRemoveClick) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remove tracking",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        FilledTonalButton(
                            onClick = onSearchClick,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add", fontSize = 14.sp)
                        }
                    }
                } else {
                    // Login button
                    FilledTonalButton(
                        onClick = onServiceClick,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Login,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Login", fontSize = 14.sp)
                    }
                }
            }
            
            // Tracking details (if tracked)
            if (service.isTracked && service.isLoggedIn) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Progress (clickable)
                    TrackingStatItem(
                        label = "Progress",
                        value = service.progress?.toString() ?: "-",
                        onClick = { showProgressDialog = true }
                    )
                    
                    // Score (clickable)
                    TrackingStatItem(
                        label = "Score",
                        value = service.score?.let { if (it > 0) it.formatDecimal(1) else "-" } ?: "-",
                        onClick = { showScoreDialog = true }
                    )
                    
                    // Status (clickable)
                    TrackingStatItem(
                        label = "Status",
                        value = getShortStatusName(service.status),
                        onClick = { showStatusDialog = true }
                    )
                }
            }
        }
    }
    
    // Edit dialogs
    if (showStatusDialog) {
        TrackingStatusDialog(
            currentStatus = service.status,
            onStatusSelected = onUpdateStatus,
            onDismiss = { showStatusDialog = false }
        )
    }
    
    if (showProgressDialog) {
        TrackingProgressDialog(
            currentProgress = service.progress,
            totalChapters = service.totalChapters,
            onProgressUpdated = onUpdateProgress,
            onDismiss = { showProgressDialog = false }
        )
    }
    
    if (showScoreDialog) {
        TrackingScoreDialog(
            currentScore = service.score,
            onScoreUpdated = onUpdateScore,
            onDismiss = { showScoreDialog = false }
        )
    }
}

@Composable
private fun TrackingStatItem(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyTrackingState() {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.Sync,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No tracking services configured",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Go to Settings → Tracking to set up AniList, MAL, MyNovelList, or other services",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

private fun getStatusDisplayName(status: String?) = when (status) {
    "Reading" -> "Reading"
    "Completed" -> "Completed"
    "OnHold" -> "On Hold"
    "Dropped" -> "Dropped"
    "Planned" -> "Plan to Read"
    "Repeating" -> "Re-reading"
    else -> status ?: "Tracking"
}

private fun getShortStatusName(status: String?) = when (status) {
    "Reading" -> "Reading"
    "Completed" -> "Done"
    "OnHold" -> "Hold"
    "Dropped" -> "Drop"
    "Planned" -> "Plan"
    "Repeating" -> "Re-read"
    else -> status ?: "-"
}
