package org.ireader.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.ireader.components.list.scrollbars.LazyColumnScrollbar
import org.ireader.core_ui.preferences.ReadingMode
import org.ireader.core_ui.ui.PreferenceAlignment
import org.ireader.core_ui.ui.mapTextAlign
import org.ireader.core_ui.ui_components.isScrolledToTheEnd
import org.ireader.reader.reverse_swip_refresh.ISwipeRefreshIndicator
import org.ireader.reader.reverse_swip_refresh.MultiSwipeRefresh
import org.ireader.reader.reverse_swip_refresh.SwipeRefreshState
import org.ireader.reader.viewmodel.ReaderScreenState
import org.ireader.reader.viewmodel.ReaderScreenViewModel

@OptIn(ExperimentalMaterialApi::class, ExperimentalTextApi::class)
@Composable
fun ReaderText(
    modifier: Modifier = Modifier,
    vm: ReaderScreenViewModel,
    uiState: ReaderScreenState,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    swipeState: SwipeRefreshState,
    scrollState: LazyListState,
    modalState: ModalBottomSheetState,
    toggleReaderMode: () -> Unit,
) {

    val scope = rememberCoroutineScope()

    val interactionSource = remember { MutableInteractionSource() }
    Box(
        Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                toggleReaderMode()
            }
            .fillMaxSize()
            .background(vm.backgroundColor.value)
            .wrapContentSize(Alignment.CenterStart)
    ) {
        MultiSwipeRefresh(
            modifier = Modifier.fillMaxSize(),
            state = swipeState,
            indicators = listOf(
                ISwipeRefreshIndicator(
                    scrollState.firstVisibleItemScrollOffset == 0,
                    alignment = Alignment.TopCenter,
                    indicator = { state, trigger ->
                        ArrowIndicator(
                            icon = Icons.Default.KeyboardArrowUp,
                            swipeRefreshState = swipeState,
                            refreshTriggerDistance = 80.dp,
                            color = vm.textColor.value
                        )
                    }, onRefresh = {
                        onPrev()
                    }
                ),
                ISwipeRefreshIndicator(
                    scrollState.firstVisibleItemScrollOffset != 0,
                    alignment = Alignment.BottomCenter,
                    onRefresh = {
                        onNext()
                    },
                    indicator = { state, trigger ->
                        ArrowIndicator(
                            icon = Icons.Default.KeyboardArrowDown,
                            swipeRefreshState = swipeState,
                            refreshTriggerDistance = 80.dp,
                            color = vm.textColor.value
                        )
                    }
                ),
            ),
        ) {

            LazyColumnScrollbar(
                listState = scrollState,
                padding = if (vm.scrollIndicatorPadding.value < 0) 0.dp else vm.scrollIndicatorPadding.value.dp,
                thickness = if (vm.scrollIndicatorWith.value < 0) 0.dp else vm.scrollIndicatorWith.value.dp,
                enable = vm.showScrollIndicator.value,
                thumbColor = vm.unselectedScrollBarColor.value,
                thumbSelectedColor = vm.selectedScrollBarColor.value,
                isDraggable = vm.isScrollIndicatorDraggable.value,
                rightSide = vm.scrollIndicatorAlignment.value == PreferenceAlignment.Right
            ) {
                LazyColumn(
                    modifier = modifier,
                    state = scrollState,
                ) {
                    item(
                        key = {
                            "start"
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .height(50.dp)
                                .background(MaterialTheme.colorScheme.contentColorFor(vm.backgroundColor.value))
                        )
                    }
                    when (vm.readingMode.value) {
                        ReadingMode.Continues -> {
                            vm.chapterShell.forEach { chapter ->
                                item(
                                    key = {
                                        "-${chapter.id}"
                                    }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .height(100.dp)
                                            .background(MaterialTheme.colorScheme.contentColorFor(vm.backgroundColor.value))
                                    )
                                }
                                item(
                                    key = {
                                        "-${chapter.id}"
                                    },
                                    contentType = {
                                        "text"
                                    },
                                ) {
                                    if (chapter.content.isEmpty()) return@item
                                    Column(modifier = Modifier.fillMaxSize()) {
                                        chapter.content.forEachIndexed { index, text ->
                                            TextSelectionContainer(selectable = vm.selectableMode.value) {
                                                Text(
                                                    modifier = modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = vm.paragraphsIndent.value.dp)
                                                        .background(
                                                            if (index in vm.queriedTextIndex) vm.textColor.value.copy(
                                                                .1f
                                                            ) else Color.Transparent
                                                        ),
                                                    text = text.plus(
                                                        "\n".repeat(
                                                            vm.distanceBetweenParagraphs.value
                                                        )
                                                    ),
                                                    fontSize = vm.fontSize.value.sp,
                                                    fontFamily = vm.font.value.fontFamily,
                                                    textAlign = mapTextAlign(vm.textAlignment.value),
                                                    color = vm.textColor.value,
                                                    lineHeight = vm.lineHeight.value.sp,
                                                    //  style = MaterialTheme.typography.
                                                )
                                            }
                                        }
                                    }

                                }
                                item() {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .height(100.dp)
                                            .background(MaterialTheme.colorScheme.contentColorFor(vm.backgroundColor.value)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (vm.isLoading && scrollState.layoutInfo.visibleItemsInfo.getOrNull(
                                                scrollState.layoutInfo.visibleItemsInfo.lastIndex - 1
                                            )?.key.toString().contains(chapter.id.toString())
                                        ) {
                                            CircularProgressIndicator()
                                        }
                                    }
                                }

                            }
                        }
                        else -> {
                            item {
                                if (vm.state.stateContent.isEmpty()) return@item
                                Column(modifier = Modifier.fillMaxSize()) {
                                    vm.stateContent.forEachIndexed { index, text ->
                                        TextSelectionContainer(selectable = vm.selectableMode.value) {
                                            Text(
                                                modifier = modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = vm.paragraphsIndent.value.dp)
                                                    .background(
                                                        if (index in vm.queriedTextIndex) vm.textColor.value.copy(
                                                            .1f
                                                        ) else Color.Transparent
                                                    ),
                                                text = text.plus(
                                                    "\n".repeat(vm.distanceBetweenParagraphs.value)
                                                ),
                                                fontSize = vm.fontSize.value.sp,
                                                fontFamily = vm.font.value.fontFamily,
                                                textAlign = mapTextAlign(vm.textAlignment.value),
                                                color = vm.textColor.value,
                                                lineHeight = vm.lineHeight.value.sp,
                                                //  style = MaterialTheme.typography.
                                            )
                                        }

                                    }

                                }

                            }
                        }
                    }
                    item(
                        key = {
                            "end"
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.contentColorFor(vm.backgroundColor.value))
                        )
                    }

                }

            }
        }

        if (!vm.verticalScrolling.value) {
            Row(modifier = Modifier.fillMaxSize()) {

                Box(
                    Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            scope.launch {

                                if (scrollState.layoutInfo.viewportStartOffset != scrollState.firstVisibleItemScrollOffset) {
                                    scrollState.scrollBy(-scrollState.layoutInfo.viewportEndOffset.toFloat())
                                } else {
                                    onPrev()
                                }
                            }
                        }
                ) {
                }
                Box(
                    Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            toggleReaderMode()
                        }
                ) {
                }
                Box(
                    Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            scope.launch {
                                if (!scrollState.isScrolledToTheEnd()) {
                                    scrollState.scrollBy(scrollState.layoutInfo.viewportEndOffset.toFloat())
                                } else {
                                    onNext()
                                }
                            }
                        }
                ) {
                }
            }
        }
    }
}

@Composable
private fun TextSelectionContainer(
    modifier: Modifier = Modifier,
    selectable: Boolean,
    content: @Composable () -> Unit,
) {
    when (selectable) {
        true -> SelectionContainer {
            content()
        }
        else -> {
            content()
        }
    }
}
