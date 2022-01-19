package ir.kazemcodes.infinity.feature_reader.presentation.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PublishedWithChanges
import androidx.compose.material.icons.filled.Sort
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhuinden.simplestackcomposeintegration.core.LocalBackstack
import com.zhuinden.simplestackcomposeintegration.services.rememberService
import ir.kazemcodes.infinity.core.presentation.reusable_composable.ErrorTextWithEmojis
import ir.kazemcodes.infinity.core.presentation.reusable_composable.TopAppBarActionButton
import ir.kazemcodes.infinity.core.presentation.reusable_composable.TopAppBarBackButton
import ir.kazemcodes.infinity.core.presentation.reusable_composable.TopAppBarTitle
import ir.kazemcodes.infinity.feature_activity.presentation.WebViewKey
import ir.kazemcodes.infinity.feature_reader.presentation.reader.components.MainBottomSettingComposable
import ir.kazemcodes.infinity.feature_reader.presentation.reader.components.ReaderSettingComposable
import ir.kazemcodes.infinity.feature_sources.sources.models.FetchType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ReadingScreen(
    modifier: Modifier = Modifier,
) {
    val viewModel = rememberService<ReaderScreenViewModel>()
    val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))
    val modalBottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Expanded)

    val scope = rememberCoroutineScope()
    val book = viewModel.state.value.book
    val backStack = LocalBackstack.current
    val state = viewModel.state.value
    val interactionSource = remember { MutableInteractionSource() }


    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(topBar = {
            if (!state.isReaderModeEnable && state.isLoaded && modalBottomSheetState.targetValue ==  ModalBottomSheetValue.Expanded) {
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
                        TopAppBarActionButton(imageVector = Icons.Default.Language,
                            title = "WebView",
                            onClick = { backStack.goTo(WebViewKey(url = viewModel.state.value.chapter.link, sourceName = viewModel.state.value.source.name, fetchType = FetchType.Content.index)) })
                    }
                )
            } else if (!state.isLoaded) {
                TopAppBar(title = {},
                    elevation = 0.dp,
                    backgroundColor = Color.Transparent,
                    actions = {
                        TopAppBarActionButton(imageVector = Icons.Default.Language,
                            title = "WebView",
                            onClick = { backStack.goTo(WebViewKey(url = viewModel.state.value.chapter.link, sourceName = viewModel.state.value.source.name, fetchType = FetchType.Content.index))})
                    },
                    navigationIcon = {
                        TopAppBarBackButton(backStack = backStack)
                    })
            }
        },
            scaffoldState = scaffoldState,
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
                        TopAppBarActionButton(imageVector = Icons.Default.Sort,
                            title = "Reverse list icon",
                            onClick = {
                                viewModel.reverseChapters()
                            })
                    }

                    Spacer(modifier = modifier.height(5.dp))
                    Divider(modifier = modifier.fillMaxWidth(), thickness = 1.dp)
                    if (state.chapters.isNotEmpty()) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(count = state.drawerChapters.size) { index ->
                                Row(
                                    modifier = modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                        .height(40.dp)
                                        .clickable {
                                            viewModel.getContent(viewModel.state.value.chapters[viewModel.getIndexOfChapter(
                                                index)])
                                            viewModel.updateChapterSliderIndex(index = index)

                                        },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = state.drawerChapters[index].title,
                                        color = if (state.drawerChapters[index].haveBeenRead) MaterialTheme.colors.onBackground.copy(
                                            alpha = .4f) else MaterialTheme.colors.onBackground,
                                        style = MaterialTheme.typography.subtitle1,
                                        fontWeight = FontWeight.SemiBold,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(7f)
                                    )
                                    Text(modifier = Modifier.weight(2f),
                                        text = state.drawerChapters[index].dateUploaded ?: "",
                                        fontStyle = FontStyle.Italic,
                                        color = if (state.drawerChapters[index].haveBeenRead) MaterialTheme.colors.onBackground.copy(
                                            alpha = .4f) else MaterialTheme.colors.onBackground,
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.caption
                                    )
                                    Spacer(modifier = modifier.width(20.dp))
                                    Icon(
                                        imageVector = Icons.Default.PublishedWithChanges,
                                        contentDescription = "Cached",
                                        tint = if (state.drawerChapters[index].content.joinToString(
                                                " , ").length > 10
                                        ) MaterialTheme.colors.onBackground else MaterialTheme.colors.background,
                                    )
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
                    .verticalScroll(rememberScrollState())
                    .clickable(interactionSource = interactionSource,
                        indication = null) {
                        viewModel.onEvent(ReaderEvent.ToggleReaderMode(!state.isReaderModeEnable))
                        if (state.isReaderModeEnable) {
                            scope.launch(Dispatchers.Main) {
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
                if (state.chapter.isChapterNotEmpty()) {
                    Text(
                        modifier = modifier
                            .fillMaxWidth()
                            .align(Alignment.TopStart),
                        text = state.chapter.content.joinToString("\n".repeat(state.distanceBetweenParagraphs)),
                        fontSize = viewModel.state.value.fontSize.sp,
                        fontFamily = viewModel.state.value.font.fontFamily,
                        textAlign = TextAlign.Start,
                        color = state.textColor,
                        lineHeight = state.lineHeight.sp
                    )
                }
            }
        }
        if (state.error.isNotBlank()) {
            ErrorTextWithEmojis(error = state.error, modifier = Modifier
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


