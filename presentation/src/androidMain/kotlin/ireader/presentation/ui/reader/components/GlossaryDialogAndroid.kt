package ireader.presentation.ui.reader.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import ireader.domain.models.entities.Glossary
import ireader.domain.models.entities.GlossaryTermType
import ireader.i18n.UiText

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
    val context = LocalContext.current
    var showExportPicker by remember { mutableStateOf(false) }
    var showImportPicker by remember { mutableStateOf(false) }
    var exportJson by remember { mutableStateOf<String?>(null) }
    
    // Export file picker
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null && exportJson != null) {
            try {
                writeToUri(context, uri, exportJson!!)
                onShowSnackBar(UiText.DynamicString("Glossary exported successfully"))
            } catch (e: Exception) {
                onShowSnackBar(UiText.ExceptionString(e))
            }
        }
        exportJson = null
        showExportPicker = false
    }
    
    // Import file picker
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val content = inputStream?.readBytes() ?: ByteArray(0)
                inputStream?.close()
                val json = content.decodeToString()
                onImportGlossary(json)
            } catch (e: Exception) {
                onShowSnackBar(UiText.ExceptionString(e))
            }
        }
        showImportPicker = false
    }
    
    // Trigger export
    if (showExportPicker && exportJson != null) {
        val fileName = "${bookTitle ?: "glossary"}_glossary.json"
        exportLauncher.launch(fileName)
    }
    
    // Trigger import
    if (showImportPicker) {
        importLauncher.launch(arrayOf("application/json", "application/*", "*/*"))
    }
    
    GlossaryDialog(
        glossaryEntries = glossaryEntries,
        onDismiss = onDismiss,
        onAddEntry = onAddEntry,
        onEditEntry = onEditEntry,
        onDeleteEntry = onDeleteEntry,
        onExport = {
            onExportGlossary { json ->
                exportJson = json
                showExportPicker = true
            }
        },
        onImport = {
            showImportPicker = true
        },
        modifier = modifier
    )
}

/**
 * Helper function to write content to URI on Android
 */
private fun writeToUri(context: Context, uri: Uri, content: String): Boolean {
    return try {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(content.toByteArray())
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}
