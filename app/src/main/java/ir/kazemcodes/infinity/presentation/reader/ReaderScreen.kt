package ir.kazemcodes.infinity.presentation.reader

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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhuinden.simplestackcomposeintegration.core.LocalBackstack
import com.zhuinden.simplestackcomposeintegration.services.rememberService
import ir.kazemcodes.infinity.base_feature.navigation.WebViewKey
import ir.kazemcodes.infinity.presentation.reader.components.MainBottomSettingComposable
import ir.kazemcodes.infinity.presentation.reader.components.ReaderSettingComposable
import ir.kazemcodes.infinity.presentation.reusable_composable.ErrorTextWithEmojis
import ir.kazemcodes.infinity.presentation.reusable_composable.TopAppBarActionButton
import ir.kazemcodes.infinity.presentation.reusable_composable.TopAppBarBackButton
import ir.kazemcodes.infinity.presentation.reusable_composable.TopAppBarTitle


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ReadingScreen(
    modifier: Modifier = Modifier,
) {
    val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))
    val scope = rememberCoroutineScope()
    val viewModel = rememberService<ReaderScreenViewModel>()
    val book = viewModel.state.value.book
    val backStack = LocalBackstack.current
    val state = viewModel.state.value
    val interactionSource = remember { MutableInteractionSource() }
    val drawerChapter = state.chapters


    Box(modifier = modifier.fillMaxSize()) {

        Scaffold(
            topBar = {
                if (!state.isReaderModeEnable && state.isLoaded) {
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
                                onClick = { viewModel.getReadingContentRemotely(viewModel.state.value.chapter) })
                            TopAppBarActionButton(imageVector = Icons.Default.Language,
                                title = "WebView",
                                onClick = { backStack.goTo(WebViewKey(url = viewModel.state.value.chapter.link)) })
                        }
                    )
                } else if (!state.isLoaded) {
                    TopAppBar(title = {},
                        elevation = 0.dp,
                        backgroundColor = MaterialTheme.colors.background,
                        navigationIcon = {
                            TopAppBarBackButton(backStack = backStack)
                        })
                }
            },
            scaffoldState = scaffoldState,
            bottomBar = {
                if (!state.isReaderModeEnable && state.isLoaded) {
                    BottomAppBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (viewModel.state.value.isMainBottomModeEnable) 130.dp else 320.dp),
                        elevation = 8.dp,
                        backgroundColor = MaterialTheme.colors.background
                    ) {
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
                }
            },
            drawerGesturesEnabled = true,
            drawerBackgroundColor = MaterialTheme.colors.background,
            drawerContent = {
                Column(modifier = modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center) {
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
                            items(count = state.chapters.size) { index ->
                                Row(
                                    modifier = modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                        .height(40.dp)
                                        .clickable {
                                            viewModel.getContent(state.chapters[index])
                                            viewModel.updateChapterSliderIndex(index = index)

                                        },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = state.chapters[index].title,
                                        color = if (state.chapters[index].haveBeenRead) MaterialTheme.colors.onBackground.copy(
                                            alpha = .4f) else MaterialTheme.colors.onBackground,
                                        style = MaterialTheme.typography.subtitle1,
                                        fontWeight = FontWeight.SemiBold,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(7f)
                                    )
                                    Text(modifier = Modifier.weight(2f),
                                        text = state.chapters[index].dateUploaded ?: "",
                                        fontStyle = FontStyle.Italic,
                                        color = if (state.chapters[index].haveBeenRead) MaterialTheme.colors.onBackground.copy(
                                            alpha = .4f) else MaterialTheme.colors.onBackground,
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.caption
                                    )
                                    Spacer(modifier = modifier.width(20.dp))
                                    Icon(
                                        imageVector = Icons.Default.PublishedWithChanges,
                                        contentDescription = "Cached",
                                        tint = if (state.chapters[index].content.joinToString(" , ").length > 10) MaterialTheme.colors.onBackground else MaterialTheme.colors.background,
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
                        indication = null) { viewModel.onEvent(ReaderEvent.ToggleReaderMode(!state.isReaderModeEnable)) }
                    .background(viewModel.state.value.backgroundColor)
                    .padding(8.dp)
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
                .align(Alignment.Center))
        }

        if (viewModel.state.value.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colors.primary
            )
        }

    }
}


