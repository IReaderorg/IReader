package org.ireader.bookDetails

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import coil.compose.AsyncImage
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.ireader.bookDetails.components.BookSummary
import org.ireader.bookDetails.viewmodel.ChapterState
import org.ireader.bookDetails.viewmodel.DetailState
import org.ireader.common_models.entities.Book
import org.ireader.common_models.entities.Chapter
import org.ireader.components.components.BookImageComposable
import org.ireader.core_api.source.Source
import org.ireader.core_ui.ui_components.CardTile
import org.ireader.core_ui.ui_components.DotsFlashing
import org.ireader.domain.utils.copyToClipboard
import org.ireader.image_loader.BookCover

@OptIn(
    ExperimentalCoroutinesApi::class, ExperimentalMaterialApi::class,
    ExperimentalMaterial3Api::class
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
    isSummaryExpanded: Boolean
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
                        source = source
                    )
                }
                BrushedDivider()
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
fun BookHeaderImage(
    book: Book
) {
    var imageLoaded by remember { mutableStateOf(false) }
    val fadeInImage by animateFloatAsState(
        if (imageLoaded) 0.2f else 0f, tween(easing = LinearOutSlowInEasing)
    )
    Box {
        AsyncImage(
            model = BookCover.from(book),
            contentDescription = null,
            onSuccess = {
                if (!imageLoaded) {
                    imageLoaded = true
                }
            },
            alignment = Alignment.TopCenter,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
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
                            MaterialTheme.colorScheme.background,
                        )
                    )
                )
                .align(Alignment.BottomCenter)
        )
    }
}


@Composable
private fun BookHeader(
    modifier : Modifier = Modifier,
    book:Book,
    onTitle: (String) -> Unit,
    source: Source?,

    ) {

    Column {
        Row(
            modifier = modifier
                .padding(start = 16.dp, end = 16.dp, top = 80.dp)
                .fillMaxWidth()
        ) {
            /** Book Image **/
            BookImageComposable(
                image = BookCover.from(book),
                modifier = modifier
                    .padding(8.dp)
                    .weight(0.40f)
                    .aspectRatio(3f / 4f)
                    .clip(MaterialTheme.shapes.medium)
                    .border(2.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = .1f)),
                contentScale = ContentScale.Crop,
                useSavedCoverImage = true
            )
            Spacer(modifier = modifier.width(8.dp))
            /** Book Info **/
            BookInfo(
                book = book,
                onTitle = onTitle,
                source = source
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RowScope.BookInfo(
    onTitle: (String) -> Unit,
    book: Book,
    source: Source?
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .padding(bottom = 16.dp)
            .weight(0.60f)
            .align(Alignment.Bottom)
    ) {
        Text(
            modifier = Modifier.combinedClickable(
                onClick = { onTitle(book.title) },
                onLongClick = { context.copyToClipboard(book.title, book.title) }
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
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = .5f),
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(
            modifier = Modifier.padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
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
private fun BrushedDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.Transparent,
                        MaterialTheme.colorScheme.background,
                    )
                )
            )

    )
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
            .padding(horizontal = 16.dp)
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()

    ) {
        Spacer(modifier = Modifier.height(8.dp))
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
    isChapterLoading:Boolean,
    chapters:List<Chapter>
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