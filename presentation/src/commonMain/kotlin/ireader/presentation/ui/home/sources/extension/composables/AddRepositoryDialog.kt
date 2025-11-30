package ireader.presentation.ui.home.sources.extension.composables

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.core.theme.LocalLocalizeHelper

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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var repositoryUrl by remember { mutableStateOf("") }
    var isValidUrl by remember { mutableStateOf(true) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = localizeHelper.localize(Res.string.add_repository))
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = localizeHelper.localize(Res.string.enter_the_url_of_the),
                    style = MaterialTheme.typography.bodyMedium
                )
                
                OutlinedTextField(
                    value = repositoryUrl,
                    onValueChange = { 
                        repositoryUrl = it
                        isValidUrl = isValidRepositoryUrl(it)
                    },
                    label = { Text(localizeHelper.localize(Res.string.repository_url)) },
                    placeholder = { Text("https://example.com/repo/index.min.json") },
                    singleLine = true,
                    isError = !isValidUrl && repositoryUrl.isNotBlank(),
                    supportingText = {
                        if (!isValidUrl && repositoryUrl.isNotBlank()) {
                            Text(
                                text = localizeHelper.localize(Res.string.please_enter_a_valid_url),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(
                    text = localizeHelper.localize(Res.string.example_httpsrawgithubusercontentcomusernamerepomainindexminjson),
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
                Text(localizeHelper.localize(Res.string.add))
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
