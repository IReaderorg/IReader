package ireader.presentation.ui.sourcecreator.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import ireader.domain.usersource.help.SourceCreatorHelp

/**
 * Text field with integrated help tooltip.
 */
@Composable
fun HelpfulTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    helpTopicId: String? = null,
    placeholder: String? = null,
    supportingText: String? = null,
    isRequired: Boolean = false,
    isCode: Boolean = false,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true
) {
    var showHelp by remember { mutableStateOf(false) }
    val helpTopic = helpTopicId?.let { SourceCreatorHelp.getTopicById(it) }
    
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { 
                Text(if (isRequired) "$label *" else label)
            },
            placeholder = placeholder?.let { { Text(it) } },
            supportingText = supportingText?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            textStyle = if (isCode) {
                LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
            } else {
                LocalTextStyle.current
            },
            trailingIcon = if (helpTopic != null) {
                {
                    IconButton(onClick = { showHelp = !showHelp }) {
                        Icon(
                            if (showHelp) Icons.Default.Close else Icons.Default.Help,
                            contentDescription = "Help",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } else null
        )
        
        // Inline help
        AnimatedVisibility(visible = showHelp && helpTopic != null) {
            helpTopic?.let { topic ->
                InlineHelpCard(
                    topic = topic,
                    onDismiss = { showHelp = false }
                )
            }
        }
    }
}

/**
 * Inline help card that appears below a field.
 */
@Composable
fun InlineHelpCard(
    topic: SourceCreatorHelp.HelpTopic,
    onDismiss: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = topic.title,
                    style = MaterialTheme.typography.titleSmall
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = topic.shortDescription,
                style = MaterialTheme.typography.bodySmall
            )
            
            // Show first example if available
            if (topic.examples.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                val example = topic.examples.first()
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.small
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = example.description,
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = example.code,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }
            }
            
            // Show first tip if available
            if (topic.tips.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = topic.tips.first(),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

/**
 * Floating help button that shows contextual help.
 */
@Composable
fun FloatingHelpButton(
    helpTopicId: String,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    val topic = SourceCreatorHelp.getTopicById(helpTopicId)
    
    IconButton(
        onClick = { showDialog = true },
        modifier = modifier
    ) {
        Icon(
            Icons.Default.HelpOutline,
            contentDescription = "Help",
            tint = MaterialTheme.colorScheme.primary
        )
    }
    
    if (showDialog && topic != null) {
        HelpDialog(
            topic = topic,
            onDismiss = { showDialog = false }
        )
    }
}

/**
 * Full help dialog with detailed information.
 */
@Composable
fun HelpDialog(
    topic: SourceCreatorHelp.HelpTopic,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(topic.title) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = topic.content,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                if (topic.examples.isNotEmpty()) {
                    Text(
                        text = "Examples:",
                        style = MaterialTheme.typography.titleSmall
                    )
                    topic.examples.take(3).forEach { example ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = example.description,
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    text = example.code,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace
                                    )
                                )
                                if (example.result != null) {
                                    Text(
                                        text = "→ ${example.result}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got it")
            }
        }
    )
}

/**
 * Error message with suggested fixes.
 */
@Composable
fun ErrorWithSuggestions(
    errorType: String,
    modifier: Modifier = Modifier
) {
    val fixes = SourceCreatorHelp.ErrorMessages.commonFixes[errorType]
    
    if (fixes != null) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
            ),
            modifier = modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = errorType,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Try these fixes:",
                    style = MaterialTheme.typography.labelMedium
                )
                
                fixes.forEach { fix ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = fix,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

/**
 * Selector input with suggestions dropdown.
 */
@Composable
fun SelectorInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    suggestions: List<Pair<String, String>>,
    helpTopicId: String? = null,
    isRequired: Boolean = false,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }
    val helpTopic = helpTopicId?.let { SourceCreatorHelp.getTopicById(it) }
    
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(if (isRequired) "$label *" else label) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
            trailingIcon = {
                Row {
                    if (helpTopic != null) {
                        IconButton(onClick = { showHelp = !showHelp }) {
                            Icon(
                                Icons.Default.Help,
                                contentDescription = "Help",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Suggestions"
                        )
                    }
                }
            }
        )
        
        // Suggestions dropdown
        AnimatedVisibility(visible = expanded) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "Common selectors:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    suggestions.forEach { (selector, desc) ->
                        TextButton(
                            onClick = {
                                onValueChange(selector)
                                expanded = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    text = selector,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FontFamily.Monospace
                                    )
                                )
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Inline help
        AnimatedVisibility(visible = showHelp && helpTopic != null) {
            helpTopic?.let { topic ->
                InlineHelpCard(
                    topic = topic,
                    onDismiss = { showHelp = false }
                )
            }
        }
    }
}
