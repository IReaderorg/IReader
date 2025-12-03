package ireader.presentation.ui.settings.backups

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.domain.usecases.backup.CloudBackupFile
import ireader.domain.usecases.backup.CloudProvider
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudBackupScreen(
    modifier: Modifier = Modifier,
    onPopBackStack: () -> Unit,
    selectedProvider: CloudProvider?,
    isAuthenticated: Boolean,
    cloudBackups: List<CloudBackupFile>,
    onProviderSelected: (CloudProvider) -> Unit,
    onAuthenticate: () -> Unit,
    onSignOut: () -> Unit,
    onUploadBackup: () -> Unit,
    onDownloadBackup: (CloudBackupFile) -> Unit,
    onDeleteBackup: (CloudBackupFile) -> Unit,
    onNavigateToGoogleDrive: () -> Unit = {}
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var showProviderDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(localizeHelper.localize(Res.string.cloud_backup)) },
                navigationIcon = {
                    IconButton(onClick = onPopBackStack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = localizeHelper.localize(Res.string.back)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (isAuthenticated) {
                FloatingActionButton(
                    onClick = onUploadBackup,
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = localizeHelper.localize(Res.string.upload_backup)
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Google Drive card
            item {
                GoogleDriveCard(
                    onNavigate = onNavigateToGoogleDrive
                )
            }
            
            // Provider selection card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = localizeHelper.localize(Res.string.cloud_storage_provider),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedButton(
                            onClick = { showProviderDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = when (selectedProvider) {
                                    CloudProvider.GOOGLE_DRIVE -> Icons.Default.CloudQueue
                                    CloudProvider.DROPBOX -> Icons.Default.Cloud
                                    else -> Icons.Default.CloudOff
                                },
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = selectedProvider?.name?.replace("_", " ") ?: "Select Provider"
                            )
                        }
                        
                        if (selectedProvider != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            if (isAuthenticated) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = localizeHelper.localize(Res.string.connected),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    TextButton(onClick = onSignOut) {
                                        Text(localizeHelper.localize(Res.string.sign_out))
                                    }
                                }
                            } else {
                                Button(
                                    onClick = onAuthenticate,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Login,
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(localizeHelper.localize(Res.string.sign_in))
                                }
                            }
                        }
                    }
                }
            }
            
            // Info card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Column {
                            Text(
                                text = localizeHelper.localize(Res.string.cloud_backup_setup),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = localizeHelper.localize(Res.string.note_cloud_backup_integration_requires) +
                                        "This feature is currently in development and will be fully " +
                                        "functional in a future update.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
            
            // Cloud backups list
            if (isAuthenticated && cloudBackups.isNotEmpty()) {
                item {
                    Text(
                        text = localizeHelper.localize(Res.string.available_backups),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                items(cloudBackups) { backup ->
                    CloudBackupItem(
                        backup = backup,
                        onDownload = { onDownloadBackup(backup) },
                        onDelete = { onDeleteBackup(backup) }
                    )
                }
            } else if (isAuthenticated) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = localizeHelper.localize(Res.string.no_cloud_backups_found),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = localizeHelper.localize(Res.string.upload_your_first_backup_using_the_button_below),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
    
    if (showProviderDialog) {
        CloudProviderDialog(
            onDismiss = { showProviderDialog = false },
            onProviderSelected = { provider ->
                onProviderSelected(provider)
                showProviderDialog = false
            }
        )
    }
}

@Composable
private fun CloudBackupItem(
    backup: CloudBackupFile,
    onDownload: () -> Unit,
    onDelete: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = backup.fileName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatFileSize(backup.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onDownload) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = localizeHelper.localize(Res.string.download)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = localizeHelper.localize(Res.string.delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun CloudProviderDialog(
    onDismiss: () -> Unit,
    onProviderSelected: (CloudProvider) -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(localizeHelper.localize(Res.string.select_cloud_provider)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CloudProviderOption(
                    name = "Google Drive",
                    icon = Icons.Default.CloudQueue,
                    onClick = { onProviderSelected(CloudProvider.GOOGLE_DRIVE) }
                )
                CloudProviderOption(
                    name = "Dropbox",
                    icon = Icons.Default.Cloud,
                    onClick = { onProviderSelected(CloudProvider.DROPBOX) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(localizeHelper.localize(Res.string.cancel))
            }
        }
    )
}

@Composable
private fun CloudProviderOption(
    name: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun GoogleDriveCard(
    onNavigate: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        onClick = onNavigate,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CloudQueue,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = localizeHelper.localize(Res.string.google_drive),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = localizeHelper.localize(Res.string.backup_and_restore_your_library_to_google_drive),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = localizeHelper.localize(Res.string.open),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "${ireader.presentation.ui.core.utils.formatDecimal(bytes / (1024.0 * 1024.0), 1)} MB"
    }
}
