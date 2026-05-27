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

/**
 * Dialog for downloading next N chapters from a selected starting point.
 * 
 * Allows users to:
 * - Select a starting chapter
 * - Specify how many chapters to download
 * - Download all remaining chapters from the starting point
 */
@Composable
fun DownloadNextChaptersDialog(
    vm: BookDetailViewModel,
    totalChapters: Int,
    currentChapterIndex: Int = 0
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    
    if (vm.showDownloadNextChaptersDialog) {
        AlertDialog(
            onDismissRequest = { vm.hideDownloadNextChaptersDialog() },
            title = {
                Text(text = "Download Next Chapters")
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Download chapters starting from a specific point. " +
                               "Total available: $totalChapters chapters",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Start chapter input
                    OutlinedTextField(
                        value = vm.downloadNextStartChapter,
                        onValueChange = { vm.updateDownloadNextStartChapter(it) },
                        label = { Text("Start From Chapter") },
                        placeholder = { Text((currentChapterIndex + 1).toString()) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        supportingText = { 
                            Text("Chapter number to start downloading from (1-$totalChapters)")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Number of chapters input
                    OutlinedTextField(
                        value = vm.downloadNextChapterCount,
                        onValueChange = { vm.updateDownloadNextChapterCount(it) },
                        label = { Text("Number of Chapters") },
                        placeholder = { Text("10") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        supportingText = { 
                            Text("How many chapters to download (or leave empty for all remaining)")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Quick action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Download next 5 chapters
                        OutlinedButton(
                            onClick = {
                                vm.updateDownloadNextStartChapter((currentChapterIndex + 1).toString())
                                vm.updateDownloadNextChapterCount("5")
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Next 5", style = MaterialTheme.typography.labelMedium)
                        }
                        
                        // Download next 10 chapters
                        OutlinedButton(
                            onClick = {
                                vm.updateDownloadNextStartChapter((currentChapterIndex + 1).toString())
                                vm.updateDownloadNextChapterCount("10")
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Next 10", style = MaterialTheme.typography.labelMedium)
                        }
                        
                        // Download next 20 chapters
                        OutlinedButton(
                            onClick = {
                                vm.updateDownloadNextStartChapter((currentChapterIndex + 1).toString())
                                vm.updateDownloadNextChapterCount("20")
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Next 20", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    
                    // All remaining option
                    OutlinedButton(
                        onClick = {
                            vm.updateDownloadNextStartChapter((currentChapterIndex + 1).toString())
                            vm.updateDownloadNextChapterCount("")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("All Remaining Chapters")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { vm.downloadNextChapters() }
                ) {
                    Text(localizeHelper.localize(Res.string.download))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { vm.hideDownloadNextChaptersDialog() }
                ) {
                    Text(localizeHelper.localize(Res.string.cancel))
                }
            }
        )
    }
}
