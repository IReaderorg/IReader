package ir.kazemcodes.infinity.feature_reader.presentation.reader

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.zhuinden.simplestackcomposeintegration.core.LocalBackstack
import com.zhuinden.simplestackcomposeintegration.services.rememberService
import ir.kazemcodes.infinity.core.presentation.components.ChapterListItemComposable
import ir.kazemcodes.infinity.core.presentation.components.ISnackBarHost
import ir.kazemcodes.infinity.core.presentation.components.handlePagingChapterResult
import ir.kazemcodes.infinity.core.presentation.reusable_composable.ErrorTextWithEmojis
import ir.kazemcodes.infinity.core.presentation.reusable_composable.TopAppBarActionButton
import ir.kazemcodes.infinity.core.presentation.reusable_composable.TopAppBarBackButton
import ir.kazemcodes.infinity.core.presentation.reusable_composable.TopAppBarTitle
import ir.kazemcodes.infinity.core.presentation.theme.Colour.scrollingThumbColor
import ir.kazemcodes.infinity.core.utils.UiEvent
import ir.kazemcodes.infinity.core.utils.scroll.Carousel
import ir.kazemcodes.infinity.core.utils.scroll.CarouselDefaults
import ir.kazemcodes.infinity.core.utils.scroll.rememberCarouselScrollState
import ir.kazemcodes.infinity.core.utils.scroll.verticalScroll
import ir.kazemcodes.infinity.feature_activity.presentation.WebViewKey
import ir.kazemcodes.infinity.feature_reader.presentation.reader.components.MainBottomSettingComposable
import ir.kazemcodes.infinity.feature_reader.presentation.reader.components.ReaderSettingComposable
import ir.kazemcodes.infinity.feature_sources.sources.models.FetchType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@ExperimentalAnimationApi
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ReadingScreen(
    modifier: Modifier = Modifier,
) {
    val viewModel = rememberService<ReaderScreenViewModel>()
    val chapters = viewModel.chapters.collectAsLazyPagingItems()
    val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))
    val modalBottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Expanded)
    val scope = rememberCoroutineScope()
    val backStack = LocalBackstack.current
    val state = viewModel.state.value
    val interactionSource = remember { MutableInteractionSource() }
    val context = LocalContext.current
    val scrollState = rememberCarouselScrollState()


    //viewModel.updateChapters(chapters.itemSnapshotList.items)


    val isWebViewEnable by remember {
        mutableStateOf(viewModel.webView.originalUrl == viewModel.state.value.chapter.link)
    }



    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    scaffoldState.snackbarHostState.showSnackbar(
                        event.uiText
                    )
                }
                else -> {}
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(topBar = {
            if (!state.isReaderModeEnable && state.isLoaded && modalBottomSheetState.targetValue == ModalBottomSheetValue.Expanded) {
                TopAppBar(
                    title = {

                        Text(
                            text = viewModel.state.value.chapter.title,
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
                        TopAppBarBackButton(backStack = backStack)
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
                        TopAppBarActionButton(imageVector = Icons.Default.Language,
                            title = "WebView",
                            onClick = {
                                backStack.goTo(WebViewKey(url = viewModel.state.value.chapter.link,
                                    sourceName = viewModel.state.value.source.name,
                                    fetchType = FetchType.Content.index))
                            })
                    }
                )
            } else if (!state.isLoaded) {
                TopAppBar(title = {},
                    elevation = 0.dp,
                    backgroundColor = Color.Transparent,
                    actions = {
                        TopAppBarActionButton(imageVector = Icons.Default.Language,
                            title = "WebView",
                            onClick = {
                                backStack.goTo(WebViewKey(
                                    url = viewModel.state.value.chapter.link,
                                    sourceName = viewModel.state.value.source.name,
                                    fetchType = FetchType.Content.index,
                                    bookId = state.chapter.bookId,
                                    chapterId = state.chapter.chapterId
                                ))
                            })
                        if (isWebViewEnable) {
                            TopAppBarActionButton(imageVector = Icons.Default.TrackChanges,
                                title = "Content Fetcher",
                                onClick = { viewModel.getFromWebView() })
                        }
                    },
                    navigationIcon = {
                        TopAppBarBackButton(backStack = backStack)
                    })
            }
        },
            scaffoldState = scaffoldState,
            snackbarHost = { ISnackBarHost(snackBarHostState = it) },
            bottomBar = {
                if (!state.isReaderModeEnable && state.isLoaded) {
                    ModalBottomSheetLayout(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (viewModel.state.value.isMainBottomModeEnable) 130.dp else 320.dp),
                        sheetBackgroundColor = MaterialTheme.colors.background,
                        sheetElevation = 8.dp,
                        sheetState = modalBottomSheetState,
                        sheetContent = {
                            Column(modifier.fillMaxSize()) {
                                Divider(modifier = modifier.fillMaxWidth(),
                                    color = MaterialTheme.colors.onBackground.copy(alpha = .2f),
                                    thickness = 1.dp)
                                Spacer(modifier = modifier.height(15.dp))
                                if (viewModel.state.value.isMainBottomModeEnable) {
                                    MainBottomSettingComposable(viewModel = viewModel,
                                        scope = scope,
                                        scaffoldState = scaffoldState)
                                }
                                if (viewModel.state.value.isSettingModeEnable) {
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
                        Row() {
                            TopAppBarActionButton(imageVector = Icons.Default.SettingsBackupRestore,
                                title = "Reverse Chapter List",
                                onClick = { viewModel.reverseSlider() })

                            TopAppBarActionButton(imageVector = Icons.Default.Sort,
                                title = "Reverse list icon",
                                onClick = {
                                    viewModel.reverseChapters()
                                    viewModel.getLocalChaptersByPaging()
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
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(items = chapters) { chapter ->
                                    if (chapter != null) {
                                        ChapterListItemComposable(modifier = modifier,
                                            chapter = chapter, goTo = {
                                                viewModel.getChapter(chapter)
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
            Box(
                Modifier
                    .fillMaxSize()
                    .clickable(interactionSource = interactionSource,
                        indication = null) {
                        viewModel.onEvent(ReaderEvent.ToggleReaderMode(!state.isReaderModeEnable))
                        if (state.isReaderModeEnable) {
                            scope.launch(Dispatchers.Main) {
                                viewModel.getLocalChaptersByPaging()
                                modalBottomSheetState.animateTo(ModalBottomSheetValue.Expanded)
                            }
                        } else {
                            scope.launch(Dispatchers.Main) {
                                modalBottomSheetState.animateTo(ModalBottomSheetValue.Hidden)
                            }
                        }

                    }
                    .background(viewModel.state.value.backgroundColor)
                    .padding(viewModel.state.value.paragraphsIndent.dp)
                    .wrapContentSize(Alignment.CenterStart)
            ) {
                if (state.chapter.isChapterNotEmpty() && !state.isLoading) {
                    Row(modifier = modifier.fillMaxSize()) {
                        Text(
                            modifier = modifier
                                .verticalScroll(scrollState)
                                .weight(1f),
                            text = state.chapter.content.joinToString("\n".repeat(state.distanceBetweenParagraphs)),
                            fontSize = viewModel.state.value.fontSize.sp,
                            fontFamily = viewModel.state.value.font.fontFamily,
                            textAlign = TextAlign.Start,
                            color = state.textColor,
                            lineHeight = state.lineHeight.sp
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
                                backgroundColor = viewModel.state.value.backgroundColor,
                                scrollingBackgroundColor = viewModel.state.value.backgroundColor
                            )

                        )
                    }

                }
            }
        }
        if (state.error.isNotBlank()) {
            ErrorTextWithEmojis(
                error = state.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .wrapContentSize(Alignment.Center)
                    .align(Alignment.Center),
            )
        }

        if (viewModel.state.value.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colors.primary
            )
        }

    }
}


