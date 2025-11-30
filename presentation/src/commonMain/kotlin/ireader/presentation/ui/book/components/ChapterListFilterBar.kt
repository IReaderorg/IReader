package ireader.presentation.ui.book.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.book.viewmodel.ChaptersFilters
import ireader.presentation.ui.core.theme.LocalLocalizeHelper

@Composable
fun ChapterListFilterBar(
    filters: List<ChaptersFilters>,
    onToggleFilter: (ChaptersFilters.Type) -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var showFilterMenu by remember { mutableStateOf(false) }
    
    // Count active filters
    val activeFilterCount = filters.count { it.value != ChaptersFilters.Value.Missing }
    
    // Only show if there are active filters or user wants to add filters
    if (activeFilterCount > 0 || showFilterMenu) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Filter chips for quick access
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Hide Read Chapters chip
                    val hideReadFilter = filters.find { it.type == ChaptersFilters.Type.Read }
                    if (hideReadFilter?.value == ChaptersFilters.Value.Excluded) {
                        CompactFilterChip(
                            label = localizeHelper.localize(Res.string.hide_read),
                            onClick = { onToggleFilter(ChaptersFilters.Type.Read) }
                        )
                    }
                    
                    // Hide Duplicate Chapters chip
                    val hideDuplicateFilter = filters.find { it.type == ChaptersFilters.Type.Duplicate }
                    if (hideDuplicateFilter?.value == ChaptersFilters.Value.Excluded) {
                        CompactFilterChip(
                            label = localizeHelper.localize(Res.string.hide_duplicates),
                            onClick = { onToggleFilter(ChaptersFilters.Type.Duplicate) }
                        )
                    }
                    
                    // Show other active filters
                    filters.forEach { filter ->
                        if (filter.value == ChaptersFilters.Value.Included && 
                            filter.type != ChaptersFilters.Type.Read && 
                            filter.type != ChaptersFilters.Type.Duplicate) {
                            CompactFilterChip(
                                label = getShortFilterLabel(filter.type),
                                onClick = { onToggleFilter(filter.type) }
                            )
                        }
                    }
                }
                
                // Filter menu button with badge
                Box {
                    FilledTonalIconButton(
                        onClick = { showFilterMenu = true },
                        modifier = Modifier.size(32.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        BadgedBox(
                            badge = {
                                if (activeFilterCount > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.offset(x = 4.dp, y = (-4).dp)
                                    ) {
                                        Text(
                                            text = activeFilterCount.toString(),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = localizeHelper.localize(Res.string.more_filters),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    
                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false }
                    ) {
                        Text(
                            text = localizeHelper.localize(Res.string.chapter_filters),
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        HorizontalDivider()
                        
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
    }
}

@Composable
private fun CompactFilterChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
            )
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Close,
                contentDescription = localizeHelper.localize(Res.string.remove_filter),
                modifier = Modifier.size(12.dp)
            )
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
private fun getShortFilterLabel(type: ChaptersFilters.Type): String {
    return when (type) {
        ChaptersFilters.Type.Unread -> "Unread"
        ChaptersFilters.Type.Downloaded -> "Downloaded"
        ChaptersFilters.Type.Bookmarked -> "Bookmarked"
        ChaptersFilters.Type.Read -> "Read"
        ChaptersFilters.Type.Duplicate -> "Duplicates"
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
