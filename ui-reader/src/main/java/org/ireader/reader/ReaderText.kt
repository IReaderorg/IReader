package org.ireader.reader

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.ireader.common_models.entities.Chapter
import org.ireader.components.list.scrollbars.ColumnScrollbar
import org.ireader.components.list.scrollbars.LazyColumnScrollbar
import org.ireader.core_api.log.Log
import org.ireader.core_ui.preferences.ReadingMode
import org.ireader.core_ui.ui.PreferenceAlignment
import org.ireader.core_ui.ui.mapTextAlign
import org.ireader.reader.reverse_swip_refresh.ISwipeRefreshIndicator
import org.ireader.reader.reverse_swip_refresh.MultiSwipeRefresh
import org.ireader.reader.reverse_swip_refresh.SwipeRefreshState
import org.ireader.reader.viewmodel.ReaderScreenState
import org.ireader.reader.viewmodel.ReaderScreenViewModel

@OptIn(ExperimentalMaterialApi::class, ExperimentalTextApi::class, ExperimentalFoundationApi::class)
@Composable
fun ReaderText(
    modifier: Modifier = Modifier,
    vm: ReaderScreenViewModel,
    uiState: ReaderScreenState,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    swipeState: SwipeRefreshState,
    scrollState: ScrollState,
    lazyListState: LazyListState,
    modalState: ModalBottomSheetState,
    toggleReaderMode: () -> Unit,
    onChapterShown: (chapter: Chapter) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    BoxWithConstraints(
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                toggleReaderMode()
            }
            .fillMaxSize()
            .background(vm.backgroundColor.value)
            .wrapContentSize(Alignment.CenterStart),
    )

    {

        val maxHeight = remember {
            constraints.maxHeight.toFloat()
        }


            MultiSwipeRefresh(
                modifier = Modifier.fillMaxSize(),
                state = swipeState,
                indicators = listOf(
                    ISwipeRefreshIndicator(
                        (scrollState.value == 0 && vm.readingMode.value == ReadingMode.Page) || (vm.readingMode.value == ReadingMode.Continues && lazyListState.firstVisibleItemScrollOffset == 0),
                        alignment = Alignment.TopCenter,
                        indicator = { _, _ ->
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
                        (scrollState.value != 0 && vm.readingMode.value == ReadingMode.Page) || (vm.readingMode.value == ReadingMode.Continues && lazyListState.firstVisibleItemScrollOffset != 0),
                        alignment = Alignment.BottomCenter,
                        onRefresh = {
                            onNext()
                        },
                        indicator = { _, _ ->
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
                TextSelectionContainer(selectable = vm.selectableMode.value) {
                    when (vm.readingMode.value) {
                        ReadingMode.Page -> {
                            PagedReaderText(
                                interactionSource = interactionSource,
                                scrollState = scrollState,
                                vm = vm,
                                maxHeight = maxHeight,
                                onNext = onNext,
                                onPrev = onPrev,
                                toggleReaderMode = toggleReaderMode
                            )
                        }
                        ReadingMode.Continues -> {
                            ContinuesReaderPage(
                                interactionSource = interactionSource,
                                scrollState = lazyListState,
                                vm = vm,
                                maxHeight = maxHeight,
                                onNext = onNext,
                                onPrev = onPrev,
                                toggleReaderMode = toggleReaderMode,
                                onChapterShown = onChapterShown
                            )
                        }
                    }
                }
                ReaderHorizontalScreen(
                    interactionSource = interactionSource,
                    scrollState = scrollState,
                    vm = vm,
                    maxHeight = maxHeight,
                    onNext = onNext,
                    onPrev = onPrev,
                    toggleReaderMode = toggleReaderMode
                )

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

@Composable
private fun PagedReaderText(
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource,
    scrollState: ScrollState,
    vm: ReaderScreenViewModel,
    maxHeight: Float,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    toggleReaderMode: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {

        ColumnScrollbar(
            state = scrollState,
            padding = if (vm.scrollIndicatorPadding.value < 0) 0.dp else vm.scrollIndicatorPadding.value.dp,
            thickness = if (vm.scrollIndicatorWith.value < 0) 0.dp else vm.scrollIndicatorWith.value.dp,
            enable = vm.showScrollIndicator.value,
            thumbColor = vm.unselectedScrollBarColor.value,
            thumbSelectedColor = vm.selectedScrollBarColor.value,
            isDraggable = vm.isScrollIndicatorDraggable.value,
            rightSide = vm.scrollIndicatorAlignment.value == PreferenceAlignment.Right
        ) {
            Column(modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 32.dp)

                ) {
                    vm.stateContent.forEachIndexed { index, text ->
                            Text(
                                modifier = modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = vm.paragraphsIndent.value.dp),
                                text = text.plus(
                                    "\n".repeat(vm.distanceBetweenParagraphs.value)
                                ),
                                fontSize = vm.fontSize.value.sp,
                                fontFamily = vm.font.value.fontFamily,
                                textAlign = mapTextAlign(vm.textAlignment.value),
                                color = vm.textColor.value,
                                lineHeight = vm.lineHeight.value.sp,
                            )
                        }
                }

            }
        }

    }
}

@Composable
private fun ContinuesReaderPage(
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource,
    scrollState: LazyListState,
    vm: ReaderScreenViewModel,
    maxHeight: Float,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    toggleReaderMode: () -> Unit,
    onChapterShown: (chapter: Chapter) -> Unit,
) {
    var lastChapterId: Chapter? by remember {
        mutableStateOf(null)
    }
    LaunchedEffect(key1 = lastChapterId) {
        lastChapterId?.let { onChapterShown(it) }
    }
    LaunchedEffect(key1 = scrollState.layoutInfo.visibleItemsInfo.firstOrNull()?.key) {
        runCatching {
            vm.chapterShell.firstOrNull { it.id == scrollState.layoutInfo.visibleItemsInfo.firstOrNull()?.key.toString().substringAfter("-").toLong() }
                ?.let { chapter ->
                    if (chapter.id != lastChapterId?.id) {
                        lastChapterId = chapter
                        Log.error { "UPDATE" }
                    }
                }
        }


    }
    val items by remember {
        derivedStateOf {
            vm.chapterShell.map { chapter ->
                chapter.content.map { chapter.id to it }
            }.flatten()
        }
    }

    LazyColumnScrollbar(
        listState = scrollState,
        padding = if (vm.scrollIndicatorPadding.value < 0) 0.dp else vm.scrollIndicatorPadding.value.dp,
        thickness = if (vm.scrollIndicatorWith.value < 0) 0.dp else vm.scrollIndicatorWith.value.dp,
        enable = vm.showScrollIndicator.value,
        thumbColor = vm.unselectedScrollBarColor.value,
        thumbSelectedColor = vm.selectedScrollBarColor.value,
        isDraggable = vm.isScrollIndicatorDraggable.value,
        rightSide = vm.scrollIndicatorAlignment.value == PreferenceAlignment.Right,
    ) {

        LazyColumn(
            modifier = modifier.fillMaxSize(),
            state = scrollState,
        ) {
            items(
                count = items.size,
                key = { index ->
                    "$index-${items[index].first}"
                }
            ) { index ->
                    Text(
                        modifier = modifier
                            .fillMaxWidth()
                            .padding(horizontal = vm.paragraphsIndent.value.dp),
                        text = items[index].second.plus(
                            "\n".repeat(
                                vm.distanceBetweenParagraphs.value
                            )
                        ),
                        fontSize = vm.fontSize.value.sp,
                        fontFamily = vm.font.value.fontFamily,
                        textAlign = mapTextAlign(vm.textAlignment.value),
                        color = vm.textColor.value,
                        lineHeight = vm.lineHeight.value.sp,
                    )
            }

        }

    }
}

@Composable
fun ContinuousReaderPageWithScrollState(
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource,
    scrollState: ScrollState,
    vm: ReaderScreenViewModel,
    maxHeight: Float,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    toggleReaderMode: () -> Unit,
    onChapterShown: (chapter: Chapter) -> Unit,
) {
    val positions = remember { vm.chapterShell.map { 0f }.toMutableStateList() }
    val firstVisibleItem by remember {
        derivedStateOf {
            positions.indexOfLast {
                it <= scrollState.value
            }
        }
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
    ) {
        vm.chapterShell.forEachIndexed { index, chapter ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .onGloballyPositioned {
                        runCatching {
                            Log.error {
                                positions
                                    .map { it }
                                    .toString()
                            }
                            Log.error {
                                it.positionInParent().y.toString()
                            }
                            positions[index] = it.positionInParent().y

                        }
                    }
            ) {
                chapter.content.forEachIndexed { index, text ->
                        Text(
                            modifier = modifier
                                .fillMaxWidth()
                                .padding(horizontal = vm.paragraphsIndent.value.dp),
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
                        )
                    }
            }
        }

    }
}

@Composable
private fun ReaderHorizontalScreen(
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource,
    scrollState: ScrollState,
    vm: ReaderScreenViewModel,
    maxHeight: Float,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    toggleReaderMode: () -> Unit
) {
    val scope = rememberCoroutineScope()
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

                            if (scrollState.value != 0) {
                                scrollState.scrollBy(-maxHeight)
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
                            if (scrollState.value != scrollState.maxValue) {
                                scrollState.scrollBy(maxHeight)
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