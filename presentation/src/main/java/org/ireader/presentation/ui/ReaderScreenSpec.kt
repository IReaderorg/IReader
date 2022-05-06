package org.ireader.presentation.ui

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.DrawerValue
import androidx.compose.material.rememberDrawerState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDeepLink
import androidx.navigation.navDeepLink
import com.google.accompanist.pager.ExperimentalPagerApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.ireader.common_extensions.async.viewModelIOCoroutine
import org.ireader.common_resources.UiEvent
import org.ireader.common_resources.UiText
import org.ireader.core.R
import org.ireader.core_api.log.Log
import org.ireader.domain.ui.NavigationArgs
import org.ireader.reader.ReadingScreen
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
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val vm: ReaderScreenViewModel = hiltViewModel()
        val currentIndex = vm.currentChapterIndex
        val source = vm.source
        val chapters = vm.stateChapters
        val chapter = vm.stateChapter
        val book = vm.book

        val scrollState = rememberLazyListState()
        val drawerScrollState = rememberLazyListState()
        val swipeState = rememberSwipeRefreshState(isRefreshing = false)
        val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))
        DisposableEffect(key1 = true) {
            onDispose {
                vm.restoreSetting(context, scrollState)

            }
        }
        if (source != null) {
            LaunchedEffect(key1 = vm.autoScrollMode) {
                while (vm.autoScrollInterval != 0L && vm.autoScrollMode) {
                    scrollState.scrollBy(vm.autoScrollOffset.toFloat())
                    delay(vm.autoScrollInterval)
                }
            }
            LaunchedEffect(key1 = vm.autoBrightnessMode) {
                vm.prefFunc.apply {
                    vm.readBrightness(context)
                }
            }
            LaunchedEffect(key1 = vm.initialized) {
                vm.prefFunc.apply {
                    vm.readImmersiveMode(context)
                }
                kotlin.runCatching {
                    scrollState.scrollToItem(chapter?.progress ?: 1)
                }
            }
            LaunchedEffect(key1 = true) {
                vm.prefFunc.apply {
                    vm.readImmersiveMode(context)
                }
            }
            LaunchedEffect(key1 = true) {

                vm.prefFunc.apply {
                    vm.readOrientation(context)
                    vm.readBrightness(context)
                   // vm.readImmersiveMode(context)
                }

                vm.eventFlow.collectLatest { event ->
                    when (event) {
                        is UiEvent.ShowSnackbar -> {
                            scaffoldState.snackbarHostState.showSnackbar(
                                event.uiText.asString(context)
                            )
                        }
                        else -> {}
                    }
                }
            }
            ReadingScreen(
                scaffoldState = scaffoldState,
                vm = vm,
                scrollState = scrollState,
                source = source,
                onNext = {
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
                            vm.showSnackBar(
                                UiText.StringResource(
                                    R.string.this_is_last_chapter
                                )
                            )
                        }
                    }
                },
                onPrev = { scrollToEnd ->
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
                                    UiText.StringResource(
                                        R.string.this_is_first_chapter
                                    )
                                )
                            }
                        }
                    } catch (e: Throwable) {
                        Log.error(e, "Reader Spec failed to go previous chapter")
                    }
                },
                onSliderFinished = {
                    scope.launch {
                        vm.showSnackBar(
                            UiText.DynamicString(
                                chapters[vm.currentChapterIndex].title
                            )
                        )
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
                onSliderChange = {
                    vm.currentChapterIndex = it.toInt()
                },
                swipeState = swipeState,
                drawerScrollState = drawerScrollState,
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
                onDrawerRevereIcon = { ch ->
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
                onReaderPlay = {
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
                onReaderBottomOnSetting = {
                    vm.toggleSettingMode(true, null)
                },
                onReaderBookmark = {
                    vm.bookmarkChapter()
                },
                onReaderWebView = { modalState ->
                    try {
                        navController.navigate(
                            WebViewScreenSpec.buildRoute(
                                url = chapter?.link,
                                sourceId = source.id,
                                chapterId = chapter?.id,
                                bookId = book?.id
                            )
                        )


//                        if (chapter != null && !vm.isReaderModeEnable && modalState.targetValue == ModalBottomSheetValue.Expanded) {
//
//
//                            navController.navigate(
//                                WebViewScreenSpec.buildRoute(
//                                    url = chapter.link,
//                                    sourceId = source.id,
//                                    chapterId = chapter.id,
//                                    bookId = book?.id
//                                )
//                            )
//                        } else if (chapter != null) {
//                            navController.navigate(
//                                WebViewScreenSpec.buildRoute(
//                                    url = chapter.link,
//                                    sourceId = source.id,
//                                    chapterId = chapter.id,
//                                    bookId = book?.id
//                                )
//                            )
//                        }
                    } catch (e: Throwable) {
                        scope.launch {
                            vm.showSnackBar(
                                UiText.ExceptionString(
                                    e
                                )
                            )
                        }
                    }
                },
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
                onPopBackStack = {
                    navController.popBackStack()
                },
                toggleReaderMode = {
                    vm.apply {
                        vm.toggleReaderMode(!vm.isReaderModeEnable)
                    }
                },
                readerScreenPreferencesState = vm,
                onDismiss = {
                    vm.scrollIndicatorDialogShown = false
                    vm.prefFunc.apply {
                        vm.viewModelIOCoroutine {
                            vm.readScrollIndicatorPadding()
                            vm.readScrollIndicatorWidth()
                        }
                    }
                    vm.prefFunc.apply {
                        vm.viewModelIOCoroutine {
                            vm.readBackgroundColor()
                            vm.readTextColor()
                        }
                    }
                },
                onBackgroundValueChange = {
                    try {
                        vm.backgroundColor = Color(it.toColorInt())
                    } catch (e: Throwable) {
                        vm.prefFunc.apply {
                            vm.viewModelIOCoroutine {
                                vm.readBackgroundColor()
                            }
                        }
                    }
                },
                onTextColorValueChange = {
                    try {
                        vm.textColor = Color(it.toColorInt())
                    } catch (e: Throwable) {
                        vm.prefFunc.apply {
                            vm.viewModelIOCoroutine {
                                vm.readTextColor()
                            }
                        }
                    }
                },
                onBackgroundColorAndTextColorApply = { bgColor, txtColor ->
                    try {
                        if (bgColor.isNotBlank()) {
                            vm.prefFunc.apply {
                                vm.setReaderBackgroundColor(vm.backgroundColor)
                            }
                        }
                    } catch (e: Throwable) {
                    }

                    try {
                        if (txtColor.isNotBlank()) {
                            vm.prefFunc.apply {
                                vm.setReaderTextColor(vm.textColor)
                            }
                        }
                    } catch (e: Throwable) {
                    }
                },
                onShowScrollIndicator = {
                    vm.showScrollIndicator = it
                    vm.apply {
                        vm.setShowScrollIndicator(it)
                    }
                },
                onTextAlign = {
                    vm.textAlignment = it
                    vm.readerUseCases.textAlignmentUseCase.save(it)
                }

            )
        }
    }
}