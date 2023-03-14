package ireader.presentation.ui.home.history

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ireader.domain.models.entities.HistoryWithRelations
import ireader.i18n.localize
import ireader.i18n.resources.MR
import ireader.presentation.ui.core.ui.EmptyScreen
import ireader.presentation.ui.home.history.viewmodel.HistoryViewModel

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
    Box(modifier = modifier) {

        when {
            items.values.isEmpty() -> EmptyScreen(text = localize(
                    MR.strings.nothing_read_recently
            )
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
    }
}
