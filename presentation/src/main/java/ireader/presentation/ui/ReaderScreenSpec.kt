package ireader.presentation.ui

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavDeepLink
import androidx.navigation.navDeepLink
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ireader.common.resources.UiEvent
import ireader.common.resources.UiText
import ireader.common.resources.asString
import ireader.ui.component.Controller
import ireader.ui.component.findComponentActivity
import ireader.core.api.log.Log
import ireader.core.ui.preferences.ReadingMode
import ireader.core.ui.theme.AppColors
import ireader.core.ui.theme.CustomSystemColor
import ireader.core.ui.theme.FontType
import ireader.core.ui.theme.getDefaultFont
import ireader.presentation.ui.util.NavigationArgs
import ireader.presentation.R
import ireader.ui.reader.ReaderScreenDrawer
import ireader.ui.reader.ReaderScreenTopBar
import ireader.ui.reader.ReadingScreen
import ireader.ui.reader.components.ReaderSettingMainLayout
import ireader.ui.reader.reverse_swip_refresh.rememberSwipeRefreshState
import ireader.ui.reader.viewmodel.ReaderScreenViewModel
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
object ReaderScreenSpec : ScreenSpec {

    override val navHostRoute: String = "reader_screen_route/{bookId}/{chapterId}/"

    override val arguments: List<NamedNavArgument> = listOf(
        NavigationArgs.bookId,
        NavigationArgs.chapterId,
        NavigationArgs.haveDrawer,
        NavigationArgs.showModalSheet,
    )

    fun buildRoute(
        bookId: Long,
        chapterId: Long,
    ): String {
        return "reader_screen_route/$bookId/$chapterId/"
    }

    fun buildDeepLink(
        bookId: Long,
        sourceId: Long,
        chapterId: Long,
        readingParagraph: Long,
    ): String {
        return "https://www.ireader.org/reader_screen_route/$bookId/$chapterId/$readingParagraph"
    }

    override val deepLinks: List<NavDeepLink> = listOf(

        navDeepLink {
            uriPattern =
                "https://www.ireader.org/reader_screen_route/{bookId}/{chapterId}/{readingParagraph}"
            NavigationArgs.bookId
            NavigationArgs.chapterId
            NavigationArgs.readingParagraph
        }
    )

    @OptIn(
        ExperimentalAnimationApi::class,
        ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class
    )
    @Composable
    override fun Content(
        controller: Controller
    ) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val vm: ReaderScreenViewModel = getViewModel(owner = controller.navBackStackEntry, parameters = {
            org.koin.core.parameter.parametersOf(
                ReaderScreenViewModel.createParam(controller)
            )
        })
        val currentIndex = vm.currentChapterIndex
        val chapters = vm.stateChapters
        val chapter = vm.stateChapter

        val scrollState = rememberScrollState()
        val lazyListState = rememberLazyListState()

        DisposableEffect(key1 = scrollState.hashCode()) {
            vm.readerScrollState = scrollState
            onDispose {  }
        }

        val swipeState = rememberSwipeRefreshState(isRefreshing = false)
        DisposableEffect(key1 = true) {
            onDispose {
                vm.restoreSetting(context, scrollState, lazyListState)
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
        LaunchedEffect(key1 = vm.orientation.value) {
            vm.prefFunc.apply {
                vm.readOrientation(context)
            }
        }

        LaunchedEffect(key1 = vm.screenAlwaysOn.value) {
            vm.prefFunc.apply {
                vm.screenAlwaysOnUseCase(context, vm.screenAlwaysOn.value)
            }
        }
        val bars = AppColors.current
        LaunchedEffect(key1 = true) {
            if (Build.VERSION.SDK_INT < 25 && bars.isBarLight) {
                controller.requestedCustomSystemColor(CustomSystemColor(Color.LightGray, bars.bars))
            } else {
                controller.requestedCustomSystemColor(CustomSystemColor(bars.bars, bars.bars))
            }
        }

        LaunchedEffect(key1 = vm.initialized) {

            vm.prepareReaderSetting(
                context,
                scrollState,
                onHideNav = controller.requestHideSystemNavbar,
                onHideStatus = controller.requestedHideSystemStatusBar
            )
        }

        LaunchedEffect(key1 = vm.immersiveMode.value) {
            vm.prefFunc.apply {
                vm.readImmersiveMode(
                    context,
                    controller.requestHideSystemNavbar,
                    controller.requestedHideSystemStatusBar
                )
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
                                vm.getLocalChapter(
                                    nextChapter.id,
                                )
                            }
                            scope.launch {
                                if (rest) {
                                    vm.clearChapterShell(scrollState)
                                }
                            }
                            when (readingMode.value) {
                                ReadingMode.Continues -> {}
                                ReadingMode.Page -> {
                                    scope.launch {
                                        scrollState.scrollTo(0)
                                    }
                                }
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
            onPrev = { reset ->
                try {
                    if (currentIndex > 0) {
                        val prevChapter = vm.prevChapter()

                        scope.launch {
                            if (reset) {
                                vm.clearChapterShell(scrollState)
                            }

                            vm.getLocalChapter(
                                prevChapter.id,
                                false
                            )

                            if (vm.readingMode.value == ReadingMode.Page && !reset) {
                                scrollState.scrollTo(scrollState.maxValue)
                            }
                            if (vm.readingMode.value == ReadingMode.Continues) {
                                lazyListState.scrollToItem(1)
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
                    scrollState.animateScrollTo(0)
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
            lazyListState = lazyListState,
            onChapterShown = { chapter ->
                if (chapter.id != vm.stateChapter?.id) {
                    kotlin.runCatching {
                        vm.stateChapter = chapter
                        val index =
                            vm.stateChapters.indexOfFirst { it.id == chapter.id }
                        if (index != -1) {
                            vm.currentChapterIndex = index
                        }
                    }
                }
            }
        )
    }

    @Composable
    override fun ModalDrawer(
        controller: Controller
    ) {
        val vm: ReaderScreenViewModel = getViewModel(owner = controller.navBackStackEntry, parameters = {
            org.koin.core.parameter.parametersOf(
                ReaderScreenViewModel.createParam(controller)
            )
        })
        val drawerScrollState = rememberLazyListState()
        val drawerState = controller.drawerState
        val chapter = vm.stateChapter
        val scope = rememberCoroutineScope()
        val scrollState = vm.readerScrollState
        LaunchedEffect(key1 = drawerState.targetValue) {
            if (chapter != null && drawerState.targetValue == androidx.compose.material3.DrawerValue.Open && vm.stateChapters.isNotEmpty()) {
                val index = vm.stateChapters.indexOfFirst { it.id == chapter.id }
                if (index != -1) {
                    scope.launch {
                        drawerScrollState.scrollToItem(
                            index,
                            -drawerScrollState.layoutInfo.viewportEndOffset / 2
                        )
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(initialOffsetY = { -it }),
            exit = slideOutVertically(targetOffsetY = { -it })
        ) {
            ReaderScreenDrawer(
                modifier = if (vm.immersiveMode.value) Modifier else Modifier.systemBarsPadding(),
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
                            scrollState?.scrollTo(0)
                        }
                        vm.currentChapterIndex = index
                    }
                },
                chapter = chapter,
                chapters = vm.drawerChapters.value,
                drawerScrollState = drawerScrollState,
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

    @Composable
    override fun TopBar(
        controller: Controller
    ) {

        val vm: ReaderScreenViewModel = getViewModel(owner = controller.navBackStackEntry, parameters = {
            org.koin.core.parameter.parametersOf(
                ReaderScreenViewModel.createParam(controller)
            )
        })
        val catalog = vm.catalog
        val book = vm.book
        val chapter = vm.stateChapter
        val readerScrollState = vm.readerScrollState
        val scope = rememberCoroutineScope()
        if (readerScrollState != null) {
            ReaderScreenTopBar(
                // modifier = Modifier.padding(controller.scaffoldPadding),
                modifier = Modifier,
                isReaderModeEnable = vm.isReaderModeEnable,
                isLoaded = vm.isChapterLoaded.value,
                modalBottomSheetValue = controller.sheetState.targetValue,
                onRefresh = {
                    scope.launch {
                        vm.getLocalChapter(
                            chapter?.id,
                            force = true
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
                                    bookId = book?.id,
                                    enableChapterFetch = true
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
        controller: Controller
    ) {
        val vm: ReaderScreenViewModel = getViewModel(owner = controller.navBackStackEntry, parameters = {
            org.koin.core.parameter.parametersOf(
                ReaderScreenViewModel.createParam(controller)
            )
        })
        val context = LocalContext.current

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
                    vm.font.value = FontType(vm.fonts.getOrNull(index) ?: getDefaultFont().name, FontFamily.Default)
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

@Composable
fun LockScreenOrientation(orientation: Int) {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val activity = context.findComponentActivity() ?: return@DisposableEffect onDispose {}
        val originalOrientation = activity.requestedOrientation
        activity.requestedOrientation = orientation
        onDispose {
            // restore original orientation when view disappears
            activity.requestedOrientation = originalOrientation
        }
    }
}
