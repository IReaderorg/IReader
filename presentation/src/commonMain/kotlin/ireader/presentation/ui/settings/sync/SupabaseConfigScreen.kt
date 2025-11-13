package ireader.presentation.ui.settings.sync

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.presentation.ui.component.components.Toolbar
import org.koin.compose.koinInject

class SupabaseConfigScreen : Screen {
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: SupabaseConfigViewModel = koinInject()
        val state by viewModel.state.collectAsState()
        
        Scaffold(
            topBar = {
                Toolbar(
                    title = { Text("Supabase Configuration") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    InfoCard()
                }
                
                item {
                    ConfigurationCard(
                        useCustom = state.useCustomSupabase,
                        url = state.supabaseUrl,
                        apiKey = state.supabaseApiKey,
                        onUseCustomChanged = { viewModel.setUseCustom(it) },
                        onUrlChanged = { viewModel.setUrl(it) },
                        onApiKeyChanged = { viewModel.setApiKey(it) },
                        onSave = { viewModel.saveConfiguration() },
                        onTest = { viewModel.testConnection() },
                        isTesting = state.isTesting,
                        testResult = state.testResult
                    )
                }
                
                item {
                    SyncSettingsCard(
                        autoSyncEnabled = state.autoSyncEnabled,
                        syncOnWifiOnly = state.syncOnWifiOnly,
                        onAutoSyncChanged = { viewModel.setAutoSync(it) },
                        onWifiOnlyChanged = { viewModel.setWifiOnly(it) }
                    )
                }
                
                item {
                    LastSyncCard(
                        lastSyncTime = state.lastSyncTime,
                        onManualSync = { viewModel.triggerManualSync() },
                        isSyncing = state.isSyncing
                    )
                }
                
                item {
                    DatabaseSchemaCard()
                }
            }
        }
        
        state.error?.let { error ->
            LaunchedEffect(error) {
                // Show error snackbar
                viewModel.clearError()
            }
        }
    }
}

@Composable
private fun InfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = "Custom Supabase Instance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Configure your own Supabase instance for data sync. Leave disabled to use the default server.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun ConfigurationCard(
    useCustom: Boolean,
    url: String,
    apiKey: String,
    onUseCustomChanged: (Boolean) -> Unit,
    onUrlChanged: (String) -> Unit,
    onApiKeyChanged: (String) -> Unit,
    onSave: () -> Unit,
    onTest: () -> Unit,
    isTesting: Boolean,
    testResult: String?
) {
    var showApiKey by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
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
                Text(
                    text = "Use Custom Supabase",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Switch(
                    checked = useCustom,
                    onCheckedChange = onUseCustomChanged
                )
            }
            
            if (useCustom) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = url,
                    onValueChange = onUrlChanged,
                    label = { Text("Supabase URL") },
                    placeholder = { Text("https://your-project.supabase.co") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Link, contentDescription = null)
                    },
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = onApiKeyChanged,
                    label = { Text("Supabase Anon Key") },
                    placeholder = { Text("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Key, contentDescription = null)
                    },
                    trailingIcon = {
                        IconButton(onClick = { showApiKey = !showApiKey }) {
                            Icon(
                                imageVector = if (showApiKey) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (showApiKey) "Hide" else "Show"
                            )
                        }
                    },
                    visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true
                )
                
                testResult?.let { result ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (result.contains("Success")) 
                                MaterialTheme.colorScheme.primaryContainer 
                            else 
                                MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = result,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onTest,
                        modifier = Modifier.weight(1f),
                        enabled = !isTesting && url.isNotBlank() && apiKey.isNotBlank()
                    ) {
                        if (isTesting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.CloudSync, contentDescription = null)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Test Connection")
                    }
                    
                    Button(
                        onClick = onSave,
                        modifier = Modifier.weight(1f),
                        enabled = url.isNotBlank() && apiKey.isNotBlank()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
private fun SyncSettingsCard(
    autoSyncEnabled: Boolean,
    syncOnWifiOnly: Boolean,
    onAutoSyncChanged: (Boolean) -> Unit,
    onWifiOnlyChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Sync Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Auto Sync",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Automatically sync reading progress",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Switch(
                    checked = autoSyncEnabled,
                    onCheckedChange = onAutoSyncChanged
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "WiFi Only",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Sync only when connected to WiFi",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Switch(
                    checked = syncOnWifiOnly,
                    onCheckedChange = onWifiOnlyChanged,
                    enabled = autoSyncEnabled
                )
            }
        }
    }
}

@Composable
private fun LastSyncCard(
    lastSyncTime: Long,
    onManualSync: () -> Unit,
    isSyncing: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
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
                Column {
                    Text(
                        text = "Last Sync",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (lastSyncTime > 0) formatTime(lastSyncTime) else "Never",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Button(
                    onClick = onManualSync,
                    enabled = !isSyncing
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Default.Sync, contentDescription = null)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sync Now")
                }
            }
        }
    }
}

@Composable
private fun DatabaseSchemaCard() {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
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
                Text(
                    text = "Database Schema",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand"
                    )
                }
            }
            
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Required Tables:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                SchemaTable(
                    tableName = "users",
                    columns = listOf(
                        "id (uuid, primary key)",
                        "email (text)",
                        "username (text, nullable)",
                        "eth_wallet_address (text, nullable)",
                        "is_supporter (boolean, default false)",
                        "created_at (timestamp)"
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                SchemaTable(
                    tableName = "reading_progress",
                    columns = listOf(
                        "id (uuid, primary key)",
                        "user_id (uuid, foreign key)",
                        "book_id (text)",
                        "last_chapter_slug (text)",
                        "last_scroll_position (float)",
                        "updated_at (timestamp)"
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                SchemaTable(
                    tableName = "synced_books",
                    columns = listOf(
                        "id (uuid, primary key)",
                        "user_id (uuid, foreign key)",
                        "book_id (text)",
                        "source_id (bigint)",
                        "title (text)",
                        "author (text)",
                        "cover (text)",
                        "favorite (boolean)",
                        "last_read (bigint)",
                        "updated_at (timestamp)"
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                SchemaTable(
                    tableName = "synced_chapters",
                    columns = listOf(
                        "id (uuid, primary key)",
                        "user_id (uuid, foreign key)",
                        "book_id (text)",
                        "chapter_key (text)",
                        "chapter_name (text)",
                        "read (boolean)",
                        "bookmark (boolean)",
                        "last_page_read (bigint)",
                        "updated_at (timestamp)"
                    )
                )
            }
        }
    }
}

@Composable
private fun SchemaTable(tableName: String, columns: List<String>) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = tableName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            columns.forEach { column ->
                Text(
                    text = "• $column",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                )
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000} minutes ago"
        diff < 86400_000 -> "${diff / 3600_000} hours ago"
        else -> "${diff / 86400_000} days ago"
    }
}
