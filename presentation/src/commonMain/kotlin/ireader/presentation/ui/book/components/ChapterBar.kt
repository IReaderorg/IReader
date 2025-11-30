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
import androidx.compose.ui.unit.sp
import ireader.domain.models.entities.Chapter
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.book.viewmodel.BookDetailViewModel
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.core.theme.LocalLocalizeHelper

@Composable
fun ChapterBar(
        vm: BookDetailViewModel,
        chapters:List<Chapter>,
        onMap: () -> Unit,
        onSortClick: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var showDownloadMenu by remember { mutableStateOf(false) }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
        // Header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Chapter count
            Column {
                Text(
                    text = "Chapters",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )
                Text(
                    text = "${chapters.size} ${if (chapters.size == 1) "chapter" else "chapters"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    fontSize = 12.sp
                )
            }
            
            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Bulk download menu
                Box {
                    FilledTonalIconButton(
                        onClick = { showDownloadMenu = true },
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = localizeHelper.localize(Res.string.download_chapters),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showDownloadMenu,
                        onDismissRequest = { showDownloadMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(localizeHelper.localize(Res.string.download_all_unread)) },
                            onClick = {
                                showDownloadMenu = false
                                vm.downloadUnreadChapters()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(localizeHelper.localize(Res.string.download_all_un_downloaded)) },
                            onClick = {
                                showDownloadMenu = false
                                vm.downloadUndownloadedChapters()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(localizeHelper.localize(Res.string.download_all)) },
                            onClick = {
                                showDownloadMenu = false
                                vm.booksState.book?.let { book ->
                                    vm.startDownloadService(book)
                                }
                            }
                        )
                    }
                }
                
                FilledTonalIconButton(
                    onClick = { vm.searchMode = !vm.searchMode },
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = localize(Res.string.search),
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                FilledTonalIconButton(
                    onClick = onMap,
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Place,
                        contentDescription = localize(Res.string.find_current_chapter),
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                FilledTonalIconButton(
                    onClick = onSortClick,
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = localizeHelper.localize(Res.string.sort),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
        
            // Divider
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                thickness = 1.dp
            )
        }
    }
}