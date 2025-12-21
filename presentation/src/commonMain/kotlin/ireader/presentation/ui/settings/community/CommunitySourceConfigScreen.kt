package ireader.presentation.ui.settings.community

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Cloud
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import ireader.domain.community.CommunitySource
import ireader.presentation.core.LocalNavigator
import ireader.presentation.ui.component.components.Toolbar
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import org.koin.compose.koinInject
import ireader.presentation.core.safePopBackStack
import ireader.i18n.resources.*
class CommunitySourceConfigScreen {
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val viewModel: CommunitySourceConfigViewModel = koinInject()
        val state by viewModel.state.collectAsState()
        
        Scaffold(
            topBar = {
                Toolbar(
                    title = { Text(localizeHelper.localize(Res.string.community_source)) },
                    navigationIcon = {
                        IconButton(onClick = { navController.safePopBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = localizeHelper.localize(Res.string.back))
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
                    CommunityInfoCard()
                }
                
                // Enable/Disable Toggle
                item {
                    EnableToggleCard(
                        enabled = state.communitySourceEnabled,
                        onToggle = { viewModel.setCommunitySourceEnabled(it) }
                    )
                }
                
                // Custom URL Configuration
                if (state.communitySourceEnabled) {
                    item {
                        UrlConfigCard(
                            url = state.communitySourceUrl,
                            apiKey = state.communitySourceApiKey,
                            onUrlChanged = { viewModel.setCommunitySourceUrl(it) },
                            onApiKeyChanged = { viewModel.setCommunitySourceApiKey(it) },
                            onSave = { viewModel.saveConfiguration() },
                            onTest = { viewModel.testConnection() },
                            isTesting = state.isTesting,
                            testResult = state.testResult
                        )
                    }
                    
                    // Contributor Settings
                    item {
                        ContributorSettingsCard(
                            contributorName = state.contributorName,
                            autoShareTranslations = state.autoShareTranslations,
                            autoShareAiOnly = state.autoShareAiOnly,
                            showContributorBadge = state.showContributorBadge,
                            checkCommunityFirst = state.checkCommunityFirst,
                            onContributorNameChanged = { viewModel.setContributorName(it) },
                            onAutoShareChanged = { viewModel.setAutoShareTranslations(it) },
                            onAutoShareAiOnlyChanged = { viewModel.setAutoShareAiOnly(it) },
                            onShowBadgeChanged = { viewModel.setShowContributorBadge(it) },
                            onCheckCommunityFirstChanged = { viewModel.setCheckCommunityFirst(it) }
                        )
                    }
                    
                    // Cloudflare D1 + R2 Configuration
                    item {
                        CloudflareConfigCard(
                            accountId = state.cloudflareAccountId,
                            apiToken = state.cloudflareApiToken,
                            d1DatabaseId = state.cloudflareD1DatabaseId,
                            r2BucketName = state.cloudflareR2BucketName,
                            r2PublicUrl = state.cloudflareR2PublicUrl,
                            compressionEnabled = state.cloudflareCompressionEnabled,
                            isConfigured = state.isCloudflareConfigured,
                            isTesting = state.isTestingCloudflare,
                            testResult = state.cloudflareTestResult,
                            onAccountIdChanged = { viewModel.setCloudflareAccountId(it) },
                            onApiTokenChanged = { viewModel.setCloudflareApiToken(it) },
                            onD1DatabaseIdChanged = { viewModel.setCloudflareD1DatabaseId(it) },
                            onR2BucketNameChanged = { viewModel.setCloudflareR2BucketName(it) },
                            onR2PublicUrlChanged = { viewModel.setCloudflareR2PublicUrl(it) },
                            onCompressionEnabledChanged = { viewModel.setCloudflareCompressionEnabled(it) },
                            onSave = { viewModel.saveCloudflareConfiguration() },
                            onTest = { viewModel.testCloudflareConnection() }
                        )
                    }
                    
                    // Content Preferences
                    item {
                        ContentPreferencesCard(
                            preferredLanguage = state.preferredLanguage,
                            showNsfwContent = state.showNsfwContent,
                            minimumRating = state.minimumRating,
                            onLanguageChanged = { viewModel.setPreferredLanguage(it) },
                            onNsfwChanged = { viewModel.setShowNsfwContent(it) },
                            onRatingChanged = { viewModel.setMinimumRating(it) }
                        )
                    }
                }
                
                // Database Schema Info
                item {
                    DatabaseSchemaCard()
                }
            }
        }
    }
}

@Composable
private fun CommunityInfoCard() {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
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
                imageVector = Icons.Default.People,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = localizeHelper.localize(Res.string.community_source),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = localizeHelper.localize(Res.string.browse_and_read_novels_translated),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun EnableToggleCard(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
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
                    text = localizeHelper.localize(Res.string.enable_community_source),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = localizeHelper.localize(Res.string.show_community_source_in_your_catalog_list),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Switch(
                checked = enabled,
                onCheckedChange = onToggle
            )
        }
    }
}

@Composable
private fun UrlConfigCard(
    url: String,
    apiKey: String,
    onUrlChanged: (String) -> Unit,
    onApiKeyChanged: (String) -> Unit,
    onSave: () -> Unit,
    onTest: () -> Unit,
    isTesting: Boolean,
    testResult: String?
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CloudSync,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = localizeHelper.localize(Res.string.supabase_configuration),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = localizeHelper.localize(Res.string.configure_your_own_supabase_instance),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = url,
                onValueChange = onUrlChanged,
                label = { Text(localizeHelper.localize(Res.string.supabase_url)) },
                placeholder = { Text("https://your-project.supabase.co") },
                leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = apiKey,
                onValueChange = onApiKeyChanged,
                label = { Text(localizeHelper.localize(Res.string.api_key_anon_key)) },
                placeholder = { Text(localizeHelper.localize(Res.string.eyjhbgcioijiuzi1niis)) },
                leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { showApiKey = !showApiKey }) {
                        Icon(
                            imageVector = if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showApiKey) "Hide" else "Show"
                        )
                    }
                },
                visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onTest,
                    enabled = !isTesting,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(localizeHelper.localize(Res.string.test))
                    }
                }
                
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(localizeHelper.localize(Res.string.save))
                }
            }
            
            testResult?.let { result ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = result,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (result.contains("✓") || result.contains("success", ignoreCase = true))
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun ContributorSettingsCard(
    contributorName: String,
    autoShareTranslations: Boolean,
    autoShareAiOnly: Boolean,
    showContributorBadge: Boolean,
    checkCommunityFirst: Boolean,
    onContributorNameChanged: (String) -> Unit,
    onAutoShareChanged: (Boolean) -> Unit,
    onAutoShareAiOnlyChanged: (Boolean) -> Unit,
    onShowBadgeChanged: (Boolean) -> Unit,
    onCheckCommunityFirstChanged: (Boolean) -> Unit
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = localizeHelper.localize(Res.string.contributor_settings),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = contributorName,
                onValueChange = onContributorNameChanged,
                label = { Text(localizeHelper.localize(Res.string.display_name)) },
                placeholder = { Text(localizeHelper.localize(Res.string.your_name_for_contributions)) },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = localizeHelper.localize(Res.string.auto_share_translations),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = localizeHelper.localize(Res.string.automatically_share_your_translations_with),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = autoShareTranslations,
                    onCheckedChange = onAutoShareChanged
                )
            }
            
            if (autoShareTranslations) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = localizeHelper.localize(Res.string.ai_translations_only),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = localizeHelper.localize(Res.string.only_share_high_quality_ai),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = autoShareAiOnly,
                        onCheckedChange = onAutoShareAiOnlyChanged
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = localizeHelper.localize(Res.string.check_community_first),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = localizeHelper.localize(Res.string.look_for_existing_community_translations),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = checkCommunityFirst,
                    onCheckedChange = onCheckCommunityFirstChanged
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
                        text = localizeHelper.localize(Res.string.show_contributor_badge),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = localizeHelper.localize(Res.string.display_badge_on_your_profile_for_contributions),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = showContributorBadge,
                    onCheckedChange = onShowBadgeChanged
                )
            }
        }
    }
}

@Composable
private fun ContentPreferencesCard(
    preferredLanguage: String,
    showNsfwContent: Boolean,
    minimumRating: Int,
    onLanguageChanged: (String) -> Unit,
    onNsfwChanged: (Boolean) -> Unit,
    onRatingChanged: (Int) -> Unit
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = localizeHelper.localize(Res.string.content_preferences),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Preferred Language: ${CommunitySource.SUPPORTED_LANGUAGES.find { it.first == preferredLanguage }?.second ?: preferredLanguage}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Language selection would be a dropdown in a real implementation
            Text(
                text = localizeHelper.localize(Res.string.tap_to_change_language_preference),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = localizeHelper.localize(Res.string.show_nsfw_content),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = localizeHelper.localize(Res.string.display_adult_content_in_search_results),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = showNsfwContent,
                    onCheckedChange = onNsfwChanged
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Minimum Rating: ${if (minimumRating == 0) "Any" else "$minimumRating+ stars"}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = localizeHelper.localize(Res.string.only_show_translations_with_this_rating_or_higher),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DatabaseSchemaCard() {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = localizeHelper.localize(Res.string.database_schema),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                IconButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "▲" else "▼")
                }
            }
            
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = localizeHelper.localize(Res.string.required_tables),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                SchemaTable(
                    tableName = "community_books",
                    columns = listOf(
                        "id (uuid, primary key)",
                        "title (text, required)",
                        "author (text)",
                        "description (text)",
                        "cover (text)",
                        "genres (text[])",
                        "status (text)",
                        "original_language (text)",
                        "available_languages (text[])",
                        "contributor_id (uuid)",
                        "contributor_name (text)",
                        "view_count (bigint)",
                        "chapter_count (integer)"
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                SchemaTable(
                    tableName = "community_chapters",
                    columns = listOf(
                        "id (uuid, primary key)",
                        "book_id (uuid, foreign key)",
                        "name (text, required)",
                        "number (real)",
                        "content (text, required)",
                        "language (text, required)",
                        "translator_id (uuid)",
                        "translator_name (text)",
                        "rating (real)",
                        "rating_count (integer)",
                        "view_count (bigint)"
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = localizeHelper.localize(Res.string.run_the_migration_community_sourcesql),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
private fun CloudflareConfigCard(
    accountId: String,
    apiToken: String,
    d1DatabaseId: String,
    r2BucketName: String,
    r2PublicUrl: String,
    compressionEnabled: Boolean,
    isConfigured: Boolean,
    isTesting: Boolean,
    testResult: String?,
    onAccountIdChanged: (String) -> Unit,
    onApiTokenChanged: (String) -> Unit,
    onD1DatabaseIdChanged: (String) -> Unit,
    onR2BucketNameChanged: (String) -> Unit,
    onR2PublicUrlChanged: (String) -> Unit,
    onCompressionEnabledChanged: (Boolean) -> Unit,
    onSave: () -> Unit,
    onTest: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var showApiToken by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(!isConfigured) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isConfigured) 
                MaterialTheme.colorScheme.secondaryContainer 
            else 
                MaterialTheme.colorScheme.surface
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Cloud,
                        contentDescription = null,
                        tint = if (isConfigured) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = localizeHelper.localize(Res.string.cloudflare_d1_r2),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isConfigured) "✓ Configured" else "Not configured",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isConfigured) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                IconButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "▲" else "▼")
                }
            }
            
            if (!expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = localizeHelper.localize(Res.string.high_capacity_storage_for_community),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = localizeHelper.localize(Res.string.cloudflare_provides_5gb_d1_10gb),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = accountId,
                    onValueChange = onAccountIdChanged,
                    label = { Text(localizeHelper.localize(Res.string.account_id)) },
                    placeholder = { Text(localizeHelper.localize(Res.string.your_cloudflare_account_id)) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = apiToken,
                    onValueChange = onApiTokenChanged,
                    label = { Text(localizeHelper.localize(Res.string.api_token)) },
                    placeholder = { Text(localizeHelper.localize(Res.string.token_with_d1_and_r2_permissions)) },
                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { showApiToken = !showApiToken }) {
                            Icon(
                                imageVector = if (showApiToken) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showApiToken) "Hide" else "Show"
                            )
                        }
                    },
                    visualTransformation = if (showApiToken) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = d1DatabaseId,
                    onValueChange = onD1DatabaseIdChanged,
                    label = { Text(localizeHelper.localize(Res.string.d1_database_id)) },
                    placeholder = { Text(localizeHelper.localize(Res.string.uuid_of_your_d1_database)) },
                    leadingIcon = { Icon(Icons.Default.Storage, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = r2BucketName,
                    onValueChange = onR2BucketNameChanged,
                    label = { Text(localizeHelper.localize(Res.string.r2_bucket_name)) },
                    placeholder = { Text(localizeHelper.localize(Res.string.eg_community_translations)) },
                    leadingIcon = { Icon(Icons.Default.Cloud, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = r2PublicUrl,
                    onValueChange = onR2PublicUrlChanged,
                    label = { Text(localizeHelper.localize(Res.string.r2_public_url_optional)) },
                    placeholder = { Text("https://your-bucket.r2.dev") },
                    leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Compress,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = localizeHelper.localize(Res.string.enable_compression),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = localizeHelper.localize(Res.string.reduce_storage_usage_by_40),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = compressionEnabled,
                        onCheckedChange = onCompressionEnabledChanged
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onTest,
                        enabled = !isTesting,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isTesting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(localizeHelper.localize(Res.string.validate))
                        }
                    }
                    
                    Button(
                        onClick = onSave,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(localizeHelper.localize(Res.string.save))
                    }
                }
                
                testResult?.let { result ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = result,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (result.contains("✓"))
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
