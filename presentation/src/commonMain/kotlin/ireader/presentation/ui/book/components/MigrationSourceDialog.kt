package ireader.presentation.ui.book.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import ireader.domain.models.entities.CatalogLocal
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*

/**
 * Dialog for selecting a target source for migration.
 * TODO: Implement full migration functionality
 * See spec: .kiro/specs/migration-feature/ (to be created)
 * 
 * Full implementation should:
 * - Display list of available sources with icons
 * - Filter sources by language
 * - Show source compatibility info
 * - Handle source selection
 * - Initiate migration process
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
            Text(
                "Source migration feature is under development.\n\n" +
                "This feature will allow you to migrate your books from one source to another, " +
                "preserving your reading progress and bookmarks.\n\n" +
                "Available sources: ${sources.size}"
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(localize(Res.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localize(Res.string.cancel))
            }
        }
    )
}
