package ireader.ui.home.history

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ireader.common.models.entities.HistoryWithRelations
import ireader.core.ui.ui.EmptyScreen
import ireader.core.ui.ui.LoadingScreen
import ireader.ui.home.history.viewmodel.HistoryViewModel
import ireader.presentation.R

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    state: HistoryViewModel,
    onHistory: (HistoryWithRelations) -> Unit,
    onHistoryDelete: (HistoryWithRelations) -> Unit,
    onHistoryPlay: (HistoryWithRelations) -> Unit,
    onBookCover: (HistoryWithRelations) -> Unit,
    onLongClickDelete: (HistoryWithRelations) -> Unit,
) {

    Box(modifier = modifier) {
        Crossfade(targetState = Pair(state.isLoading, state.isEmpty)) { (isLoading, isEmpty) ->
            when {
                isLoading -> LoadingScreen()
                isEmpty -> EmptyScreen(text = stringResource(R.string.nothing_read_recently))
                else -> HistoryContent(
                    state = state,
                    onClickItem = onHistory,
                    onClickDelete = onHistoryDelete,
                    onClickPlay = onHistoryPlay,
                    onBookCover = onBookCover,
                    onLongClickDelete = onLongClickDelete
                )
            }
        }
    }
}
