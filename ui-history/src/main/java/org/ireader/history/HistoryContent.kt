package org.ireader.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import org.ireader.common_extensions.asRelativeTimeString
import org.ireader.common_models.entities.HistoryWithRelations
import org.ireader.components.text_related.TextSection
import org.ireader.history.viewmodel.HistoryViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryContent(
    state: HistoryViewModel,
    onBookCover: (HistoryWithRelations) -> Unit,
    onClickItem: (HistoryWithRelations) -> Unit,
    onClickDelete: (HistoryWithRelations) -> Unit,
    onLongClickDelete: (HistoryWithRelations) -> Unit,
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
                    text = date.asRelativeTimeString(range = state.relativeFormat)
                )
            }
            items(
                count = history.size,
            ) { index ->
                HistoryItem(
                    history = history[index],
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
