package ireader.presentation.ui.home.library.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.domain.models.library.LibraryFilter
import ireader.domain.models.library.LibrarySort
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.IBackHandler

/**
 * Bottom sheet for library filtering and sorting options
 * Implements real-time updates and clear visual feedback
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryFilterBottomSheet(
    filters: List<LibraryFilter>,
    sorting: LibrarySort,
    columnCount: Int,
    displayMode: ireader.domain.models.DisplayMode = ireader.domain.models.DisplayMode.CompactGrid,
    showResumeReadingCard: Boolean = true,
    showArchivedBooks: Boolean = false,
    onFilterToggle: (LibraryFilter.Type) -> Unit,
    onSortChange: (LibrarySort.Type) -> Unit,
    onSortDirectionToggle: () -> Unit,
    onColumnCountChange: (Int) -> Unit,
    onDisplayModeChange: (ireader.domain.models.DisplayMode) -> Unit = {},
    onResumeReadingCardToggle: (Boolean) -> Unit = {},
    onArchivedBooksToggle: (Boolean) -> Unit = {},
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,

) {
    // Handle back button to dismiss the sheet
    IBackHandler(enabled = true, onBack = onDismiss)
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = false
        ),
        dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BottomSheetDefaults.DragHandle()
                Text(
                    text = localize(Res.string.filter_and_sort),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            // Filter Section
            FilterSection(
                filters = filters,
                onFilterToggle = onFilterToggle
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            
            // Sort Section
            SortSection(
                sorting = sorting,
                onSortChange = onSortChange,
                onSortDirectionToggle = onSortDirectionToggle
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            
            // Display Section
            DisplaySection(
                columnCount = columnCount,
                displayMode = displayMode,
                onColumnCountChange = onColumnCountChange,
                onDisplayModeChange = onDisplayModeChange,
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            
            // General Settings Section
            GeneralSection(
                showResumeReadingCard = showResumeReadingCard,
                showArchivedBooks = showArchivedBooks,
                onResumeReadingCardToggle = onResumeReadingCardToggle,
                onArchivedBooksToggle = onArchivedBooksToggle
            )

        }
    }
}

@Composable
private fun GeneralSection(
    showResumeReadingCard: Boolean,
    showArchivedBooks: Boolean,
    onResumeReadingCardToggle: (Boolean) -> Unit,
    onArchivedBooksToggle: (Boolean) -> Unit
) {
    Text(
        text = localize(Res.string.general),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 12.dp)
    )
    
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = showResumeReadingCard,
            onClick = { onResumeReadingCardToggle(!showResumeReadingCard) },
            label = {
                Text(
                    text = "Show Continue Reading Bar",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            leadingIcon = if (showResumeReadingCard) {
                {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else null,
            modifier = Modifier.fillMaxWidth()
        )
        
        FilterChip(
            selected = showArchivedBooks,
            onClick = { onArchivedBooksToggle(!showArchivedBooks) },
            label = {
                Text(
                    text = "Show Archived Books",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            leadingIcon = if (showArchivedBooks) {
                {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else null,
            modifier = Modifier.fillMaxWidth()
        )
    }
}



@Composable
private fun FilterSection(
    filters: List<LibraryFilter>,
    onFilterToggle: (LibraryFilter.Type) -> Unit
) {
    Text(
        text = localize(Res.string.filter),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 12.dp)
    )
    
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LibraryFilter.Type.values().forEach { type ->
            val filter = filters.find { it.type == type }
            val isActive = filter?.value == LibraryFilter.Value.Included
            
            FilterChip(
                selected = isActive,
                onClick = { onFilterToggle(type) },
                label = {
                    Text(
                        text = getFilterLabel(type),
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                leadingIcon = if (isActive) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else null,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SortSection(
    sorting: LibrarySort,
    onSortChange: (LibrarySort.Type) -> Unit,
    onSortDirectionToggle: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = localize(Res.string.sort),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        
        // Sort direction toggle button
        IconButton(
            onClick = onSortDirectionToggle
        ) {
            Icon(
                imageVector = if (sorting.isAscending) {
                    Icons.Default.ArrowUpward
                } else {
                    Icons.Default.ArrowDownward
                },
                contentDescription = if (sorting.isAscending) {
                    localize(Res.string.ascending)
                } else {
                    localize(Res.string.descending)
                },
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
    
    Spacer(modifier = Modifier.height(12.dp))
    
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LibrarySort.Type.values().forEach { type ->
            val isActive = sorting.type == type
            
            FilterChip(
                selected = isActive,
                onClick = { onSortChange(type) },
                label = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text =LibrarySort.Type.name(type).toString(),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        
                        if (isActive) {
                            Icon(
                                imageVector = if (sorting.isAscending) {
                                    Icons.Default.ArrowUpward
                                } else {
                                    Icons.Default.ArrowDownward
                                },
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun DisplaySection(
    columnCount: Int,
    displayMode: ireader.domain.models.DisplayMode,
    onColumnCountChange: (Int) -> Unit,
    onDisplayModeChange: (ireader.domain.models.DisplayMode) -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    
    // Use derivedStateOf for immediate updates
    var sliderValue by remember { mutableStateOf(columnCount.toFloat()) }
    var lastHapticValue by remember { mutableStateOf(columnCount) }
    
    // Update slider value when columnCount changes externally
    LaunchedEffect(columnCount) {
        sliderValue = columnCount.toFloat()
    }
    
    Text(
        text = localize(Res.string.display),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 12.dp)
    )
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Display Mode Toggle
        Column {
            Text(
                text = localize(Res.string.display_mode),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Grid mode button
                FilterChip(
                    selected = displayMode != ireader.domain.models.DisplayMode.List,
                    onClick = { 
                        if (displayMode == ireader.domain.models.DisplayMode.List) {
                            onDisplayModeChange(ireader.domain.models.DisplayMode.CompactGrid)
                        }
                    },
                    label = {
                        Text(
                            text = "Grid",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
                
                // List mode button
                FilterChip(
                    selected = displayMode == ireader.domain.models.DisplayMode.List,
                    onClick = { 
                        onDisplayModeChange(ireader.domain.models.DisplayMode.List)
                    },
                    label = {
                        Text(
                            text = "List",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // Column count slider (only show for grid modes)
        if (displayMode != ireader.domain.models.DisplayMode.List) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = localize(Res.string.columns),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = sliderValue.toInt().toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Slider(
                    value = sliderValue,
                    onValueChange = { newValue ->
                        val intValue = newValue.toInt()
                        sliderValue = newValue
                        
                        // Provide haptic feedback when value changes
                        if (intValue != lastHapticValue) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            lastHapticValue = intValue
                        }
                        
                        // Update immediately for real-time feedback
                        onColumnCountChange(intValue)
                    },
                    valueRange = 1f..6f,
                    steps = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun getFilterLabel(type: LibraryFilter.Type): String {
    return when (type) {
        LibraryFilter.Type.Unread -> localize(Res.string.unread)
        LibraryFilter.Type.Completed -> localize(Res.string.completed)
        LibraryFilter.Type.Downloaded -> localize(Res.string.downloaded)
        LibraryFilter.Type.InProgress -> localize(Res.string.in_progress)
    }
}
