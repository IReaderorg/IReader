package org.ireader.presentation.feature_reader.presentation.reader

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.ireader.core.utils.UiEvent
import org.ireader.core.utils.UiText
import org.ireader.core_ui.ui.Colour.scrollingThumbColor
import org.ireader.domain.models.source.FetchType
import org.ireader.domain.view_models.reader.ReaderEvent
import org.ireader.domain.view_models.reader.ReaderScreenViewModel
import org.ireader.presentation.feature_reader.presentation.reader.components.MainBottomSettingComposable
import org.ireader.presentation.feature_reader.presentation.reader.components.ReaderSettingComposable
import org.ireader.presentation.presentation.components.ChapterListItemComposable
import org.ireader.presentation.presentation.components.ISnackBarHost
import org.ireader.presentation.presentation.components.handlePagingChapterResult
import org.ireader.presentation.presentation.reusable_composable.ErrorTextWithEmojis
import org.ireader.presentation.presentation.reusable_composable.TopAppBarActionButton
import org.ireader.presentation.presentation.reusable_composable.TopAppBarBackButton
import org.ireader.presentation.presentation.reusable_composable.TopAppBarTitle
import org.ireader.presentation.ui.WebViewScreenSpec
import org.ireader.presentation.utils.scroll.Carousel
import org.ireader.presentation.utils.scroll.CarouselDefaults
import org.ireader.presentation.utils.scroll.rememberCarouselScrollState
import org.ireader.presentation.utils.scroll.verticalScroll


@ExperimentalAnimationApi
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ReadingScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    viewModel: ReaderScreenViewModel = hiltViewModel(),
) {

    val chapters = viewModel.chapters.collectAsLazyPagingItems()
    val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))
    val modalBottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Expanded)
    val scope = rememberCoroutineScope()

    val state = viewModel.state
    val chapter = viewModel.state.chapter
    val prefState = viewModel.prefState
    val interactionSource = remember { MutableInteractionSource() }
    val context = LocalContext.current
    val scrollState = rememberCarouselScrollState()
    val drawerScrollState = rememberLazyListState()
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

    val isWebViewEnable by remember {
        mutableStateOf(viewModel.webView.originalUrl == chapter?.link)
    }
    Scaffold(topBar = {
        if (!state.isReaderModeEnable && state.isLocalLoaded && modalBottomSheetState.targetValue == ModalBottomSheetValue.Expanded) {
            TopAppBar(
                title = {
                    Text(
                        text = chapter?.title ?: "",
                        color = MaterialTheme.colors.onBackground,
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Bold,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colors.background,
                contentColor = MaterialTheme.colors.onBackground,
                elevation = 8.dp,
                navigationIcon = {
                    TopAppBarBackButton(navController = navController)
                },
                actions = {
                    TopAppBarActionButton(imageVector = Icons.Default.Autorenew,
                        title = "Refresh",
                        onClick = { viewModel.getReadingContentRemotely() })
                    if (isWebViewEnable) {
                        TopAppBarActionButton(imageVector = Icons.Default.TrackChanges,
                            title = "Content Fetcher",
                            onClick = { viewModel.getFromWebView() })
                    }
                    TopAppBarActionButton(imageVector = Icons.Default.Public,
                        title = "WebView",
                        onClick = {
                            try {
                                navController.navigate(WebViewScreenSpec.buildRoute(
                                    url = chapter?.link ?: "",
                                    sourceId = viewModel.state.source.sourceId,
                                    fetchType = FetchType.ContentFetchType.index,
                                )
                                )
                            } catch (e: Exception) {
                                scope.launch {
                                    viewModel.showSnackBar(UiText.ExceptionString(e))
                                }
                            }

                        })
                }
            )
        } else if (!state.isLocalLoaded) {
            TopAppBar(title = {},
                elevation = 0.dp,
                backgroundColor = Color.Transparent,
                actions = {
                    TopAppBarActionButton(imageVector = Icons.Default.Public,
                        title = "WebView",
                        onClick = {
                            if (chapter != null) {
                                try {
                                    navController.navigate(WebViewScreenSpec.buildRoute(
                                        url = chapter.link,
                                        sourceId = viewModel.state.source.sourceId,
                                        fetchType = FetchType.ContentFetchType.index,
                                        bookId = chapter.bookId,
                                        chapterId = chapter.id
                                    ))

                                } catch (e: Exception) {
                                    scope.launch {
                                        viewModel.showSnackBar(UiText.ExceptionString(e))
                                    }
                                }
                            }
                        })
                    if (isWebViewEnable) {
                        TopAppBarActionButton(imageVector = Icons.Default.TrackChanges,
                            title = "Content Fetcher",
                            onClick = { viewModel.getFromWebView() })
                    }
                },
                navigationIcon = {
                    TopAppBarBackButton(navController = navController)
                })
        }
    },
        scaffoldState = scaffoldState,
        snackbarHost = { ISnackBarHost(snackBarHostState = it) },
        bottomBar = {
            if (!state.isReaderModeEnable && state.isLocalLoaded && chapter != null) {
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
                                    viewModel = viewModel,
                                    scope = scope,
                                    scaffoldState = scaffoldState, scrollState = scrollState,
                                    chapter = chapter
                                )
                            }
                            if (viewModel.state.isSettingModeEnable) {
                                ReaderSettingComposable(viewModel = viewModel)
                            }

                        }
                    }
                ) {

                }
            }
        },
        drawerGesturesEnabled = true,
        drawerBackgroundColor = MaterialTheme.colors.background,
        drawerContent = {
            Column(modifier = modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top) {
                Spacer(modifier = modifier.height(5.dp))
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = modifier.fillMaxWidth()) {
                    TopAppBarTitle(title = "Content", modifier = modifier.padding(start = 8.dp))
                    Row {
                        TopAppBarActionButton(imageVector = Icons.Default.Sort,
                            title = "Reverse list icon",
                            onClick = {
                                if (chapter != null) {
                                    viewModel.reverseChapters()
                                    viewModel.getLocalChaptersByPaging(chapter.bookId)
                                }
                            })
                    }
                }

                Spacer(modifier = modifier.height(5.dp))
                Divider(modifier = modifier.fillMaxWidth(), thickness = 1.dp)

                val result = handlePagingChapterResult(books = chapters, onEmptyResult = {
                    Box(modifier = modifier.fillMaxSize()) {
                        ErrorTextWithEmojis(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                                .align(Alignment.Center),
                            error = "There is no book is Library, you can add books in the Explore screen"
                        )
                    }

                })
                if (result) {
                    AnimatedContent(chapters.loadState.refresh is LoadState.NotLoading) {
                        LazyColumn(modifier = Modifier.fillMaxSize(),
                            state = drawerScrollState) {
                            items(items = chapters) { chapter ->
                                if (chapter != null) {
                                    ChapterListItemComposable(modifier = modifier,
                                        chapter = chapter, goTo = {
                                            viewModel.getChapter(chapter.id)
                                            coroutineScope.launch {

                                                scrollState.scrollTo(0)
                                            }
                                            viewModel.updateChapterSliderIndex(viewModel.getCurrentIndexOfChapter(
                                                chapter))
                                        })
                                }
                            }
                        }
                    }
                }


            }
        }
    ) {
        if (chapter != null) {
            Box(
                Modifier
                    .fillMaxSize()
                    .clickable(interactionSource = interactionSource,
                        indication = null) {
                        viewModel.onEvent(ReaderEvent.ToggleReaderMode(!state.isReaderModeEnable))
                        if (state.isReaderModeEnable) {
                            scope.launch {
                                viewModel.getLocalChaptersByPaging(chapter.bookId)
                                modalBottomSheetState.animateTo(ModalBottomSheetValue.Expanded)
                            }
                        } else {
                            scope.launch {
                                modalBottomSheetState.animateTo(ModalBottomSheetValue.Hidden)
                            }
                        }

                    }
                    .background(viewModel.prefState.backgroundColor)
                    .padding(viewModel.prefState.paragraphsIndent.dp)
                    .wrapContentSize(Alignment.CenterStart)
            ) {
                Box(modifier = modifier.fillMaxSize()) {

                    if (chapter.isChapterNotEmpty() && !state.isLocalLoading) {
                        Row(modifier = modifier.fillMaxSize()) {

                            Text(
                                modifier = modifier
                                    .verticalScroll(scrollState)
                                    .weight(1f),
                                text = chapter.content.map { it.trimStart() }
                                    .joinToString("\n".repeat(prefState.distanceBetweenParagraphs)),
                                fontSize = viewModel.prefState.fontSize.sp,
                                fontFamily = viewModel.prefState.font.fontFamily,
                                textAlign = TextAlign.Start,
                                color = prefState.textColor,
                                lineHeight = prefState.lineHeight.sp,
                            )


                            Carousel(
                                state = scrollState,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(.02f)
                                    .padding(start = 6.dp),
                                colors = CarouselDefaults.colors(
                                    thumbColor = MaterialTheme.colors.scrollingThumbColor,
                                    scrollingThumbColor = MaterialTheme.colors.scrollingThumbColor,
                                    backgroundColor = viewModel.prefState.backgroundColor,
                                    scrollingBackgroundColor = viewModel.prefState.backgroundColor
                                )

                            )
                        }

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

                    if (viewModel.state.isLocalLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colors.primary
                        )
                    }
                }
            }
        }
    }
}



