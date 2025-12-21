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
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*

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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = localizeHelper.localize(Res.string.back))
                    }
                },
                actions = {
                    if (state.selectedPlugin != null && state.remainingGrants > 0) {
                        IconButton(onClick = { viewModel.showGrantDialog() }) {
                            Icon(Icons.Default.PersonAdd, contentDescription = localizeHelper.localize(Res.string.grant_access))
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
                            Text(localizeHelper.localize(Res.string.notification_dismiss))
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
                            Text(localizeHelper.localize(Res.string.ok))
                        }
                    }
                ) {
                    Text(localizeHelper.localize(Res.string.access_granted_successfully))
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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
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
            text = localizeHelper.localize(Res.string.developer_portal),
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
            text = localizeHelper.localize(Res.string.developer_badges_are_granted_to),
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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
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
                text = localizeHelper.localize(Res.string.no_plugins_yet),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = localizeHelper.localize(Res.string.create_and_publish_plugins_to_manage_them_here),
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
                    text = localizeHelper.localize(Res.string.your_plugins),
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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
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
                StatItem(label = localizeHelper.localize(Res.string.users), value = plugin.activeUsers.toString())
                StatItem(label = localizeHelper.localize(Res.string.purchases), value = plugin.totalPurchases.toString())
                StatItem(label = localizeHelper.localize(Res.string.grants), value = "${plugin.grantedUsers}/${plugin.maxGrants}")
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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
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
                        text = localizeHelper.localize(Res.string.statistics),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(label = localizeHelper.localize(Res.string.downloads), value = (stats?.totalDownloads ?: 0).toString())
                        StatItem(label = localizeHelper.localize(Res.string.purchases), value = (stats?.totalPurchases ?: 0).toString())
                        StatItem(label = localizeHelper.localize(Res.string.active_downloads), value = (stats?.activeUsers ?: 0).toString())
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
                    text = localizeHelper.localize(Res.string.access_grants),
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
                            text = localizeHelper.localize(Res.string.no_grants_yet),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = localizeHelper.localize(Res.string.grant_access_to_users_for_testing_or_promotion),
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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
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
                    Text(localizeHelper.localize(Res.string.revoke))
                }
            } else {
                Text(
                    text = localizeHelper.localize(Res.string.revoked),
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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
        title = { Text(localizeHelper.localize(Res.string.grant_plugin_access)) },
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
                    label = { Text(localizeHelper.localize(Res.string.username)) },
                    placeholder = { Text(localizeHelper.localize(Res.string.enter_username)) },
                    singleLine = true,
                    enabled = !isGranting,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = reason,
                    onValueChange = onReasonChange,
                    label = { Text(localizeHelper.localize(Res.string.reason)) },
                    placeholder = { Text(localizeHelper.localize(Res.string.eg_beta_tester_contributor)) },
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
                    Text(localizeHelper.localize(Res.string.grant))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isGranting) {
                Text(localizeHelper.localize(Res.string.cancel))
            }
        }
    )
}
