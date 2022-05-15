package org.ireader.bookDetails

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
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
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import org.ireader.bookDetails.components.BookSummary
import org.ireader.common_models.entities.Book
import org.ireader.components.components.BookImageComposable
import org.ireader.core_api.source.Source
import org.ireader.domain.utils.copyToClipboard
import org.ireader.image_loader.BookCover

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookDetailScreenLoadedComposable(
    modifier: Modifier = Modifier,
    book: Book,
    source: Source?,
    onTitle: (String) -> Unit,
    onSummaryExpand: () -> Unit,
    isSummaryExpanded: Boolean,
) {
    val context = LocalContext.current
    var imageLoaded by remember { mutableStateOf(false) }
    var isScreenLoaded by remember { mutableStateOf(false) }

    val fadeInImage by animateFloatAsState(
        if (imageLoaded) 0.2f else 0f, tween(easing = LinearOutSlowInEasing)
    )

    Box() {
        Box {

            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(data = BookCover.from(book))
                        .apply(block = fun ImageRequest.Builder.() {
                            listener(onSuccess = { _, _ ->
                                imageLoaded = true
                            })
                        }).build()
                ),
                contentDescription = null,
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
                )
                Spacer(modifier = modifier.width(8.dp))
                /** Book Info **/
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
                        style = MaterialTheme.typography.bodyMedium,
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
                        MaterialTheme.colorScheme.background,
                    )
                )
            )

    )
    /** Book Summary **/
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
