package ireader.presentation.ui.home.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.HistoryWithRelations
import ireader.domain.utils.extensions.daysAgoStartMillis
import ireader.domain.utils.extensions.formatDate
import ireader.domain.utils.extensions.formatTime12Hour
import ireader.domain.utils.extensions.todayStartMillis
import ireader.domain.utils.extensions.yesterdayStartMillis
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.recently
import ireader.i18n.resources.relative_time_today
import ireader.i18n.resources.weekly
import ireader.i18n.resources.yesterday
import ireader.presentation.ui.home.history.viewmodel.DateFilter
import ireader.presentation.ui.home.history.viewmodel.HistoryPaginationState
import ireader.presentation.ui.home.history.viewmodel.HistoryViewModel

private fun stableHistoryKey(history: HistoryWithRelations): Any = history.id

private data class GroupedHistoryItems(
    val todayItems: List<HistoryWithRelations>,
    val yesterdayItems: List<HistoryWithRelations>,
    val thisWeekItems: List<HistoryWithRelations>,
    val earlierItems: List<HistoryWithRelations>
)

@Composable
fun HistoryContent(
    items: Map<Long, List<HistoryWithRelations>>,
    listState: LazyListState,
    onClickItem: (HistoryWithRelations) -> Unit,
    onClickDelete: (HistoryWithRelations) -> Unit,
    onClickPlay: (HistoryWithRelations) -> Unit,
    onBookCover: (HistoryWithRelations) -> Unit,
    onLongClickDelete: (HistoryWithRelations) -> Unit,
    vm: HistoryViewModel,
    dateFilter: DateFilter? = null,
    searchQuery: String = ""
) {
    val state by vm.state.collectAsState()
    val paginationState = state.paginationState
    
    val timeBoundaries = remember {
        Triple(
            todayStartMillis(),
            yesterdayStartMillis(),
            daysAgoStartMillis(7)
        )
    }
    
    val (today, yesterday, lastWeek) = timeBoundaries
    
    val groupedItems by remember(items, dateFilter, today, yesterday, lastWeek, paginationState.loadedCount) {
        derivedStateOf {
            val allItems = items.values.flatten()
            val filteredItems = when (dateFilter) {
                DateFilter.TODAY -> allItems.filter { it.readAt >= today }
                DateFilter.YESTERDAY -> allItems.filter { it.readAt >= yesterday && it.readAt < today }
                DateFilter.PAST_7_DAYS -> allItems.filter { it.readAt >= lastWeek }
                null -> allItems
            }
            
            val paginatedItems = filteredItems
                .sortedByDescending { it.readAt }
                .take(paginationState.loadedCount)
            
            val todayItems = mutableListOf<HistoryWithRelations>()
            val yesterdayItems = mutableListOf<HistoryWithRelations>()
            val thisWeekItems = mutableListOf<HistoryWithRelations>()
            val earlierItems = mutableListOf<HistoryWithRelations>()
            
            paginatedItems.forEach { history ->
                when {
                    history.readAt >= today -> todayItems.add(history)
                    history.readAt >= yesterday -> yesterdayItems.add(history)
                    history.readAt >= lastWeek -> thisWeekItems.add(history)
                    else -> earlierItems.add(history)
                }
            }
            
            GroupedHistoryItems(
                todayItems = todayItems,
                yesterdayItems = yesterdayItems,
                thisWeekItems = thisWeekItems,
                earlierItems = earlierItems
            )
        }
    }
    
    val totalVisibleItems by remember(groupedItems) {
        derivedStateOf {
            groupedItems.todayItems.size + groupedItems.yesterdayItems.size + 
            groupedItems.thisWeekItems.size + groupedItems.earlierItems.size
        }
    }
    
    LaunchedEffect(listState) {
        snapshotFlow { 
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        }.collect { lastVisibleIndex ->
            vm.checkAndLoadMore(lastVisibleIndex, totalVisibleItems)
        }
    }
    
    val formatTime: (Long) -> String = remember { { it.formatTime12Hour() } }
    val formatDateStr: (Long) -> String = remember { { it.formatDate() } }
    
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp),
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            // Today section
            if (groupedItems.todayItems.isNotEmpty()) {
                item(key = "header_today") {
                    HistoryTimeHeader(title = localize(Res.string.relative_time_today))
                }
                
                items(
                    items = groupedItems.todayItems,
                    key = { history -> stableHistoryKey(history) },
                    contentType = { "history_item" }
                ) { history ->
                    HistoryItem(
                        history = history,
                        timeString = formatTime(history.readAt),
                        onClickItem = onClickItem,
                        onClickDelete = onClickDelete,
                        onClickPlay = onClickPlay,
                        onBookCover = onBookCover,
                        onLongClickDelete = onLongClickDelete,
                        onHistoryDelete = onClickDelete,
                        vm = vm,
                        searchQuery = searchQuery
                    )
                }
            }
            
            // Yesterday section
            if (groupedItems.yesterdayItems.isNotEmpty()) {
                item(key = "header_yesterday") {
                    HistoryTimeHeader(title = localize(Res.string.yesterday))
                }
                
                items(
                    items = groupedItems.yesterdayItems,
                    key = { history -> stableHistoryKey(history) },
                    contentType = { "history_item" }
                ) { history ->
                    HistoryItem(
                        history = history,
                        timeString = formatTime(history.readAt),
                        onClickItem = onClickItem,
                        onClickDelete = onClickDelete,
                        onClickPlay = onClickPlay,
                        onBookCover = onBookCover,
                        onLongClickDelete = onLongClickDelete,
                        onHistoryDelete = onClickDelete,
                        vm = vm,
                        searchQuery = searchQuery
                    )
                }
            }
            
            // This week section
            if (groupedItems.thisWeekItems.isNotEmpty()) {
                item(key = "header_week") {
                    HistoryTimeHeader(title = localize(Res.string.weekly))
                }
                
                items(
                    items = groupedItems.thisWeekItems,
                    key = { history -> stableHistoryKey(history) },
                    contentType = { "history_item" }
                ) { history ->
                    HistoryItem(
                        history = history,
                        timeString = formatDateStr(history.readAt),
                        onClickItem = onClickItem,
                        onClickDelete = onClickDelete,
                        onClickPlay = onClickPlay,
                        onBookCover = onBookCover,
                        onLongClickDelete = onLongClickDelete,
                        onHistoryDelete = onClickDelete,
                        vm = vm,
                        searchQuery = searchQuery
                    )
                }
            }
            
            // Earlier section
            if (groupedItems.earlierItems.isNotEmpty()) {
                item(key = "header_earlier") {
                    HistoryTimeHeader(title = localize(Res.string.recently))
                }
                
                items(
                    items = groupedItems.earlierItems,
                    key = { history -> stableHistoryKey(history) },
                    contentType = { "history_item" }
                ) { history ->
                    HistoryItem(
                        history = history,
                        timeString = formatDateStr(history.readAt),
                        onClickItem = onClickItem,
                        onClickDelete = onClickDelete,
                        onClickPlay = onClickPlay,
                        onBookCover = onBookCover,
                        onLongClickDelete = onLongClickDelete,
                        onHistoryDelete = onClickDelete,
                        vm = vm,
                        searchQuery = searchQuery
                    )
                }
            }
            
            // Pagination footer
            item(key = "pagination_footer") {
                HistoryPaginationFooter(
                    paginationState = paginationState
                )
            }
        }
    }
}

@Composable
private fun HistoryPaginationFooter(
    paginationState: HistoryPaginationState
) {
    if (paginationState.isLoadingMore) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        }
    }
}

@Composable
fun HistoryTimeHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier
                .weight(0.15f)
                .padding(end = 8.dp),
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        HorizontalDivider(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            color = MaterialTheme.colorScheme.primary
        )
    }
}
