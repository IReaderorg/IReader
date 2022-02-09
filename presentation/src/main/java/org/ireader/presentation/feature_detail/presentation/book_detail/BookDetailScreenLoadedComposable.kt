package org.ireader.presentation.feature_detail.presentation.book_detail

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.ireader.core.utils.Constants
import org.ireader.core.utils.UiEvent
import org.ireader.core.utils.UiText
import org.ireader.core.utils.formatBasedOnDot
import org.ireader.domain.view_models.detail.book_detail.BookDetailEvent
import org.ireader.domain.view_models.detail.book_detail.BookDetailViewModel
import org.ireader.presentation.R
import org.ireader.presentation.feature_detail.presentation.book_detail.components.BookSummary
import org.ireader.presentation.feature_detail.presentation.book_detail.components.ButtonWithIconAndText
import org.ireader.presentation.feature_detail.presentation.book_detail.components.CardTileComposable
import org.ireader.presentation.feature_detail.presentation.book_detail.components.DotsFlashing
import org.ireader.presentation.presentation.components.BookImageComposable
import org.ireader.presentation.presentation.components.ISnackBarHost
import org.ireader.presentation.ui.ChapterScreenSpec
import org.ireader.presentation.ui.ReaderScreenSpec


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

    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = true) {
        viewModel.getLocalBookById(state.book.id)
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


    Box(modifier = Modifier
        .fillMaxSize()
    ) {
        var imageLoaded by remember { mutableStateOf(false) }
        val fadeInImage by animateFloatAsState(
            if (imageLoaded) 0.2f else 0f, tween(easing = LinearOutSlowInEasing)
        )
        Box {
            Image(
                painter = rememberImagePainter(
                    data = state.book.coverLink ?: "",
                    builder = {
                        listener(onSuccess = { _, _ ->
                            imageLoaded = true
                        })
                    }
                ),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(fadeInImage),
                contentScale = ContentScale.Crop,
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                MaterialTheme.colors.background,
                            )
                        )
                    )
                    .align(Alignment.BottomCenter)
            )

        }
        Column(modifier = Modifier) {
            Column {
                BookDetailTopAppBar(isWebViewEnable = isWebViewEnable,
                    viewModel = viewModel,
                    navController = navController)
                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    /** Book Image **/
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
                    }
                }
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                MaterialTheme.colors.background,
                            )
                        )
                    )

            )
            Scaffold(
                topBar = {},
                scaffoldState = scaffoldState,
                snackbarHost = { ISnackBarHost(snackBarHostState = it) },
                bottomBar = {
                    BottomAppBar(
                        modifier = modifier.fillMaxWidth(),
                        backgroundColor = MaterialTheme.colors.background,
                        contentColor = MaterialTheme.colors.onBackground,
                        elevation = 8.dp,
                    ) {
                        Row(
                            modifier = modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically

                        ) {
                            ButtonWithIconAndText(
                                modifier = Modifier.weight(1F),
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
                                modifier = Modifier.weight(1F),
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
                                        scope.launch {
                                            viewModel.showSnackBar(UiText.StringResource(R.string.no_chapter_is_available))
                                        }
                                    }
                                }
                            )

                            ButtonWithIconAndText(
                                modifier = Modifier.weight(1F),
                                text = "Download",
                                imageVector = Icons.Default.FileDownload,
                                onClick = {
                                    viewModel.startDownloadService(context)
                                }
                            )
                        }
                    }
                }) {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    /** Book Summary **/
                    BookSummary(
                        onClickToggle = { viewModel.onEvent(BookDetailEvent.ToggleSummary) },
                        description = state.book.description.formatBasedOnDot(),
                        genres = state.book.category,
                        expandedSummary = state.isSummaryExpanded)
                    //ExpandingText(text = state.book.description.formatBasedOnDot())
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
    }
}
