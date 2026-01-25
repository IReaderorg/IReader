package ireader.presentation.ui.book.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.book.viewmodel.BookDetailViewModel
import ireader.presentation.ui.core.theme.LocalLocalizeHelper

@Composable
fun ChapterRangeDownloadDialog(
    vm: BookDetailViewModel,
    totalChapters: Int
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    
    if (vm.showChapterRangeDownloadDialog) {
        AlertDialog(
            onDismissRequest = { vm.hideChapterRangeDownloadDialog() },
            title = {
                Text(text = "Download Chapter Range")
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Enter the chapter numbers you want to download (1 to $totalChapters)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    OutlinedTextField(
                        value = vm.chapterRangeStart,
                        onValueChange = { vm.updateChapterRangeStart(it) },
                        label = { Text("Start Chapter") },
                        placeholder = { Text("1") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = vm.chapterRangeEnd,
                        onValueChange = { vm.updateChapterRangeEnd(it) },
                        label = { Text("End Chapter") },
                        placeholder = { Text(totalChapters.toString()) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { vm.downloadChapterRange() }
                ) {
                    Text(localizeHelper.localize(Res.string.download))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { vm.hideChapterRangeDownloadDialog() }
                ) {
                    Text(localizeHelper.localize(Res.string.cancel))
                }
            }
        )
    }
}
