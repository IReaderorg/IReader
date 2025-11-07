package ireader.presentation.ui.reader.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ireader.domain.models.entities.Glossary
import ireader.domain.models.entities.GlossaryTermType
import ireader.i18n.UiText
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

@Composable
actual fun GlossaryDialogWithFilePickers(
    glossaryEntries: List<Glossary>,
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
                val fileDialog = FileDialog(null as Frame?, "Export Glossary", FileDialog.SAVE)
                fileDialog.file = "${bookTitle ?: "glossary"}_glossary.json"
                fileDialog.isVisible = true
                
                val directory = fileDialog.directory
                val filename = fileDialog.file
                
                if (directory != null && filename != null) {
                    try {
                        val file = File(directory, filename)
                        file.writeText(json)
                        onShowSnackBar(UiText.DynamicString("Glossary exported successfully"))
                    } catch (e: Exception) {
                        onShowSnackBar(UiText.ExceptionString(e))
                    }
                }
            }
        },
        onImport = {
            val fileDialog = FileDialog(null as Frame?, "Import Glossary", FileDialog.LOAD)
            fileDialog.file = "*.json"
            fileDialog.isVisible = true
            
            val directory = fileDialog.directory
            val filename = fileDialog.file
            
            if (directory != null && filename != null) {
                try {
                    val file = File(directory, filename)
                    if (file.exists()) {
                        val json = file.readText()
                        onImportGlossary(json)
                    }
                } catch (e: Exception) {
                    onShowSnackBar(UiText.ExceptionString(e))
                }
            }
        },
        modifier = modifier
    )
}
