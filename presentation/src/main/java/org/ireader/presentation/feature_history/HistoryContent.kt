package org.ireader.presentation.feature_history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import org.ireader.domain.feature_services.io.HistoryWithRelations
import org.ireader.presentation.feature_history.viewmodel.HistoryState
import org.ireader.presentation.feature_sources.presentation.extension.composables.TextSection

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryContent(
    state: HistoryState,
    onClickItem: (HistoryWithRelations) -> Unit,
    onClickDelete: (HistoryWithRelations) -> Unit,
    onClickPlay: (HistoryWithRelations) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(
            bottom = 16.dp,
            top = 8.dp
        )
    ) {
        state.history.forEach { (date, history) ->
            item {
                TextSection(
                    text = date
                )
            }
            items(
                count = history.size,
            ) { index ->
                HistoryItem(history = history[index],
                    onClickItem = onClickItem,
                    onClickDelete = onClickDelete,
                    onClickPlay = onClickPlay
                )
            }
        }
    }
}