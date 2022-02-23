package org.ireader.presentation.feature_detail.presentation.chapter_detail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Sort
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import org.ireader.core.utils.Constants
import org.ireader.domain.view_models.detail.chapter_detail.ChapterDetailEvent
import org.ireader.domain.view_models.detail.chapter_detail.ChapterDetailViewModel
import org.ireader.presentation.presentation.components.CenterTopAppBar
import org.ireader.presentation.presentation.components.ChapterListItemComposable
import org.ireader.presentation.presentation.components.handlePagingChapterResult
import org.ireader.presentation.presentation.reusable_composable.ErrorTextWithEmojis
import org.ireader.presentation.presentation.reusable_composable.MidSizeTextComposable
import org.ireader.presentation.presentation.reusable_composable.TopAppBarTitle
import org.ireader.presentation.ui.ReaderScreenSpec


@ExperimentalAnimationApi
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ChapterDetailScreen(
    modifier: Modifier = Modifier,
    viewModel: ChapterDetailViewModel = hiltViewModel(),
    navController: NavController = rememberNavController(),
) {

    val chapters = viewModel.chapters.collectAsLazyPagingItems()
    val book = viewModel.state.book
    val scrollState = rememberLazyListState()
    val state = viewModel.state
    val context = LocalContext.current



    LaunchedEffect(key1 = true) {
        viewModel.getLocalBookById(state.book.id)
    }

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
        },
        drawerGesturesEnabled = true,
        drawerBackgroundColor = MaterialTheme.colors.background,
        drawerContent = {
            Column(modifier = modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top) {
                Spacer(modifier = modifier.height(5.dp))
                TopAppBarTitle(title = "Advance Setting")

                Spacer(modifier = modifier.height(5.dp))
                Divider(modifier = modifier.fillMaxWidth(), thickness = 1.dp)
                TextButton(modifier = Modifier.fillMaxWidth(),
                    onClick = { viewModel.reverseChapterInDB() }) {
                    MidSizeTextComposable(text = "Reverse Chapters in DB")
                }
            }
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
                        .fillMaxSize(), state = scrollState) {

                        items(items = chapters) { chapter ->
                            if (chapter != null) {
                                ChapterListItemComposable(modifier = modifier,
                                    chapter = chapter, goTo = {
                                        navController.navigate(ReaderScreenSpec.buildRoute(
                                            bookId = book.id,
                                            sourceId = book.sourceId,
                                            chapterId = chapter.id,
                                        ))
                                    })
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
