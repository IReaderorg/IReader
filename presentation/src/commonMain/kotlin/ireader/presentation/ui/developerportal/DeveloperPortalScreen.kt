package ireader.presentation.ui.developerportal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.domain.plugins.DeveloperPlugin
import ireader.domain.plugins.PluginAccessGrant

/**
 * Developer Portal screen for plugin developers.
 * Allows developers to manage their plugins and grant access to users.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperPortalScreen(
    viewModel: DeveloperPortalViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.selectedPlugin != null) state.selectedPlugin!!.name
                        else "Developer Portal"
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (state.selectedPlugin != null) viewModel.goBack()
                            else onNavigateBack()
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.selectedPlugin != null && state.remainingGrants > 0) {
                        IconButton(onClick = { viewModel.showGrantDialog() }) {
                            Icon(Icons.Default.PersonAdd, contentDescription = "Grant Access")
                        }
                    }
                }
            )
        },
        modifier = modifier
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                state.isLoading -> LoadingState()
                !state.isDeveloper -> NotDeveloperState(state.error)
                state.selectedPlugin != null -> PluginDetailContent(
                    plugin = state.selectedPlugin!!,
                    grants = state.pluginGrants,
                    stats = state.pluginStats,
                    remainingGrants = state.remainingGrants,
                    onRevokeAccess = viewModel::revokeAccess
                )
                else -> PluginListContent(
                    plugins = state.plugins,
                    onPluginClick = viewModel::selectPlugin
                )
            }

            // Error snackbar
            state.error?.let { error ->
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(error)
                }
            }

            // Success snackbar
            if (state.grantSuccess) {
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    action = {
                        TextButton(onClick = { viewModel.clearGrantSuccess() }) {
                            Text("OK")
                        }
                    }
                ) {
                    Text("Access granted successfully!")
                }
            }
        }
    }

    // Grant dialog
    if (state.showGrantDialog) {
        GrantAccessDialog(
            username = state.grantUsername,
            reason = state.grantReason,
            remainingGrants = state.remainingGrants,
            isGranting = state.isGranting,
            error = state.grantError,
            onUsernameChange = viewModel::updateGrantUsername,
            onReasonChange = viewModel::updateGrantReason,
            onGrant = viewModel::grantAccess,
            onDismiss = viewModel::hideGrantDialog
        )
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun NotDeveloperState(error: String?, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Code,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Developer Portal",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error ?: "You need a Developer badge to access this feature",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Developer badges are granted to plugin creators who contribute to the IReader ecosystem.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PluginListContent(
    plugins: List<DeveloperPlugin>,
    onPluginClick: (DeveloperPlugin) -> Unit,
    modifier: Modifier = Modifier
) {
    if (plugins.isEmpty()) {
        Column(
            modifier = modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Extension,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No plugins yet",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Create and publish plugins to manage them here",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Your Plugins",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(plugins, key = { it.id }) { plugin ->
                PluginCard(plugin = plugin, onClick = { onPluginClick(plugin) })
            }
        }
    }
}

@Composable
private fun PluginCard(
    plugin: DeveloperPlugin,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plugin.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "v${plugin.version}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            when (plugin.monetizationType) {
                                "FREE" -> "Free"
                                "PREMIUM" -> "$${plugin.price}"
                                "FREEMIUM" -> "Freemium"
                                else -> plugin.monetizationType
                            }
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(label = "Users", value = plugin.activeUsers.toString())
                StatItem(label = "Purchases", value = plugin.totalPurchases.toString())
                StatItem(label = "Grants", value = "${plugin.grantedUsers}/${plugin.maxGrants}")
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PluginDetailContent(
    plugin: DeveloperPlugin,
    grants: List<PluginAccessGrant>,
    stats: ireader.domain.plugins.PluginStats?,
    remainingGrants: Int,
    onRevokeAccess: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stats card
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Statistics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(label = "Downloads", value = (stats?.totalDownloads ?: 0).toString())
                        StatItem(label = "Purchases", value = (stats?.totalPurchases ?: 0).toString())
                        StatItem(label = "Active", value = (stats?.activeUsers ?: 0).toString())
                    }
                    if (plugin.monetizationType == "PREMIUM") {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Total Revenue: $${stats?.totalRevenue ?: 0.0}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Grants section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Access Grants",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$remainingGrants/${plugin.maxGrants} remaining",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (grants.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No grants yet",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Grant access to users for testing or promotion",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(grants, key = { it.id }) { grant ->
                GrantCard(grant = grant, onRevoke = { onRevokeAccess(grant.id) })
            }
        }
    }
}

@Composable
private fun GrantCard(
    grant: PluginAccessGrant,
    onRevoke: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = grant.grantedToUsername,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = grant.reason,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (grant.isActive) {
                TextButton(
                    onClick = onRevoke,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Revoke")
                }
            } else {
                Text(
                    text = "Revoked",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun GrantAccessDialog(
    username: String,
    reason: String,
    remainingGrants: Int,
    isGranting: Boolean,
    error: String?,
    onUsernameChange: (String) -> Unit,
    onReasonChange: (String) -> Unit,
    onGrant: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
        title = { Text("Grant Plugin Access") },
        text = {
            Column {
                Text(
                    text = "Grant free access to a user ($remainingGrants slots remaining)",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = onUsernameChange,
                    label = { Text("Username") },
                    placeholder = { Text("Enter username") },
                    singleLine = true,
                    enabled = !isGranting,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = reason,
                    onValueChange = onReasonChange,
                    label = { Text("Reason") },
                    placeholder = { Text("e.g., Beta tester, Contributor") },
                    singleLine = true,
                    enabled = !isGranting,
                    modifier = Modifier.fillMaxWidth()
                )
                error?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onGrant,
                enabled = !isGranting && username.isNotBlank() && reason.isNotBlank()
            ) {
                if (isGranting) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("Grant")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isGranting) {
                Text("Cancel")
            }
        }
    )
}
