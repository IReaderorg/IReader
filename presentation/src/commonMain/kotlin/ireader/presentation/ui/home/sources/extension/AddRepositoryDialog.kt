package ireader.presentation.ui.home.sources.extension

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

/**
 * Dialog for adding a custom extension repository
 */
@Composable
fun AddRepositoryDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, url: String, fingerprint: String?) -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
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
            Text(localizeHelper.localize(Res.string.add_repository))
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(localizeHelper.localize(Res.string.repository_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text(localizeHelper.localize(Res.string.repository_url)) },
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
                        label = { Text(localizeHelper.localize(Res.string.fingerprint_optional)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(localizeHelper.localize(Res.string.sha_256_fingerprint)) },
                        singleLine = true
                    )
                    
                    Text(
                        text = localizeHelper.localize(Res.string.fingerprint_verification_adds_an_extra),
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
                Text(localizeHelper.localize(Res.string.add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localizeHelper.localize(Res.string.cancel))
            }
        }
    )
}
