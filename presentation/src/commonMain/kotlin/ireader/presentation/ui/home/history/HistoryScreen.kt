package ireader.presentation.ui.home.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import ireader.domain.models.entities.HistoryWithRelations
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.delete_all_histories
import ireader.i18n.resources.no_matches_found_in_search
import ireader.i18n.resources.nothing_read_recently
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.core.ui.EmptyScreen
import ireader.presentation.ui.home.history.components.WarningAlertDialog
import ireader.presentation.ui.home.history.viewmodel.HistoryViewModel

private enum class HistoryScreenState {
    NoSearchResults,
    Empty,
    Content
}

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
    val state by vm.state.collectAsState()
    val items = state.histories
    val searchQuery = state.searchQuery
    val dateFilter = state.dateFilter
    
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = state.savedScrollIndex,
        initialFirstVisibleItemScrollOffset = state.savedScrollOffset
    )
    
    LaunchedEffect(listState) {
        snapshotFlow { 
            listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset 
        }.collect { (index, offset) ->
            vm.saveScrollPosition(index, offset)
        }
    }
    
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    
    val screenState by remember(items, searchQuery) {
        derivedStateOf {
            when {
                items.values.flatten().isEmpty() && searchQuery.isNotEmpty() -> HistoryScreenState.NoSearchResults
                items.values.isEmpty() -> HistoryScreenState.Empty
                else -> HistoryScreenState.Content
            }
        }
    }
    
    LaunchedEffect(searchQuery) {
        vm.applySearchFilter()
    }
    
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        when (screenState) {
            HistoryScreenState.NoSearchResults -> EmptyScreen(
                text = localize(Res.string.no_matches_found_in_search)
            )
            HistoryScreenState.Empty -> EmptyScreen(
                text = localize(Res.string.nothing_read_recently)
            )
            HistoryScreenState.Content -> HistoryContent(
                items = items,
                listState = listState,
                onClickItem = onHistory,
                onClickDelete = onHistoryDelete,
                onClickPlay = onHistoryPlay,
                onBookCover = onBookCover,
                onLongClickDelete = onLongClickDelete,
                vm = vm,
                dateFilter = dateFilter,
                searchQuery = searchQuery
            )
        }
        
        if (items.values.isNotEmpty() && screenState == HistoryScreenState.Content) {
            FloatingActionButton(
                onClick = { vm.deleteAllHistories(localizeHelper) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.errorContainer,
                shape = CircleShape,
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = localize(Res.string.delete_all_histories),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
        
        // Subtle top progress line when loading in background - zero layout shift
        if (state.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .align(Alignment.TopCenter)
            )
        }
        
        if (vm.warningAlert.enable) {
            WarningAlertDialog(data = vm.warningAlert)
        }
    }
}
