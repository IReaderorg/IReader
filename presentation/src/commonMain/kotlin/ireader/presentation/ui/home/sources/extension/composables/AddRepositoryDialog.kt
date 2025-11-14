package ireader.presentation.ui.home.sources.extension.composables

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*

/**
 * Dialog for adding a new repository
 * @param onDismiss Callback when dialog is dismissed
 * @param onAdd Callback when repository is added with URL
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRepositoryDialog(
    onDismiss: () -> Unit,
    onAdd: (url: String) -> Unit
) {
    var repositoryUrl by remember { mutableStateOf("") }
    var isValidUrl by remember { mutableStateOf(true) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Add Repository")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Enter the URL of the repository you want to add. The repository should contain an index.min.json file with available sources.",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                OutlinedTextField(
                    value = repositoryUrl,
                    onValueChange = { 
                        repositoryUrl = it
                        isValidUrl = isValidRepositoryUrl(it)
                    },
                    label = { Text("Repository URL") },
                    placeholder = { Text("https://example.com/repo/index.min.json") },
                    singleLine = true,
                    isError = !isValidUrl && repositoryUrl.isNotBlank(),
                    supportingText = {
                        if (!isValidUrl && repositoryUrl.isNotBlank()) {
                            Text(
                                text = "Please enter a valid URL",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(
                    text = "Example: https://raw.githubusercontent.com/username/repo/main/index.min.json",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isValidUrl && repositoryUrl.isNotBlank()) {
                        onAdd(repositoryUrl)
                    }
                },
                enabled = isValidUrl && repositoryUrl.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localize(Res.string.cancel))
            }
        }
    )
}

/**
 * Validate if the URL is a valid repository URL
 */
private fun isValidRepositoryUrl(url: String): Boolean {
    if (url.isBlank()) return true
    
    return try {
        url.startsWith("http://") || url.startsWith("https://")
    } catch (e: Exception) {
        false
    }
}
