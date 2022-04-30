package org.ireader.presentation.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDeepLink
import androidx.navigation.navDeepLink
import com.google.accompanist.pager.ExperimentalPagerApi
import kotlinx.coroutines.launch
import org.ireader.common_models.entities.Chapter
import org.ireader.components.components.EmptyScreenComposable
import org.ireader.core.R
import org.ireader.core_api.log.Log
import org.ireader.core_api.source.Source
import org.ireader.core_ui.theme.TransparentStatusBar
import org.ireader.domain.ui.NavigationArgs
import org.ireader.reader.ReadingScreen
import org.ireader.reader.reverse_swip_refresh.SwipeRefreshState
import org.ireader.reader.reverse_swip_refresh.rememberSwipeRefreshState
import org.ireader.reader.viewmodel.ReaderScreenViewModel

object ReaderScreenSpec : ScreenSpec {

    override val navHostRoute: String = "reader_screen_route/{bookId}/{chapterId}/{sourceId}"

    override val arguments: List<NamedNavArgument> = listOf(
        NavigationArgs.bookId,
        NavigationArgs.chapterId,
        NavigationArgs.sourceId,
    )

    fun buildRoute(
        bookId: Long,
        sourceId: Long,
        chapterId: Long,
    ): String {
        return "reader_screen_route/$bookId/$chapterId/$sourceId"
    }

    fun buildDeepLink(
        bookId: Long,
        sourceId: Long,
        chapterId: Long,
        readingParagraph: Long,
    ): String {
        return "https://www.ireader.org/reader_screen_route/$bookId/$chapterId/$sourceId/$readingParagraph"
    }

    override val deepLinks: List<NavDeepLink> = listOf(

        navDeepLink {
            uriPattern =
                "https://www.ireader.org/reader_screen_route/{bookId}/{chapterId}/{sourceId}/{readingParagraph}}"
            NavigationArgs.bookId
            NavigationArgs.chapterId
            NavigationArgs.sourceId
            NavigationArgs.readingParagraph
        }
    )

    @OptIn(
        ExperimentalPagerApi::class, androidx.compose.animation.ExperimentalAnimationApi::class,
        androidx.compose.material.ExperimentalMaterialApi::class
    )
    @Composable
    override fun Content(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
    ) {
        val vm: ReaderScreenViewModel = hiltViewModel()
        val currentIndex = vm.currentChapterIndex
        val source = vm.source
        val chapters = vm.stateChapters
        val chapter = vm.stateChapter
        val scope = rememberCoroutineScope()
        val scrollState = rememberLazyListState()
        val drawerScrollState = rememberLazyListState()
        val swipeState = rememberSwipeRefreshState(isRefreshing = false)
        val context = LocalContext.current

        DisposableEffect(key1 = true) {
            onDispose {
                vm.restoreSetting(context, scrollState)

            }
        }
        if (source != null) {
            MainReader(
                onChapter = { ch ->
                    val index = vm.stateChapters.indexOfFirst { it.id == ch.id }
                    if (index != -1) {
                        scope.launch {
                            vm.getLocalChapter(ch.id)
                        }
                        scope.launch {
                            scrollState.scrollToItem(0, 0)
                        }
                        vm.currentChapterIndex = index
                    }
                },
                onReaderNext = {
                    if (currentIndex < chapters.lastIndex) {
                        try {
                            vm.apply {
                                val nextChapter = vm.nextChapter()
                                scope.launch {
                                    vm.getLocalChapter(
                                        nextChapter.id,
                                    )
                                    scrollState.scrollToItem(0, 0)
                                }
                            }
                        } catch (e: Throwable) {
                            Log.error(e, "Reader Spec failed to go next chapter")
                        }
                    } else {
                        scope.launch {
                            vm.showSnackBar(org.ireader.common_extensions.UiText.StringResource(R.string.this_is_last_chapter))
                        }
                    }
                },
                onReaderChapter = { ch ->
                    val index = vm.stateChapters.indexOfFirst { it.id == ch.id }
                    if (index != -1) {
                        vm.getLocalChapter(
                            ch.id,
                        )
                        scope.launch {
                            scrollState.scrollToItem(0, 0)
                        }
                        vm.currentChapterIndex = index
                    }
                },
                onReaderPrev = { scrollToEnd ->
                    try {
                        if (currentIndex > 0) {
                            val prevChapter = vm.prevChapter()
                            scope.launch {
                                vm.getLocalChapter(
                                    prevChapter.id,
                                )
                                when (scrollToEnd) {
                                    true -> {
                                        if (chapter != null) {
                                            scrollState.scrollToItem(
                                                chapter.content.lastIndex,
                                                Int.MAX_VALUE
                                            )
                                        }
                                    }
                                    else -> {
                                        scrollState.scrollToItem(0, 0)
                                    }
                                }
                            }
                        } else {
                            scope.launch {
                                vm.showSnackBar(
                                    org.ireader.common_extensions.UiText.StringResource(
                                        R.string.this_is_first_chapter
                                    )
                                )
                            }
                        }
                    } catch (e: Throwable) {
                        Log.error(e, "Reader Spec failed to go previous chapter")
                    }
                },
                onReaderSliderChange = {
                    vm.currentChapterIndex = it.toInt()
                },
                onReaderSliderFinished = {
                    scope.launch {
                        vm.showSnackBar(org.ireader.common_extensions.UiText.DynamicString(chapters[vm.currentChapterIndex].title))
                    }
                    vm.currentChapterIndex = currentIndex
                    scope.launch {
                        vm.getLocalChapter(
                            chapters[vm.currentChapterIndex].id,
                        )
                    }

                    scope.launch {
                        scrollState.animateScrollToItem(0, 0)
                    }
                },
                source = source,
                onReaderDrawerRevereIcon = { ch ->
                    if (ch != null) {
                        vm.isDrawerAsc = !vm.isDrawerAsc
                    }
                },
                onReaderRefresh = { ch ->
                    if (ch != null) {
                        vm.getLocalChapter(
                            ch.id
                        )
                    }
                },
                onReaderWebView = { modalState ->
                    try {
                        if (chapter != null && !vm.isReaderModeEnable && modalState.targetValue == ModalBottomSheetValue.Expanded) {
                            navController.navigate(
                                WebViewScreenSpec.buildRoute(
                                    url = chapter.link,
                                )
                            )
                        } else if (chapter != null) {
                            navController.navigate(
                                WebViewScreenSpec.buildRoute(
                                    url = chapter.link,
                                )
                            )
                        }
                    } catch (e: Throwable) {
                        scope.launch {
                            vm.showSnackBar(org.ireader.common_extensions.UiText.ExceptionString(e))
                        }
                    }
                },
                onReaderBookmark = {
                    vm.bookmarkChapter()
                },
                onReaderBottomBarSetting = {
                    vm.toggleSettingMode(true, null)
                },
                onBottomBarReaderPlay = {
                    vm.book?.let { book ->
                        vm.stateChapter?.let { chapter ->
                            navController.navigate(
                                TTSScreenSpec.buildRoute(
                                    book.id,
                                    book.sourceId,
                                    chapterId = chapter.id
                                )
                            )

                        }
                    }

                },
                vm = vm,
                onMap = { drawer ->
                    scope.launch {
                        try {
                            val index =
                                vm.drawerChapters.value.indexOfFirst { it.id == vm.stateChapter?.id }
                            if (index != -1) {
                                drawer.scrollToItem(
                                    index,
                                    -drawer.layoutInfo.viewportEndOffset / 2
                                )
                            }
                        } catch (e: Throwable) {
                        }
                    }
                },
                drawerScrollState = drawerScrollState,
                scrollState = scrollState,
                swipeState = swipeState,
                onPopBackStack = {
                    navController.popBackStack()
                }
            )
        } else {
            EmptyScreenComposable(
                onPopBackStack = {
                    navController.popBackStack()
                },
                errorResId = org.ireader.presentation.R.string.something_is_wrong_with_this_book
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable
fun MainReader(
    vm: ReaderScreenViewModel,
    source: Source,
    onPopBackStack: () -> Unit,
    scrollState: LazyListState,
    drawerScrollState: LazyListState,
    swipeState: SwipeRefreshState,
    onChapter: (Chapter) -> Unit,
    onReaderNext: () -> Unit,
    onReaderPrev: (scrollToEnd: Boolean) -> Unit,
    onReaderChapter: (Chapter) -> Unit,
    onReaderSliderFinished: () -> Unit,
    onReaderSliderChange: (index: Float) -> Unit,
    onReaderDrawerRevereIcon: (Chapter?) -> Unit,
    onReaderRefresh: (Chapter?) -> Unit,
    onReaderWebView: (ModalBottomSheetState) -> Unit,
    onReaderBookmark: () -> Unit,
    onReaderBottomBarSetting: () -> Unit,
    onBottomBarReaderPlay: () -> Unit,
    onMap: (LazyListState) -> Unit,
) {
    val context = LocalContext.current
    TransparentStatusBar {

        ReadingScreen(
            vm = vm,
            scrollState = scrollState,
            source = source,
            onNext = onReaderNext,
            onPrev = onReaderPrev,
            onSliderFinished = onReaderSliderFinished,
            onSliderChange = onReaderSliderChange,
            swipeState = swipeState,
            drawerScrollState = drawerScrollState,
            onChapter = onReaderChapter,
            onDrawerRevereIcon = onReaderDrawerRevereIcon,
            onReaderRefresh = onReaderRefresh,
            onToggleScrollMode = {
                vm.prefFunc.apply {
                    vm.toggleScrollMode()
                }
            },
            onToggleSelectedMode = {
                vm.prefFunc.apply {
                    vm.toggleSelectableMode()
                }
            },
            onToggleOrientation = {
                vm.prefFunc.apply {
                    vm.saveOrientation(context)
                }
            },
            onToggleImmersiveMode = {
                vm.prefFunc.apply {
                    vm.toggleImmersiveMode(context)
                }
            },
            onToggleAutoScroll = {
                vm.prefFunc.apply {
                    vm.toggleAutoScrollMode()
                }
            },
            onScrollIndicatorWidthIncrease = {
                when (it) {
                    true -> {
                        vm.prefFunc.apply {
                            vm.saveScrollIndicatorWidth(true)
                        }
                    }
                    else -> {
                        vm.prefFunc.apply {
                            vm.saveScrollIndicatorWidth(false)
                        }
                    }
                }
            },
            onScrollIndicatorPaddingIncrease = {
                when (it) {
                    true -> {
                        vm.prefFunc.apply {
                            vm.saveScrollIndicatorPadding(true)
                        }
                    }
                    else -> {
                        vm.apply {
                            vm.saveScrollIndicatorPadding(false)
                        }
                    }
                }
            },
            onParagraphIndentIncrease = {
                when (it) {
                    true -> {
                        vm.prefFunc.apply {
                            vm.saveParagraphIndent(true)
                        }
                    }
                    else -> {
                        vm.prefFunc.apply {
                            vm.saveParagraphIndent(false)
                        }
                    }
                }
            },
            onParagraphDistanceIncrease = {
                when (it) {
                    true -> {
                        vm.prefFunc.apply {
                            vm.saveParagraphDistance(true)
                        }
                    }
                    else -> {
                        vm.prefFunc.apply {
                            vm.saveParagraphDistance(false)
                        }
                    }
                }
            },
            onLineHeightIncrease = {
                when (it) {
                    true -> {
                        vm.prefFunc.apply {
                            vm.saveFontHeight(true)
                        }
                    }
                    else -> {
                        vm.prefFunc.apply {
                            vm.saveFontHeight(false)
                        }
                    }
                }
            },
            onFontSizeIncrease = {
                when (it) {
                    true -> {
                        vm.apply {
                            vm.saveFontSize(true)
                        }
                    }
                    else -> {
                        vm.apply {
                            vm.saveFontSize(false)
                        }
                    }
                }
            },
            onAutoscrollOffsetIncrease = {
                when (it) {
                    true -> {
                        vm.prefFunc.apply {
                            vm.setAutoScrollOffsetReader(true)
                        }
                    }
                    else -> {
                        vm.prefFunc.apply {
                            vm.setAutoScrollOffsetReader(false)
                        }
                    }
                }
            },
            onAutoscrollIntervalIncrease = {
                when (it) {
                    true -> {
                        vm.prefFunc.apply {
                            vm.setAutoScrollIntervalReader(true)
                        }
                    }
                    else -> {
                        vm.prefFunc.apply {
                            vm.setAutoScrollIntervalReader(false)
                        }
                    }
                }
            },
            onFontSelected = { index ->
                vm.apply {
                    vm.saveFont(index)
                }
            },
            onReaderPlay = onBottomBarReaderPlay,
            onReaderBottomOnSetting = onReaderBottomBarSetting,
            onReaderBookmark = onReaderBookmark,
            onReaderWebView = onReaderWebView,
            onChangeBrightness = {
                vm.apply {
                    vm.saveBrightness(it, context)
                }
            },
            onToggleAutoBrightness = {
                vm.prefFunc.apply {
                    vm.toggleAutoBrightness()
                }
            },
            onBackgroundChange = { index ->
                vm.prefFunc.apply {
                    vm.changeBackgroundColor(index)
                }
            },
            onMap = onMap,
            onPopBackStack = onPopBackStack
        )
    }
}