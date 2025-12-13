package ireader.presentation.ui.book.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ireader.core.source.model.Command
import ireader.core.source.model.CommandList
import ireader.core.source.model.groupByCategory
import ireader.core.util.replace
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.reusable_composable.DropDownMenu
import ireader.presentation.ui.component.reusable_composable.MidSizeTextComposable

@Composable
fun ChapterCommandBottomSheet(
    modifier: Modifier,
    onFetch: () -> Unit,
    onReset: () -> Unit,
    onUpdate: (List<Command<*>>) -> Unit,
    commandList: CommandList,
) {
    val scrollState = rememberScrollState()
    val groupedCommands = remember(commandList) { commandList.groupByCategory() }
    
    Column(
        modifier = modifier
            .padding(8.dp)
            .verticalScroll(scrollState)
    ) {
        // Action buttons
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(
                onClick = { onReset() },
                modifier = Modifier.width(92.dp),
                shape = RoundedCornerShape(4.dp)
            ) {
                MidSizeTextComposable(text = localize(Res.string.reset), color = MaterialTheme.colorScheme.primary)
            }
            Button(
                onClick = { onFetch() },
                modifier = Modifier.width(92.dp),
                shape = RoundedCornerShape(4.dp)
            ) {
                MidSizeTextComposable(text = localize(Res.string.fetch), color = MaterialTheme.colorScheme.onPrimary)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Render commands grouped by category
        groupedCommands.forEach { (category, commands) ->
            if (commands.isNotEmpty()) {
                CommandCategorySection(
                    category = category,
                    commands = commands,
                    allCommands = commandList,
                    onUpdate = onUpdate
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun CommandCategorySection(
    category: String,
    commands: List<Command<*>>,
    allCommands: CommandList,
    onUpdate: (List<Command<*>>) -> Unit
) {
    val icon = getCategoryIcon(category)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Category header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = category,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
            
            // Render each command
            commands.forEachIndexed { _, command ->
                val index = allCommands.indexOf(command)
                if (index >= 0) {
                    CommandItem(
                        command = command,
                        onUpdate = { updatedCommand ->
                            onUpdate(allCommands.replace(index, updatedCommand))
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun CommandItem(
    command: Command<*>,
    onUpdate: (Command<*>) -> Unit
) {
    when (command) {
        // Note commands (no input)
        is Command.Note -> NoteCommandItem(command.name)
        is Command.Chapter.Note -> NoteCommandItem(command.name)
        
        // Text input commands - handle all text-based commands
        is Command.Text -> GenericTextCommandItem(command.name, command.value, command.hint) { 
            command.value = it
            onUpdate(command)
        }
        is Command.Chapter.Text -> GenericTextCommandItem(command.name, command.value, "") { 
            command.value = it
            onUpdate(command)
        }
        
        // Select commands - handle all select-based commands
        is Command.Select -> GenericSelectCommandItem(command.name, command.options, command.value, command.description) {
            command.value = it
            onUpdate(command)
        }
        is Command.Chapter.Select -> GenericSelectCommandItem(command.name, command.options, command.value, "") {
            command.value = it
            onUpdate(command)
        }
        
        // Toggle commands - handle all toggle-based commands
        is Command.Toggle -> GenericToggleCommandItem(command.name, command.value, command.description) {
            command.value = it
            onUpdate(command)
        }
        
        // Range commands
        is Command.Range -> GenericRangeCommandItem(command.name, command.value, command.min, command.max) {
            command.value = it
            onUpdate(command)
        }
        
        // Fetcher commands (usually not shown in UI)
        is Command.Fetchers -> {}
        
        // Fallback for any unhandled command types
        else -> {
            Text(
                text = "${command.name}: ${command.value}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun GenericTextCommandItem(
    name: String,
    currentValue: String,
    hint: String,
    onValueChange: (String) -> Unit
) {
    var state by remember { mutableStateOf(currentValue) }
    
    LaunchedEffect(currentValue) {
        state = currentValue
    }
    
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        OutlinedTextField(
            value = state,
            onValueChange = {
                state = it
                onValueChange(it)
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(name) },
            placeholder = if (hint.isNotBlank()) {
                { Text(hint, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)) }
            } else null,
            singleLine = true,
            shape = RoundedCornerShape(8.dp)
        )
    }
}

@Composable
private fun GenericSelectCommandItem(
    name: String,
    options: Array<String>,
    currentValue: Int,
    description: String,
    onValueChange: (Int) -> Unit
) {
    var state by remember { mutableStateOf(currentValue) }
    
    LaunchedEffect(currentValue) {
        state = currentValue
    }
    
    Column {
        DropDownMenu(
            text = name,
            onSelected = { value ->
                state = value
                onValueChange(value)
            },
            currentValue = options.getOrElse(state) { "" },
            items = options
        )
        if (description.isNotBlank()) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}

@Composable
private fun GenericToggleCommandItem(
    name: String,
    currentValue: Boolean,
    description: String,
    onValueChange: (Boolean) -> Unit
) {
    var checked by remember { mutableStateOf(currentValue) }
    
    LaunchedEffect(currentValue) {
        checked = currentValue
    }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            if (description.isNotBlank()) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = { newValue ->
                checked = newValue
                onValueChange(newValue)
            }
        )
    }
}

@Composable
private fun GenericRangeCommandItem(
    name: String,
    currentValue: Int,
    min: Int,
    max: Int,
    onValueChange: (Int) -> Unit
) {
    var state by remember { mutableStateOf(currentValue.toString()) }
    
    LaunchedEffect(currentValue) {
        state = currentValue.toString()
    }
    
    OutlinedTextField(
        value = state,
        onValueChange = { newValue ->
            state = newValue
            newValue.toIntOrNull()?.let { intValue ->
                if (intValue in min..max) {
                    onValueChange(intValue)
                }
            }
        },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("$name ($min - $max)") },
        singleLine = true,
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
private fun NoteCommandItem(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.W400,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
        textAlign = TextAlign.Justify,
        style = MaterialTheme.typography.labelMedium,
    )
}

private fun getCategoryIcon(category: String): ImageVector {
    return when (category) {
        "Chapter" -> Icons.Default.FormatListBulleted
        "Content" -> Icons.Default.TextFormat
        "AI" -> Icons.Default.AutoAwesome
        "Auth" -> Icons.Default.Key
        "Batch" -> Icons.Default.Download
        "Transform" -> Icons.Default.TextFormat
        "Cache" -> Icons.Default.Cached
        "Social" -> Icons.Default.Group
        "Migration" -> Icons.Default.SwapHoriz
        "Explore" -> Icons.Default.Explore
        else -> Icons.Default.FormatListBulleted
    }
}
