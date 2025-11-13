package ireader.presentation.ui.settings.font_screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.models.common.Uri
import ireader.presentation.core.util.FilePicker
import ireader.presentation.ui.component.components.PreferenceRow
import ireader.presentation.core.toComposeFontFamily
import ireader.presentation.ui.component.reusable_composable.AppIcon
import ireader.presentation.ui.reader.components.FontPicker
import ireader.presentation.ui.reader.components.ImportFontDialog

@Composable
fun FontScreen(
        vm: FontScreenViewModel,
        onFont: (String) -> Unit
) {
    var showImportDialog by remember { mutableStateOf(false) }
    var showFilePicker by remember { mutableStateOf(false) }
    var pendingFontName by remember { mutableStateOf("") }

    Column {
        if (vm.previewMode.value) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                text = """
                    Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum
                """.trimIndent(),
                maxLines = 5,
                fontFamily = vm.font?.value?.fontFamily?.toComposeFontFamily()
            )
        }

        // Show custom font picker if custom fonts are available
        if (vm.customFonts.isNotEmpty() || vm.systemFonts.isNotEmpty()) {
            FontPicker(
                selectedFontId = vm.selectedFontId.value,
                customFonts = vm.customFonts,
                systemFonts = vm.systemFonts,
                onFontSelected = { fontId ->
                    vm.onFontSelected(fontId)
                },
                onImportFont = {
                    showImportDialog = true
                },
                onDeleteFont = { fontId ->
                    vm.deleteFont(fontId)
                }
            )
        } else {
            // Fallback to original font list
            LazyColumn {
                items(count = vm.uiFonts.size) { index ->
                    PreferenceRow(
                        title = vm.uiFonts[index],
                        onClick = {
                            onFont(vm.uiFonts[index])
                        },
                        action = {
                            if (vm.uiFonts[index] == vm.font?.value?.name) {
                                AppIcon(
                                    modifier = Modifier.padding(16.dp),
                                    imageVector = Icons.Default.Check,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )
                }
            }
        }
    }
    
    // Import font dialog
    if (showImportDialog) {
        ImportFontDialog(
            onDismiss = { showImportDialog = false },
            onConfirm = { fontName ->
                pendingFontName = fontName
                showImportDialog = false
                showFilePicker = true
            }
        )
    }
    
    // File picker for selecting font file
    FilePicker(
        show = showFilePicker,
        fileExtensions = listOf("ttf", "otf"),
        onFileSelected = { uri ->
            showFilePicker = false
            if (uri != null && pendingFontName.isNotBlank()) {
                vm.importFont(pendingFontName, uri)
            }
            pendingFontName = ""
        }
    )
}
