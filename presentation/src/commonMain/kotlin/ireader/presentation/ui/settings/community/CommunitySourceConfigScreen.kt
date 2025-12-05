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
import ireader.domain.community.SUPPORTED_LANGUAGES
import ireader.presentation.core.LocalNavigator
import ireader.presentation.ui.component.components.Toolbar
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import org.koin.compose.koinInject

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
                    title = { Text("Community Source") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                            showContributorBadge = state.showContributorBadge,
                            onContributorNameChanged = { viewModel.setContributorName(it) },
                            onAutoShareChanged = { viewModel.setAutoShareTranslations(it) },
                            onShowBadgeChanged = { viewModel.setShowContributorBadge(it) }
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
                    text = "Community Source",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Browse and read novels translated by the community. Share your own translations to help others!",
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
                    text = "Enable Community Source",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Show Community Source in your catalog list",
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
                    text = "Supabase Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Configure your own Supabase instance for community content, or leave empty to use the default.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = url,
                onValueChange = onUrlChanged,
                label = { Text("Supabase URL") },
                placeholder = { Text("https://your-project.supabase.co") },
                leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = apiKey,
                onValueChange = onApiKeyChanged,
                label = { Text("API Key (anon key)") },
                placeholder = { Text("eyJhbGciOiJIUzI1NiIs...") },
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
                        Text("Test")
                    }
                }
                
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save")
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
    showContributorBadge: Boolean,
    onContributorNameChanged: (String) -> Unit,
    onAutoShareChanged: (Boolean) -> Unit,
    onShowBadgeChanged: (Boolean) -> Unit
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Contributor Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = contributorName,
                onValueChange = onContributorNameChanged,
                label = { Text("Display Name") },
                placeholder = { Text("Your name for contributions") },
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
                        text = "Auto-share Translations",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Automatically share your translations with the community",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = autoShareTranslations,
                    onCheckedChange = onAutoShareChanged
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
                        text = "Show Contributor Badge",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Display badge on your profile for contributions",
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
                    text = "Content Preferences",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Preferred Language: ${SUPPORTED_LANGUAGES.find { it.first == preferredLanguage }?.second ?: preferredLanguage}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Language selection would be a dropdown in a real implementation
            Text(
                text = "Tap to change language preference",
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
                        text = "Show NSFW Content",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Display adult content in search results",
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
                text = "Only show translations with this rating or higher",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Database Schema",
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
                    text = "Required Tables:",
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
                    text = "Run the migration_community_source.sql file in your Supabase SQL Editor to create these tables.",
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
