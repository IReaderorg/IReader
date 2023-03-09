package ireader.presentation.ui.home.history

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.items
import ireader.domain.models.entities.HistoryWithRelations
import ireader.presentation.ui.component.text_related.TextSection
import ireader.presentation.ui.core.utils.shimmerGradient
import ireader.presentation.ui.home.history.viewmodel.HistoryUiModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryContent(
        items: LazyPagingItems<HistoryUiModel>,
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
        items(items) { item ->
            when (item) {
                is HistoryUiModel.Header -> {
                    TextSection(
                        text = item.date
                    )
                }
                is HistoryUiModel.Item -> {
                    HistoryItem(
                        history = item.item,
                        onClickItem = onClickItem,
                        onClickDelete = onClickDelete,
                        onClickPlay = onClickPlay,
                        onBookCover = onBookCover,
                        onLongClickDelete = onLongClickDelete
                    )
                }

                null -> {
                    val transition = rememberInfiniteTransition()
                    val translateAnimation = transition.animateFloat(
                        initialValue = 0f,
                        targetValue = 1000f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(
                                durationMillis = 1000,
                                easing = LinearEasing,
                            ),
                        ),
                    )

                    val brush = remember {
                        Brush.linearGradient(
                            colors = shimmerGradient,
                            start = Offset(0f, 0f),
                            end = Offset(
                                x = translateAnimation.value,
                                y = 00f,
                            ),
                        )
                    }
                    HistoryItemShimmer(brush = brush)
                }
            }

        }
//        state.history.forEach { (date, history) ->
//            item {
//                TextSection(
//                    text = date.asRelativeTimeString(PreferenceValues.RelativeTime.Hour)
//                )
//            }
//            items(
//                count = history.size,
//            ) { index ->
//                HistoryItem(
//                    history = history[index],
//                    onClickItem = onClickItem,
//                    onClickDelete = onClickDelete,
//                    onClickPlay = onClickPlay,
//                    onBookCover = onBookCover,
//                    onLongClickDelete = onLongClickDelete
//                )
//            }
//        }
    }
}
