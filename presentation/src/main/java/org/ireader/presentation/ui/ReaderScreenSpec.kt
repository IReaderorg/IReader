package org.ireader.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
import org.ireader.common_resources.UiEvent
import org.ireader.common_resources.UiText
import org.ireader.core.R
import org.ireader.core_api.log.Log
import org.ireader.core_ui.theme.Roboto
import org.ireader.core_ui.theme.fonts
import org.ireader.domain.ui.NavigationArgs
import org.ireader.reader.ReaderScreenDrawer
import org.ireader.reader.ReaderScreenTopBar
import org.ireader.reader.ReadingScreen
import org.ireader.reader.components.ReaderSettingMainLayout
import org.ireader.reader.reverse_swip_refresh.rememberSwipeRefreshState
import org.ireader.reader.viewmodel.ReaderScreenViewModel

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
object ReaderScreenSpec : ScreenSpec {

    override val navHostRoute: String = "reader_screen_route/{bookId}/{chapterId}/{sourceId}"

    override val arguments: List<NamedNavArgument> = listOf(
        NavigationArgs.bookId,
        NavigationArgs.chapterId,
        NavigationArgs.sourceId,
        NavigationArgs.haveDrawer,
        NavigationArgs.showModalSheet,
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
            NavigationArgs.systemBarPadding
        }
    )

    @OptIn(
        ExperimentalPagerApi::class, ExperimentalAnimationApi::class,
        ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class
    )
    @Composable
    override fun Content(
        controller: ScreenSpec.Controller
    ) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val vm: ReaderScreenViewModel = hiltViewModel   (controller.navBackStackEntry)
        val currentIndex = vm.currentChapterIndex
        val chapters = vm.stateChapters
        val chapter = vm.stateChapter

        val scrollState = rememberLazyListState()
        val drawerScrollState = rememberLazyListState()


        LaunchedEffect(key1 = drawerScrollState.hashCode()) {
            vm.drawerListState = drawerScrollState
        }
        LaunchedEffect(key1 = scrollState.hashCode()) {
            vm.readerScrollState = scrollState
        }
        val chapterIdKey by remember {
            derivedStateOf { scrollState.getId() }
        }

        LaunchedEffect(key1 = chapterIdKey) {
            kotlin.runCatching {
                vm.stateChapters.firstOrNull {
                    it.id == chapterIdKey
                }?.let {
                    vm.stateChapter = it
                }
                val index = vm.stateChapters.indexOfFirst { it.id == chapterIdKey }
                if (index != -1) {
                    vm.currentChapterIndex = index
                }
            }
        }

        val swipeState = rememberSwipeRefreshState(isRefreshing = false)
        DisposableEffect(key1 = true) {
            onDispose {
                vm.restoreSetting(context, scrollState)

            }
        }

        LaunchedEffect(key1 = vm.autoScrollMode) {
            while (vm.autoScrollInterval.value.toInt() != 0 && vm.autoScrollMode) {
                scrollState.scrollBy(vm.autoScrollOffset.value.toFloat())
                delay(vm.autoScrollInterval.value.toLong())
            }
        }
        LaunchedEffect(key1 = vm.autoBrightnessMode.value) {
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
//        LaunchedEffect(key1 = true) {
//            vm.prefFunc.apply {
//                vm.readOrientation(context)
//                //vm.readBrightness(context)
//                // vm.readImmersiveMode(context)
//            }
//        }

        LaunchedEffect(key1 = vm.orientation.value) {
            vm.prefFunc.apply {
                vm.readOrientation(context)
            }
        }
        LaunchedEffect(key1 = true) {

            vm.eventFlow.collectLatest { event ->
                when (event) {
                    is UiEvent.ShowSnackbar -> {
                        controller.snackBarHostState.showSnackbar(
                            event.uiText.asString(context)
                        )
                    }
                    else -> {}
                }
            }
        }

        Scaffold() { padding ->

            Box(modifier = Modifier
                .padding(padding)
                .systemBarsPadding()) {
                ReadingScreen(
                    drawerState = controller.drawerState,
                    vm = vm,
                    scrollState = scrollState,
                    onNext = { rest ->
                        if (currentIndex < chapters.lastIndex) {
                            try {
                                vm.apply {
                                    val nextChapter = vm.nextChapter()
                                    scope.launch {
                                        if (rest) {
                                            vm.clearChapterShell(scrollState)
                                        }
                                        vm.getLocalChapter(
                                            nextChapter.id,
                                        )

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
                    scaffoldPaddingValues = controller.scaffoldPadding,
                    onPrev = { reset ->
                        try {
                            if (currentIndex > 0) {
                                val prevChapter = vm.prevChapter()
                                scope.launch {
                                    if (reset) {
                                        vm.clearChapterShell(scrollState)
                                    }
                                    val ch = vm.getLocalChapter(
                                        prevChapter.id,
                                        false
                                    )
                                    if (vm.readingMode.value) {
                                        scrollState.scrollToItem(ch?.content?.lastIndex ?: 0)
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
                    toggleReaderMode = {
                        vm.apply {
                            vm.toggleReaderMode(!vm.isReaderModeEnable)
                        }
                    },
                    readerScreenPreferencesState = vm,
                    onBackgroundColorAndTextColorApply = { bgColor, txtColor ->
                        try {
                            if (bgColor.isNotBlank()) {
                                vm.prefFunc.apply {
                                    vm.setReaderBackgroundColor(vm.backgroundColor.value)
                                }
                            }
                        } catch (e: Throwable) {
                        }

                        try {
                            if (txtColor.isNotBlank()) {
                                vm.prefFunc.apply {
                                    vm.setReaderTextColor(vm.textColor.value)
                                }
                            }
                        } catch (e: Throwable) {
                        }
                    },

                    snackBarHostState = controller.snackBarHostState,
                    drawerScrollState = drawerScrollState,
                    swipeState = swipeState,
                    onSliderFinished = {
                        scope.launch {
                            vm.showSnackBar(
                                UiText.DynamicString(
                                    chapters[vm.currentChapterIndex].name
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
                    onReaderPlay = {
                        vm.book?.let { book ->
                            vm.stateChapter?.let { chapter ->
                                controller.navController.navigate(
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
                        scope.launch {
                            controller.sheetState.show()
                        }
                    },
                )
            }
        }
    }

    @Composable
    override fun ModalDrawer(
        controller: ScreenSpec.Controller
    ) {
        val vm: ReaderScreenViewModel = hiltViewModel   (controller.navBackStackEntry)
        val lazyListState = vm.drawerListState
        val chapter = vm.stateChapter
        val scope = rememberCoroutineScope()
        val scrollState = vm.readerScrollState
        if (lazyListState != null) {
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(initialOffsetY = { -it }),
                exit = slideOutVertically(targetOffsetY = { -it })
            ) {
                ReaderScreenDrawer(
                    modifier = Modifier.statusBarsPadding(),
                    onReverseIcon = {
                        vm.isDrawerAsc = !vm.isDrawerAsc
                    },
                    onChapter = { ch ->
                        val index = vm.stateChapters.indexOfFirst { it.id == ch.id }
                        if (index != -1) {
                            scope.launch {
                                vm.clearChapterShell(scrollState)
                                vm.getLocalChapter(ch.id)
                            }
                            scope.launch {
                                lazyListState.scrollToItem(0, 0)
                            }
                            vm.currentChapterIndex = index
                        }
                    },
                    chapter = chapter,
                    chapters = vm.drawerChapters.value,
                    drawerScrollState = lazyListState,
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
                )
            }
        }
    }

    @Composable
    override fun TopBar(
        controller: ScreenSpec.Controller
    ) {
        val vm: ReaderScreenViewModel = hiltViewModel   (controller.navBackStackEntry)
        val catalog = vm.catalog
        val book = vm.book
        val chapter = vm.stateChapter
        val readerScrollState = vm.readerScrollState
        val scope = rememberCoroutineScope()
        if (readerScrollState != null) {
            ReaderScreenTopBar(
                isReaderModeEnable = vm.isReaderModeEnable,
                isLoaded = vm.isChapterLoaded.value,
                modalBottomSheetValue = controller.sheetState.targetValue,
                onRefresh = {
                    scope.launch {
                        vm.getLocalChapter(
                            chapter?.id
                        )
                    }

                },
                chapter = chapter,
                onWebView = {
                    try {
                        catalog?.let { catalog ->
                            controller.navController.navigate(
                                WebViewScreenSpec.buildRoute(
                                    url = chapter?.key,
                                    sourceId = catalog.sourceId,
                                    chapterId = chapter?.id,
                                    bookId = book?.id
                                )
                            )
                        }
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
                vm = vm,
                state = vm,
                scrollState = readerScrollState,
                onBookMark = {
                    vm.bookmarkChapter()
                },
                onPopBackStack = {
                    controller.navController.popBackStack()
                }
            )
        }
    }

    @Composable
    override fun BottomModalSheet(
        controller: ScreenSpec.Controller
    ) {
        val vm: ReaderScreenViewModel = hiltViewModel   (controller.navBackStackEntry)
        val context = LocalContext.current

        LaunchedEffect(key1 = vm.immersiveMode.value) {
            vm.prefFunc.apply {
                vm.toggleImmersiveMode(vm.immersiveMode.value, context)
            }
        }
        Column(
            Modifier
                .fillMaxSize()
        ) {
            Divider(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = .2f),
                thickness = 1.dp
            )
            Spacer(modifier = Modifier.height(5.dp))
            ReaderSettingMainLayout(
                onFontSelected = { index ->
                    vm.font.value = fonts.getOrNull(index) ?: Roboto
                },
                onChangeBrightness = {
                    vm.apply {
                        vm.saveBrightness(it, context)
                    }
                },
                onBackgroundChange = { index ->
                    vm.prefFunc.apply {
                        vm.changeBackgroundColor(index)
                    }
                },
                vm = vm,
                onTextAlign = {
                    vm.textAlignment.value = it
                    vm.readerUseCases.textAlignmentUseCase.save(it)
                },
                onToggleAutoBrightness = {
                    vm.autoBrightnessMode.value = !vm.autoBrightnessMode.value
//                    vm.prefFunc.apply {
//                        vm.toggleAutoBrightness()
//                    }
                }
            )
        }
    }
}

private fun LazyListState.getId(): Long? {
    return kotlin.runCatching {
        return@runCatching this.layoutInfo.visibleItemsInfo.firstOrNull()?.key.toString()
            .substringAfter("-").toLong()
    }.getOrNull()
}