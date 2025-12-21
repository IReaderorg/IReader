package ireader.presentation.ui.reader.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ireader.domain.models.entities.Glossary
import ireader.domain.models.entities.GlossaryTermType
import ireader.i18n.UiText
import ireader.i18n.resources.*
import ireader.i18n.resources.glossary_exported_successfully
import kotlinx.collections.immutable.ImmutableList
import platform.UIKit.UIPasteboard

@Composable
actual fun GlossaryDialogWithFilePickers(
    glossaryEntries: ImmutableList<Glossary>,
    bookTitle: String?,
    onDismiss: () -> Unit,
    onAddEntry: (String, String, GlossaryTermType, String?) -> Unit,
    onEditEntry: (Glossary) -> Unit,
    onDeleteEntry: (Long) -> Unit,
    onExportGlossary: ((String) -> Unit) -> Unit,
    onImportGlossary: (String) -> Unit,
    onShowSnackBar: (UiText) -> Unit,
    modifier: Modifier
) {
    GlossaryDialog(
        glossaryEntries = glossaryEntries,
        onDismiss = onDismiss,
        onAddEntry = onAddEntry,
        onEditEntry = onEditEntry,
        onDeleteEntry = onDeleteEntry,
        onExport = {
            onExportGlossary { json ->
                UIPasteboard.generalPasteboard.string = json
                onShowSnackBar(UiText.MStringResource(Res.string.glossary_exported_successfully))
            }
        },
        onImport = {
            val clipboardContent = UIPasteboard.generalPasteboard.string
            if (clipboardContent != null && clipboardContent.isNotBlank()) {
                onImportGlossary(clipboardContent)
            } else {
                onShowSnackBar(UiText.DynamicString("No content in clipboard to import"))
            }
        },
        modifier = modifier
    )
}
