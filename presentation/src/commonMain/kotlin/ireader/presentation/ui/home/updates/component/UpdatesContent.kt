package ireader.presentation.ui.home.updates.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.UpdatesWithRelations
import ireader.core.util.asRelativeTimeString
import ireader.presentation.ui.component.text_related.TextSection
import ireader.presentation.ui.home.updates.viewmodel.UpdatesViewModel
import ireader.presentation.ui.home.updates.viewmodel.UpdatesPaginationState
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UpdatesContent(
        state: UpdatesViewModel,
        onClickItem: (UpdatesWithRelations) -> Unit,
        onLongClickItem: (UpdatesWithRelations) -> Unit,
        onClickCover: (UpdatesWithRelations) -> Unit,
        onClickDownload: (UpdatesWithRelations) -> Unit,
) {
    // Collect state reactively - this is the key fix!
    val screenState by state.state.collectAsState()
    val updates = screenState.updates
    val selection = screenState.selectedChapterIds
    val updateHistory = screenState.updateHistory
    val paginationState = screenState.paginationState
    
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val listState = rememberLazyListState()
    
    // Calculate paginated updates
    val paginatedUpdates by remember(updates, paginationState.loadedCount) {
        derivedStateOf {
            val allUpdates = updates.entries.flatMap { (date, list) -> 
                list.map { date to it }
            }
            val paginatedList = allUpdates.take(paginationState.loadedCount)
            
            // Group back by date
            paginatedList.groupBy({ it.first }, { it.second })
        }
    }
    
    // Calculate total visible items
    val totalVisibleItems by remember(paginatedUpdates) {
        derivedStateOf {
            paginatedUpdates.values.sumOf { it.size }
        }
    }
    
    // Detect scroll to end for pagination
    LaunchedEffect(listState) {
        snapshotFlow { 
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        }.collect { lastVisibleIndex ->
            state.checkAndLoadMore(lastVisibleIndex, totalVisibleItems)
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                bottom = 16.dp,
                top = 8.dp
            )
        ) {
            // New Updates Section
            if (paginatedUpdates.isNotEmpty()) {
                item {
                    TextSection(text = localizeHelper.localize(Res.string.new_updates))
                }
            }
            
            paginatedUpdates.entries.forEachIndexed { groupIndex, (date, updatesList) ->
                item(key = "date_${groupIndex}_${date.date}") {
                    TextSection(
                        text = date.date.asRelativeTimeString()
                    )
                }
                items(
                    count = updatesList.size,
                    key = { index -> "update_${updatesList[index].chapterId}" },
                    contentType = { "update_item" }
                ) { index ->
                    val update = updatesList[index]
                    UpdatesItem(
                        book = update,
                        isSelected = update.chapterId in selection,
                        onClickItem = onClickItem,
                        onLongClickItem = onLongClickItem,
                        onClickCover = onClickCover,
                        onClickDownload = onClickDownload,
                        isDownloadable = !update.downloaded
                    )
                }
            }
            
            // Update History Section
            if (updateHistory.isNotEmpty()) {
                item(key = "history_header") {
                    TextSection(text = localizeHelper.localize(Res.string.update_history))
                }
                items(
                    count = updateHistory.size,
                    key = { index -> "history_${updateHistory[index].id}" },
                    contentType = { "history_item" }
                ) { index ->
                    UpdateHistoryItem(
                        history = updateHistory[index]
                    )
                }
            }
            
            // Pagination footer
            item(key = "pagination_footer") {
                UpdatesPaginationFooter(
                    paginationState = paginationState,
                    totalVisibleItems = totalVisibleItems
                )
            }
        }
    }
}

@Composable
private fun UpdatesPaginationFooter(
    paginationState: UpdatesPaginationState,
    totalVisibleItems: Int
) {
    // Only show loading indicator when actively loading more items
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
