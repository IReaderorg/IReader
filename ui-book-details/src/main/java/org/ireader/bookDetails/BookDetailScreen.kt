package org.ireader.bookDetails

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import org.ireader.bookDetails.components.BookSummary
import org.ireader.bookDetails.viewmodel.ChapterState
import org.ireader.bookDetails.viewmodel.DetailState
import org.ireader.common_models.entities.Book
import org.ireader.common_models.entities.Chapter
import org.ireader.components.components.BookImageCover
import org.ireader.core_api.source.Source
import org.ireader.core_api.source.model.MangaInfo
import org.ireader.core_ui.modifier.clickableNoIndication
import org.ireader.core_ui.modifier.secondaryItemAlpha
import org.ireader.core_ui.ui_components.CardTile
import org.ireader.core_ui.ui_components.DotsFlashing
import org.ireader.domain.utils.copyToClipboard
import org.ireader.image_loader.BookCover

@OptIn(
    ExperimentalMaterialApi::class,

    )
@Composable
fun BookDetailScreen(
    modifier: Modifier = Modifier,
    detailState: DetailState,
    modalBottomSheetState: ModalBottomSheetState,
    chapterState: ChapterState,
    onSummaryExpand: () -> Unit,
    onSwipeRefresh: () -> Unit,
    onChapterContent: () -> Unit,
    book: Book,
    onTitle: (String) -> Unit,
    snackBarHostState: SnackbarHostState,
    source: Source?,
    isSummaryExpanded: Boolean,
    appbarPadding: Dp,
) {

    val swipeRefreshState =
        rememberSwipeRefreshState(isRefreshing = detailState.detailIsLoading)


    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = {
            onSwipeRefresh()
        },
        indicatorPadding = PaddingValues(vertical = 40.dp),
        indicator = { state, trigger ->
            SwipeRefreshIndicator(
                state = state,
                refreshTriggerDistance = trigger,
                scale = true,
                backgroundColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primaryContainer,
                elevation = 8.dp,
            )
        }
    ) {
        Box(modifier = modifier) {

            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Box {
                    BookHeaderImage(book = book)
                    BookHeader(
                        book = book,
                        onTitle = onTitle,
                        source = source,
                        appbarPadding = appbarPadding
                    )
                }
                BookSummaryInfo(
                    book = book,
                    isSummaryExpanded = isSummaryExpanded,
                    onSummaryExpand = onSummaryExpand
                )
                ChapterContentHeader(
                    onChapterContent = onChapterContent,
                    isChapterLoading = chapterState.chapterIsLoading,
                    chapters = chapterState.chapters
                )

                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

@Composable
fun BoxScope.BookHeaderImage(
    book: Book
) {
    val backdropGradientColors = listOf(
        Color.Transparent,
        MaterialTheme.colorScheme.background,
    )
    AsyncImage(
        model = BookCover.from(book),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        alignment = Alignment.TopCenter,
        modifier = Modifier
            .matchParentSize()
            .drawWithContent {
                drawContent()
                drawRect(
                    brush = Brush.verticalGradient(colors = backdropGradientColors),
                )
            }
            .alpha(.2f),
    )
}

@Composable
private fun BookHeader(
    modifier: Modifier = Modifier,
    book: Book,
    onTitle: (String) -> Unit,
    source: Source?,
    appbarPadding: Dp
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = appbarPadding + 16.dp, end = 16.dp),
    ) {
        /** Book Image **/
        BookImageCover.Book(
            data = BookCover.from(book),
            modifier = Modifier
                .sizeIn(maxWidth = 100.dp)
                .align(Alignment.Top)
                .border(2.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = .1f)),

            )
        Spacer(modifier = modifier.width(8.dp))
        /** Book Info **/
        BookInfo(
            book = book,
            onTitle = onTitle,
            source = source,
        )
    }
}

@Composable
private fun RowScope.BookInfo(
    onTitle: (String) -> Unit,
    book: Book,
    source: Source?,
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .padding(bottom = 16.dp)
            .weight(0.60f)
            .align(Alignment.Bottom)
    ) {
        Text(
            modifier = Modifier
                .clickableNoIndication(
                    onClick = {
                        if (book.title.isNotBlank()) {
                            onTitle(book.title)
                        }
                    },
                    onLongClick = {
                        if (book.title.isNotBlank()) {
                            context.copyToClipboard(book.title, book.title)
                        }
                    }
                ),
            text = book.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            overflow = TextOverflow.Ellipsis
        )
        if (book.author.isNotBlank()) {
            Text(
                text = "Author: ${book.author}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.secondaryItemAlpha(),
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(
            modifier = Modifier
                .secondaryItemAlpha()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = when (book.status) {
                    MangaInfo.ONGOING -> Icons.Default.Schedule
                    MangaInfo.COMPLETED -> Icons.Default.DoneAll
                    MangaInfo.LICENSED -> Icons.Default.AttachMoney
                    MangaInfo.PUBLISHING_FINISHED -> Icons.Default.Done
                    MangaInfo.CANCELLED -> Icons.Default.Close
                    MangaInfo.ON_HIATUS -> Icons.Default.Pause
                    else -> Icons.Default.Block
                },
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .size(16.dp),
            )
            Text(
                text = book.getStatusByName(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = .5f),
                overflow = TextOverflow.Ellipsis
            )
            Text("•")
            if (source != null) {
                Text(
                    text = source.name,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = .5f),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun BookSummaryInfo(
    modifier: Modifier = Modifier,
    onSummaryExpand: () -> Unit,
    book: Book,
    isSummaryExpanded: Boolean
) {
    Column(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()

    ) {
        BookSummary(
            onClickToggle = { onSummaryExpand() },
            description = book.description,
            genres = book.genres,
            expandedSummary = isSummaryExpanded,
        )
        Divider(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        )
    }
}

@Composable
fun ChapterContentHeader(
    onChapterContent: () -> Unit,
    isChapterLoading: Boolean,
    chapters: List<Chapter>
) {
    CardTile(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .fillMaxWidth(),
        onClick = onChapterContent,
        title = "Contents",
        subtitle = "${chapters.size} Chapters",
        trailing = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyLarge
                )
                DotsFlashing(isChapterLoading)

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Contents Detail",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    )
}