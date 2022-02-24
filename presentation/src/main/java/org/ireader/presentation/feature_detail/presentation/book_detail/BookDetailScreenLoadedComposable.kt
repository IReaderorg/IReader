package org.ireader.presentation.feature_detail.presentation.book_detail

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import org.ireader.domain.models.entities.Book
import org.ireader.presentation.feature_detail.presentation.book_detail.components.BookSummary
import org.ireader.presentation.presentation.components.BookImageComposable
import org.ireader.source.core.CatalogSource


@Composable
fun BookDetailScreenLoadedComposable(
    modifier: Modifier = Modifier,
    navController: NavController,
    book: Book,
    source: CatalogSource,
    onWebView: () -> Unit,
    onRefresh: () -> Unit,
    onSummaryExpand: () -> Unit,
    isSummaryExpanded: Boolean,
) {
    var imageLoaded by remember { mutableStateOf(false) }

    val fadeInImage by animateFloatAsState(
        if (imageLoaded) 0.2f else 0f, tween(easing = LinearOutSlowInEasing)
    )
    var isExpandable by remember {
        mutableStateOf<Boolean?>(null)
    }

    Box(Modifier.height(IntrinsicSize.Min)) {
        Box {

            Image(
                painter = rememberImagePainter(
                    data = book.cover,
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
        Column {
            BookDetailTopAppBar(
                navController = navController,
                onWebView = {
                    onWebView()
                },
                onRefresh = {
                    onRefresh()
                },
            )
            Row(
                modifier = modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            ) {
                /** Book Image **/
                /** Book Image **/
                BookImageComposable(
                    image = book.cover,
                    modifier = modifier
                        .padding(8.dp)
                        .weight(0.40f)
                        .aspectRatio(3f / 4f)
                        .clip(MaterialTheme.shapes.medium)
                        .border(2.dp, MaterialTheme.colors.onBackground.copy(alpha = .1f)),
                    contentScale = ContentScale.Crop,
                    headers = source.headers
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
                        text = book.title,
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.onBackground,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (book.author.isNotBlank()) {
                        Text(
                            text = "Author: ${book.author}",
                            style = MaterialTheme.typography.subtitle2,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.onBackground.copy(alpha = .5f),
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (book.translator.isNotBlank()) {
                        Text(
                            text = "Translator: ${book.translator}",
                            style = MaterialTheme.typography.subtitle2,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.onBackground.copy(alpha = .5f),
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = book.getStatusByName(),
                            style = MaterialTheme.typography.subtitle2,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.onBackground.copy(alpha = .5f),
                            overflow = TextOverflow.Ellipsis
                        )
                        Text("•")
                        Text(
                            text = source.name,
                            color = MaterialTheme.colors.onBackground.copy(alpha = .5f),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.subtitle2,
                            overflow = TextOverflow.Ellipsis
                        )
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
                        MaterialTheme.colors.background,
                    )
                )
            )

    )
    /** Book Summary **/
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .background(MaterialTheme.colors.background)
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