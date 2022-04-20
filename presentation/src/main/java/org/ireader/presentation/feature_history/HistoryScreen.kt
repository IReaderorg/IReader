package org.ireader.presentation.feature_history

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.ireader.core.utils.UiText
import org.ireader.core_ui.ui.EmptyScreen
import org.ireader.core_ui.ui.LoadingScreen
import org.ireader.domain.feature_service.io.HistoryWithRelations
import org.ireader.presentation.feature_history.viewmodel.HistoryState

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HistoryScreen(
    state: HistoryState,
    onAppbarDeleteAll:() -> Unit,
    getHistories:() -> Unit,
    onHistory:(HistoryWithRelations) -> Unit,
    onHistoryDelete: (HistoryWithRelations) -> Unit,
    onHistoryPlay: (HistoryWithRelations) -> Unit,
    onBookCover: (HistoryWithRelations) -> Unit,
) {

    Scaffold(
        topBar = { HistoryTopAppBar(
            vm = state,
            getHistories = getHistories,
            onDeleteAll = onAppbarDeleteAll,
        ) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            Crossfade(targetState = Pair(state.isLoading, state.isEmpty)) { (isLoading, isEmpty) ->
                when {
                    isLoading -> LoadingScreen()
                    isEmpty -> EmptyScreen(UiText.DynamicString("Nothing read recently"))
                    else -> HistoryContent(
                        state = state,
                        onClickItem = onHistory,
                        onClickDelete = onHistoryDelete,
                        onClickPlay = onHistoryPlay,
                        onBookCover = onBookCover
                    )
                }
            }
        }
    }
}