package org.ireader.presentation.feature_reader.presentation.reader

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.ireader.core_ui.ui.Colour.scrollingThumbColor
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.view_models.reader.ReaderEvent
import org.ireader.domain.view_models.reader.ReaderScreenViewModel
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
) {
    val modalBottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Expanded)
    val scope = rememberCoroutineScope()

    val state = viewModel.state
    val prefState = viewModel.prefState
    val interactionSource = remember { MutableInteractionSource() }

    val scrollState = rememberLazyListState()

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
            Row(
                modifier = modifier
            ) {
                val animatedProgress = remember { Animatable(1f) }

                LaunchedEffect(animatedProgress) {
                    animatedProgress.animateTo(0.5f,
                        animationSpec = tween(
                            durationMillis = 2000,
                            delayMillis = 2000
                        ))
                }
                if (scrollState.firstVisibleItemScrollOffset == 0) {
                    SwipeRefresh(
                        modifier = Modifier.fillMaxSize(),
                        state = swipeState,
                        onRefresh = {
                            onPrev()
                        },
                        indicatorAlignment = Alignment.TopCenter,
                        indicator = { state, trigger ->
                            ArrowIndicator(
                                icon = Icons.Default.KeyboardArrowUp,
                                swipeRefreshState = swipeState,
                                refreshTriggerDistance = 80.dp
                            )
                        }
                    )
                    {
                        LazyColumn(
                            state = scrollState,
                            modifier = Modifier
                        ) {
                            item {
                                Box(modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Red)) {

                                }

                            }
                            item {
                                Text(
                                    modifier = modifier
                                        .weight(1f),
                                    text = chapter.content.map { it.trimStart() }
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

                } else {
                    SwipeRefresh(
                        modifier = Modifier.fillMaxSize(),
                        state = swipeState,
                        onRefresh = {
                            onNext()
                        },
                        indicatorAlignment = Alignment.BottomCenter,
                        indicator = { state, trigger ->
                            ArrowIndicator(
                                icon = Icons.Default.KeyboardArrowUp,
                                swipeRefreshState = swipeState,
                                refreshTriggerDistance = 80.dp
                            )
                        }
                    )
                    {
                        LazyColumn(
                            state = scrollState,
                            modifier = Modifier
                        ) {
                            item {
                                Box(modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Red)) {

                                }

                            }
                            item {
                                Text(
                                    modifier = modifier
                                        .weight(1f),
                                    text = chapter.content.map { it.trimStart() }
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
                }


                Carousel(
                    state = scrollState,
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(.02f)
                        .padding(start = 6.dp),
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
}