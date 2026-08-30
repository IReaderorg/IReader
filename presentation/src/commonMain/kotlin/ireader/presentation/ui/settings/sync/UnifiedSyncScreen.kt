package ireader.presentation.ui.settings.sync

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ireader.domain.models.sync.SyncProviderType
import ireader.domain.utils.extensions.currentTimeToLong
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.TitleToolbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedSyncScreen(
    state: UnifiedSyncScreenState,
    onNavigateUp: () -> Unit,
    onSelectProvider: (SyncProviderType) -> Unit,
    onSyncNow: () -> Unit,
    onCancelSync: () -> Unit,
    onToggleAutoSyncOnLaunch: (Boolean) -> Unit,
    onToggleAutoSyncOnChapterFinish: (Boolean) -> Unit,
    onToggleSyncOnWifiOnly: (Boolean) -> Unit,
    onOpenGoogleDriveAuth: () -> Unit = {},
    onOpenSupabaseAuth: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    IScaffold(
        modifier = modifier,
        topBar = { scrollBehavior ->
            TitleToolbar(
                title = "Sync & Cloud Backup",
                popBackStack = onNavigateUp,
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // 1. Sync Status Card
            item {
                SyncStatusCard(
                    state = state,
                    onSyncNow = onSyncNow,
                    onCancelSync = onCancelSync
                )
            }

            // 2. Provider Selection Header
            item {
                Text(
                    text = "Sync Provider",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Provider Cards
            item {
                ProviderOptionCard(
                    title = "Google Drive",
                    subtitle = "Lightweight cloud delta sync (<100KB)",
                    icon = Icons.Outlined.CloudUpload,
                    isSelected = state.selectedProvider == SyncProviderType.GOOGLE_DRIVE,
                    accountStatus = if (state.isGoogleDriveConnected) "Connected as ${state.googleDriveEmail ?: "Google Account"}" else "Not connected",
                    isConnected = state.isGoogleDriveConnected,
                    onSelect = { onSelectProvider(SyncProviderType.GOOGLE_DRIVE) },
                    onConfigure = onOpenGoogleDriveAuth
                )
            }

            item {
                ProviderOptionCard(
                    title = "Supabase Cloud",
                    subtitle = "Account library & real-time progress sync",
                    icon = Icons.Outlined.Storage,
                    isSelected = state.selectedProvider == SyncProviderType.SUPABASE,
                    accountStatus = if (state.isSupabaseConnected) "Signed in as ${state.supabaseEmail ?: "User"}" else "Not signed in",
                    isConnected = state.isSupabaseConnected,
                    onSelect = { onSelectProvider(SyncProviderType.SUPABASE) },
                    onConfigure = onOpenSupabaseAuth
                )
            }

            item {
                ProviderOptionCard(
                    title = "Local Wi-Fi P2P",
                    subtitle = "Direct device-to-device sync over local network",
                    icon = Icons.Outlined.Wifi,
                    isSelected = state.selectedProvider == SyncProviderType.LOCAL_WIFI,
                    accountStatus = "Ready for local discovery",
                    isConnected = true,
                    onSelect = { onSelectProvider(SyncProviderType.LOCAL_WIFI) }
                )
            }

            item {
                ProviderOptionCard(
                    title = "Disabled",
                    subtitle = "Fully offline mode (no cloud or device sync)",
                    icon = Icons.Outlined.CloudOff,
                    isSelected = state.selectedProvider == SyncProviderType.NONE,
                    accountStatus = "Sync inactive",
                    isConnected = true,
                    onSelect = { onSelectProvider(SyncProviderType.NONE) }
                )
            }

            // 3. Sync Triggers & Settings
            item {
                Text(
                    text = "Sync Settings",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleAutoSyncOnLaunch(!state.autoSyncOnLaunch) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Auto-sync on app launch", style = MaterialTheme.typography.bodyLarge)
                                Text("Check for cloud updates when opening app", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(checked = state.autoSyncOnLaunch, onCheckedChange = onToggleAutoSyncOnLaunch)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleAutoSyncOnChapterFinish(!state.autoSyncOnChapterFinish) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Auto-sync reading progress", style = MaterialTheme.typography.bodyLarge)
                                Text("Sync reading progress after completing chapters", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(checked = state.autoSyncOnChapterFinish, onCheckedChange = onToggleAutoSyncOnChapterFinish)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleSyncOnWifiOnly(!state.syncOnWifiOnly) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Sync on Wi-Fi only", style = MaterialTheme.typography.bodyLarge)
                                Text("Prevent background mobile data usage", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(checked = state.syncOnWifiOnly, onCheckedChange = onToggleSyncOnWifiOnly)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun SyncStatusCard(
    state: UnifiedSyncScreenState,
    onSyncNow: () -> Unit,
    onCancelSync: () -> Unit
) {
    val syncState = state.syncState
    val isSyncing = syncState.isSyncing

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (state.selectedProvider) {
                            SyncProviderType.GOOGLE_DRIVE -> "Google Drive Sync"
                            SyncProviderType.SUPABASE -> "Supabase Cloud Sync"
                            SyncProviderType.LOCAL_WIFI -> "Local Wi-Fi Sync"
                            SyncProviderType.NONE -> "Sync Disabled"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = if (state.lastSyncTimestamp > 0) "Last synced: ${formatTimestamp(state.lastSyncTimestamp)}" else "Not synced yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }

                if (state.selectedProvider != SyncProviderType.NONE) {
                    if (isSyncing) {
                        OutlinedButton(onClick = onCancelSync) {
                            Text("Cancel")
                        }
                    } else {
                        Button(onClick = onSyncNow) {
                            Icon(Icons.Outlined.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sync Now")
                        }
                    }
                }
            }

            AnimatedVisibility(visible = isSyncing) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    LinearProgressIndicator(
                        progress = { syncState.progress },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = syncState.currentStep.ifEmpty { "Syncing in progress..." },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            AnimatedVisibility(visible = syncState.errorMessage != null) {
                Text(
                    text = "Error: ${syncState.errorMessage}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun ProviderOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    accountStatus: String,
    isConnected: Boolean,
    onSelect: () -> Unit,
    onConfigure: (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = accountStatus,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            RadioButton(
                selected = isSelected,
                onClick = onSelect
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    if (timestamp <= 0) return "Never"
    val diffMs = currentTimeToLong() - timestamp
    val seconds = diffMs / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        seconds < 60 -> "Just now"
        minutes < 60 -> "$minutes min ago"
        hours < 24 -> "$hours hours ago"
        else -> "$days days ago"
    }
}
