package ireader.ui.home.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ireader.common.extensions.asRelativeTimeString
import ireader.common.models.entities.HistoryWithRelations
import ireader.core.ui.preferences.PreferenceValues
import ireader.ui.component.text_related.TextSection
import ireader.ui.home.history.viewmodel.HistoryViewModel

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
    val context = LocalContext.current
    LazyColumn(
        contentPadding = PaddingValues(
            bottom = 16.dp,
            top = 8.dp
        )
    ) {
        state.history.forEach { (date, history) ->
            item {
                TextSection(
                    text = date.asRelativeTimeString(PreferenceValues.RelativeTime.Hour)
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
