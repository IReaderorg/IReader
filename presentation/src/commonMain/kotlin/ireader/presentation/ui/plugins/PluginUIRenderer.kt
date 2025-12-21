package ireader.presentation.ui.plugins

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ireader.plugin.api.*

/**
 * Renders declarative UI components from plugins.
 * Plugins define their UI using PluginUIComponent data classes,
 * and this renderer converts them to Compose UI.
 */
@Composable
fun PluginUIRenderer(
    screen: PluginUIScreen,
    onEvent: (PluginUIEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(screen.components) { component ->
            RenderComponent(component, onEvent)
        }
    }
}

@Composable
private fun RenderComponent(
    component: PluginUIComponent,
    onEvent: (PluginUIEvent) -> Unit
) {
    when (component) {
        is PluginUIComponent.Text -> RenderText(component)
        is PluginUIComponent.TextField -> RenderTextField(component, onEvent)
        is PluginUIComponent.Button -> RenderButton(component, onEvent)
        is PluginUIComponent.Card -> RenderCard(component, onEvent)
        is PluginUIComponent.Row -> RenderRow(component, onEvent)
        is PluginUIComponent.Column -> RenderColumn(component, onEvent)
        is PluginUIComponent.ItemList -> RenderList(component, onEvent)
        is PluginUIComponent.Tabs -> RenderTabs(component, onEvent)
        is PluginUIComponent.Switch -> RenderSwitch(component, onEvent)
        is PluginUIComponent.Chip -> RenderChip(component, onEvent)
        is PluginUIComponent.ChipGroup -> RenderChipGroup(component, onEvent)
        is PluginUIComponent.Loading -> RenderLoading(component)
        is PluginUIComponent.Empty -> RenderEmpty(component)
        is PluginUIComponent.Error -> RenderError(component)
        is PluginUIComponent.Spacer -> Spacer(Modifier.height(component.height.dp))
        is PluginUIComponent.Divider -> HorizontalDivider(thickness = component.thickness.dp)
        is PluginUIComponent.ProgressBar -> RenderProgressBar(component)
        is PluginUIComponent.Image -> RenderImage(component)
    }
}

@Composable
private fun RenderText(component: PluginUIComponent.Text) {
    val style = when (component.style) {
        TextStyle.TITLE_LARGE -> MaterialTheme.typography.titleLarge
        TextStyle.TITLE_MEDIUM -> MaterialTheme.typography.titleMedium
        TextStyle.TITLE_SMALL -> MaterialTheme.typography.titleSmall
        TextStyle.BODY -> MaterialTheme.typography.bodyMedium
        TextStyle.BODY_SMALL -> MaterialTheme.typography.bodySmall
        TextStyle.LABEL -> MaterialTheme.typography.labelMedium
    }
    Text(text = component.text, style = style)
}

@Composable
private fun RenderTextField(
    component: PluginUIComponent.TextField,
    onEvent: (PluginUIEvent) -> Unit
) {
    var value by remember(component.value) { mutableStateOf(component.value) }
    
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            value = newValue
            onEvent(PluginUIEvent(
                componentId = component.id,
                eventType = UIEventType.TEXT_CHANGED,
                data = mapOf("value" to newValue)
            ))
        },
        label = { Text(component.label) },
        modifier = Modifier.fillMaxWidth(),
        maxLines = if (component.multiline) component.maxLines else 1,
        minLines = if (component.multiline) 2 else 1
    )
}

@Composable
private fun RenderButton(
    component: PluginUIComponent.Button,
    onEvent: (PluginUIEvent) -> Unit
) {
    val onClick = {
        onEvent(PluginUIEvent(
            componentId = component.id,
            eventType = UIEventType.CLICK
        ))
    }
    
    when (component.style) {
        ButtonStyle.PRIMARY -> {
            Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
                component.icon?.let {
                    Icon(getIcon(it), contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                }
                if (component.label.isNotBlank()) Text(component.label)
            }
        }
        ButtonStyle.SECONDARY -> {
            FilledTonalButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
                component.icon?.let {
                    Icon(getIcon(it), contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                }
                if (component.label.isNotBlank()) Text(component.label)
            }
        }
        ButtonStyle.OUTLINED -> {
            OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
                component.icon?.let {
                    Icon(getIcon(it), contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                }
                if (component.label.isNotBlank()) Text(component.label)
            }
        }
        ButtonStyle.TEXT -> {
            TextButton(onClick = onClick) {
                component.icon?.let {
                    Icon(getIcon(it), contentDescription = null, modifier = Modifier.size(18.dp))
                    if (component.label.isNotBlank()) Spacer(Modifier.width(4.dp))
                }
                if (component.label.isNotBlank()) Text(component.label)
            }
        }
    }
}

@Composable
private fun RenderCard(
    component: PluginUIComponent.Card,
    onEvent: (PluginUIEvent) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            component.children.forEach { child ->
                RenderComponent(child, onEvent)
            }
        }
    }
}

@Composable
private fun RenderRow(
    component: PluginUIComponent.Row,
    onEvent: (PluginUIEvent) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(component.spacing.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        component.children.forEach { child ->
            RenderComponent(child, onEvent)
        }
    }
}

@Composable
private fun RenderColumn(
    component: PluginUIComponent.Column,
    onEvent: (PluginUIEvent) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(component.spacing.dp)) {
        component.children.forEach { child ->
            RenderComponent(child, onEvent)
        }
    }
}

@Composable
private fun RenderList(
    component: PluginUIComponent.ItemList,
    onEvent: (PluginUIEvent) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        component.items.forEach { item ->
            ListItem(
                headlineContent = { Text(item.title) },
                supportingContent = item.subtitle?.let { { Text(it) } },
                leadingContent = item.icon?.let { { Icon(getIcon(it), contentDescription = null) } },
                trailingContent = item.trailing?.let { trailingId ->
                    {
                        IconButton(onClick = {
                            onEvent(PluginUIEvent(
                                componentId = trailingId,
                                eventType = UIEventType.CLICK
                            ))
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                modifier = Modifier.clickable {
                    onEvent(PluginUIEvent(
                        componentId = component.id,
                        eventType = UIEventType.LIST_ITEM_CLICKED,
                        data = mapOf("itemId" to item.id)
                    ))
                }
            )
        }
    }
}

@Composable
private fun RenderTabs(
    component: PluginUIComponent.Tabs,
    onEvent: (PluginUIEvent) -> Unit
) {
    var selectedIndex by remember { mutableStateOf(0) }
    
    Column {
        TabRow(selectedTabIndex = selectedIndex) {
            component.tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedIndex == index,
                    onClick = {
                        selectedIndex = index
                        onEvent(PluginUIEvent(
                            componentId = tab.id,
                            eventType = UIEventType.TAB_SELECTED,
                            data = mapOf("index" to index.toString())
                        ))
                    },
                    text = { Text(tab.title) },
                    icon = tab.icon?.let { { Icon(getIcon(it), contentDescription = null) } }
                )
            }
        }
        
        // Render selected tab content
        if (selectedIndex < component.tabs.size) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                component.tabs[selectedIndex].content.forEach { child ->
                    RenderComponent(child, onEvent)
                }
            }
        }
    }
}

@Composable
private fun RenderSwitch(
    component: PluginUIComponent.Switch,
    onEvent: (PluginUIEvent) -> Unit
) {
    var checked by remember(component.checked) { mutableStateOf(component.checked) }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(component.label)
        Switch(
            checked = checked,
            onCheckedChange = { newValue ->
                checked = newValue
                onEvent(PluginUIEvent(
                    componentId = component.id,
                    eventType = UIEventType.SWITCH_TOGGLED,
                    data = mapOf("checked" to newValue.toString())
                ))
            }
        )
    }
}

@Composable
private fun RenderChip(
    component: PluginUIComponent.Chip,
    onEvent: (PluginUIEvent) -> Unit
) {
    FilterChip(
        selected = component.selected,
        onClick = {
            onEvent(PluginUIEvent(
                componentId = component.id,
                eventType = UIEventType.CHIP_SELECTED,
                data = mapOf("value" to component.id)
            ))
        },
        label = { Text(component.label) }
    )
}

@Composable
private fun RenderChipGroup(
    component: PluginUIComponent.ChipGroup,
    onEvent: (PluginUIEvent) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        component.chips.forEach { chip ->
            FilterChip(
                selected = chip.selected,
                onClick = {
                    onEvent(PluginUIEvent(
                        componentId = component.id,
                        eventType = UIEventType.CHIP_SELECTED,
                        data = mapOf("value" to chip.id)
                    ))
                },
                label = { Text(chip.label) }
            )
        }
    }
}

@Composable
private fun RenderLoading(component: PluginUIComponent.Loading) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CircularProgressIndicator()
        component.message?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RenderEmpty(component: PluginUIComponent.Empty) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        component.icon?.let {
            Icon(getIcon(it), contentDescription = null, modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        }
        Text(component.message, style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        component.description?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun RenderError(component: PluginUIComponent.Error) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Error, contentDescription = null,
                tint = MaterialTheme.colorScheme.error)
            Text(component.message, color = MaterialTheme.colorScheme.onErrorContainer)
        }
    }
}

@Composable
private fun RenderProgressBar(component: PluginUIComponent.ProgressBar) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        component.label?.let {
            Text(it, style = MaterialTheme.typography.bodySmall)
        }
        LinearProgressIndicator(
            progress = { component.progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun RenderImage(component: PluginUIComponent.Image) {
    // Placeholder for image - actual implementation would use Coil/Kamel
    val width = component.width
    val height = component.height
    Box(
        modifier = Modifier
            .then(if (width != null) Modifier.width(width.dp) else Modifier)
            .then(if (height != null) Modifier.height(height.dp) else Modifier)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Image,
            contentDescription = component.contentDescription,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

/**
 * Map icon names to Material Icons
 */
private fun getIcon(name: String): ImageVector {
    return when (name) {
        "notes", "note_add" -> Icons.Default.Notes
        "people", "person_add" -> Icons.Default.People
        "person" -> Icons.Default.Person
        "timeline" -> Icons.Default.Timeline
        "auto_awesome" -> Icons.Default.AutoAwesome
        "list" -> Icons.Default.List
        "history" -> Icons.Default.History
        "settings" -> Icons.Default.Settings
        "save" -> Icons.Default.Save
        "delete" -> Icons.Default.Delete
        "add" -> Icons.Default.Add
        "refresh" -> Icons.Default.Refresh
        "close" -> Icons.Default.Close
        "check" -> Icons.Default.Check
        "error" -> Icons.Default.Error
        "warning" -> Icons.Default.Warning
        "info" -> Icons.Default.Info
        "search" -> Icons.Default.Search
        "bookmark", "bookmark_add", "bookmarks" -> Icons.Default.Bookmark
        "label" -> Icons.Default.Label
        "folder" -> Icons.Default.Folder
        "highlight", "format_quote" -> Icons.Default.FormatQuote
        "timer", "hourglass_empty" -> Icons.Default.Timer
        "play_arrow" -> Icons.Default.PlayArrow
        "stop" -> Icons.Default.Stop
        "analytics", "trending_up" -> Icons.Default.TrendingUp
        "local_fire_department" -> Icons.Default.LocalFireDepartment
        "flag" -> Icons.Default.Flag
        "emoji_events" -> Icons.Default.EmojiEvents
        "school" -> Icons.Default.School
        "book" -> Icons.Default.Book
        "download_done" -> Icons.Default.DownloadDone
        "extension" -> Icons.Default.Extension
        "coffee" -> Icons.Default.Coffee
        "image" -> Icons.Default.Image
        else -> Icons.Default.Extension
    }
}
