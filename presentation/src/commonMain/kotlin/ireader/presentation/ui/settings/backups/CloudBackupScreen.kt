package ireader.presentation.ui.settings.backups

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ireader.domain.models.backup.BackupInfo
import ireader.i18n.resources.*
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.domain.utils.extensions.formatDateTime

/**
 * Modern unified Cloud Backup Screen
 * Supports Google Drive backup with a clean, modern UI
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudBackupScreen(
    modifier: Modifier = Modifier,
    onPopBackStack: () -> Unit,
    viewModel: GoogleDriveViewModel,
    onStartOAuthFlow: (() -> Unit)? = null
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    
    val isConnected by viewModel.isConnected
    val accountEmail by viewModel.accountEmail
    val backups by viewModel.backups.collectAsState()
    val isLoading by viewModel.isLoading
    val isCreatingBackup by viewModel.isCreatingBackup
    val isRestoringBackup by viewModel.isRestoringBackup
    val needsOAuthFlow by viewModel.needsOAuthFlow
    val errorMessage by viewModel.errorMessage
    val successMessage by viewModel.successMessage
    
    var showRestoreDialog by remember { mutableStateOf<BackupInfo?>(null) }
    var showDeleteDialog by remember { mutableStateOf<BackupInfo?>(null) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val pullToRefreshState = rememberPullToRefreshState()
    
    // Handle OAuth flow
    LaunchedEffect(needsOAuthFlow) {
        if (needsOAuthFlow && onStartOAuthFlow != null) {
            onStartOAuthFlow()
            viewModel.clearOAuthFlowFlag()
        }
    }
    
    // Show messages
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Long)
            viewModel.clearMessages()
        }
    }
    
    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearMessages()
        }
    }
    
    // FAB rotation animation when creating backup
    val fabRotation by animateFloatAsState(
        targetValue = if (isCreatingBackup) 360f else 0f,
        label = "fab_rotation"
    )
    
    Scaffold(
        modifier = modifier,
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = localizeHelper.localize(Res.string.cloud_backup),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (isConnected && accountEmail != null) {
                            Text(
                                text = accountEmail ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onPopBackStack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = localizeHelper.localize(Res.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = isConnected,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                FloatingActionButton(
                    onClick = { if (!isCreatingBackup) viewModel.createBackup() },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(64.dp)
                ) {
                    if (isCreatingBackup) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 3.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = localizeHelper.localize(Res.string.create_backup_1),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isLoading && isConnected,
            onRefresh = { viewModel.loadBackups() },
            state = pullToRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Connection Card
                item {
                    GoogleDriveConnectionCard(
                        isConnected = isConnected,
                        accountEmail = accountEmail,
                        isLoading = isLoading && !isConnected,
                        onConnect = { viewModel.connect() },
                        onDisconnect = { viewModel.disconnect() }
                    )
                }
                
                // Status indicators
                if (isCreatingBackup || isRestoringBackup) {
                    item {
                        StatusCard(
                            isCreating = isCreatingBackup,
                            isRestoring = isRestoringBackup
                        )
                    }
                }
                
                // Backups section
                if (isConnected) {
                    if (backups.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = localizeHelper.localize(Res.string.available_backups),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${backups.size} ${if (backups.size == 1) "backup" else "backups"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        items(backups, key = { it.id }) { backup ->
                            BackupCard(
                                backup = backup,
                                onRestore = { showRestoreDialog = backup },
                                onDelete = { showDeleteDialog = backup }
                            )
                        }
                    } else if (!isLoading && !isCreatingBackup) {
                        item {
                            EmptyStateCard()
                        }
                    }
                }
                
                // Bottom spacing for FAB
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
    
    // Restore Dialog
    showRestoreDialog?.let { backup ->
        RestoreConfirmationDialog(
            backup = backup,
            onConfirm = {
                viewModel.restoreBackup(backup)
                showRestoreDialog = null
            },
            onDismiss = { showRestoreDialog = null }
        )
    }
    
    // Delete Dialog
    showDeleteDialog?.let { backup ->
        DeleteConfirmationDialog(
            backup = backup,
            onConfirm = {
                viewModel.deleteBackup(backup)
                showDeleteDialog = null
            },
            onDismiss = { showDeleteDialog = null }
        )
    }
}


@Composable
private fun GoogleDriveConnectionCard(
    isConnected: Boolean,
    accountEmail: String?,
    isLoading: Boolean,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Google Drive Icon with status
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isConnected)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudQueue,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = if (isConnected) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Status badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isConnected)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isConnected) Icons.Default.CheckCircle else Icons.Outlined.CloudOff,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (isConnected) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isConnected) 
                                localizeHelper.localize(Res.string.connected) 
                            else 
                                localizeHelper.localize(Res.string.not_connected),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isConnected) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = localizeHelper.localize(Res.string.google_drive),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isConnected) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = if (isConnected && accountEmail != null)
                    localizeHelper.localize(Res.string.backup_and_restore_your_library_to_google_drive)
                else
                    localizeHelper.localize(Res.string.connect_to_google_drive) + " " +
                    localizeHelper.localize(Res.string.backup_and_restore_your_library_to_google_drive),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isConnected) 
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            if (isConnected) {
                OutlinedButton(
                    onClick = onDisconnect,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(localizeHelper.localize(Res.string.disconnect))
                }
            } else {
                Button(
                    onClick = onConnect,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Login,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(localizeHelper.localize(Res.string.connect_to_google_drive))
                }
            }
        }
    }
}

@Composable
private fun StatusCard(
    isCreating: Boolean,
    isRestoring: Boolean
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCreating) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 3.dp,
                color = if (isCreating) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onSecondaryContainer
            )
            Column {
                Text(
                    text = if (isCreating) 
                        localizeHelper.localize(Res.string.creating_backup_1)
                    else 
                        localizeHelper.localize(Res.string.restoring_backup_1),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isCreating) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = if (isCreating) 
                        "Please wait while your data is being backed up..."
                    else 
                        "Please wait while your data is being restored...",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isCreating) 
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) 
                    else 
                        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun BackupCard(
    backup: BackupInfo,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Backup icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Backup,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            // Backup info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = backup.name.removePrefix("ireader_backup_").removeSuffix(".json.gz"),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = formatTimestamp(backup.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Storage,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = formatFileSize(backup.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Action buttons
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                FilledTonalIconButton(
                    onClick = onRestore,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = localizeHelper.localize(Res.string.restore),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                FilledTonalIconButton(
                    onClick = onDelete,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = localizeHelper.localize(Res.string.delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}


@Composable
private fun EmptyStateCard() {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.CloudOff,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = localizeHelper.localize(Res.string.no_backups_found),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = localizeHelper.localize(Res.string.create_your_first_backup_using_the_button_below),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RestoreConfirmationDialog(
    backup: BackupInfo,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = {
            Text(
                text = localizeHelper.localize(Res.string.restore_backup_1),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "This will replace all current data with the backup from:",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = formatTimestamp(backup.timestamp),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "This action cannot be undone.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Restore,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(localizeHelper.localize(Res.string.restore))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localizeHelper.localize(Res.string.cancel))
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun DeleteConfirmationDialog(
    backup: BackupInfo,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = {
            Text(
                text = localizeHelper.localize(Res.string.delete_backup),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Are you sure you want to delete this backup?",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = formatTimestamp(backup.timestamp),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(localizeHelper.localize(Res.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localizeHelper.localize(Res.string.cancel))
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

// Utility functions
private fun formatTimestamp(timestamp: Long): String {
    return if (timestamp > 0) timestamp.formatDateTime() else "Unknown"
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes <= 0 -> "Unknown size"
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "${((bytes / (1024.0 * 1024.0)) * 10).toLong() / 10.0} MB"
    }
}
