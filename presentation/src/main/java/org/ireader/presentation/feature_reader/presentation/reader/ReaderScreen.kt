package org.ireader.presentation.feature_reader.presentation.reader

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.ireader.core.utils.UiEvent
import org.ireader.core.utils.UiText
import org.ireader.domain.FetchType
import org.ireader.domain.models.entities.Chapter
import org.ireader.presentation.feature_reader.presentation.ScrollIndicatorSetting
import org.ireader.presentation.feature_reader.presentation.reader.components.MainBottomSettingComposable
import org.ireader.presentation.feature_reader.presentation.reader.components.ReaderSettingComposable
import org.ireader.presentation.feature_reader.presentation.reader.reverse_swip_refresh.SwipeRefreshState
import org.ireader.presentation.feature_reader.presentation.reader.viewmodel.ReaderScreenViewModel
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
    drawerScrollState: LazyListState,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onChapter: (Chapter) -> Unit,
    onSliderFinished: () -> Unit,
    onSliderChange: (index: Float) -> Unit,
    swipeState: SwipeRefreshState,
) {

    val chapters = vm.stateChapters
    val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))
    val modalState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()

    val chapter = vm.stateChapter
    val context = LocalContext.current


    DisposableEffect(key1 = true) {
        onDispose {
            vm.speaker?.shutdown()
            vm.restoreSetting(context, scrollState)
        }
    }
    LaunchedEffect(key1 = scaffoldState.drawerState.targetValue) {
        if (chapter != null && scaffoldState.drawerState.targetValue == DrawerValue.Open && vm.stateChapters.isNotEmpty()) {
            drawerScrollState.scrollToItem(vm.getCurrentIndexOfChapter(chapter))
        }
    }
    LaunchedEffect(key1 = vm.currentChapterIndex) {
        val index = vm.currentChapterIndex
        if (index != -1) {
            drawerScrollState.scrollToItem(vm.currentChapterIndex, -500)
        }
    }
    LaunchedEffect(key1 = vm.autoScrollMode) {
        while (vm.autoScrollInterval != 0L && vm.autoScrollMode) {
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
                    modalState.snapTo(ModalBottomSheetValue.Expanded)
                }
            }
            true -> {
                scope.launch {
                    modalState.snapTo(ModalBottomSheetValue.Hidden)
                }
            }
        }
    }
    LaunchedEffect(key1 = vm.autoBrightnessMode) {
        vm.readBrightness(context)
    }
    LaunchedEffect(key1 = vm.initialized) {
        if (chapter != null) {
            scrollState.scrollBy(chapter.progress.toFloat())
        }
    }
//    LaunchedEffect(key1 = vm.stateChapter) {
//        if (chapter != null) {
//            scrollState.scrollBy(chapter.progress.toFloat())
//        }
//    }
    LaunchedEffect(key1 = true) {
        vm.readOrientation(context)
        vm.readBrightness(context)
        vm.readImmersiveMode(context)


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
                            Divider(modifier = modifier.fillMaxWidth(),
                                color = MaterialTheme.colors.onBackground.copy(alpha = .2f),
                                thickness = 1.dp)
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
                                    onSliderFinished = { onSliderFinished() },
                                    onPlay = {
                                        vm.voiceMode = true
//                                        vm.isPlaying = !vm.isPlaying
//                                        vm.readText(context)
                                    }
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

            ReaderScreenDrawer(
                modifier = Modifier.statusBarsPadding(),
                onReverseIcon = {
                    if (chapter != null) {
                        vm.reverseChapters()
                        scope.launch {
                            vm.getLocalChaptersByPaging(chapter.bookId)
                        }
                    }
                },
                onChapter = onChapter,
                chapter = chapter,
                source = source,
                chapters = chapters,
                drawerScrollState = drawerScrollState
            )

        }
    ) {
        ScrollIndicatorSetting(enable = vm.scrollIndicatorDialogShown, vm)
        if (chapter != null) {
            Box(modifier = modifier.fillMaxSize()) {
                if (chapter.isChapterNotEmpty() && !vm.isLoading) {
                    ReaderText(
                        vm = vm,
                        chapter = chapter,
                        onNext = onNext,
                        swipeState = swipeState,
                        onPrev = onPrev,
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

