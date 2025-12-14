package ireader.presentation.ui.sourcecreator.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp

/**
 * Section title component.
 */
@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
}

/**
 * Help text component.
 */
@Composable
fun HelpText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/**
 * Text field for rule input.
 */
@Composable
fun RuleTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    helperText: String? = null,
    singleLine: Boolean = true,
    maxLines: Int = 1
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            maxLines = maxLines
        )
        if (helperText != null) {
            Text(
                text = helperText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

/**
 * Dialog for viewing/importing JSON.
 */
@Composable
fun JsonDialog(
    jsonContent: String,
    onDismiss: () -> Unit,
    onImport: (String) -> Unit
) {
    var editableJson by remember(jsonContent) { mutableStateOf(jsonContent) }
    val clipboardManager = LocalClipboardManager.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Source JSON") },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(editableJson))
                        }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy")
                    }
                    TextButton(
                        onClick = {
                            clipboardManager.getText()?.let {
                                editableJson = it.text
                            }
                        }
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Paste")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = editableJson,
                    onValueChange = { editableJson = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    textStyle = MaterialTheme.typography.bodySmall
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "You can copy this JSON to share with others, or paste a JSON to import a source.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onImport(editableJson) }) {
                Text("Import")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

/**
 * Card for displaying a user source in a list.
 */
@Composable
fun UserSourceCard(
    sourceName: String,
    sourceUrl: String,
    sourceGroup: String,
    enabled: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sourceName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = sourceUrl,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (sourceGroup.isNotBlank()) {
                        Text(
                            text = sourceGroup,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Switch(
                    checked = enabled,
                    onCheckedChange = onToggleEnabled
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onShare) {
                    Text("Share")
                }
                TextButton(onClick = onEdit) {
                    Text("Edit")
                }
                TextButton(onClick = onDelete) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

/**
 * Rule syntax help dialog.
 */
@Composable
fun RuleSyntaxHelpDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rule Syntax Help") },
        text = {
            SelectionContainer {
                Column {
                    HelpSection("CSS Selectors", listOf(
                        "div.class - Select by class",
                        "tag#id - Select by ID",
                        "tag[attr=value] - Select by attribute",
                        "parent > child - Direct child",
                        "ancestor descendant - Any descendant"
                    ))
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    HelpSection("Attribute Extraction", listOf(
                        "selector@attr - Get attribute (e.g., a@href)",
                        "img@src - Get image source",
                        "selector@text - Get text content"
                    ))
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    HelpSection("Special Patterns", listOf(
                        "rule1||rule2 - Fallback (try rule1, then rule2)",
                        "selector.0 - First element",
                        "selector.-1 - Last element",
                        "-selector - Reverse order"
                    ))
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    HelpSection("URL Templates", listOf(
                        "{{key}} - Search keyword",
                        "{{page}} - Page number (1-indexed)",
                        "{{baseUrl}} - Base URL"
                    ))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun HelpSection(title: String, items: List<String>) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(4.dp))
    items.forEach { item ->
        Text(
            text = "• $item",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
