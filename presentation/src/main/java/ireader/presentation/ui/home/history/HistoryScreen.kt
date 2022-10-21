package ireader.presentation.ui.home.history

import androidx.compose.foundation.layout.Box
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.paging.LoadState
import ireader.common.models.entities.HistoryWithRelations
import ireader.presentation.ui.core.ui.EmptyScreen
import ireader.presentation.ui.core.ui.LoadingScreen
import ireader.presentation.R
import ireader.presentation.ui.home.history.viewmodel.HistoryViewModel

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
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
    val items = vm.getLazyHistory()
    Box(modifier = modifier) {

    when {
        items.loadState.refresh is LoadState.Loading && items.itemCount < 1 -> LoadingScreen()
        items.loadState.refresh is LoadState.NotLoading && items.itemCount < 1 -> EmptyScreen(text = stringResource(
            id = R.string.nothing_read_recently
        ))
        else -> HistoryContent(
            items = items,
            onClickItem = onHistory,
            onClickDelete = onHistoryDelete,
            onClickPlay = onHistoryPlay,
            onBookCover = onBookCover,
            onLongClickDelete = onLongClickDelete
        )
    }}
//    Box(modifier = modifier) {
//        Crossfade(targetState = Pair(state.isLoading, state.isEmpty)) { (isLoading, isEmpty) ->
//            when {
//                isLoading -> LoadingScreen()
//                isEmpty -> EmptyScreen(text = stringResource(R.string.nothing_read_recently))
//                else -> HistoryContent(
//                    state = state,
//                    onClickItem = onHistory,
//                    onClickDelete = onHistoryDelete,
//                    onClickPlay = onHistoryPlay,
//                    onBookCover = onBookCover,
//                    onLongClickDelete = onLongClickDelete
//                )
//            }
//        }
//    }
}
