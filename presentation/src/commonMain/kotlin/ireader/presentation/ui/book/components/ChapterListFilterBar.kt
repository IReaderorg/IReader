package ireader.presentation.ui.book.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.book.viewmodel.ChaptersFilters

@Composable
fun ChapterListFilterBar(
    filters: List<ChaptersFilters>,
    onToggleFilter: (ChaptersFilters.Type) -> Unit,
    modifier: Modifier = Modifier
) {
    var showFilterMenu by remember { mutableStateOf(false) }
    
    // Count active filters
    val activeFilterCount = filters.count { it.value != ChaptersFilters.Value.Missing }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Filter chips for quick access
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Hide Read Chapters chip
            val hideReadFilter = filters.find { it.type == ChaptersFilters.Type.Read }
            FilterChip(
                selected = hideReadFilter?.value == ChaptersFilters.Value.Excluded,
                onClick = { onToggleFilter(ChaptersFilters.Type.Read) },
                label = { Text("Hide Read") }
            )
            
            // Hide Duplicate Chapters chip
            val hideDuplicateFilter = filters.find { it.type == ChaptersFilters.Type.Duplicate }
            FilterChip(
                selected = hideDuplicateFilter?.value == ChaptersFilters.Value.Excluded,
                onClick = { onToggleFilter(ChaptersFilters.Type.Duplicate) },
                label = { Text("Hide Duplicates") }
            )
        }
        
        // Filter menu button with badge
        Box {
            IconButton(onClick = { showFilterMenu = true }) {
                BadgedBox(
                    badge = {
                        if (activeFilterCount > 0) {
                            Badge {
                                Text(activeFilterCount.toString())
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "More filters"
                    )
                }
            }
            
            DropdownMenu(
                expanded = showFilterMenu,
                onDismissRequest = { showFilterMenu = false }
            ) {
                Text(
                    text = "Chapter Filters",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                Divider()
                
                filters.forEach { filter ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(getFilterLabel(filter.type))
                                Text(
                                    text = getFilterValueLabel(filter.value),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = when (filter.value) {
                                        ChaptersFilters.Value.Included -> MaterialTheme.colorScheme.primary
                                        ChaptersFilters.Value.Excluded -> MaterialTheme.colorScheme.error
                                        ChaptersFilters.Value.Missing -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        },
                        onClick = {
                            onToggleFilter(filter.type)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun getFilterLabel(type: ChaptersFilters.Type): String {
    return when (type) {
        ChaptersFilters.Type.Unread -> "Unread Chapters"
        ChaptersFilters.Type.Downloaded -> "Downloaded Chapters"
        ChaptersFilters.Type.Bookmarked -> "Bookmarked Chapters"
        ChaptersFilters.Type.Read -> "Read Chapters"
        ChaptersFilters.Type.Duplicate -> "Duplicate Chapters"
    }
}

@Composable
private fun getFilterValueLabel(value: ChaptersFilters.Value): String {
    return when (value) {
        ChaptersFilters.Value.Included -> "Show Only"
        ChaptersFilters.Value.Excluded -> "Hide"
        ChaptersFilters.Value.Missing -> "Show All"
    }
}
