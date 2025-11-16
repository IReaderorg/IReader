package ireader.presentation.ui.book.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.CatalogLocal
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*

/**
 * Dialog for selecting a target source for migration
 */
@Composable
fun MigrationSourceDialog(
    sources: List<CatalogLocal>,
    onDismiss: () -> Unit,
    onSourceSelected: (CatalogLocal) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(localize(Res.string.migrate)) },
        text = { 
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Select a source to migrate to:",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                if (sources.isEmpty()) {
                    Text(
                        text = "No alternative sources available",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        sources.forEach { source ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSourceSelected(source)
                                        onDismiss()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = source.name,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            text = source.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localize(Res.string.cancel))
            }
        }
    )
}
