package ireader.presentation.ui.reader.plugins

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.plugin.api.*
import ireader.presentation.ui.plugins.PluginUIRenderer
import ireader.core.log.Log
import kotlinx.coroutines.launch

/**
 * Panel that displays feature plugins in the reader screen.
 * Integrates with PluginUIProvider to render declarative plugin UI.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderPluginPanel(
    plugins: List<FeaturePlugin>,
    context: PluginScreenContext,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    initialPluginId: String? = null
) {
    val scope = rememberCoroutineScope()
    
    // Get plugins that implement PluginUIProvider
    val uiPlugins = remember(plugins) {
        plugins.filter { it is PluginUIProvider }.map { it to (it as? PluginUIProvider) }
    }
    
    // Debug logging
    LaunchedEffect(plugins, uiPlugins) {
        ireader.core.log.Log.debug { "ReaderPluginPanel: Total plugins: ${plugins.size}, UI plugins: ${uiPlugins.size}" }
        plugins.forEach { plugin ->
            ireader.core.log.Log.debug { "ReaderPluginPanel: Plugin ${plugin.manifest.id} implements PluginUIProvider: ${plugin is PluginUIProvider}" }
        }
    }
    
    var selectedPluginIndex by remember(initialPluginId) {
        mutableStateOf(
            initialPluginId?.let { id ->
                uiPlugins.indexOfFirst { (plugin, _) -> plugin.manifest.id == id }.takeIf { it >= 0 } ?: 0
            } ?: 0
        )
    }
    
    var currentScreen by remember { mutableStateOf<PluginUIScreen?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    val (selectedPlugin, uiProvider) = uiPlugins.getOrNull(selectedPluginIndex) ?: (null to null)
    
    // Load initial screen when plugin is selected
    LaunchedEffect(selectedPluginIndex, uiPlugins) {
        if (uiProvider != null) {
            isLoading = true
            error = null
            try {
                Log.debug { "ReaderPluginPanel: Loading screen for plugin ${selectedPlugin?.manifest?.id}" }
                currentScreen = uiProvider.getScreen("main", context)
                Log.debug { "ReaderPluginPanel: Screen loaded: ${currentScreen?.id}, components: ${currentScreen?.components?.size}" }
            } catch (e: Exception) {
                Log.error { "ReaderPluginPanel: Failed to load plugin screen" }
                error = e.message ?: "Failed to load plugin screen"
            }
            isLoading = false
        } else {
            Log.debug { "ReaderPluginPanel: No UI provider available" }
        }
    }
    
    // Handle events from plugin UI
    val handleEvent: (PluginUIEvent) -> Unit = { event ->
        scope.launch {
            if (uiProvider != null) {
                isLoading = true
                error = null
                try {
                    val updatedScreen = uiProvider.handleEvent("main", event, context)
                    if (updatedScreen != null) {
                        currentScreen = updatedScreen
                    }
                } catch (e: Exception) {
                    error = e.message ?: "Failed to handle event"
                }
                isLoading = false
            }
        }
    }
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Handle bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                )
            }
            
            // Header with plugin tabs
            if (uiPlugins.size > 1) {
                PluginTabRow(
                    plugins = uiPlugins.map { it.first },
                    selectedIndex = selectedPluginIndex,
                    onPluginSelected = { selectedPluginIndex = it },
                    onClose = onDismiss
                )
            } else {
                // Single plugin header
                selectedPlugin?.let { plugin ->
                    PluginHeader(
                        name = plugin.manifest.name,
                        onClose = onDismiss
                    )
                }
            }
            
            // Content area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
            ) {
                when {
                    isLoading -> {
                        LoadingContent()
                    }
                    error != null -> {
                        ErrorContent(
                            message = error ?: "Unknown error",
                            onRetry = {
                                if (uiProvider != null) {
                                    scope.launch {
                                        isLoading = true
                                        error = null
                                        try {
                                            currentScreen = uiProvider.getScreen("main", context)
                                        } catch (e: Exception) {
                                            error = e.message ?: "Failed to load plugin screen"
                                        }
                                        isLoading = false
                                    }
                                }
                            }
                        )
                    }
                    currentScreen != null -> {
                        PluginUIRenderer(
                            screen = currentScreen!!,
                            onEvent = handleEvent,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    uiPlugins.isEmpty() -> {
                        EmptyPluginsContent()
                    }
                    else -> {
                        NoPluginSelectedContent()
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Tab row for switching between plugins
 */
@Composable
private fun PluginTabRow(
    plugins: List<FeaturePlugin>,
    selectedIndex: Int,
    onPluginSelected: (Int) -> Unit,
    onClose: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Reader Tools",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }
        
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(plugins, key = { it.manifest.id }) { plugin ->
                FilterChip(
                    selected = plugins.indexOf(plugin) == selectedIndex,
                    onClick = { onPluginSelected(plugins.indexOf(plugin)) },
                    label = { Text(plugin.manifest.name) },
                    leadingIcon = {
                        Icon(
                            imageVector = getPluginIcon(plugin),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
    }
}

/**
 * Header for single plugin mode
 */
@Composable
private fun PluginHeader(
    name: String,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        IconButton(onClick = onClose) {
            Icon(Icons.Default.Close, contentDescription = "Close")
        }
    }
}

/**
 * Loading content
 */
@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "Loading...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Error content
 */
@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            TextButton(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

/**
 * Empty plugins content
 */
@Composable
private fun EmptyPluginsContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.Extension,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "No plugins available",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Install plugins to enhance your reading experience",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * No plugin selected content
 */
@Composable
private fun NoPluginSelectedContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Select a plugin to get started",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Get icon for plugin based on its manifest
 */
@Composable
private fun getPluginIcon(plugin: FeaturePlugin): ImageVector {
    val id = plugin.manifest.id.lowercase()
    return when {
        id.contains("summarizer") || id.contains("summary") -> Icons.Default.AutoAwesome
        id.contains("dictionary") -> Icons.Default.MenuBook
        id.contains("translate") -> Icons.Default.Translate
        id.contains("note") -> Icons.Default.Note
        id.contains("bookmark") -> Icons.Default.Bookmark
        id.contains("timer") -> Icons.Default.Timer
        id.contains("stats") -> Icons.Default.BarChart
        id.contains("goal") -> Icons.Default.Flag
        id.contains("quote") -> Icons.Default.FormatQuote
        id.contains("highlight") -> Icons.Default.Highlight
        else -> Icons.Default.Extension
    }
}

/**
 * FAB to open plugin panel
 */
@Composable
fun ReaderPluginFab(
    hasPlugins: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        icon = {
            Icon(
                imageVector = Icons.Default.Extension,
                contentDescription = null
            )
        },
        text = { Text("Tools") },
        modifier = modifier,
        containerColor = if (hasPlugins) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }
    )
}

/**
 * Quick access toolbar for plugins
 */
@Composable
fun ReaderPluginQuickAccess(
    plugins: List<FeaturePlugin>,
    onPluginClick: (FeaturePlugin) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiPlugins = remember(plugins) {
        plugins.filter { it is PluginUIProvider }
    }
    
    if (uiPlugins.isEmpty()) return
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            uiPlugins.forEach { plugin ->
                IconButton(
                    onClick = { onPluginClick(plugin) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = getPluginIcon(plugin),
                        contentDescription = plugin.manifest.name,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

/**
 * Plugin menu item for overflow menu
 */
@Composable
fun ReaderPluginMenuItem(
    plugin: FeaturePlugin,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenuItem(
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = getPluginIcon(plugin),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Text(plugin.manifest.name)
            }
        },
        onClick = onClick,
        modifier = modifier
    )
}
