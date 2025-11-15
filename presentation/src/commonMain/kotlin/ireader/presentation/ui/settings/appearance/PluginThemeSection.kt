package ireader.presentation.ui.settings.appearance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.plugins.PluginManager
import ireader.presentation.ui.component.components.Divider
import ireader.presentation.ui.core.theme.*
import kotlinx.coroutines.launch

/**
 * Section for displaying plugin themes in appearance settings
 * Requirements: 3.1, 3.2, 3.3, 3.5
 */
@Composable
fun PluginThemeSection(
    pluginManager: PluginManager,
    viewModel: AppearanceViewModel,
    onThemeSelected: (ThemeOption) -> Unit,
    currentThemeId: Long,
    modifier: Modifier = Modifier
) {
    val pluginExtension = rememberPluginThemeIntegration(pluginManager, viewModel)
    val allThemes by pluginExtension.getAllThemesFlow().collectAsState()
    val errors by rememberThemeErrors(pluginExtension.getErrorHandler())
    val scope = rememberCoroutineScope()
    
    // Separate plugin themes
    val pluginThemes = remember(allThemes) {
        allThemes.filterIsInstance<ThemeOption.Plugin>()
    }
    
    val lightPluginThemes = remember(pluginThemes) {
        pluginThemes.filter { !it.isDark }
    }
    
    val darkPluginThemes = remember(pluginThemes) {
        pluginThemes.filter { it.isDark }
    }
    
    // Show section only if there are plugin themes
    if (pluginThemes.isNotEmpty()) {
        Column(modifier = modifier) {
            // Section header with refresh button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Plugin Themes",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${pluginThemes.size} plugin themes available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                IconButton(
                    onClick = {
                        scope.launch {
                            pluginExtension.reloadPluginThemes()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reload plugin themes",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Error display
            AnimatedVisibility(visible = errors.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Theme Plugin Errors",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            errors.take(3).forEach { error ->
                                Text(
                                    text = error.toUserMessage(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                        TextButton(
                            onClick = { pluginExtension.getErrorHandler().clearErrors() }
                        ) {
                            Text("Dismiss")
                        }
                    }
                }
            }
            
            // Light plugin themes
            if (lightPluginThemes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Light Plugin Themes",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                PluginThemeGrid(
                    themes = lightPluginThemes,
                    currentThemeId = currentThemeId,
                    onThemeSelected = { theme ->
                        val appliedTheme = pluginExtension.applyTheme(theme)
                        viewModel.colorTheme.value = appliedTheme.id
                        viewModel.saveNightModePreferences(PreferenceValues.ThemeMode.Light)
                        onThemeSelected(theme)
                    }
                )
            }
            
            // Dark plugin themes
            if (darkPluginThemes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Dark Plugin Themes",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                PluginThemeGrid(
                    themes = darkPluginThemes,
                    currentThemeId = currentThemeId,
                    onThemeSelected = { theme ->
                        val appliedTheme = pluginExtension.applyTheme(theme)
                        viewModel.colorTheme.value = appliedTheme.id
                        viewModel.saveNightModePreferences(PreferenceValues.ThemeMode.Dark)
                        onThemeSelected(theme)
                    }
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 16.dp))
        }
    }
}

/**
 * Grid display for plugin themes
 */
@Composable
private fun PluginThemeGrid(
    themes: List<ThemeOption.Plugin>,
    currentThemeId: Long,
    onThemeSelected: (ThemeOption.Plugin) -> Unit,
    modifier: Modifier = Modifier
) {
    val gridMinSize = 180.dp
    val itemHeight = gridMinSize + 16.dp
    val columns = 2
    val rows = (themes.size + columns - 1) / columns
    val gridHeight = (itemHeight * rows) + 16.dp
    
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = gridMinSize),
        modifier = modifier
            .fillMaxWidth()
            .requiredHeight(gridHeight)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(8.dp),
        userScrollEnabled = false
    ) {
        items(items = themes, key = { it.id }) { themeOption ->
            PluginThemeCard(
                themeOption = themeOption,
                isSelected = false, // TODO: Track selected plugin theme
                onClick = { onThemeSelected(themeOption) },
                gridMinSize = gridMinSize
            )
        }
    }
}

/**
 * Card for displaying a plugin theme
 * Requirements: 3.3
 */
@Composable
private fun PluginThemeCard(
    themeOption: ThemeOption.Plugin,
    isSelected: Boolean,
    onClick: () -> Unit,
    gridMinSize: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.2f / 1f),
        onClick = onClick,
        colors = CardDefaults.elevatedCardColors(),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Plugin info
            Text(
                text = themeOption.name,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            
            Text(
                text = "by ${themeOption.plugin.manifest.author.name}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Theme preview placeholder
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Preview",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
