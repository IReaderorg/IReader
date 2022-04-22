package org.ireader.presentation.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.*
import com.google.accompanist.pager.ExperimentalPagerApi
import kotlinx.coroutines.launch
import org.ireader.core.R
import org.ireader.core.utils.UiText
import org.ireader.core_api.log.Log
import org.ireader.core_api.source.Source
import org.ireader.core_ui.theme.TransparentStatusBar
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.services.tts_service.Player
import org.ireader.domain.ui.NavigationArgs
import org.ireader.presentation.feature_reader.presentation.reader.ReadingScreen
import org.ireader.presentation.feature_reader.presentation.reader.reverse_swip_refresh.SwipeRefreshState
import org.ireader.presentation.feature_reader.presentation.reader.reverse_swip_refresh.rememberSwipeRefreshState
import org.ireader.presentation.feature_reader.presentation.reader.viewmodel.FontSizeEvent
import org.ireader.presentation.feature_reader.presentation.reader.viewmodel.ReaderScreenViewModel
import org.ireader.presentation.feature_ttl.TTSScreen
import org.ireader.presentation.feature_ttl.TTSState
import org.ireader.presentation.presentation.EmptyScreenComposable

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
        voiceMode: Long,
    ): String {
        return "https://www.ireader.org/reader_screen_route/$bookId/$chapterId/$sourceId/$readingParagraph/$voiceMode"
    }

    override val deepLinks: List<NavDeepLink> = listOf(
        navDeepLink {
            uriPattern =
                "https://www.ireader.org/reader_screen_route/{bookId}/{chapterId}/{sourceId}/{readingParagraph}/{voiceMode}"
            NavigationArgs.bookId
            NavigationArgs.chapterId
            NavigationArgs.sourceId
            NavigationArgs.readingParagraph
            NavigationArgs.voiceMode
        }
    )

    @OptIn(ExperimentalPagerApi::class, androidx.compose.animation.ExperimentalAnimationApi::class,
        androidx.compose.material.ExperimentalMaterialApi::class)
    @Composable
    override fun Content(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        scaffoldState: ScaffoldState,
    ) {
        val vm: ReaderScreenViewModel = hiltViewModel()
        val currentIndex = vm.currentChapterIndex
        val source = vm.ttsSource
        val chapters = vm.stateChapters
        val chapter = vm.stateChapter
        val scope = rememberCoroutineScope()
        val scrollState = rememberLazyListState()
        val drawerScrollState = rememberLazyListState()
        val swipeState = rememberSwipeRefreshState(isRefreshing = vm.ttsIsLoading)
        val context = LocalContext.current


        DisposableEffect(key1 = true) {
            onDispose {
                vm.uiFunc.apply {
                    vm.restoreSetting(context, scrollState)
                }
            }
        }
        if (source != null) {
            MainReader(
                onTTTSPrev = {
                    vm.runTTSService(context, Player.SKIP_PREV)
                },
                onTTTSPlay = {
                    vm.runTTSService(context, Player.PLAY_PAUSE)
                },
                onTTTSNext = {
                    vm.runTTSService(context, Player.SKIP_NEXT)
                },
                onChapter = { ch ->
                    val index = vm.stateChapters.indexOfFirst { it.id == ch.id }
                    if (index != -1) {
                        scope.launch {
                            vm.mainFunc.apply {
                                vm.getChapter(ch.id,
                                    source = source) {
                                    vm.runTTSService(context)
                                }
                            }

                        }
                        scope.launch {
                            scrollState.scrollToItem(0, 0)
                        }
                        vm.uiFunc.apply {
                            vm.currentChapterIndex = index
                        }
                    }
                },
                onTTTSPrevPar = {
                    vm.runTTSService(context, Player.PREV_PAR)
                },
                onTTTSNextPar = {
                    vm.runTTSService(context, Player.NEXT_PAR)
                },
                onTTTSValueChange = {
                    vm.ttsState.tts?.stop()
                    vm.currentReadingParagraph = it.toInt()
                },
                onTTTSValueChangeFinished = {
                    if (vm.isPlaying) {
                        vm.runTTSService(context)
                    }
                },
                onReaderNext = {
                    if (currentIndex < chapters.lastIndex) {
                        try {
                            vm.apply {
                                val nextChapter = vm.nextChapter()
                                scope.launch {
                                    vm.getChapter(nextChapter.id,
                                        source = source)
                                    scrollState.scrollToItem(0, 0)
                                }
                            }
                        } catch (e: Throwable) {
                            Log.error(e, "Reader Spec failed to go next chapter")
                        }
                    } else {
                        scope.launch {
                            vm.showSnackBar(UiText.StringResource(R.string.this_is_last_chapter))

                        }
                    }
                },
                onReaderChapter = { ch ->
                    val index = vm.stateChapters.indexOfFirst { it.id == ch.id }
                    if (index != -1) {
                        vm.uiFunc.apply {
                            vm.mainFunc.apply {
                                scope.launch {
                                    vm.getChapter(ch.id,
                                        source = source)
                                }
                                scope.launch {
                                    scrollState.scrollToItem(0, 0)
                                }
                                vm.currentChapterIndex = index
                            }
                        }
                    }
                },
                onReaderPrev = { scrollToEnd ->
                    try {
                        if (currentIndex > 0) {
                            vm.mainFunc.apply {
                                val prevChapter = vm.prevChapter()
                                scope.launch {
                                    vm.getChapter(prevChapter.id,
                                        source = source)
                                    when (scrollToEnd) {
                                        true -> {
                                            if (chapter != null) {
                                                scrollState.scrollToItem(chapter.content.lastIndex,
                                                    Int.MAX_VALUE)
                                            }
                                        }
                                        else -> {
                                            scrollState.scrollToItem(0, 0)
                                        }
                                    }
                                }
                            }
                        } else {
                            scope.launch {
                                vm.showSnackBar(UiText.StringResource(R.string.this_is_first_chapter))
                            }
                        }
                    } catch (e: Throwable) {
                        Log.error(e, "Reader Spec failed to go previous chapter")
                    }
                },
                onReaderSliderChange = {
                    vm.uiFunc.apply {
                        vm.currentChapterIndex = it.toInt()
                    }
                },
                onReaderSliderFinished = {
                    scope.launch {
                        vm.showSnackBar(UiText.DynamicString(chapters[vm.currentChapterIndex].title))
                    }
                    vm.uiFunc.apply {
                        vm.mainFunc.apply {
                            vm.currentChapterIndex = currentIndex
                            scope.launch {
                                vm.getChapter(chapters[vm.currentChapterIndex].id,
                                    source = source)
                            }
                        }
                    }
                    scope.launch {
                        scrollState.animateScrollToItem(0, 0)
                    }
                },
                source = source,
                ttsState = vm,
                onReaderDrawerRevereIcon = { ch ->
                    if (ch != null) {
                        vm.uiFunc.apply {
                            vm.isDrawerAsc = !vm.isDrawerAsc
                        }
//                        vm.mainFunc.apply {
//                            scope.launch {
//                                vm.getLocalChaptersByPaging(ch.bookId)
//                            }
//                        }
                    }
                },
                onReaderRefresh = { ch ->
                    if (ch != null) {
                        vm.mainFunc.apply {
                            vm.getReadingContentRemotely(chapter = ch,
                                source = source)
                        }
                    }
                },
                onReaderWebView = { modalState ->
                    try {
                        if (chapter != null && !vm.isReaderModeEnable && vm.isLocalLoaded && modalState.targetValue == ModalBottomSheetValue.Expanded) {
                            navController.navigate(WebViewScreenSpec.buildRoute(
                                url = chapter.link,
                            )
                            )
                        } else if (chapter != null && !vm.isLocalLoaded) {
                            navController.navigate(WebViewScreenSpec.buildRoute(
                                url = chapter.link,
                            ))
                        }
                    } catch (e: Throwable) {
                        scope.launch {
                            vm.showSnackBar(UiText.ExceptionString(e))
                        }
                    }
                },
                onReaderBookmark = {
                    vm.uiFunc.apply {
                        vm.bookmarkChapter()

                    }
                },
                onReaderBottomBarSetting = {
                    vm.uiFunc.apply {
                        vm.toggleSettingMode(true)
                    }
                },
                onBottomBarReaderPlay = {
                    vm.voiceMode = true
                    vm.state.isReaderModeEnable = true
                },
                vm = vm,
                navController = navController,
                onMap = { drawer ->
                    scope.launch {
                        try {
                            val index =
                                vm.drawerChapters.value.indexOfFirst { it.id == vm.stateChapter?.id }
                            if (index != -1) {
                                drawer.scrollToItem(index,
                                    -drawer.layoutInfo.viewportEndOffset / 2)
                            }
                        } catch (e: Throwable) {

                        }
                    }
                },
                drawerScrollState = drawerScrollState,
                scrollState = scrollState,
                swipeState = swipeState
            )
        } else {
            EmptyScreenComposable(navController = navController,
                errorResId = org.ireader.presentation.R.string.something_is_wrong_with_this_book)
        }


    }
}


@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable
fun MainReader(
    vm: ReaderScreenViewModel,
    ttsState: TTSState,
    source: Source,
    navController: NavController,
    scrollState: LazyListState,
    drawerScrollState: LazyListState,
    swipeState: SwipeRefreshState,
    onTTTSPrev: () -> Unit,
    onTTTSPrevPar: () -> Unit,
    onTTTSNext: () -> Unit,
    onTTTSNextPar: () -> Unit,
    onTTTSPlay: () -> Unit,
    onTTTSValueChange: (Float) -> Unit,
    onTTTSValueChangeFinished: () -> Unit,
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
    onMap:(LazyListState) -> Unit
) {
    val context = LocalContext.current
    when {
        ttsState.voiceMode -> {
            TTSScreen(
                vm = vm,
                onPrev = onTTTSPrev,
                onPlay = onTTTSPlay,
                onNext = onTTTSNext,
                onChapter = onChapter,
                source = source,
                navController = navController,
                onPrevPar = onTTTSPrevPar,
                onNextPar = onTTTSNextPar,
                onValueChange = onTTTSValueChange,
                onValueChangeFinished = onTTTSValueChangeFinished,
                onMap = onMap
            )
        }
        else -> {


            TransparentStatusBar {

                ReadingScreen(
                    navController = navController,
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
                                    vm.saveFontSize(FontSizeEvent.Increase)
                                }
                            }
                            else -> {
                                vm.apply {
                                    vm.saveFontSize(FontSizeEvent.Decrease)
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
                    onMap = onMap
                )
            }


        }
    }


}