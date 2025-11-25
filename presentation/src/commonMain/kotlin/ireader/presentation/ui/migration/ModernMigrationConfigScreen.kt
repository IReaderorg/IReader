package ireader.presentation.ui.migration

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.domain.models.migration.MigrationFlags
import ireader.domain.models.migration.MigrationSource

// Type alias to use ViewModel's state class
typealias MigrationConfigState = MigrationConfigScreenModel.State

/**
 * Migration Configuration Screen - Main Entry Point
 */
@Composable
fun MigrationConfigScreen(
    vm: MigrationConfigScreenModel = ireader.presentation.core.ui.getIViewModel(),
    onBack: () -> Unit = {}
) {
    val state by vm.state.collectAsState()
    
    ModernMigrationConfigScreen(
        state = state,
        onToggleSource = vm::toggleSource,
        onReorderSources = vm::reorderSources,
        onUpdateFlags = vm::updateMigrationFlags,
        onSave = vm::saveConfiguration,
        onBack = onBack
    )
}

/**
 * Modern Migration Configuration Screen - Composable Content
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernMigrationConfigScreen(
    state: MigrationConfigState,
    onToggleSource: (MigrationSource) -> Unit,
    onReorderSources: (List<MigrationSource>) -> Unit,
    onUpdateFlags: (MigrationFlags) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Migration Settings",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Configure migration behavior",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = onSave,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item {
                        ModernMigrationFlagsSection(
                            flags = state.migrationFlags,
                            onUpdateFlags = onUpdateFlags
                        )
                    }

                    item {
                        ModernMigrationSourcesSection(
                            availableSources = state.availableSources,
                            selectedSources = state.selectedSources,
                            onToggleSource = onToggleSource
                        )
                    }

                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun ModernMigrationFlagsSection(
    flags: MigrationFlags,
    onUpdateFlags: (MigrationFlags) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Outlined.Settings,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        "Migration Options",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Choose what to transfer",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            ModernMigrationFlagItem(
                icon = Icons.Outlined.MenuBook,
                title = "Chapters",
                description = "Transfer chapter list and reading progress",
                checked = flags.chapters,
                onCheckedChange = { onUpdateFlags(flags.copy(chapters = it)) }
            )

            ModernMigrationFlagItem(
                icon = Icons.Outlined.Bookmark,
                title = "Bookmarks",
                description = "Transfer bookmarked chapters",
                checked = flags.bookmarks,
                onCheckedChange = { onUpdateFlags(flags.copy(bookmarks = it)) }
            )

            ModernMigrationFlagItem(
                icon = Icons.Outlined.Category,
                title = "Categories",
                description = "Transfer book categories",
                checked = flags.categories,
                onCheckedChange = { onUpdateFlags(flags.copy(categories = it)) }
            )

            ModernMigrationFlagItem(
                icon = Icons.Outlined.Image,
                title = "Custom Cover",
                description = "Transfer custom cover image",
                checked = flags.customCover,
                onCheckedChange = { onUpdateFlags(flags.copy(customCover = it)) }
            )

            ModernMigrationFlagItem(
                icon = Icons.Outlined.AutoStories,
                title = "Reading Progress",
                description = "Transfer current reading position",
                checked = flags.readingProgress,
                onCheckedChange = { onUpdateFlags(flags.copy(readingProgress = it)) }
            )
        }
    }
}

@Composable
private fun ModernMigrationFlagItem(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (checked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else Color.Transparent
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            icon,
            null,
            tint = if (checked) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (checked) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun ModernMigrationSourcesSection(
    availableSources: List<MigrationSource>,
    selectedSources: List<MigrationSource>,
    onToggleSource: (MigrationSource) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Outlined.Source,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        "Target Sources",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${selectedSources.size} of ${availableSources.size} selected",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            if (availableSources.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Source,
                            null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            "No sources available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                availableSources.forEach { source ->
                    ModernMigrationSourceItem(
                        source = source,
                        isSelected = source in selectedSources,
                        onToggle = { onToggleSource(source) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernMigrationSourceItem(
    source: MigrationSource,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        onClick = onToggle,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 2.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary
                )
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = source.sourceName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Priority: ${source.priority}", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.height(24.dp),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        )
                    )

                    if (!source.isEnabled) {
                        AssistChip(
                            onClick = {},
                            label = { Text("Disabled", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.height(24.dp),
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                labelColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// MigrationConfigState is defined in MigrationScreens.kt to avoid redeclaration
