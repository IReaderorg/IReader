package ir.kazemcodes.infinity.feature_detail.presentation.chapter_detail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PublishedWithChanges
import androidx.compose.material.icons.filled.Sort
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import ir.kazemcodes.infinity.core.presentation.components.CenterTopAppBar
import ir.kazemcodes.infinity.core.presentation.components.handlePagingChapterResult
import ir.kazemcodes.infinity.core.presentation.reusable_composable.ErrorTextWithEmojis
import ir.kazemcodes.infinity.core.presentation.reusable_composable.TopAppBarTitle
import ir.kazemcodes.infinity.core.utils.Constants
import ir.kazemcodes.infinity.feature_activity.presentation.Screen


@ExperimentalAnimationApi
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ChapterDetailScreen(
    modifier: Modifier = Modifier,
    viewModel: ChapterDetailViewModel = hiltViewModel(),
    navController: NavController = rememberNavController(),
) {

    val chapters = viewModel.chapters.collectAsLazyPagingItems()
    val book = viewModel.state.value.book
    val scrollState = rememberLazyListState()
    val state = viewModel.state.value
    val context = LocalContext.current

    Scaffold(
        topBar = {

            CenterTopAppBar(title = {
                TopAppBarTitle(title = "Content")
            },
                modifier = modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colors.background,
                contentColor = MaterialTheme.colors.onBackground,
                elevation = Constants.DEFAULT_ELEVATION,
                actions = {
                    IconButton(onClick = {
                        viewModel.onEvent(ChapterDetailEvent.ToggleOrder)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Sort Icon"
                        )
                    }

                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back Icon"
                        )
                    }
                }
            )
        }
    ) {
        Box(modifier.fillMaxSize()) {

            val result = handlePagingChapterResult(books = chapters, onEmptyResult = {
                ErrorTextWithEmojis(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .align(Alignment.Center),
                    error = "There is no chapter."
                )
            })
            if (result) {
                AnimatedContent(chapters.loadState.refresh is LoadState.NotLoading) {
                    LazyColumn(modifier = Modifier
                        .fillMaxSize() ,state = scrollState){

                        items(items=chapters) { chapter ->
                            if (chapter != null) {
                                ListItem(
                                    modifier = modifier.clickable {
                                        navController.navigate(Screen.ReaderScreen.passArgs(
                                            bookId = book.id,
                                            sourceId = viewModel.state.value.source.sourceId,
                                            chapterId = chapter.chapterId,
                                        ))
                                    },
                                    text = {                             Text(
                                        text =chapter.title,
                                        color = if (chapter.haveBeenRead) MaterialTheme.colors.onBackground.copy(
                                            alpha = .4f) else MaterialTheme.colors.onBackground,
                                        style = MaterialTheme.typography.subtitle1,
                                        fontWeight = FontWeight.SemiBold,
                                        overflow = TextOverflow.Ellipsis,
                                    ) },
                                    trailing = {
                                        Icon(
                                            imageVector = Icons.Default.PublishedWithChanges,
                                            contentDescription = "Cached",
                                            tint = if (chapter.content.joinToString(" , ").length > 10) MaterialTheme.colors.onBackground else MaterialTheme.colors.background,
                                        )
                                    },
                                    secondaryText = {
                                        Text(
                                            text = chapter.dateUploaded ?: "",
                                            fontStyle = FontStyle.Italic,
                                            color = if (chapter.haveBeenRead) MaterialTheme.colors.onBackground.copy(
                                                alpha = .4f) else MaterialTheme.colors.onBackground,
                                            fontWeight = FontWeight.SemiBold,
                                            style = MaterialTheme.typography.caption
                                        )
                                    }

                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Modifier.simpleVerticalScrollbar(
    state: LazyListState,
    width: Dp = 8.dp,
    color: Color = MaterialTheme.colors.primary,
): Modifier {
    val targetAlpha = if (state.isScrollInProgress) 1f else 0f
    val duration = if (state.isScrollInProgress) 150 else 500

    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = duration)
    )

    return drawWithContent {
        drawContent()

        val firstVisibleElementIndex = state.layoutInfo.visibleItemsInfo.firstOrNull()?.index
        val needDrawScrollbar = state.isScrollInProgress || alpha > 0.0f

        // Draw scrollbar if scrolling or if the animation is still running and lazy column has content
        if (needDrawScrollbar && firstVisibleElementIndex != null) {
            val elementHeight = this.size.height / state.layoutInfo.totalItemsCount
            val scrollbarOffsetY = firstVisibleElementIndex * elementHeight
            val scrollbarHeight = state.layoutInfo.visibleItemsInfo.size * elementHeight

            drawRect(
                color = color,
                topLeft = Offset(this.size.width - width.toPx(), scrollbarOffsetY),
                size = Size(width.toPx(), scrollbarHeight),
                alpha = alpha
            )
        }
    }
}
