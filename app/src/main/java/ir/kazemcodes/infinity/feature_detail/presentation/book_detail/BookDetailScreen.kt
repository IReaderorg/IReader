package ir.kazemcodes.infinity.feature_detail.presentation.book_detail


import android.webkit.WebView
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zhuinden.simplestackcomposeintegration.core.LocalBackstack
import com.zhuinden.simplestackcomposeintegration.services.rememberService
import com.zhuinden.simplestackextensions.servicesktx.lookup
import ir.kazemcodes.infinity.core.data.network.utils.toast
import ir.kazemcodes.infinity.core.presentation.components.BookImageComposable
import ir.kazemcodes.infinity.core.presentation.components.ISnackBarHost
import ir.kazemcodes.infinity.core.presentation.reusable_composable.ErrorTextWithEmojis
import ir.kazemcodes.infinity.core.utils.UiEvent
import ir.kazemcodes.infinity.core.utils.formatBasedOnDot
import ir.kazemcodes.infinity.core.utils.formatList
import ir.kazemcodes.infinity.core.utils.getUrlWithoutDomain
import ir.kazemcodes.infinity.feature_activity.presentation.ChapterDetailKey
import ir.kazemcodes.infinity.feature_activity.presentation.ReaderScreenKey
import ir.kazemcodes.infinity.feature_activity.presentation.WebViewKey
import ir.kazemcodes.infinity.feature_detail.presentation.book_detail.components.ButtonWithIconAndText
import ir.kazemcodes.infinity.feature_detail.presentation.book_detail.components.CardTileComposable
import ir.kazemcodes.infinity.feature_detail.presentation.book_detail.components.DotsFlashing
import ir.kazemcodes.infinity.feature_detail.presentation.book_detail.components.ExpandingText
import ir.kazemcodes.infinity.feature_sources.sources.models.FetchType
import kotlinx.coroutines.flow.collectLatest


@Composable
fun BookDetailScreen(
    modifier: Modifier = Modifier,
) {
    val viewModel = rememberService<BookDetailViewModel>()
    val backStack = LocalBackstack.current


    Box(modifier = Modifier.fillMaxSize()) {
        if (viewModel.state.value.isLoaded) {
            BookDetailScreenLoadedComposable(
                modifier = modifier,
                viewModel = viewModel
            )
        } else {
            Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
                TopAppBar(title = {},
                    backgroundColor = MaterialTheme.colors.background,
                    navigationIcon = {
                        IconButton(onClick = { backStack.goBack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "ArrowBack",
                                tint = MaterialTheme.colors.onBackground,
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            backStack.goTo(WebViewKey(
                                viewModel.state.value.source.baseUrl + getUrlWithoutDomain(viewModel.state.value.book.link),
                                sourceName = viewModel.state.value.source.name,
                                fetchType = FetchType.Detail.index,
                                bookId = viewModel.state.value.book.id
                            ))
                        }) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "WebView",
                                tint = MaterialTheme.colors.onBackground,
                            )
                        }
                    }
                )

            },
                snackbarHost = {
                    ISnackBarHost(it)
                }

                ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (viewModel.state.value.error.isNotBlank()) {
                        ErrorTextWithEmojis(error = viewModel.state.value.error, modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .wrapContentSize(Alignment.Center)
                            .align(Alignment.Center))
                    }
                    if (viewModel.state.value.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }


    }


}


@Composable
fun BookDetailScreenLoadedComposable(
    modifier: Modifier = Modifier,
    viewModel: BookDetailViewModel,
) {
    val source = viewModel.state.value.source
    val backStack = LocalBackstack.current
    val state = viewModel.state.value
    val chapters = viewModel.chapterState.value.chapters
    val scaffoldState = rememberScaffoldState()
    val context = LocalContext.current
    val webview = backStack.lookup<WebView>()
    val isWebViewEnable by remember {
        mutableStateOf(webview.originalUrl == viewModel.state.value.book.link)
    }

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    scaffoldState.snackbarHostState.showSnackbar(
                        event.uiText
                    )
                }
            }
        }
    }

        Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                backgroundColor = Color.Transparent,
                contentColor = MaterialTheme.colors.onBackground,
                elevation = 0.dp,
                actions = {
                    if (isWebViewEnable) {
                        IconButton(onClick = { viewModel.getWebViewData() }) {
                            Icon(
                                imageVector = Icons.Default.TrackChanges,
                                contentDescription = "Get from webview",
                                tint = MaterialTheme.colors.onBackground,
                            )
                        }
                    }

                    IconButton(onClick = { viewModel.getRemoteChapterDetail(state.book) }) {
                        Icon(
                            imageVector = Icons.Default.Autorenew,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colors.onBackground,
                        )
                    }
                    /** ERROR: This may cause error later: mismatch between baseurl and book link**/
                    IconButton(onClick = {
                        backStack.goTo(WebViewKey(
                            source.baseUrl + getUrlWithoutDomain(state.book.link),
                            sourceName = source.name,
                            fetchType = FetchType.Detail.index,
                            bookId = viewModel.state.value.book.id
                        ))
                    }) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "WebView",
                            tint = MaterialTheme.colors.onBackground,
                        )
                    }


                },
                navigationIcon = {
                    IconButton(onClick = { backStack.goBack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "back Button",
                            tint = MaterialTheme.colors.onBackground
                        )
                    }

                }
            )
        }, scaffoldState = scaffoldState, snackbarHost = { ISnackBarHost(snackBarHostState = it)},
            bottomBar = {
            BottomAppBar(
                modifier = modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colors.background,
                contentColor = MaterialTheme.colors.onBackground,
                elevation = 8.dp,
            ) {
                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically

                ) {
                    ButtonWithIconAndText(
                        text = if (!state.inLibrary) "Add to Library" else "Added To Library",
                        imageVector = if (!state.inLibrary) Icons.Default.AddCircleOutline else Icons.Default.Check,
                        onClick = {
                            if (!state.inLibrary) {
                                viewModel.toggleInLibrary(true)
                            } else {
                                viewModel.toggleInLibrary(false)
                            }
                        },
                    )
                    ButtonWithIconAndText(
                        text = if (viewModel.chapterState.value.lastChapter != viewModel.chapterState.value.chapters.getOrNull(
                                0)
                        ) "Continue Reading" else "Read",
                        imageVector = Icons.Default.AutoStories,
                        onClick = {
                            if (viewModel.chapterState.value.lastChapter != null) {
                                backStack.goTo(
                                    ReaderScreenKey(
                                        bookId = state.book.id,
                                        sourceName = source.name,
                                        chapterId = viewModel.chapterState.value.lastChapter!!.chapterId,

                                    ),
                                )
                            } else if (viewModel.chapterState.value.chapters.isNotEmpty()) {
                                backStack.goTo(ReaderScreenKey(
                                    bookId = state.book.id,
                                    sourceName = source.name,
                                    chapterId = viewModel.chapterState.value.chapters.first().chapterId,
                                ))
                            } else {
                                context.toast("No Chapter is Available")
                            }
                        }
                    )

                    ButtonWithIconAndText(
                        text = "Download",
                        imageVector = Icons.Default.FileDownload,
                        onClick = {
                            context.toast("Not available yet.")
                            //viewModel.startDownloadService(context)
                        }
                    )
                }
            }
        }) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Box {
                /** Image and Book Information **/
                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    /** Book Image **/
                    BookImageComposable(
                        image = state.book.coverLink ?: "",
                        modifier = modifier
                            .width(120.dp)
                            .height(180.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .border(2.dp, MaterialTheme.colors.onBackground.copy(alpha = .1f)),
                        contentScale = ContentScale.Crop,
                        headers = viewModel.state.value.source.headers
                    )
                    Spacer(modifier = modifier.width(8.dp))
                    /** Book Info **/
                    Column {
                        Text(
                            text = state.book.bookName,
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.onBackground,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!state.book.author.isNullOrBlank()) {
                            Text(
                                text = "Author: ${state.book.author}",
                                style = MaterialTheme.typography.subtitle2,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colors.onBackground.copy(alpha = .5f),
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (!state.book.translator.isNullOrBlank()) {
                            Text(
                                text = "Translator: ${state.book.translator}",
                                style = MaterialTheme.typography.subtitle2,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colors.onBackground.copy(alpha = .5f),
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (state.book.status != -1) {
                            Text(
                                text = "Status: ${state.book.getStatusByName()}",
                                style = MaterialTheme.typography.subtitle2,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colors.onBackground.copy(alpha = .5f),
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (state.book.rating != 0) {
                            Text(
                                text = "Rating: ${"⭐".repeat(if (state.book.rating in 1..4) state.book.rating else 5)}",
                                style = MaterialTheme.typography.subtitle2,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colors.onBackground.copy(alpha = .5f),
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = "Source: ${state.book.source}",
                            color = MaterialTheme.colors.onBackground.copy(alpha = .5f),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.subtitle2,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!state.book.category.isNullOrEmpty()) {
                            Text(
                                text = "Genre: ${state.book.category.formatList()}",
                                color = MaterialTheme.colors.onBackground.copy(alpha = .5f),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.subtitle2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }


                }
            }

            Divider(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )
            /** Book Summary **/
            Text(
                text = "Synopsis", fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.onBackground,
                style = MaterialTheme.typography.h6,
            )
            ExpandingText(text = state.book.description.formatBasedOnDot())
            Divider(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )
            /** Chapter Content **/
            CardTileComposable(
                modifier = modifier.clickable {
                    backStack.goTo(ChapterDetailKey(
                        bookId = state.book.id,
                        sourceName = source.name
                    ))
                },
                title = "Contents",
                subtitle = "${chapters.size} Chapters",
                trailing = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "", color = MaterialTheme.colors.onBackground,
                            style = MaterialTheme.typography.subtitle2
                        )
                        if (viewModel.chapterState.value.isLoading) {
                            DotsFlashing()
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Contents Detail",
                            tint = MaterialTheme.colors.onBackground,
                        )
                    }
                })
            Spacer(modifier = modifier.height(60.dp))
        }
    }
}