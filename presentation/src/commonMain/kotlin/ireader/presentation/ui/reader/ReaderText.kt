package ireader.presentation.ui.reader

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seiko.imageloader.cache.CachePolicy
import com.seiko.imageloader.model.ImageRequest
import ireader.core.source.model.ImageUrl
import ireader.core.source.model.Page
import ireader.core.source.model.Text
import ireader.domain.models.entities.Chapter
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.models.prefs.mapTextAlign
import ireader.domain.preferences.prefs.ReadingMode
import ireader.presentation.imageloader.IImageLoader
import ireader.presentation.ui.component.list.scrollbars.IColumnScrollbar
import ireader.presentation.ui.component.list.scrollbars.ILazyColumnScrollbar
import ireader.presentation.ui.core.modifier.supportDesktopScroll
import ireader.presentation.ui.reader.reverse_swip_refresh.ISwipeRefreshIndicator
import ireader.presentation.ui.reader.reverse_swip_refresh.MultiSwipeRefresh
import ireader.presentation.ui.reader.reverse_swip_refresh.SwipeRefreshState
import ireader.presentation.ui.reader.viewmodel.ReaderScreenState
import ireader.presentation.ui.reader.viewmodel.ReaderScreenViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
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
    val scope = rememberCoroutineScope()
    BoxWithConstraints(
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                toggleReaderMode()
            }
            .supportDesktopScroll(scrollState,scope,enable = vm.readingMode.value == ReadingMode.Page)
            .supportDesktopScroll(lazyListState,scope,enable = vm.readingMode.value == ReadingMode.Continues)
            .fillMaxSize()
            .background(vm.backgroundColor.value),
    ) {

        val maxHeight = remember {
            constraints.maxHeight.toFloat()
        }
        val firstVisibleValue =  remember { derivedStateOf { lazyListState.firstVisibleItemScrollOffset } }


        Box(
            modifier = Modifier.padding(
                top = vm.topMargin.value.dp,
                bottom = vm.bottomMargin.value.dp,
                start = vm.leftMargin.value.dp,
                end = vm.rightMargin.value.dp
            )
        ) {
            MultiSwipeRefresh(
                modifier = Modifier.fillMaxSize(),
                state = swipeState,
                indicators = listOf(
                    ISwipeRefreshIndicator(
                        (scrollState.value == 0 && vm.readingMode.value == ReadingMode.Page) || (vm.readingMode.value == ReadingMode.Continues && firstVisibleValue.value == 0),
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
                        (scrollState.value != 0 && vm.readingMode.value == ReadingMode.Page) || (vm.readingMode.value == ReadingMode.Continues && firstVisibleValue.value != 0),
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

        IColumnScrollbar(
            state = scrollState,
            padding = if (vm.scrollIndicatorPadding.value < 0) 0.dp else vm.scrollIndicatorPadding.value.dp,
            thickness = if (vm.scrollIndicatorWith.value < 0) 0.dp else vm.scrollIndicatorWith.value.dp,
            enabled = vm.showScrollIndicator.value,
            thumbColor = vm.unselectedScrollBarColor.value,
            thumbSelectedColor = vm.selectedScrollBarColor.value,
            selectionMode = vm.isScrollIndicatorDraggable.value,
            rightSide = vm.scrollIndicatorAlignment.value == PreferenceValues.PreferenceTextAlignment.Right
        ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(top = 32.dp)

                ) {
                    vm.stateContent.forEachIndexed { index, text ->
                        MainText(
                            modifier = modifier,
                            index = index,
                            page = text,
                            vm = vm
                        )
                    }
                }
        }
    }
}

@Composable
private fun MainText(
    modifier: Modifier,
    index: Int,
    page: Page,
    vm: ReaderScreenViewModel
) {
    when (page) {
        is Text -> {
            Text(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = vm.paragraphsIndent.value.dp),
                text = setText(
                    text = page.text,
                    index = index,
                    isLast = index == vm.stateContent.lastIndex,
                    topContentPadding = vm.topContentPadding.value,
                    contentPadding = vm.distanceBetweenParagraphs.value,
                    bottomContentPadding = vm.bottomContentPadding.value
                ),
                fontSize = vm.fontSize.value.sp,
                fontFamily = vm.font?.value?.fontFamily,
                textAlign = mapTextAlign(vm.textAlignment.value),
                color = vm.textColor.value,
                lineHeight = vm.lineHeight.value.sp,
                letterSpacing = vm.betweenLetterSpaces.value.sp,
                fontWeight = FontWeight(vm.textWeight.value),
            )
        }
        is ImageUrl -> {
            val isLoading = remember {
                mutableStateOf(false)

            }
            Box(contentAlignment = Alignment.Center) {
                IImageLoader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .requiredHeight(500.dp),
                    model = ImageRequest {
                    data(page.url)
                        this.options {
                            diskCachePolicy = CachePolicy.DISABLED
                        }

                    },
                    contentDescription = "image",
                    contentScale = ContentScale.FillWidth,
                    onLoading = {
                        isLoading.value = true
                    },
                    onError = {
                        isLoading.value = false
                    },
                    onSuccess = {
                        isLoading.value = false
                    },
                )
                if (isLoading.value) {
                    androidx.compose.material3.CircularProgressIndicator()
                }
            }
        }
        else -> {
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
    val visibleItemInfo =  remember { derivedStateOf { scrollState.layoutInfo } }
    LaunchedEffect(key1 = visibleItemInfo.value.visibleItemsInfo.firstOrNull()?.key) {
        runCatching {
            vm.chapterShell.firstOrNull {
                it.id == scrollState.layoutInfo.visibleItemsInfo.firstOrNull()?.key.toString()
                    .substringAfter("-").toLong()
            }
                ?.let { chapter ->
                    if (chapter.id != lastChapterId?.id) {
                        lastChapterId = chapter
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

    ILazyColumnScrollbar(
        listState = scrollState,
        padding = if (vm.scrollIndicatorPadding.value < 0) 0.dp else vm.scrollIndicatorPadding.value.dp,
        thickness = if (vm.scrollIndicatorWith.value < 0) 0.dp else vm.scrollIndicatorWith.value.dp,
        enable = vm.showScrollIndicator.value,
        thumbColor = vm.unselectedScrollBarColor.value,
        thumbSelectedColor = vm.selectedScrollBarColor.value,
        selectionMode = vm.isScrollIndicatorDraggable.value,
        rightSide = vm.scrollIndicatorAlignment.value == PreferenceValues.PreferenceTextAlignment.Right,
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
                MainText(
                    modifier = modifier,
                    index = index,
                    page = items[index].second,
                    vm = vm
                )
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
    Box(modifier = Modifier.fillMaxSize().background(Color.Red))
    }
}

private fun setText(
    text: String,
    index: Int,
    isLast: Boolean,
    topContentPadding: Int,
    bottomContentPadding: Int,
    contentPadding: Int,
): String {
    return text.let {
        if (index == 0) {
            "\n".repeat(topContentPadding) + it
        } else {
            it
        }
    }.let {
        if (isLast) {
            it + "\n".repeat(bottomContentPadding)
        } else {
            it
        }
    }.let {
        it + "\n".repeat(contentPadding)
    }
}
