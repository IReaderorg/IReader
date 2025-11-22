package ireader.presentation.ui.home.sources.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.presentation.ui.home.sources.extension.Language
import ireader.presentation.ui.home.sources.extension.LocaleHelper

/**
 * Browse Settings Screen
 * - Multiple language selection for sources
 * - Shows only languages with available sources
 * - Checkbox-based interface
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseSettingsScreen(
    onBackPressed: () -> Unit,
    availableLanguages: List<String>,
    selectedLanguages: Set<String>,
    onLanguageToggled: (String) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Browse Settings",
                        fontWeight = FontWeight.SemiBold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (selectedLanguages.size == availableLanguages.size) {
                                onDeselectAll()
                            } else {
                                onSelectAll()
                            }
                        }
                    ) {
                        Text(
                            if (selectedLanguages.size == availableLanguages.size) 
                                "Deselect All" 
                            else 
                                "Select All"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // Section header
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Language Preferences",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Select languages to show in sources (${selectedLanguages.size}/${availableLanguages.size} selected)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            
            // Language options
            items(availableLanguages) { languageCode ->
                LanguageCheckboxItem(
                    languageCode = languageCode,
                    isSelected = selectedLanguages.contains(languageCode),
                    onToggle = { onLanguageToggled(languageCode) }
                )
            }
            
            // Empty state
            if (availableLanguages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No sources available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LanguageCheckboxItem(
    languageCode: String,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val language = Language(languageCode)
    val emoji = language.toEmoji() ?: ""
    val displayName = LocaleHelper.getDisplayName(languageCode)
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggle() }
        )
        
        if (emoji.isNotEmpty()) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.titleMedium
            )
        }
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
            Text(
                text = languageCode.uppercase(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
