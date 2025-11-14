package ireader.presentation.ui.book.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.Chapter
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.book.viewmodel.BookDetailViewModel
import ireader.presentation.ui.component.reusable_composable.AppIconButton

@Composable
fun ChapterBar(
        vm: BookDetailViewModel,
        chapters:List<Chapter>,
        onMap: () -> Unit,
        onSortClick: () -> Unit
) {
    var showDownloadMenu by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier,
            text = "${chapters.size.toString()} Chapters",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Row {
            // Bulk download menu
            Box {
                AppIconButton(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Download chapters",
                    onClick = { showDownloadMenu = true }
                )
                
                DropdownMenu(
                    expanded = showDownloadMenu,
                    onDismissRequest = { showDownloadMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Download all unread") },
                        onClick = {
                            showDownloadMenu = false
                            vm.downloadUnreadChapters()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Download all un-downloaded") },
                        onClick = {
                            showDownloadMenu = false
                            vm.downloadUndownloadedChapters()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Download all") },
                        onClick = {
                            showDownloadMenu = false
                            vm.booksState.book?.let { book ->
                                vm.startDownloadService(book)
                            }
                        }
                    )
                }
            }
            
            AppIconButton(
                imageVector = Icons.Default.Search,
                contentDescription = localize(Res.string.search),
                onClick = {
                    vm.searchMode = !vm.searchMode
                },
            )
            AppIconButton(
                imageVector = Icons.Filled.Place,
                contentDescription = localize(Res.string.find_current_chapter),
                onClick = onMap
            )
            AppIconButton(
                imageVector = Icons.Default.Sort,
                onClick = onSortClick
            )
        }

    }
}