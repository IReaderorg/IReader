package ireader.presentation.ui.settings.sync

import ireader.presentation.core.LocalNavigator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import ireader.presentation.ui.component.components.Toolbar
import org.koin.compose.koinInject
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*

class SupabaseConfigScreen  {
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val viewModel: SupabaseConfigViewModel = koinInject()
        val state by viewModel.state.collectAsState()
        
        Scaffold(
            topBar = {
                Toolbar(
                    title = { Text(localizeHelper.localize(Res.string.supabase_configuration)) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = localizeHelper.localize(Res.string.back))
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
                // Info Card
                item {
                    DefaultConfigInfoCard(
                        hasDefaultConfig = state.hasDefaultConfig,
                        useCustom = state.useCustomSupabase
                    )
                }
                
                // Custom Configuration Toggle
                item {
                    CustomConfigToggleCard(
                        hasDefaultConfig = state.hasDefaultConfig,
                        useCustom = state.useCustomSupabase,
                        onToggle = { viewModel.setUseCustomSupabase(it) }
                    )
                }
                
                // 7-Project Configuration (shown when custom is enabled or no default exists)
                if (state.useCustomSupabase || !state.hasDefaultConfig) {
                    item {
                        MultiProjectCard(
                            authUrl = state.authUrl,
                            authApiKey = state.authApiKey,
                            readingUrl = state.readingUrl,
                            readingApiKey = state.readingApiKey,
                            libraryUrl = state.libraryUrl,
                            libraryApiKey = state.libraryApiKey,
                            bookReviewsUrl = state.bookReviewsUrl,
                            bookReviewsApiKey = state.bookReviewsApiKey,
                            chapterReviewsUrl = state.chapterReviewsUrl,
                            chapterReviewsApiKey = state.chapterReviewsApiKey,
                            badgesUrl = state.badgesUrl,
                            badgesApiKey = state.badgesApiKey,
                            analyticsUrl = state.analyticsUrl,
                            analyticsApiKey = state.analyticsApiKey,
                            onAuthUrlChanged = { viewModel.setAuthUrl(it) },
                            onAuthApiKeyChanged = { viewModel.setAuthApiKey(it) },
                            onReadingUrlChanged = { viewModel.setReadingUrl(it) },
                            onReadingApiKeyChanged = { viewModel.setReadingApiKey(it) },
                            onLibraryUrlChanged = { viewModel.setLibraryUrl(it) },
                            onLibraryApiKeyChanged = { viewModel.setLibraryApiKey(it) },
                            onBookReviewsUrlChanged = { viewModel.setBookReviewsUrl(it) },
                            onBookReviewsApiKeyChanged = { viewModel.setBookReviewsApiKey(it) },
                            onChapterReviewsUrlChanged = { viewModel.setChapterReviewsUrl(it) },
                            onChapterReviewsApiKeyChanged = { viewModel.setChapterReviewsApiKey(it) },
                            onBadgesUrlChanged = { viewModel.setBadgesUrl(it) },
                            onBadgesApiKeyChanged = { viewModel.setBadgesApiKey(it) },
                            onAnalyticsUrlChanged = { viewModel.setAnalyticsUrl(it) },
                            onAnalyticsApiKeyChanged = { viewModel.setAnalyticsApiKey(it) },
                            onFillAllWithSame = { url, key -> viewModel.fillAllWithSame(url, key) },
                            onSave = { viewModel.saveConfiguration() },
                            onTest = { viewModel.testConnection() },
                            isTesting = state.isTesting,
                            testResult = state.testResult
                        )
                    }
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
private fun DefaultConfigInfoCard(
    hasDefaultConfig: Boolean,
    useCustom: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hasDefaultConfig && !useCustom) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
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
                tint = if (hasDefaultConfig && !useCustom) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = if (hasDefaultConfig) {
                        if (useCustom) "Using Custom Configuration" else "Using Default Configuration"
                    } else {
                        "No Default Configuration"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (hasDefaultConfig) {
                        if (useCustom) {
                            "You're using your own Supabase instance. Toggle off to use the default backend."
                        } else {
                            "App is using the default Supabase backend. You can optionally use your own instance."
                        }
                    } else {
                        "No default backend configured. Please configure your own Supabase instance below."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (hasDefaultConfig && !useCustom) 
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun CustomConfigToggleCard(
    hasDefaultConfig: Boolean,
    useCustom: Boolean,
    onToggle: (Boolean) -> Unit
) {
    if (!hasDefaultConfig) {
        // No toggle needed if there's no default config
        return
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
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
                    text = "Use Custom Supabase",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Enable to use your own Supabase instance instead of the default backend",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Switch(
                checked = useCustom,
                onCheckedChange = onToggle
            )
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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
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
                    Text(localizeHelper.localize(Res.string.sync_now))
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

@Composable
private fun MultiEndpointCard(
    useMultiEndpoint: Boolean,
    booksUrl: String,
    booksApiKey: String,
    progressUrl: String,
    progressApiKey: String,
    reviewsUrl: String,
    reviewsApiKey: String,
    communityUrl: String,
    communityApiKey: String,
    onUseMultiEndpointChanged: (Boolean) -> Unit,
    onBooksUrlChanged: (String) -> Unit,
    onBooksApiKeyChanged: (String) -> Unit,
    onProgressUrlChanged: (String) -> Unit,
    onProgressApiKeyChanged: (String) -> Unit,
    onReviewsUrlChanged: (String) -> Unit,
    onReviewsApiKeyChanged: (String) -> Unit,
    onCommunityUrlChanged: (String) -> Unit,
    onCommunityApiKeyChanged: (String) -> Unit,
    onSave: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
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
                        text = "Multi-Endpoint Configuration",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Use separate Supabase projects for better scalability",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Switch(
                    checked = useMultiEndpoint,
                    onCheckedChange = onUseMultiEndpointChanged
                )
            }
            
            if (useMultiEndpoint) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                
                // Books Endpoint
                EndpointSection(
                    title = "Books Endpoint",
                    description = "Separate project for book sync data",
                    icon = Icons.Default.Book,
                    url = booksUrl,
                    apiKey = booksApiKey,
                    onUrlChanged = onBooksUrlChanged,
                    onApiKeyChanged = onBooksApiKeyChanged
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Progress Endpoint
                EndpointSection(
                    title = "Progress Endpoint",
                    description = "Separate project for reading progress",
                    icon = Icons.Default.TrendingUp,
                    url = progressUrl,
                    apiKey = progressApiKey,
                    onUrlChanged = onProgressUrlChanged,
                    onApiKeyChanged = onProgressApiKeyChanged
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Reviews Endpoint (Future)
                EndpointSection(
                    title = "Reviews Endpoint",
                    description = "Coming soon - Reviews and ratings",
                    icon = Icons.Default.Star,
                    url = reviewsUrl,
                    apiKey = reviewsApiKey,
                    onUrlChanged = onReviewsUrlChanged,
                    onApiKeyChanged = onReviewsApiKeyChanged,
                    enabled = false,
                    badge = "Coming Soon"
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Community Endpoint (Future)
                EndpointSection(
                    title = "Community Endpoint",
                    description = "Coming soon - Social features",
                    icon = Icons.Default.People,
                    url = communityUrl,
                    apiKey = communityApiKey,
                    onUrlChanged = onCommunityUrlChanged,
                    onApiKeyChanged = onCommunityApiKeyChanged,
                    enabled = false,
                    badge = "Coming Soon"
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(localizeHelper.localize(Res.string.save_multi_endpoint_configuration))
                }
            }
        }
    }
}

@Composable
private fun EndpointSection(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    url: String,
    apiKey: String,
    onUrlChanged: (String) -> Unit,
    onApiKeyChanged: (String) -> Unit,
    enabled: Boolean = true,
    badge: String? = null
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var showApiKey by remember { mutableStateOf(false) }
    
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            badge?.let {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.tertiary
                ) {
                    Text(
                        text = it,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiary
                    )
                }
            }
        }
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 28.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = url,
            onValueChange = onUrlChanged,
            label = { Text(localizeHelper.localize(Res.string.url_1)) },
            placeholder = { Text("https://$title-project.supabase.co") },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = apiKey,
            onValueChange = onApiKeyChanged,
            label = { Text(localizeHelper.localize(Res.string.api_key)) },
            placeholder = { Text(localizeHelper.localize(Res.string.eyjhbgcioijiuzi1niisinr5cci6ikpxvcj9)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            trailingIcon = {
                IconButton(onClick = { showApiKey = !showApiKey }) {
                    Icon(
                        imageVector = if (showApiKey) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (showApiKey) "Hide" else "Show"
                    )
                }
            },
            visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        )
    }
}

@Composable
private fun MultiProjectCard(
    authUrl: String,
    authApiKey: String,
    readingUrl: String,
    readingApiKey: String,
    libraryUrl: String,
    libraryApiKey: String,
    bookReviewsUrl: String,
    bookReviewsApiKey: String,
    chapterReviewsUrl: String,
    chapterReviewsApiKey: String,
    badgesUrl: String,
    badgesApiKey: String,
    analyticsUrl: String,
    analyticsApiKey: String,
    onAuthUrlChanged: (String) -> Unit,
    onAuthApiKeyChanged: (String) -> Unit,
    onReadingUrlChanged: (String) -> Unit,
    onReadingApiKeyChanged: (String) -> Unit,
    onLibraryUrlChanged: (String) -> Unit,
    onLibraryApiKeyChanged: (String) -> Unit,
    onBookReviewsUrlChanged: (String) -> Unit,
    onBookReviewsApiKeyChanged: (String) -> Unit,
    onChapterReviewsUrlChanged: (String) -> Unit,
    onChapterReviewsApiKeyChanged: (String) -> Unit,
    onBadgesUrlChanged: (String) -> Unit,
    onBadgesApiKeyChanged: (String) -> Unit,
    onAnalyticsUrlChanged: (String) -> Unit,
    onAnalyticsApiKeyChanged: (String) -> Unit,
    onFillAllWithSame: (String, String) -> Unit,
    onSave: () -> Unit,
    onTest: () -> Unit,
    isTesting: Boolean,
    testResult: String?
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var quickFillUrl by remember { mutableStateOf("") }
    var quickFillKey by remember { mutableStateOf("") }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "Supabase Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "3.5GB total storage (7 × 500MB) - Configure each project or use same URL for all",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Quick Fill Section
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                        text = "Quick Fill (Same URL for all projects)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = quickFillUrl,
                        onValueChange = { quickFillUrl = it },
                        label = { Text(localizeHelper.localize(Res.string.supabase_url)) },
                        placeholder = { Text("https://xxx.supabase.co") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = quickFillKey,
                        onValueChange = { quickFillKey = it },
                        label = { Text(localizeHelper.localize(Res.string.api_key)) },
                        placeholder = { Text(localizeHelper.localize(Res.string.eyjhbgcioijiuzi1niisinr5cci6ikpxvcj9)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = { onFillAllWithSame(quickFillUrl, quickFillKey) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = quickFillUrl.isNotEmpty() && quickFillKey.isNotEmpty()
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(localizeHelper.localize(Res.string.fill_all_projects))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                
                // Project 1 - Auth
                EndpointSection(
                    title = "Project 1 - Auth",
                    description = "User authentication and profiles (500MB)",
                    icon = Icons.Default.People,
                    url = authUrl,
                    apiKey = authApiKey,
                    onUrlChanged = onAuthUrlChanged,
                    onApiKeyChanged = onAuthApiKeyChanged
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Project 2 - Reading
                EndpointSection(
                    title = "Project 2 - Reading",
                    description = "Reading progress tracking (500MB)",
                    icon = Icons.Default.TrendingUp,
                    url = readingUrl,
                    apiKey = readingApiKey,
                    onUrlChanged = onReadingUrlChanged,
                    onApiKeyChanged = onReadingApiKeyChanged
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Project 3 - Library
                EndpointSection(
                    title = "Project 3 - Library",
                    description = "Synced books library (500MB)",
                    icon = Icons.Default.Book,
                    url = libraryUrl,
                    apiKey = libraryApiKey,
                    onUrlChanged = onLibraryUrlChanged,
                    onApiKeyChanged = onLibraryApiKeyChanged
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Project 4 - Book Reviews
                EndpointSection(
                    title = "Project 4 - Book Reviews",
                    description = "Book reviews and ratings (500MB)",
                    icon = Icons.Default.Star,
                    url = bookReviewsUrl,
                    apiKey = bookReviewsApiKey,
                    onUrlChanged = onBookReviewsUrlChanged,
                    onApiKeyChanged = onBookReviewsApiKeyChanged
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Project 5 - Chapter Reviews
                EndpointSection(
                    title = "Project 5 - Chapter Reviews",
                    description = "Chapter reviews and ratings (500MB)",
                    icon = Icons.Default.Star,
                    url = chapterReviewsUrl,
                    apiKey = chapterReviewsApiKey,
                    onUrlChanged = onChapterReviewsUrlChanged,
                    onApiKeyChanged = onChapterReviewsApiKeyChanged
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Project 6 - Badges
                EndpointSection(
                    title = "Project 6 - Badges",
                    description = "Badge system and NFT integration (500MB)",
                    icon = Icons.Default.Star,
                    url = badgesUrl,
                    apiKey = badgesApiKey,
                    onUrlChanged = onBadgesUrlChanged,
                    onApiKeyChanged = onBadgesApiKeyChanged
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Project 7 - Analytics
                EndpointSection(
                    title = "Project 7 - Analytics",
                    description = "Leaderboard and statistics (500MB)",
                    icon = Icons.Default.TrendingUp,
                    url = analyticsUrl,
                    apiKey = analyticsApiKey,
                    onUrlChanged = onAnalyticsUrlChanged,
                    onApiKeyChanged = onAnalyticsApiKeyChanged
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                testResult?.let { result ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (result.contains("✓")) 
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
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onTest,
                        modifier = Modifier.weight(1f),
                        enabled = !isTesting && authUrl.isNotBlank() && authApiKey.isNotBlank()
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
                        Text(localizeHelper.localize(Res.string.test))
                    }
                    
                    Button(
                        onClick = onSave,
                        modifier = Modifier.weight(1f),
                        enabled = authUrl.isNotBlank() && authApiKey.isNotBlank()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(localizeHelper.localize(Res.string.save_configuration))
                    }
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
