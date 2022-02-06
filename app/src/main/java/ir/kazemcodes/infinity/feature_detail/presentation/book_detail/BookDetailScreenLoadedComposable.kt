package ir.kazemcodes.infinity.feature_detail.presentation.book_detail

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import ir.kazemcodes.infinity.core.data.network.utils.toast
import ir.kazemcodes.infinity.core.presentation.components.BookImageComposable
import ir.kazemcodes.infinity.core.presentation.components.ISnackBarHost
import ir.kazemcodes.infinity.core.ui.ChapterScreenSpec
import ir.kazemcodes.infinity.core.ui.ReaderScreenSpec
import ir.kazemcodes.infinity.core.utils.Constants
import ir.kazemcodes.infinity.core.utils.UiEvent
import ir.kazemcodes.infinity.core.utils.formatBasedOnDot
import ir.kazemcodes.infinity.core.utils.formatList
import ir.kazemcodes.infinity.feature_detail.presentation.book_detail.components.ButtonWithIconAndText
import ir.kazemcodes.infinity.feature_detail.presentation.book_detail.components.CardTileComposable
import ir.kazemcodes.infinity.feature_detail.presentation.book_detail.components.DotsFlashing
import ir.kazemcodes.infinity.feature_detail.presentation.book_detail.components.ExpandingText
import kotlinx.coroutines.flow.collectLatest


@Composable
fun BookDetailScreenLoadedComposable(
    modifier: Modifier = Modifier,
    viewModel: BookDetailViewModel,
    navController: NavController,
) {
    val source = viewModel.state.source
    val state = viewModel.state
    val chapters = viewModel.chapterState.chapters
    val scaffoldState = rememberScaffoldState()
    val context = LocalContext.current
    val webview = viewModel.webView
    val isWebViewEnable by remember {
        mutableStateOf(webview.originalUrl == viewModel.state.book.link)
    }

    LaunchedEffect(key1 = true) {
        viewModel.getLocalBookById(state.book.id)
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
            BookDetailTopAppBar(isWebViewEnable = isWebViewEnable,
                viewModel = viewModel,
                navController = navController)
        }, scaffoldState = scaffoldState, snackbarHost = { ISnackBarHost(snackBarHostState = it) },
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
                        text = if (state.book.lastRead != 0L) "Continue Reading" else "Read",
                        imageVector = Icons.Default.AutoStories,
                        onClick = {
                            if (state.book.lastRead != 0L) {
                                navController.navigate(ReaderScreenSpec.buildRoute(
                                    bookId = state.book.id,
                                    sourceId = source.sourceId,
                                    chapterId = Constants.LAST_CHAPTER,
                                ))
                            } else if (viewModel.chapterState.chapters.isNotEmpty()) {
                                navController.navigate(ReaderScreenSpec.buildRoute(
                                    bookId = state.book.id,
                                    sourceId = source.sourceId,
                                    chapterId = viewModel.chapterState.chapters.first().chapterId,
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
                            //context.toast("Not available yet.")
                            viewModel.startDownloadService(context)
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
                        headers = viewModel.state.source.headers
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
                            text = "Source: ${state.source.name}",
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
                    navController.navigate(ChapterScreenSpec.buildRoute(bookId = state.book.id,
                        sourceId = source.sourceId))

                },
                title = "Contents",
                subtitle = "${chapters.size} Chapters",
                trailing = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "", color = MaterialTheme.colors.onBackground,
                            style = MaterialTheme.typography.subtitle2
                        )
                        if (viewModel.chapterState.isLocalLoading || viewModel.chapterState.isRemoteLoading) {
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