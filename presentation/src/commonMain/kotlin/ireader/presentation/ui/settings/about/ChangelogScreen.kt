package ireader.presentation.ui.settings.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res
import ireader.presentation.ui.core.theme.currentOrThrow

data class ChangelogEntry(
    val version: String,
    val isUpcoming: Boolean = false,
    val sections: List<ChangelogSection>
)

data class ChangelogSection(
    val title: String,
    val items: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangelogScreen(
    modifier: Modifier = Modifier,
    onPopBackStack: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val changelogEntries = getChangelogData()
    
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(localizeHelper.localize(Res.string.whats_new_1)) },
                navigationIcon = {
                    IconButton(onClick = onPopBackStack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = localizeHelper.localize(Res.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(changelogEntries, key = { it.version }) { entry ->
                ChangelogCard(entry = entry)
            }
        }
    }
}

@Composable
private fun ChangelogCard(entry: ChangelogEntry) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (entry.isUpcoming) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Version header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (entry.isUpcoming) {
                    Icon(
                        imageVector = Icons.Default.NewReleases,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = entry.version,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (entry.isUpcoming) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                if (entry.isUpcoming) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = localizeHelper.localize(Res.string.upcoming),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Changelog sections
            entry.sections.forEachIndexed { index, section ->
                if (index > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                ChangelogSectionContent(section = section, isUpcoming = entry.isUpcoming)
            }
        }
    }
}

@Composable
private fun ChangelogSectionContent(
    section: ChangelogSection,
    isUpcoming: Boolean
) {
    Column {
        Text(
            text = section.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isUpcoming) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        section.items.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUpcoming) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    }
                )
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUpcoming) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
@Composable
private fun getChangelogData(): List<ChangelogEntry> {
    val localizeHelper = LocalLocalizeHelper.currentOrThrow
    return listOf(
        ChangelogEntry(
            version = "v0.1.30",
            isUpcoming = true,
            sections = listOf(
                ChangelogSection(
                    title = localizeHelper.localize(Res.string.ui_improvements),
                    items = listOf(
                        "Enhanced Settings Screens with Material Design 3 components",
                        "Improved Appearance settings with better theme selection",
                        "Enhanced General settings with organized preference groups",
                        "Updated Advanced settings with descriptive subtitles",
                        "Polished About screen with improved logo and version info"
                    )
                ),
                ChangelogSection(
                    title = localizeHelper.localize(Res.string.new_components),
                    items = listOf(
                        "RowPreference: Flexible preference row with icon support",
                        "SectionHeader: Styled headers for grouping preferences",
                        "EnhancedCard: Material Design 3 card component",
                        "NavigationRowPreference: Preference row with navigation",
                        "PreferenceGroup: Utility for creating preference groups"
                    )
                ),
                ChangelogSection(
                    title = localizeHelper.localize(Res.string.explore_screen),
                    items = listOf(
                        "Enhanced novel card design with better covers",
                        "Improved grid layout with adaptive columns",
                        "Modernized filter bottom sheet",
                        "Added smooth animations and visual feedback"
                    )
                ),
                ChangelogSection(
                    title = localizeHelper.localize(Res.string.performance),
                    items = listOf(
                        "Optimized list rendering with proper keys",
                        "Improved image loading and caching",
                        "Better scroll performance (60 FPS target)",
                        "Selective resource loading for faster page loads"
                    )
                ),
                ChangelogSection(
                    title = localizeHelper.localize(Res.string.accessibility),
                    items = listOf(
                        "Content descriptions for all interactive elements",
                        "Minimum 48dp touch targets",
                        "WCAG AA compliant color contrast",
                        "Proper semantic structure for screen readers"
                    )
                )
            )
        ),
        ChangelogEntry(
            version = "v0.1.29",
            sections = listOf(
                ChangelogSection(
                    title = localizeHelper.localize(Res.string.bug_fixes),
                    items = listOf(
                        "Fixed some sources not working properly"
                    )
                )
            )
        )
    )
}
