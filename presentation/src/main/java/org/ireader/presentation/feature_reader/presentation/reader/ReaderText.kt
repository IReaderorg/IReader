package org.ireader.presentation.feature_reader.presentation.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.ireader.core_ui.ui.Colour.scrollingThumbColor
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.view_models.reader.ReaderEvent
import org.ireader.domain.view_models.reader.ReaderScreenViewModel
import org.ireader.presentation.feature_reader.presentation.reader.reverse_swip_refresh.ISwipeRefreshIndicator
import org.ireader.presentation.feature_reader.presentation.reader.reverse_swip_refresh.MultiSwipeRefresh
import org.ireader.presentation.feature_reader.presentation.reader.reverse_swip_refresh.SwipeRefreshState
import org.ireader.presentation.utils.scroll.Carousel
import org.ireader.presentation.utils.scroll.CarouselDefaults

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ReaderText(
    modifier: Modifier = Modifier,
    viewModel: ReaderScreenViewModel,
    chapter: Chapter,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    swipeState: SwipeRefreshState,
    scrollState: LazyListState,
) {

    val modalBottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Expanded)
    val scope = rememberCoroutineScope()

    val state = viewModel.state
    val prefState = viewModel.prefState
    val interactionSource = remember { MutableInteractionSource() }
    val maxOffset = remember {
        mutableStateOf(0)
    }

    Box(
        Modifier
            .clickable(interactionSource = interactionSource,
                indication = null) {
                viewModel.onEvent(ReaderEvent.ToggleReaderMode(!state.isReaderModeEnable))
                if (state.isReaderModeEnable) {
                    scope.launch {
                        viewModel.getLocalChaptersByPaging(chapter.bookId)
                        modalBottomSheetState.animateTo(ModalBottomSheetValue.Expanded)
                    }
                } else {
                    scope.launch {
                        modalBottomSheetState.animateTo(ModalBottomSheetValue.Hidden)
                    }
                }

            }
            .background(viewModel.prefState.backgroundColor)
            .padding(horizontal = viewModel.prefState.paragraphsIndent.dp,
                vertical = 4.dp)
            .fillMaxSize()
            .wrapContentSize(Alignment.CenterStart)
    ) {

        Box(modifier = Modifier
            .fillMaxSize()
        ) {
            MultiSwipeRefresh(
                modifier = Modifier.fillMaxSize(),
                state = swipeState,
                indicators = listOf(
                    ISwipeRefreshIndicator(scrollState.firstVisibleItemScrollOffset == 0,
                        alignment = Alignment.TopCenter,
                        indicator = { state, trigger ->
                            ArrowIndicator(
                                icon = Icons.Default.KeyboardArrowUp,
                                swipeRefreshState = swipeState,
                                    refreshTriggerDistance = 80.dp,
                                    color = viewModel.prefState.textColor
                                )
                            }, onRefresh = {
                                onPrev()
                            }),
                        ISwipeRefreshIndicator(scrollState.firstVisibleItemScrollOffset != 0,
                            alignment = Alignment.BottomCenter,
                            onRefresh = {
                                onNext()
                            },
                            indicator = { state, trigger ->
                                ArrowIndicator(
                                    icon = Icons.Default.KeyboardArrowDown,
                                    swipeRefreshState = swipeState,
                                    refreshTriggerDistance = 80.dp,
                                    color = viewModel.prefState.textColor
                                )
                            }),
                    ),
                )
                {
                    LazyColumn(
                        state = scrollState,
                        modifier = Modifier
                    ) {
                        item {
                            Text(
                                modifier = modifier.fillMaxSize(),
                                text = "\n\n" + chapter.content.map { it.trimStart() }
                                    .joinToString("\n".repeat(prefState.distanceBetweenParagraphs)),
                                fontSize = viewModel.prefState.fontSize.sp,
                                fontFamily = viewModel.prefState.font.fontFamily,
                                textAlign = TextAlign.Start,
                                color = prefState.textColor,
                                lineHeight = prefState.lineHeight.sp,
                            )
                        }

                    }


                }

                Carousel(
                    state = scrollState,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .fillMaxHeight()
                        .width(2.dp),
                    colors = CarouselDefaults.colors(
                        thumbColor = MaterialTheme.colors.scrollingThumbColor,
                        scrollingThumbColor = MaterialTheme.colors.scrollingThumbColor,
                        backgroundColor = viewModel.prefState.backgroundColor,
                        scrollingBackgroundColor = viewModel.prefState.backgroundColor
                    )

                )


        }
    }
}

fun LazyListState.isScrolledToTheEnd(): Boolean {
    val lastItem = layoutInfo.visibleItemsInfo.lastOrNull()
    return lastItem == null || lastItem.size + lastItem.offset <= layoutInfo.viewportEndOffset
}