package org.ireader.presentation.feature_reader.presentation.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.ireader.core.utils.UiEvent
import org.ireader.core.utils.UiText
import org.ireader.domain.FetchType
import org.ireader.domain.view_models.reader.ReaderScreenViewModel
import org.ireader.presentation.feature_reader.presentation.reader.components.MainBottomSettingComposable
import org.ireader.presentation.feature_reader.presentation.reader.components.ReaderSettingComposable
import org.ireader.presentation.presentation.components.ISnackBarHost
import org.ireader.presentation.presentation.reusable_composable.ErrorTextWithEmojis
import org.ireader.presentation.ui.WebViewScreenSpec
import org.ireader.source.core.Source


@ExperimentalAnimationApi
@OptIn(ExperimentalMaterialApi::class, com.google.accompanist.pager.ExperimentalPagerApi::class,
    dev.chrisbanes.snapper.ExperimentalSnapperApi::class)
@Composable
fun ReadingScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    viewModel: ReaderScreenViewModel,
    source: Source,
    scrollState: LazyListState,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onSliderFinished: () -> Unit,
    onSliderChange: (index: Float) -> Unit,
    swipeState: SwipeRefreshState,
) {

    val chapters = viewModel.chapters.collectAsLazyPagingItems()
    val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))
    val modalBottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Expanded)
    val scope = rememberCoroutineScope()

    val state = viewModel.state
    val chapter = viewModel.state.chapter
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()


    DisposableEffect(key1 = true) {
        onDispose {
            viewModel.restoreSetting(context)
        }
    }

    LaunchedEffect(key1 = true) {
        viewModel.readOrientation(context)
        viewModel.readBrightness(context)
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    scaffoldState.snackbarHostState.showSnackbar(
                        event.uiText.asString(context)
                    )
                }
            }
        }
    }

        Scaffold(topBar = {
            if (chapter != null) {
                ReaderScreenTopBar(
                    isReaderModeEnable = state.isReaderModeEnable,
                    isLoaded = state.isLocalLoaded,
                    modalBottomSheetValue = modalBottomSheetState.targetValue,
                    onRefresh = {
                        viewModel.getReadingContentRemotely(chapter = chapter,
                            source = source)
                    },
                    source = source,
                    chapter = chapter,
                    navController = navController,
                    onWebView = {
                        try {
                            if (!state.isReaderModeEnable && state.isLocalLoaded && modalBottomSheetState.targetValue == ModalBottomSheetValue.Expanded) {
                                navController.navigate(WebViewScreenSpec.buildRoute(
                                    url = chapter.link,
                                    sourceId = source.id,
                                    fetchType = FetchType.ContentFetchType.index,
                                )
                                )
                            } else if (!state.isLocalLoaded) {
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
                                viewModel.showSnackBar(UiText.ExceptionString(e))
                            }
                        }
                    }
                )

            }
        },
            scaffoldState = scaffoldState,
            snackbarHost = { ISnackBarHost(snackBarHostState = it) },
            bottomBar = {
                if (!state.isReaderModeEnable && state.isLocalLoaded && chapter != null) {
                    AnimatedVisibility(
                        visible = !state.isReaderModeEnable && state.isLocalLoaded,
                        enter = slideInVertically(initialOffsetY = { it }),
                        exit = slideOutVertically(targetOffsetY = { it })
                    ) {
                        ModalBottomSheetLayout(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Max)
                                .height(if (viewModel.state.isMainBottomModeEnable) 130.dp else 320.dp),
                            sheetBackgroundColor = MaterialTheme.colors.background,
                            sheetElevation = 8.dp,
                            sheetState = modalBottomSheetState,
                            sheetContent = {
                                Column(modifier.fillMaxSize()) {
                                    Divider(modifier = modifier.fillMaxWidth(),
                                        color = MaterialTheme.colors.onBackground.copy(alpha = .2f),
                                        thickness = 1.dp)
                                    Spacer(modifier = modifier.height(15.dp))
                                    if (viewModel.state.isMainBottomModeEnable) {
                                        MainBottomSettingComposable(
                                            scope = scope,
                                            scaffoldState = scaffoldState,
                                            scrollState = scrollState,
                                            chapter = chapter,
                                            chapters = viewModel.state.chapters,
                                            currentChapterIndex = viewModel.state.currentChapterIndex,
                                            onSetting = {
                                                viewModel.toggleSettingMode(true)
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
                                    if (viewModel.state.isSettingModeEnable) {
                                        ReaderSettingComposable(viewModel = viewModel)
                                    }
                                }
                            },
                            content = {}
                        )
                    }
                }
            },
            drawerGesturesEnabled = true,
            drawerBackgroundColor = MaterialTheme.colors.background,
            drawerContent = {
                if (chapter != null) {
                    ReaderScreenDrawer(
                        onReverseIcon = {
                            viewModel.reverseChapters()
                            viewModel.getLocalChaptersByPaging(chapter.bookId)
                        },
                        onChapter = { ch ->
                            viewModel.getChapter(ch.id,
                                source = source)
                            coroutineScope.launch {
                                scrollState.animateScrollToItem(0, 0)
                            }
                            viewModel.updateChapterSliderIndex(viewModel.getCurrentIndexOfChapter(
                                ch))
                        },
                        chapter = chapter,
                        source = source,
                        chapters = chapters,
                    )
                }
            }
        ) {
            if (chapter != null) {
                Box(modifier = modifier.fillMaxSize()) {
                    if (chapter.isChapterNotEmpty() && !state.isLoading) {
                        ReaderText(
                            viewModel = viewModel,
                            chapter = chapter,
                            onNext = { onNext() },
                            swipeState = swipeState,
                            onPrev = { onPrev() }
                        )
                    }


                    if (state.error.asString(context).isNotBlank()) {
                        ErrorTextWithEmojis(
                            error = state.error.asString(context),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                                .wrapContentSize(Alignment.Center)
                                .align(Alignment.Center),
                        )
                    }

                    if (viewModel.state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colors.primary
                        )
                    }
                }
            }
        }

}



