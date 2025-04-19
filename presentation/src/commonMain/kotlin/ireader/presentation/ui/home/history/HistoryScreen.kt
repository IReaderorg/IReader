package ireader.presentation.ui.home.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.HistoryWithRelations
import ireader.i18n.localize
import ireader.i18n.resources.MR
import ireader.presentation.ui.core.ui.EmptyScreen
import ireader.presentation.ui.home.history.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    vm: HistoryViewModel,
    onHistory: (HistoryWithRelations) -> Unit,
    onHistoryDelete: (HistoryWithRelations) -> Unit,
    onHistoryPlay: (HistoryWithRelations) -> Unit,
    onBookCover: (HistoryWithRelations) -> Unit,
    onLongClickDelete: (HistoryWithRelations) -> Unit,
) {
    val items = vm.histories
    val showClearAllDialog = remember { mutableStateOf(false) }
    
    Box(modifier = modifier.fillMaxSize()) {
        when {
            items.values.isEmpty() -> EmptyScreen(
                text = localize(MR.strings.nothing_read_recently),
                modifier = Modifier.fillMaxSize()
            )
            else -> HistoryContent(
                items = items,
                onClickItem = onHistory,
                onClickDelete = onHistoryDelete,
                onClickPlay = onHistoryPlay,
                onBookCover = onBookCover,
                onLongClickDelete = onLongClickDelete
            )
        }
        
        // Clear all history button
        if (items.values.isNotEmpty()) {
            FloatingActionButton(
                onClick = { showClearAllDialog.value = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.errorContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = localize(MR.strings.delete_all_histories),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
        
        // Confirmation dialog for clearing all history
        if (showClearAllDialog.value) {
            AlertDialog(
                onDismissRequest = { showClearAllDialog.value = false },
                title = { Text(localize(MR.strings.delete_all_histories)) },
                text = { Text(localize(MR.strings.dialog_remove_chapter_books_description)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            vm.deleteAllHistories()
                            showClearAllDialog.value = false
                        }
                    ) {
                        Text(
                            localize(MR.strings.confirm),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showClearAllDialog.value = false }
                    ) {
                        Text(localize(MR.strings.cancel))
                    }
                }
            )
        }
    }
}

@Composable
fun HistoryContent(
    items: Map<Long, List<HistoryWithRelations>>,
    onClickItem: (HistoryWithRelations) -> Unit,
    onClickDelete: (HistoryWithRelations) -> Unit,
    onClickPlay: (HistoryWithRelations) -> Unit,
    onBookCover: (HistoryWithRelations) -> Unit,
    onLongClickDelete: (HistoryWithRelations) -> Unit
) {
    // Group history items by time period
    val now = System.currentTimeMillis()
    val calendar = Calendar.getInstance()
    val today = calendar.apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    
    // Copy the calendar and subtract days
    val yesterday = calendar.apply {
        add(Calendar.DAY_OF_MONTH, -1)
    }.timeInMillis
    
    val lastWeek = calendar.apply {
        add(Calendar.DAY_OF_MONTH, -6) // Now 7 days ago in total
    }.timeInMillis
    
    // Group history items
    val todayItems = mutableListOf<HistoryWithRelations>()
    val yesterdayItems = mutableListOf<HistoryWithRelations>()
    val thisWeekItems = mutableListOf<HistoryWithRelations>()
    val earlierItems = mutableListOf<HistoryWithRelations>()
    
    items.values.flatten().forEach { history ->
        when {
            history.readAt >= today -> todayItems.add(history)
            history.readAt >= yesterday -> yesterdayItems.add(history)
            history.readAt >= lastWeek -> thisWeekItems.add(history)
            else -> earlierItems.add(history)
        }
    }
    
    // Format for showing time
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    
    LazyColumn(
        contentPadding = PaddingValues(bottom = 80.dp) // Space for FAB
    ) {
        // Today section
        if (todayItems.isNotEmpty()) {
            item {
                HistoryTimeHeader(title = localize(MR.strings.relative_time_today))
            }
            
            items(todayItems.sortedByDescending { it.readAt }) { history ->
                EnhancedHistoryItem(
                    history = history,
                    timeString = timeFormat.format(Date(history.readAt)),
                    onClickItem = onClickItem,
                    onClickDelete = onClickDelete,
                    onClickPlay = onClickPlay,
                    onBookCover = onBookCover,
                    onLongClickDelete = onLongClickDelete
                )
            }
        }
        
        // Yesterday section
        if (yesterdayItems.isNotEmpty()) {
            item {
                HistoryTimeHeader(title = localize(MR.strings.yesterday))
            }
            
            items(yesterdayItems.sortedByDescending { it.readAt }) { history ->
                EnhancedHistoryItem(
                    history = history,
                    timeString = timeFormat.format(Date(history.readAt)),
                    onClickItem = onClickItem,
                    onClickDelete = onClickDelete,
                    onClickPlay = onClickPlay,
                    onBookCover = onBookCover,
                    onLongClickDelete = onLongClickDelete
                )
            }
        }
        
        // This week section
        if (thisWeekItems.isNotEmpty()) {
            item {
                HistoryTimeHeader(title = localize(MR.strings.weekly))
            }
            
            items(thisWeekItems.sortedByDescending { it.readAt }) { history ->
                EnhancedHistoryItem(
                    history = history,
                    timeString = dateFormat.format(Date(history.readAt)),
                    onClickItem = onClickItem,
                    onClickDelete = onClickDelete,
                    onClickPlay = onClickPlay,
                    onBookCover = onBookCover,
                    onLongClickDelete = onLongClickDelete
                )
            }
        }
        
        // Earlier section
        if (earlierItems.isNotEmpty()) {
            item {
                HistoryTimeHeader(title = localize(MR.strings.recently))
            }
            
            items(earlierItems.sortedByDescending { it.readAt }) { history ->
                EnhancedHistoryItem(
                    history = history,
                    timeString = dateFormat.format(Date(history.readAt)),
                    onClickItem = onClickItem,
                    onClickDelete = onClickDelete,
                    onClickPlay = onClickPlay,
                    onBookCover = onBookCover,
                    onLongClickDelete = onLongClickDelete
                )
            }
        }
    }
}

@Composable
fun HistoryTimeHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Divider(
            color = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.weight(1f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedHistoryItem(
    history: HistoryWithRelations,
    timeString: String,
    onClickItem: (HistoryWithRelations) -> Unit,
    onClickDelete: (HistoryWithRelations) -> Unit,
    onClickPlay: (HistoryWithRelations) -> Unit,
    onBookCover: (HistoryWithRelations) -> Unit,
    onLongClickDelete: (HistoryWithRelations) -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Book cover (if implemented)
            Box(
                modifier = Modifier
                    .size(width = 60.dp, height = 90.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onBookCover(history) }
            ) {
                // Placeholder for actual book cover
                Text(
                    text = history.title.take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center)
                )
                
                // Reading progress indicator
                val progressValue = history.progress ?: 0f
                if (progressValue > 0) {
                    LinearProgressIndicator(
                        progress = { progressValue },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(4.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Book/Chapter details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onClickItem(history) }
            ) {
                // Book title
                Text(
                    text = history.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Chapter title
                Text(
                    text = history.chapterName ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Time read
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = timeString,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    
                    // Add progress percentage
                    val progressValue = history.progress ?: 0f
                    if (progressValue > 0) {
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Text(
                            text = "${(progressValue * 100).toInt()}% read",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // Actions
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Continue reading button
                IconButton(
                    onClick = { onClickPlay(history) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = localize(MR.strings.play),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Delete button
                IconButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = localize(MR.strings.delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(localize(MR.strings.delete)) },
            text = { Text(localize(MR.strings.dialog_remove_chapter_history_description)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClickDelete(history)
                        showDeleteConfirm = false
                    }
                ) {
                    Text(
                        localize(MR.strings.confirm),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirm = false }
                ) {
                    Text(localize(MR.strings.cancel))
                }
            }
        )
    }
}
