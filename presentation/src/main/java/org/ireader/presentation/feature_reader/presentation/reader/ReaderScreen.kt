package org.ireader.presentation.feature_reader.presentation.reader

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.ireader.core.utils.UiEvent
import org.ireader.core.utils.UiText
import org.ireader.domain.FetchType
import org.ireader.domain.view_models.reader.ReaderScreenViewModel
import org.ireader.presentation.feature_reader.presentation.ScrollIndicatorSetting
import org.ireader.presentation.feature_reader.presentation.reader.components.MainBottomSettingComposable
import org.ireader.presentation.feature_reader.presentation.reader.components.ReaderSettingComposable
import org.ireader.presentation.feature_reader.presentation.reader.reverse_swip_refresh.SwipeRefreshState
import org.ireader.presentation.presentation.components.ISnackBarHost
import org.ireader.presentation.presentation.reusable_composable.ErrorTextWithEmojis
import org.ireader.presentation.ui.WebViewScreenSpec
import tachiyomi.source.Source


@ExperimentalAnimationApi
@OptIn(ExperimentalMaterialApi::class, com.google.accompanist.pager.ExperimentalPagerApi::class,
    dev.chrisbanes.snapper.ExperimentalSnapperApi::class)
@Composable
fun ReadingScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    vm: ReaderScreenViewModel,
    source: Source,
    scrollState: LazyListState,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onSliderFinished: () -> Unit,
    onSliderChange: (index: Float) -> Unit,
    swipeState: SwipeRefreshState,
) {

    val chapters = vm.chapters.collectAsLazyPagingItems()
    val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))
    val modalState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()

    val chapter = vm.stateChapter
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val drawerScrollState = rememberLazyListState()

    DisposableEffect(key1 = true) {
        onDispose {
            vm.restoreSetting(context)
        }
    }
    LaunchedEffect(key1 = scaffoldState.drawerState.targetValue) {
        if (scaffoldState.drawerState.targetValue == DrawerValue.Open && vm.stateChapters.isNotEmpty()) {
            drawerScrollState.scrollToItem(vm.currentChapterIndex)
        }
    }
    LaunchedEffect(key1 = vm.autpScrollMode) {
        while (vm.autoScrollInterval != 0L && vm.autpScrollMode) {
            scrollState.scrollBy(vm.autoScrollOffset.toFloat())
            delay(vm.autoScrollInterval)
        }
    }
    LaunchedEffect(key1 = modalState.currentValue) {
        when (modalState.currentValue) {
            ModalBottomSheetValue.Expanded -> vm.isReaderModeEnable = false
            ModalBottomSheetValue.Hidden -> vm.isReaderModeEnable = true
            else -> {}
        }
    }
    LaunchedEffect(key1 = vm.isReaderModeEnable) {
        when (vm.isReaderModeEnable) {
            false -> {
                scope.launch {
                    if (chapter != null) {
                        vm.getLocalChaptersByPaging(chapter.bookId)
                    }
                    modalState.animateTo(ModalBottomSheetValue.Expanded)
                }
            }
            true -> {
                scope.launch {
                    modalState.animateTo(ModalBottomSheetValue.Hidden)
                }
            }
        }
    }

    LaunchedEffect(key1 = true) {
        vm.readOrientation(context)
        vm.readBrightness(context)
        vm.hideSystemBars(context)

        vm.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    scaffoldState.snackbarHostState.showSnackbar(
                        event.uiText.asString(context)
                    )
                }
            }
        }
    }
    LaunchedEffect(key1 = chapter) {
        if (chapter != null) {
            vm.updateChapterSliderIndex(vm.getCurrentIndexOfChapter(chapter))
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            ReaderScreenTopBar(
                isReaderModeEnable = vm.isReaderModeEnable,
                isLoaded = vm.isLocalLoaded,
                modalBottomSheetValue = modalState.targetValue,
                onRefresh = {
                    if (chapter != null) {
                        vm.getReadingContentRemotely(chapter = chapter,
                            source = source)
                    }
                },
                source = source,
                chapter = chapter,
                navController = navController,
                onWebView = {
                    try {
                        if (chapter != null && !vm.isReaderModeEnable && vm.isLocalLoaded && modalState.targetValue == ModalBottomSheetValue.Expanded) {
                            navController.navigate(WebViewScreenSpec.buildRoute(
                                url = chapter.link,
                                sourceId = source.id,
                                fetchType = FetchType.ContentFetchType.index,
                            )
                            )
                        } else if (chapter != null && !vm.isLocalLoaded) {
                            navController.navigate(WebViewScreenSpec.buildRoute(
                                url = chapter.link,
                                sourceId = source.id,
                                fetchType = FetchType.ContentFetchType.index,
                                bookId = chapter.bookId,
                                chapterId = chapter.id
                            ))
                        }
                    } catch (e: Exception) {
                        scope.launch {
                            vm.showSnackBar(UiText.ExceptionString(e))
                        }
                    }
                }
            )
        },
        scaffoldState = scaffoldState,
        snackbarHost = { ISnackBarHost(snackBarHostState = it) },
        bottomBar = {
            if (!vm.isReaderModeEnable && chapter != null) {
                ModalBottomSheetLayout(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max)
                        .height(if (vm.isMainBottomModeEnable) 130.dp else 320.dp),
                    sheetBackgroundColor = MaterialTheme.colors.background,
                    sheetElevation = 8.dp,
                    sheetState = modalState,
                    sheetContent = {
                        Column(modifier.fillMaxSize()) {
                            Spacer(modifier = modifier.height(4.dp))
                            Box(modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .width(50.dp)
                                .height(5.dp)
                                .background(MaterialTheme.colors.onBackground.copy(.6f))
                            ) {

                            }
                            Spacer(modifier = modifier.height(5.dp))
                            if (vm.isMainBottomModeEnable) {
                                MainBottomSettingComposable(
                                    scope = scope,
                                    scaffoldState = scaffoldState,
                                    scrollState = scrollState,
                                    chapter = chapter,
                                    chapters = vm.stateChapters,
                                    currentChapterIndex = vm.currentChapterIndex,
                                    onSetting = {
                                        vm.toggleSettingMode(true)
                                    },
                                    source = source,
                                    onNext = {
                                        onNext()
                                    },
                                    onPrev = {
                                        onPrev()
                                    },
                                    onSliderChange = { onSliderChange(it) },
                                    onSliderFinished = { onSliderFinished() }
                                )
                            }
                            if (vm.isSettingModeEnable) {
                                ReaderSettingComposable(viewModel = vm)
                            }
                        }
                    },
                    content = {}
                )

            }
        },
        drawerGesturesEnabled = true,
        drawerBackgroundColor = MaterialTheme.colors.background,
        drawerContent = {
            if (chapter != null) {
                ReaderScreenDrawer(
                    modifier = Modifier.statusBarsPadding(),
                    onReverseIcon = {
                        vm.reverseChapters()
                        vm.getLocalChaptersByPaging(chapter.bookId)
                    },
                    onChapter = { ch ->
                        vm.getChapter(ch.id,
                            source = source)
                        coroutineScope.launch {
                            scrollState.animateScrollToItem(0, 0)
                        }
                        vm.updateChapterSliderIndex(vm.getCurrentIndexOfChapter(
                            ch))
                    },
                    chapter = chapter,
                    source = source,
                    chapters = chapters,
                    drawerScrollState = drawerScrollState
                )
            }
        }
    ) {
        ScrollIndicatorSetting(enable = vm.scrollIndicatorDialogShown, vm)
        if (chapter != null) {
            Box(modifier = modifier.fillMaxSize()) {
                if (chapter.isChapterNotEmpty() && !vm.isLoading) {
                    ReaderText(
                        vm = vm,
                        chapter = chapter,
                        onNext = { onNext() },
                        swipeState = swipeState,
                        onPrev = { onPrev() },
                        scrollState = scrollState,
                        modalState = modalState
                    )
                }


                if (vm.error.asString(context).isNotBlank()) {
                    ErrorTextWithEmojis(
                        error = vm.error.asString(context),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .wrapContentSize(Alignment.Center)
                            .align(Alignment.Center),
                    )
                }

                if (vm.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colors.primary
                    )
                }
            }
        }
    }

}

