package ireader.presentation.ui.component.list.layouts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ireader.domain.models.BookCover
import ireader.domain.models.entities.BaseBook
import ireader.presentation.ui.component.LocalPerformanceConfig
import ireader.presentation.ui.component.PerformanceConfig
import ireader.presentation.ui.component.components.IBookImageComposable
import ireader.presentation.ui.component.optimizedForList

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookImage(
    modifier: Modifier = Modifier,
    onClick: (BaseBook) -> Unit = {},
    onLongClick: (BaseBook) -> Unit = {},
    book: BaseBook,
    ratio: Float = 0.70f,
    selected: Boolean = false,
    header: ((url: String) -> okhttp3.Headers?)? = null,
    onlyCover: Boolean = false,
    comfortableMode: Boolean = false,
    isScrollingFast: Boolean = false,
    performanceConfig: PerformanceConfig = LocalPerformanceConfig.current,
    badge: @Composable BoxScope.() -> Unit,
) {
    // Cache the BookCover to prevent recreation on every recomposition
    val bookCover = remember(book.id, book.cover) { BookCover.from(book) }
    
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(2.dp)
                // Apply layer promotion for better scroll performance
                .optimizedForList(enableLayerPromotion = performanceConfig.enableComplexAnimations)
                .combinedClickable(
                    onClick = { onClick(book) },
                    onLongClick = { onLongClick(book) }
                )
                .border(
                    2.dp,
                    if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(
                        alpha = .1f
                    )
                ),
        ) {
            // Always render the image composable to maintain proper caching and state
            // The image loader handles caching internally, so images won't disappear on scroll
            IBookImageComposable(
                modifier = Modifier
                    .aspectRatio(ratio)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .align(Alignment.Center),
                image = bookCover,
                headers = header,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                alignment = Alignment.Center,
                // Use faster crossfade during fast scrolling for better performance
                crossfadeDurationMs = if (isScrollingFast) 0 else performanceConfig.crossfadeDurationMs
            )
            if (!onlyCover) {
                Box(
                    Modifier
                        .height(50.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black),
                                startY = 3f, // 1/3
                                endY = 80F
                            )
                        )
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    Text(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                            .fillMaxWidth(),
                        text = book.title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        color = Color.White,
                    )
                }
            }
            badge()
        }
        if (comfortableMode) {
            Text(
                modifier = Modifier
                    .padding(bottom = 2.dp)
                    .fillMaxWidth(),
                text = book.title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,

            )
        }
    }
}
