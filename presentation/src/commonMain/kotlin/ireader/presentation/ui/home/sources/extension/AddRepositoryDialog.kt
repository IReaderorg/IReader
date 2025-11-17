package ireader.presentation.ui.home.sources.extension

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Dialog for adding a custom extension repository
 */
@Composable
fun AddRepositoryDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, url: String, fingerprint: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var fingerprint by remember { mutableStateOf("") }
    var showAdvanced by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.Add, contentDescription = null)
        },
        title = {
            Text("Add Repository")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Repository Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Repository URL") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("https://example.com/repo") },
                    singleLine = true
                )
                
                TextButton(
                    onClick = { showAdvanced = !showAdvanced }
                ) {
                    Text(if (showAdvanced) "Hide Advanced" else "Show Advanced")
                }
                
                if (showAdvanced) {
                    OutlinedTextField(
                        value = fingerprint,
                        onValueChange = { fingerprint = it },
                        label = { Text("Fingerprint (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("SHA-256 fingerprint") },
                        singleLine = true
                    )
                    
                    Text(
                        text = "Fingerprint verification adds an extra layer of security",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onAdd(
                        name.trim(),
                        url.trim(),
                        fingerprint.trim().takeIf { it.isNotEmpty() }
                    )
                },
                enabled = name.isNotBlank() && url.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
