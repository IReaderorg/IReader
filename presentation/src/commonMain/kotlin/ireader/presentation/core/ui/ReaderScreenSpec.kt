package ireader.presentation.core.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.core.log.Log
import ireader.core.util.getBuildNumber
import ireader.domain.preferences.models.FontType
import ireader.domain.preferences.models.getDefaultFont
import ireader.domain.preferences.prefs.ReadingMode
import ireader.i18n.UiText
import ireader.i18n.resources.MR
import ireader.presentation.core.IModalDrawer
import ireader.presentation.core.IModalSheets
import ireader.presentation.core.VoyagerScreen
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.getContextWrapper
import ireader.presentation.ui.core.theme.AppColors
import ireader.presentation.ui.core.theme.CustomSystemColor
import ireader.presentation.ui.core.ui.SnackBarListener
import ireader.presentation.ui.reader.ReaderScreenDrawer
import ireader.presentation.ui.reader.ReaderScreenTopBar
import ireader.presentation.ui.reader.ReadingScreen
import ireader.presentation.ui.reader.components.ReaderSettingMainLayout
import ireader.presentation.ui.reader.reverse_swip_refresh.rememberSwipeRefreshState
import ireader.presentation.ui.reader.viewmodel.ReaderScreenViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.parameter.parametersOf


@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
data class ReaderScreenSpec(
        val bookId: Long,
        val chapterId: Long
) : VoyagerScreen() {

    override val key: ScreenKey = "Reader_Screen#$chapterId"
    @OptIn(
            ExperimentalAnimationApi::class,
            ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class
    )
    @Composable
    override fun Content() {
        val scope = rememberCoroutineScope()
        val vm: ReaderScreenViewModel =
                getIViewModel(parameters = { parametersOf(ReaderScreenViewModel.Param(chapterId,bookId))})
        val currentIndex = vm.currentChapterIndex
        val chapters = vm.stateChapters
        val chapter = vm.stateChapter

        val context = getContextWrapper()
        val scrollState = rememberScrollState()
        val lazyListState = rememberLazyListState()
        val navigator = LocalNavigator.currentOrThrow

        DisposableEffect(key1 = scrollState.hashCode()) {
            vm.readerScrollState = scrollState
            onDispose { }
        }

        val swipeState = rememberSwipeRefreshState(isRefreshing = false)
        DisposableEffect(key1 = true) {
            onDispose {
                vm.prefFunc.apply {
                    if (context != null) {
                        vm.restoreSetting(context, scrollState, lazyListState)
                    }
                }
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
                if (context != null) {
                    vm.readBrightness(context)
                }
            }
        }
        LaunchedEffect(key1 = vm.orientation.value) {
            vm.prefFunc.apply {
                if (context != null) {
                    vm.readOrientation(context)
                }
            }
        }

        LaunchedEffect(key1 = vm.screenAlwaysOn.value) {
            vm.prefFunc.apply {
                vm.screenAlwaysOnUseCase(vm.screenAlwaysOn.value)
            }
        }
        val bars = AppColors.current

        val customColor = remember {
            if (getBuildNumber() < 25 && bars.isBarLight) {
                CustomSystemColor(Color.LightGray, bars.bars)
            } else {
                CustomSystemColor(bars.bars, bars.bars)
            }

        }
        val hideSystemBar = remember { mutableStateOf(false) }
        val hideNavBar = remember { mutableStateOf(false) }

        LaunchedEffect(key1 = vm.initialized) {
            vm.prefFunc.apply {
                if (context != null) {
                    vm.prepareReaderSetting(
                        context = context,
                        scrollState,
                        onHideNav = {
                            hideNavBar.value = it
                        },
                        onHideStatus = {
                            hideSystemBar.value = it
                        }
                    )
                }
            }
        }

        LaunchedEffect(key1 = vm.immersiveMode.value) {
            vm.prefFunc.apply {

                if (context != null) {
                    vm.readImmersiveMode(
                        context = context,
                        onHideNav = {
                            hideNavBar.value = it
                        },
                        onHideStatus = {
                            hideSystemBar.value = it
                        }
                    )
                }
            }
        }
        val host = SnackBarListener(vm)

        val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
        val drawerState =
                androidx.compose.material3.rememberDrawerState(androidx.compose.material3.DrawerValue.Closed)
        IModalDrawer(
                state = drawerState,
                sheetContent = {
                    val drawerScrollState = rememberLazyListState()
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
                                            scrollState.scrollTo(0)
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
        ) {
            IModalSheets(
                    bottomSheetState = sheetState,
                sheetContent = { modifier ->
                    Column(
                        modifier
                    ) {
                        Divider(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = .2f),
                            thickness = 1.dp
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        ReaderSettingMainLayout(
                                    onFontSelected = { index ->
                                        vm.font?.value = FontType(
                                                vm.fonts.getOrNull(index) ?: getDefaultFont().name,
                                                FontFamily.Default
                                        )
                                    },
                                    onChangeBrightness = {
                                        vm.apply {
                                            vm.prefFunc.apply {
                                                if (context != null) {
                                                    saveBrightness(context, it)
                                                }
                                            }
                                        }
                                    },
                                    onBackgroundChange = { index ->
                                            vm.changeBackgroundColor(index)
                                    },
                                    vm = vm,
                                    onTextAlign = {
                                        vm.textAlignment.value = it
                                        vm.saveTextAlignment(it)
                                    },
                                    onToggleAutoBrightness = {
                                        vm.autoBrightnessMode.value = !vm.autoBrightnessMode.value
                                    }
                            )
                        }
                    }
            ) {
                CustomSystemColor(

                        enable = false,
                        statusBar = customColor.status,
                        navigationBar = customColor.navigation
                ) {
                    IScaffold(
                          //  modifier = scaffoldModifier.value,
                            topBar = {
                                val catalog = vm.catalog
                                val book = vm.book
                                val readerScrollState = vm.readerScrollState

                                if (readerScrollState != null) {
                                    ReaderScreenTopBar(
                                            // modifier = Modifier.padding(controller.scaffoldPadding),
                                            modifier = Modifier,
                                            isReaderModeEnable = vm.isReaderModeEnable,
                                            isLoaded = vm.isChapterLoaded.value,
                                            modalBottomSheetValue = sheetState.targetValue,
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
                                                        navigator.push(
                                                                WebViewScreenSpec(
                                                                        url = chapter?.key,
                                                                        sourceId = catalog.sourceId,
                                                                        chapterId = chapter?.id,
                                                                        bookId = book?.id,
                                                                        enableChapterFetch = true,
                                                                        enableChaptersFetch = false, enableBookFetch = false
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
                                                popBackStack(navigator)
                                            }
                                    )
                                }
                            }
                    ) {

                        ReadingScreen(
                                drawerState = drawerState,
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
                                                    UiText.MStringResource(
                                                            MR.strings.this_is_last_chapter
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
                                                        UiText.MStringResource(
                                                                MR.strings.this_is_first_chapter
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
                                        vm.prefFunc.apply {
                                            vm.toggleReaderMode(!vm.isReaderModeEnable)
                                        }
                                    }
                                },
                                readerScreenPreferencesState = vm,
                                onBackgroundColorAndTextColorApply = { bgColor, txtColor ->
                                    try {
                                        if (bgColor.isNotBlank()) {
                                                vm.setReaderBackgroundColor(vm.backgroundColor.value)
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

                                snackBarHostState = host,
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
                                            navigator.push(
                                                    TTSScreenSpec(
                                                            bookId = book.id,
                                                            sourceId = book.sourceId,
                                                            chapterId = chapter.id,
                                                            readingParagraph = 0
                                                    )
                                            )
                                        }
                                    }
                                },
                                onReaderBottomOnSetting = {
                                    scope.launch {
                                        sheetState.show()
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

                }
            }
        }
    }

    @Composable

    private fun LazyListState.getId(): Long? {
        return kotlin.runCatching {
            return@runCatching this.layoutInfo.visibleItemsInfo.firstOrNull()?.key.toString()
                    .substringAfter("-").toLong()
        }.getOrNull()
    }

}

