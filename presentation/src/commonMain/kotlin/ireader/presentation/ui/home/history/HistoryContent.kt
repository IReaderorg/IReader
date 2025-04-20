//package ireader.presentation.ui.home.history
//
//import androidx.compose.animation.core.*
//import androidx.compose.foundation.ExperimentalFoundationApi
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.remember
//import androidx.compose.ui.unit.dp
//import ireader.core.util.asRelativeTimeString
//import ireader.domain.models.entities.HistoryWithRelations
//import ireader.presentation.ui.component.text_related.TextSection
//import kotlinx.datetime.LocalDateTime
//
//@OptIn(ExperimentalFoundationApi::class)
//@Composable
//fun HistoryContent(
//        items: Map<LocalDateTime, List<HistoryWithRelations>>,
//        onBookCover: (HistoryWithRelations) -> Unit,
//        onClickItem: (HistoryWithRelations) -> Unit,
//        onClickDelete: (HistoryWithRelations) -> Unit,
//        onLongClickDelete: (HistoryWithRelations) -> Unit,
//        onClickPlay: (HistoryWithRelations) -> Unit,
//) {
//    val histories = remember {
//        mutableListOf<String>()
//    }
//    LazyColumn(
//        contentPadding = PaddingValues(
//            bottom = 16.dp,
//            top = 8.dp
//        )
//    ) {
//        items.forEach { (date, items ) ->
//            val text = date.date.asRelativeTimeString()
//            if (text !in histories) {
//                histories.add(text)
//                item {
//                    TextSection(
//                            text = date.date.asRelativeTimeString()
//                    )
//                }
//            }
//            items(
//                    count = items.size,
//            ) { index ->
//                HistoryItem(
//                        history = items[index],
//                        onClickItem = onClickItem,
//                        onClickDelete = onClickDelete,
//                        onClickPlay = onClickPlay,
//                        onBookCover = onBookCover,
//                        onLongClickDelete = onLongClickDelete
//                )
//            }
//        }
//    }
//}
