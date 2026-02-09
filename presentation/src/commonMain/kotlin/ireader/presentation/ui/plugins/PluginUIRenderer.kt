package ireader.presentation.ui.plugins

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ireader.i18n.resources.Res
import ireader.i18n.resources.delete
import ireader.plugin.api.ButtonStyle
import ireader.plugin.api.PluginUIComponent
import ireader.plugin.api.PluginUIEvent
import ireader.plugin.api.PluginUIScreen
import ireader.plugin.api.TextStyle
import ireader.plugin.api.UIEventType
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import kotlinx.coroutines.launch

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
        items(
            items = screen.components,
            key = { component ->
                // Use stable key based on component type and id
                when (component) {
                    is PluginUIComponent.TextField -> "textfield_${component.id}"
                    is PluginUIComponent.Button -> "button_${component.id}"
                    is PluginUIComponent.Switch -> "switch_${component.id}"
                    is PluginUIComponent.Chip -> "chip_${component.id}"
                    is PluginUIComponent.ChipGroup -> "chipgroup_${component.id}"
                    is PluginUIComponent.ItemList -> "list_${component.id}"
                    is PluginUIComponent.Text -> "text_${component.text.hashCode()}"
                    is PluginUIComponent.Card -> "card_${component.hashCode()}"
                    is PluginUIComponent.Row -> "row_${component.hashCode()}"
                    is PluginUIComponent.Column -> "column_${component.hashCode()}"
                    is PluginUIComponent.Tabs -> "tabs_${component.hashCode()}"
                    is PluginUIComponent.Loading -> "loading_${component.message?.hashCode() ?: 0}"
                    is PluginUIComponent.Empty -> "empty_${component.message.hashCode()}"
                    is PluginUIComponent.Error -> "error_${component.message.hashCode()}"
                    is PluginUIComponent.Spacer -> "spacer_${component.height}"
                    is PluginUIComponent.Divider -> "divider_${component.thickness}"
                    is PluginUIComponent.ProgressBar -> "progress_${component.label?.hashCode() ?: 0}"
                    is PluginUIComponent.Image -> "image_${component.url.hashCode()}"
                }
            }
        ) { component ->
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
    // Use remember with key to maintain state during recomposition
    var value by remember(component.id) { mutableStateOf(component.value) }
    
    // Debounce job tracking
    var debounceJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    val scope = rememberCoroutineScope()
    
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            value = newValue
            // Cancel previous debounce job and start a new one
            debounceJob?.cancel()
            debounceJob = scope.launch {
                kotlinx.coroutines.delay(500) // 500ms debounce
                onEvent(PluginUIEvent(
                    componentId = component.id,
                    eventType = UIEventType.TEXT_CHANGED,
                    data = mapOf("value" to newValue)
                ))
            }
        },
        label = { Text(component.label) },
        modifier = Modifier.fillMaxWidth(),
        maxLines = if (component.multiline) component.maxLines else 1,
        minLines = if (component.multiline) 2 else 1,
        singleLine = !component.multiline
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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
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
                            Icon(Icons.Default.Delete, contentDescription = localizeHelper.localize(Res.string.delete),
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
    // Use remember with key to maintain state during recomposition
    var checked by remember(component.id) { mutableStateOf(component.checked) }
    
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
